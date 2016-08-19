
import java.util.Locale

import akka.actor._
import akka.stream.{ActorMaterializer, ClosedShape}
import akka.stream.scaladsl._

import com.mildlyskilled.api.{StatusPublisherActor, TwitterStreamClient}
import com.mildlyskilled.model.{Author, Hashtag, Tweet}

import java.util.concurrent.{ExecutorService, Executors}

import twitter4j.Status
import scala.concurrent._
import scala.io.StdIn

object ReactiveTweets {

  def main(args: Array[String]): Unit = {
    // ActorSystem & thread pools
    val execService: ExecutorService = Executors.newCachedThreadPool()
    implicit val system: ActorSystem = ActorSystem("reactive-tweets")
    implicit val ec: ExecutionContext = ExecutionContext.fromExecutorService(execService)
    implicit val materializer = ActorMaterializer()(system)
    val twitterStream = new TwitterStreamClient(system)

    val g = RunnableGraph.fromGraph(GraphDSL.create() { implicit b =>
      import GraphDSL.Implicits._

      // create a TwitterStreamClient that publish on the event bus, and start its job

      twitterStream.init()

      // create a Source, with an actor that listen items from the event bus
      val tweets = Source.actorPublisher(Props[StatusPublisherActor])

      val extractTweet = Flow[Status].map((s) => Tweet(Author(s.getUser.getScreenName), s.getCreatedAt, s.getText))

      val sink = Sink.foreach[Tweet]((t) => println(t))

      val begin = tweets ~> extractTweet
      if (args.exists((arg) => arg.startsWith("#"))) {
        val hashtags = args.filter(arg => arg.startsWith("#")).map(hashtag => Hashtag(hashtag.toLowerCase(Locale.ENGLISH)))

        val extractHashtag = Flow[Tweet].filter((tweet) => tweet.hashtags.map(oh => Hashtag(oh.name.toLowerCase)).
          exists(h => hashtags.contains(h)))
        begin ~> extractHashtag ~> sink
      } else {
        begin ~> sink
      }

      ClosedShape
    })

    g.run()

    Iterator.continually(StdIn.readLine).takeWhile(_ != "exit").foreach {
      case "restart" => println("restarting")
      case input => println(input)
    }

    println(s"${Console.RED}Exiting...${Console.RESET}")
    twitterStream.stop()
    system.terminate()
  }



}
