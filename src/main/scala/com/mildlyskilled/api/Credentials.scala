package com.mildlyskilled.api

import com.typesafe.config.ConfigFactory

object Credentials {
  val conf = ConfigFactory.load().getConfig("twitter-configuration")
  val appKey: String = conf.getString("appKey")
  val appSecret: String = conf.getString("appSecret")
  val accessToken: String = conf.getString("accessToken")
  val accessTokenSecret: String = conf.getString("accessTokenSecret")
}
