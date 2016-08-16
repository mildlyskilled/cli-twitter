import javax.print.attribute.standard.JobHoldUntil

object Model {

  final case class Author(handle: String)

  final case class Hashtag(name: String)

  final case class Tweet(author: Author, timestamp: Long, body: String) {
    def hashtags: Set[Hashtag] =
      body.split(" ").collect { case t if t.startsWith("#") => Hashtag(t) }.toSet
  }


  final case class Joke(id: Int, joke: String, categories: List[String]) {
    override def toString: String = joke
  }
  final case class JokeEntry(`type`: String, value: Joke) {
    override def toString: String = value.joke
  }
  final case class JokeEntries(`type`: String, value: List[Joke])
  val akka = Hashtag("#akka")
}
