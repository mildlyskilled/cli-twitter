package com.mildlyskilled.repository

import io.scalac.amqp.{Connection, Message}
import org.reactivestreams.Subscriber

import scala.util.{Success, Failure, Try}


object RabbitConnection {
  val connection: Try[Connection] = Try(Connection())
  val exchange: Option[Subscriber[Message]] = connection match {
    case Success(e) => Some(e.publish(exchange="reactive", routingKey = "tweets"))
    case Failure(_) => None
   }
}
