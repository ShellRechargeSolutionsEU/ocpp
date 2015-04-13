package com.thenewmotion.ocpp
package json

import org.json4s._
import org.json4s.native.JsonParser
import org.json4s.native.JsonMethods._
import org.slf4j.LoggerFactory

object TransportMessageParser {

  private[this] val logger = LoggerFactory.getLogger(TransportMessageParser.this.getClass)

  implicit val formats = DefaultFormats ++ TransportMessageJsonSerializers()
  val CALLTYPE = BigInt(TransportMessageType.CALL.id)
  val RESULTTYPE = BigInt(TransportMessageType.CALLRESULT.id)
  val ERRORTYPE = BigInt(TransportMessageType.CALLERROR.id)

  def parse(input: String): TransportMessage = {
    val parsedMsg = JsonParser.parse(input)
    parse(parsedMsg)
  }

  def parse(input: JValue): TransportMessage = {
    input match {
      case JArray(JInt(CALLTYPE) :: rest)   => input.extract[RequestMessage]
      case JArray(JInt(RESULTTYPE) :: rest) => input.extract[ResponseMessage]
      case JArray(JInt(ERRORTYPE) :: rest)  => input.extract[ErrorResponseMessage]
      case _                                => sys.error(s"Unrecognized JSON command message $input")
    }
  }

  def writeJValue(input: TransportMessage): JValue = {
    Extraction.decompose(input)
  }

  def write(input: TransportMessage): String = compact(render(writeJValue(input)))
}
