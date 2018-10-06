package com.thenewmotion.ocpp
package json

import org.json4s._
import org.json4s.native.JsonParser
import org.json4s.native.JsonMethods._

object TransportMessageParser {

  implicit val formats =
    DefaultFormats + TransportMessageJsonSerializer()

  def parse(input: String): SrpcEnvelope =
    parse(JsonParser.parse(input))

  def parse(input: JValue): SrpcEnvelope = {
    input.extract[SrpcEnvelope]
  }

  def writeJValue(input: SrpcEnvelope): JValue = {
    Extraction.decompose(input)
  }

  def write(input: SrpcEnvelope): String =
    compact(render(writeJValue(input)))
}
