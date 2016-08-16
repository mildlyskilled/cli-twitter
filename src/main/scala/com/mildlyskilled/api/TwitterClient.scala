package com.mildlyskilled.api

import twitter4j.{Twitter, TwitterFactory}
import twitter4j.auth.AccessToken
import twitter4j.conf.ConfigurationBuilder

object TwitterClient {
  def apply(): Twitter = {
    val factory = new TwitterFactory(new ConfigurationBuilder().build())
    val t = factory.getInstance()
    t.setOAuthConsumer(Credentials.appKey, Credentials.appSecret)
    t.setOAuthAccessToken(new AccessToken(Credentials.accessToken, Credentials.accessTokenSecret))
    t
  }
}
