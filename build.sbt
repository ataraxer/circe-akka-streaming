name := "circe-akka-streaming"

version := "0.1.0"

organization := "com.ataraxer"

scalaVersion := "2.11.8"

val akkaVersion = "2.4.9"
val circeVersion = "0.4.1"
val jawnVersion = "0.9.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http-core" % akkaVersion,
  "com.typesafe.akka" %% "akka-http-experimental" % akkaVersion,
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "com.typesafe" % "config" % "1.3.0",
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "org.spire-math" %% "jawn-parser" % jawnVersion)

initialCommands := {
  """
  import akka.actor._
  import akka.stream._
  import akka.stream.scaladsl._
  import akka.stream.io._
  import akka.util.ByteString
  import scala.concurrent.duration._

  implicit val system = ActorSystem("akka-streams")
  implicit val flowBuilder = ActorMaterializer()
  """
}

cleanupCommands := {
  """
  system.shutdown()
  """
}

