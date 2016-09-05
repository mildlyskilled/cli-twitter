
import java.util.Locale

import akka.actor._
import akka.stream.{ActorMaterializer, ClosedShape}
import akka.stream.scaladsl._
import com.mildlyskilled.api.{StatusPublisherActor, TwitterStreamClient}
import com.mildlyskilled.model.{Author, Hashtag, Tweet}
import java.util.concurrent.{ExecutorService, Executors}
import com.mildlyskilled.repository.RabbitConnection
import twitter4j.Status

import scala.concurrent._
import scala.io.StdIn
import io.scalac.amqp.Message

import scala.util.{Success, Failure}

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

      val sink = Sink.foreach[Tweet]((t) => println(t + s"\n ${Console.BLUE}${"-" * 80}${Console.RESET}"))

      val tweetToMessage = Flow[Tweet].map((t) => Message(body = t.body.getBytes))

      val bcast = b.add(Broadcast[Tweet](2))

      val begin = tweets ~> extractTweet
      if (args.exists((arg) => arg.startsWith("#"))) {
        val hashtags = args.filter(arg => arg.startsWith("#")).map(hashtag => Hashtag(hashtag.toLowerCase(Locale.ENGLISH)))

        val extractHashtag = Flow[Tweet].filter((tweet) => tweet.hashtags.map(oh => Hashtag(oh.name.toLowerCase)).
          exists(h => hashtags.contains(h)))
        begin ~> extractHashtag ~> sink
      } else {
        begin ~> bcast ~> sink

        // broadcast to RabbitMQ if we have a connection
        RabbitConnection.connection match {
          case Success(connection) =>
            RabbitConnection.exchange match {
              case Some(e) => bcast ~> tweetToMessage ~> Sink.fromSubscriber[Message](e)
              case None => system.log.debug("Unable to broadcast to RabbitMQ")
            }
          case Failure(ex) => system.log.debug(ex.getMessage)
        }
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
    RabbitConnection.connection.foreach(_.shutdown)
    system.terminate()
  }


}
