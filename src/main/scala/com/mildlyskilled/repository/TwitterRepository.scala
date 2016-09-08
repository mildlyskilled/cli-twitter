package com.mildlyskilled.repository

import com.mildlyskilled.api.TwitterClient
import twitter4j.{Status, User}
import scala.collection.JavaConversions._

object TwitterRepository {
  val client = TwitterClient()
  def lookupUsers(id: Long): List[User] = {
    client.lookupUsers(id).toList
  }

  def getHomeTimeline: List[Status] = {
    val client = TwitterClient()
    client.getHomeTimeline.toList
  }

  def parseStatus(status: Status): String = {
    status.getText
  }

  def postStatus(status: String): Status = {
    client.updateStatus(status)
  }
}
