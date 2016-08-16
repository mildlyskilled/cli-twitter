package com.mildlyskilled.repository

import com.mildlyskilled.api.TwitterClient
import twitter4j.{Status, User}
import scala.collection.JavaConversions._

object TwitterRepository {
  def lookupUsers(id: Long): List[User] = {
    val client = TwitterClient()
    client.lookupUsers(id).toList
  }

  def getHomeTimeline: List[Status] = {
    val client = TwitterClient()
    client.getHomeTimeline.toList
  }

  def parseStatus(status: Status): String = {
    status.getText
  }
}
