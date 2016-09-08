package com.mildlyskilled.api

import akka.actor.ActorSystem
import twitter4j._
import twitter4j.auth.AccessToken
import twitter4j.conf.ConfigurationBuilder


class TwitterStreamClient(val actorSystem: ActorSystem) {
  val factory = new TwitterStreamFactory(new ConfigurationBuilder().build())
  val twitterStream = factory.getInstance()

  def userStreamListener = new UserStreamListener() {

    def onFriendList(friendIds: Array[Long]): Unit = {}

    def onUserListUnsubscription(subscriber: User, listOwner: User, list: UserList): Unit = {}

    def onBlock(source: User, blockedUser: User): Unit = {}

    def onUserListSubscription(subscriber: User, listOwner: User, list: UserList): Unit = {}

    def onFollow(source: User, followedUser: User): Unit = {}

    def onUserListMemberAddition(addedMember: User, listOwner: User, list: UserList): Unit = {}

    def onDirectMessage(directMessage: DirectMessage): Unit = {}

    def onUnblock(source: User, unblockedUser: User): Unit = {}

    def onUserListUpdate(listOwner: User, list: UserList): Unit = {}

    def onUnfollow(source: User, unfollowedUser: User): Unit = {}

    def onUserProfileUpdate(updatedUser: User): Unit = {}

    def onUserListMemberDeletion(deletedMember: User, listOwner: User, list: UserList): Unit = {}

    def onUserDeletion(deletedUser: Long): Unit = {}

    def onRetweetedRetweet(source: User, target: User, retweetedStatus: Status): Unit = {}

    def onFavoritedRetweet(source: User, target: User, favoritedRetweeet: Status): Unit = {}

    def onDeletionNotice(directMessageId: Long, userId: Long): Unit = {}

    def onFavorite(source: User, target: User, favoritedStatus: Status): Unit = {
      actorSystem.eventStream.publish(favoritedStatus)
    }

    def onQuotedTweet(source: User, target: User, quotingTweet: Status): Unit = {}

    def onUnfavorite(source: User, target: User, unfavoritedStatus: Status): Unit = {}

    def onUserSuspension(suspendedUser: Long): Unit = {}

    def onUserListDeletion(listOwner: User, list: UserList): Unit = {}

    def onUserListCreation(listOwner: User, list: UserList): Unit = {}

    def onStallWarning(warning: StallWarning): Unit = {}

    def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice): Unit = {}

    def onScrubGeo(userId: Long, upToStatusId: Long): Unit = {}

    def onStatus(status: Status): Unit = {
      actorSystem.eventStream.publish(status)
    }

    def onTrackLimitationNotice(numberOfLimitedStatuses: Int): Unit = {}

    def onException(ex: Exception): Unit = {}
  }

  def init() = {
    twitterStream.setOAuthConsumer(Credentials.appKey, Credentials.appSecret)
    twitterStream.setOAuthAccessToken(new AccessToken(Credentials.accessToken, Credentials.accessTokenSecret))
    twitterStream.addListener(userStreamListener)
    twitterStream.user()
  }

  def stop() = {
    twitterStream.cleanUp()
    twitterStream.shutdown()
  }

  def start() = {
    twitterStream.addListener(userStreamListener)
    twitterStream.user()
  }

}