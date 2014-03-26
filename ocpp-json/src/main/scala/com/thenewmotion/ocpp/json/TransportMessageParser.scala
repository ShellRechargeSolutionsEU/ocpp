package com.thenewmotion.ocpp
package json

import net.liftweb.json._
import com.typesafe.scalalogging.slf4j.Logging

object TransportMessageParser extends Logging {

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

  def write(input: TransportMessage): String = compactRender(writeJValue(input))
}
