
import java.util.Locale

import akka.actor._
import akka.stream.{ActorMaterializer, ClosedShape}
import akka.stream.scaladsl._
import com.mildlyskilled.api.{StatusPublisherActor, TwitterStreamClient}
import com.mildlyskilled.model.{Author, Hashtag, Tweet}
import java.util.concurrent.{ExecutorService, Executors}

import com.mildlyskilled.repository.{RabbitConnection, TwitterRepository}
import twitter4j.Status

import scala.concurrent._
import scala.io.StdIn
import io.scalac.amqp.Message

import scala.util.{Failure, Success}

object ReactiveTweets {

  def main(args: Array[String]): Unit = {
    // ActorSystem & thread pools
    val execService: ExecutorService = Executors.newCachedThreadPool()
    implicit val system: ActorSystem = ActorSystem("reactive-tweets")
    implicit val ec: ExecutionContext = ExecutionContext.fromExecutorService(execService)
    implicit val materializer = ActorMaterializer()(system)
    val twitterStream: TwitterStreamClient = new TwitterStreamClient(system)




    val g = RunnableGraph.fromGraph(GraphDSL.create() { implicit b =>
      import GraphDSL.Implicits._

      // create a TwitterStreamClient that publish on the event bus, and start its job

      twitterStream.init()

      // create a Source, with an actor that listen items from the event bus
      val tweets = Source.actorPublisher(Props[StatusPublisherActor])

      val extractTweet = Flow[Status].map((s) => Tweet(Author(s.getUser.getScreenName, s.getUser.getId), s.getCreatedAt, s.getText))

      val sink = Sink.foreach[Tweet]((t) => println(s"$t \n ${Console.BLUE}${"-" * 80}${Console.RESET}"))

      val tweetToMessage = Flow[Tweet].map((t) => Message(body = t.body.getBytes))

      val extractMentions = Flow[Tweet].filter((t) => t.body.contains(twitterStream.twitterStream.getScreenName))

      val removeMentions = Flow[Tweet].filterNot((t) => t.body.contains(twitterStream.twitterStream.getScreenName))

      val sinkMentions = Sink.foreach[Tweet]((t) => println(s"$t \n ${Console.RED}${"-" * 80}${Console.RESET}"))

      lazy val broadcast = b.add(Broadcast[Tweet](3))

      val begin = tweets ~> extractTweet
      if (args.exists((arg) => arg.startsWith("#"))) {
        val hashtags = args.filter(arg => arg.startsWith("#")).map(hashtag => Hashtag(hashtag.toLowerCase(Locale.ENGLISH)))

        val extractHashtag = Flow[Tweet].filter((tweet) => tweet.hashtags.map(oh => Hashtag(oh.name.toLowerCase)).
          exists(h => hashtags.contains(h)))
        begin ~> extractHashtag ~> sink
      } else {


        // broadcast to RabbitMQ if we have a connection
        RabbitConnection.connection match {
          case Success(connection) =>
            RabbitConnection.exchange match {
              case Some(exchange) =>
                system.log.info("Successfully established connection to RabbitMQ")
                begin ~> broadcast ~> removeMentions ~> sink
                broadcast ~> tweetToMessage ~> Sink.fromSubscriber[Message](exchange)
                broadcast ~> extractMentions ~> sinkMentions
              case None =>
                system.log.info("Unable to broadcast to RabbitMQ failed to set up the exchange")
                begin ~> sink
            }
          case Failure(exception) =>
            system.log.info(s"Unable to broadcast to RabbitMQ: ${exception.getMessage}")
            begin ~> sink
        }
      }

      ClosedShape
    })

    g.run()

    val postRegex = """(/post) (.*)"""r

    Iterator.continually(StdIn.readLine).takeWhile(_ != "exit").foreach {
      case "restart" =>
        println(s"${Console.RED}Restarting...${Console.RESET}")
        twitterStream.stop()
        twitterStream.start()
      case "stop" =>
        println(s"${Console.RED}Stopping...${Console.RESET}")
        twitterStream.stop()
      case "start" =>
        println(s"${Console.RED}Starting...${Console.RESET}")
        twitterStream.start()
      case postRegex(_, msg) =>
        twitterStream.stop()
        TwitterRepository.postStatus(msg)
        twitterStream.start()
      case input => println(input)
    }

    println(s"${Console.RED}Exiting...${Console.RESET}")
    twitterStream.stop()
    RabbitConnection.connection.foreach(_.shutdown)
    system.terminate()
  }


}
