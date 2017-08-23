package com.thenewmotion.ocpp
package json
package v15

import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.native.JsonParser
import ConvertersV15._
import JsonDeserializable._

/** Reading and writing OCPP 1.5 messages encoded with JSON */
object Ocpp15J {

  implicit val formats = DefaultFormats + new ZonedDateTimeJsonFormat

  def serialize(msg: messages.Message): JValue = Extraction.decompose(toV15(msg))

  def write(msg: messages.Message): String = compact(render(serialize(msg)))

  def deserialize[M <: messages.Message : JsonDeserializable](json: JValue): M =
    jsonDeserializable[M].deserializeV15(json)

  def read[M <: messages.Message : JsonDeserializable](s: String) = deserialize[M](JsonParser.parse(s))

  val version: Version.Value = Version.V15

}
