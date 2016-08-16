import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import Model._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}
import org.json4s._
import org.json4s.native.JsonMethods._

object WebClient {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher
    implicit val formats = DefaultFormats
    val timeout = 300.millis

    val httpRequest: HttpRequest = HttpRequest(
      HttpMethods.GET,
      Uri("http://api.icndb.com/jokes")
    )

    val request = Http().singleRequest(httpRequest)

    request.flatMap{ response =>
      if (response.status.intValue() != 200) {
        println(response.entity.dataBytes.runForeach(_.utf8String))
        Future(Unit)
      }else{
        response.entity.dataBytes
          .scan("")((acc, curr) => if (acc.contains("\r\n")) curr.utf8String else acc + curr.utf8String)
          .map(json => Try(parse(json).extract[JokeEntries]))
          .runForeach{
            case Success(entries) => entries.value.foreach(println)
            case Failure(e) => println(Console.RED + e.getMessage + Console.RESET)
          }
      }
    }
  }
}
