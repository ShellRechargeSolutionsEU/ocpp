package com.thenewmotion.ocpp
package json

import org.json4s._
import org.json4s.jackson.JsonMethods
import org.slf4j.LoggerFactory

object TransportMessageParser {

  private[this] val logger =
    LoggerFactory.getLogger(TransportMessageParser.this.getClass)

  implicit val formats  =
    DefaultFormats ++ TransportMessageJsonSerializers()

  import TransportMessageType._

  def parse(input: String): TransportMessage = {
    val parsedMsg = JsonMethods.parse(input)
    parse(parsedMsg)
  }

  def parse(input: JValue): TransportMessage = {
    input match {
      case JArray(JInt(CALL.`id`)       :: rest) => input.extract[RequestMessage]
      case JArray(JInt(CALLRESULT.`id`) :: rest) => input.extract[ResponseMessage]
      case JArray(JInt(CALLERROR.`id`)  :: rest) => input.extract[ErrorResponseMessage]
      case _                                     => sys.error(s"Unrecognized JSON command message $input")
    }
  }

  def writeJValue(input: TransportMessage): JValue = {
    Extraction.decompose(input)
  }

  def write(input: TransportMessage): String =
    JsonMethods.compact(JsonMethods.render(writeJValue(input)))
}
