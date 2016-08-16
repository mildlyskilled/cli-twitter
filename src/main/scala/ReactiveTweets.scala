
import akka.actor._
import akka.stream.{ActorMaterializer, ClosedShape}
import akka.stream.scaladsl._
import com.mildlyskilled.api.{StatusPublisherActor, TwitterStreamClient}
import java.util.concurrent.{ExecutorService, Executors}
import twitter4j.Status
import scala.concurrent._

object ReactiveTweets extends App {

  // ActorSystem & thread pools
  val execService: ExecutorService = Executors.newCachedThreadPool()
  implicit val system: ActorSystem = ActorSystem("reactive-tweets")
  implicit val ec: ExecutionContext = ExecutionContext.fromExecutorService(execService)
  implicit val materializer = ActorMaterializer()(system)

  val g = RunnableGraph.fromGraph(GraphDSL.create() { implicit b =>
    import GraphDSL.Implicits._

    // create a TwitterStreamClient that pubbish on the event bus, and start its job
    val twitterStream = new TwitterStreamClient(system)
    twitterStream.init()

    // create a Source, with an actor that listen items from the event bus
    val tweets = Source.actorPublisher(Props[StatusPublisherActor])

    val extractTweet = Flow[Status].map((s) =>
      s"""${Console.BOLD}${s.getCreatedAt}:${Console.RESET}
        ${Console.GREEN}${s.getText}\n${Console.YELLOW}${s.getUser.getScreenName}${Console.RESET}
       """.stripMargin('\t'))

    val sink = Sink.foreach[String]((s) => println(s))

    tweets ~> extractTweet ~> sink

    ClosedShape
  })

  g.run()
}
