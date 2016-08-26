package streaming

import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl._
import akka.util._

import io.circe._
import io.circe.syntax._
import io.circe.generic.auto._


final case class Bar(value: String)

final case class Foo(
  anInteger: Int,
  aListOfStrings: List[String],
  `And a Map!`: Map[Bar, Int])


object JsonStreaming extends App {
  implicit val system = ActorSystem("json-streaming")
  implicit val materializer = ActorMaterializer()
  import system.dispatcher

  implicit val keyEncoder = KeyEncoder.instance[Bar]( _.value )
  implicit val keyDecoder = KeyDecoder[String].map(Bar)

  val foo = Foo(1337, List("bunch", "of", "strings"), Map(Bar("bar!") -> 42))
  val bigJson = Iterator.fill(100)(foo.asJson.noSpaces).mkString("[", ",", "]")

  Source
    .fromIterator(() => ByteString(bigJson).iterator)
    .grouped(16)
    .map( bytes => ByteString(bytes.toArray) )
    .via(JsonParser.ofByteStrings)
    .map( _.as[Foo] )
    .runForeach(println)
    .onComplete( _ => sys.exit(0) )
}

