package com.thenewmotion.ocpp
package json.v15

import net.liftweb.json._
import ConvertersV15._
import com.thenewmotion.ocpp.json.OcppMessageJsonSerializers

/** Reading and writing OCPP 1.5 messages encoded with JSON */
object OcppJ15 {

  implicit val formats = DefaultFormats + new OcppMessageJsonSerializers.JodaDateTimeJsonFormat

  def serialize(msg: messages.Message): JValue = Extraction.decompose(toV15(msg))

  def write(msg: messages.Message): String = compact(render(serialize(msg)))

  // todo: add typeclass to be able to pass generic type here
  def deserialize[M <: Message : Manifest](json: JValue): messages.Message = fromV15(Extraction.extract[M](json))

  def read(s: String) = deserialize(JsonParser.parse(s))

  val version: Version.Value = Version.V15

}
