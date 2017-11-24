package com.thenewmotion.ocpp
package json

import org.json4s._
import jackson.JsonMethods.{compact, render, parse}
import messages.Message


/** Reading and writing OCPP 1.x messages encoded with JSON */
object OcppJ {

  implicit val formats: Formats = DefaultFormats + new ZonedDateTimeJsonFormat

  def serialize[M <: Message, V <: Version](msg: M)(implicit versionVariant: OcppMessageSerializer[M, V]): JValue =
    versionVariant.serialize(msg)

  def write[M <: messages.Message, V <: Version](msg: M)(implicit versionVariant: OcppMessageSerializer[M, V]): String =
    compact(render(serialize[M, V](msg)))

  def deserialize[M <: Message, V <: Version](json: JValue)(implicit versionVariant: OcppMessageSerializer[M, V]): M =
    versionVariant.deserialize(json)

  def read[M <: Message, V <: Version](s: String)(implicit versionVariant: OcppMessageSerializer[M,  V]): M =
    deserialize[M, V](parse(s))
}
