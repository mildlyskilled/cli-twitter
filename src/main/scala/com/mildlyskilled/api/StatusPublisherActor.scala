package com.mildlyskilled.api

import akka.stream.actor.ActorPublisher
import twitter4j.Status

class StatusPublisherActor extends ActorPublisher[Status] {

  val sub = context.system.eventStream.subscribe(self, classOf[Status])

  override def receive: Receive = {
    case s: Status => {
      if (isActive && totalDemand > 0) onNext(s)
    }
    case _ =>
  }

  override def postStop(): Unit = {
    context.system.eventStream.unsubscribe(self)
  }

}