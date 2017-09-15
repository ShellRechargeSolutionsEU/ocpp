package com.thenewmotion.ocpp.json

import com.thenewmotion.ocpp.{Version, messages}
import org.json4s._

/** Reading and writing OCPP 1.5 messages encoded with JSON */
object OcppJ {

  implicit val formats: Formats = DefaultFormats + new ZonedDateTimeJsonFormat

  def serialize[M <: messages.Message, V <: Version](msg: M)(implicit versionVariant: VersionVariant[_, V]): JValue =
    Extraction.decompose(versionVariant.to(msg))

//  def write[M <: messages.Message](msg: M)(implicit versionVariant: VersionVariant[M, _, Version.type]): String =
//    compact(render(serialize(msg)))

  def deserialize[M <: VersionSpecificMessage : Manifest](json: JValue)(implicit versionVariant: VersionVariant[M, Version]): messages.Message = {
    val msg = Extraction.extract[M](json)
    versionVariant.from(msg)
  }
//  def read[M <: messages.Message : JsonDeserializable](s: String)(implicit versionVariant: VersionVariant[M, _, Version.type]): M =
//    deserialize[M](JsonParser.parse(s))


}
