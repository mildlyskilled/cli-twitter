package com.mildlyskilled.model

import java.util.Date
import PartialFunction._

final case class Author(handle: String) {
  override def toString: String = s"${Console.YELLOW}$handle${Console.RESET}"
}

final case class Hashtag(name: String) {
  override def toString: String = s"${Console.YELLOW_B}$name${Console.RESET}"
}

object EmptyTweet extends Tweet(Author(""), new Date(), "")

case class Tweet(author: Author, timestamp: Date, body: String) {

  val hashTagRegex = "#\\w+".r

  override def toString: String = {
    val returnValue =  s"${Console.BOLD}On $timestamp $author wrote: ${Console.GREEN}$body${Console.RESET}"
    if(hashtags.nonEmpty)
      returnValue + "\n" +hashtags.mkString(" ")
    else
      returnValue
  }


  def hashtags: Set[Hashtag] = {
    val hashIterator = for (hashtag <- hashTagRegex.findAllIn(body)) yield Hashtag(hashtag)
    hashIterator.toSet
  }
}