name := "reactive-tweets"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= {
  lazy val akkaVersion = "2.4.9-RC2"
  Seq(
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-experimental" % akkaVersion,
    "org.twitter4j" % "twitter4j-core" % "4.0.4",
    "org.twitter4j" % "twitter4j-stream" % "4.0.4"
  )
}