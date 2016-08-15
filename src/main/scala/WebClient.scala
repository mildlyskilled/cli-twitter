import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._

import scala.concurrent.Future
import scala.util.{Failure, Success}

object WebClient {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher
    val connectionFlow: Flow[HttpRequest, HttpResponse, Future[Http.OutgoingConnection]] =
      Http().outgoingConnectionHttps("stream.twitter.com")
    val responseFuture: Future[HttpResponse] =
      Source.single(HttpRequest(uri = Uri("/").withQuery(Uri.Query(Map("track" -> "akka")))))
        .via(connectionFlow)
        .runWith(Sink.head)

    responseFuture.andThen {
      case Success(x) => println(x)
      case Failure(exception) => println(exception.getMessage)
    }.andThen {
      case _ => system.terminate()
    }
  }
}
