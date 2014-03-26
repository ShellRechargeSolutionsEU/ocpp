package com.thenewmotion.ocpp
package json
package v15

import net.liftweb.json._
import ConvertersV15._
import com.thenewmotion.ocpp.json.{JsonSerializable, OcppMessageJsonSerializers}
import JsonSerializable._

/** Reading and writing OCPP 1.5 messages encoded with JSON */
object Ocpp15J {

  implicit val formats = DefaultFormats + new OcppMessageJsonSerializers.JodaDateTimeJsonFormat

  def serialize(msg: messages.Message): JValue = Extraction.decompose(toV15(msg))

  def write(msg: messages.Message): String = compact(render(serialize(msg)))

  def deserialize[M <: messages.Message : JsonSerializable](json: JValue): M =
    jsonSerializable[M].deserializeV15(json)

  def read[M <: messages.Message : JsonSerializable](s: String) = deserialize[M](JsonParser.parse(s))

  val version: Version.Value = Version.V15

}
