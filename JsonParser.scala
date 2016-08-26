package streaming

import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import akka.util.ByteString

import jawn.{AsyncParser, ParseException}

import io.circe.{Json, ParsingFailure}
import io.circe.jawn.CirceSupportParser


object JsonParser {
  private type Result = Either[ParseException, Seq[Json]]
  type Flow[T] = FlowShape[T, Json]
  type Stage[T] = GraphStage[Flow[T]]

  def ofByteStrings: Stage[ByteString] = ByteStringJsonParser
  def ofStrings: Stage[String] = StringJsonParser
}


private final object StringJsonParser extends JsonParser[String] {
  implicit val facade = CirceSupportParser.facade
  def parseWith(p: AsyncParser[Json], in: String) = p.absorb(in)
}

private final object ByteStringJsonParser extends JsonParser[ByteString] {
  implicit val facade = CirceSupportParser.facade
  def parseWith(p: AsyncParser[Json], in: ByteString) = p.absorb(in.toArray)
}


private abstract class JsonParser[T] extends GraphStage[JsonParser.Flow[T]] {
  private val in = Inlet[T]("JsonParser.in")
  private val out = Outlet[Json]("JsonParser.out")
  val shape = FlowShape(in, out)

  def parseWith(parser: AsyncParser[Json], input: T): JsonParser.Result

  override def createLogic(attributes: Attributes) = {
    new GraphStageLogic(shape) with InHandler with OutHandler {
      private[this] val parser = CirceSupportParser.async(AsyncParser.UnwrapArray)
      private[this] var buffer = Seq.empty[Json]

      private[this] def emitOrPull() = {
        if (buffer.nonEmpty) {
          push(out, buffer.head)
          buffer = buffer drop 1
        } else {
          pull(in)
        }
      }

      override def onPush(): Unit = {
        parseWith(parser, grab(in)) match {
          case Left(error) => failStage(error)
          case Right(elements) => buffer = elements
        }

        emitOrPull()
      }

      override def onPull(): Unit = {
        emitOrPull()
      }

      setHandlers(in, out, this)
    }
  }
}

