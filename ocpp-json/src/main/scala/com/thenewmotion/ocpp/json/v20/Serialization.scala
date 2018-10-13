package com.thenewmotion.ocpp
package json
package v20

import org.json4s.{DefaultFormats, Extraction, Formats, JValue}
import org.json4s.native.JsonMethods.{compact, render}
import org.json4s.native.JsonParser.parse
import messages.v20._

object Serialization {
  private val formats: Formats = DefaultFormats ++ serialization.ocppSerializers

  def serialize(msg: Message): JValue =
    Extraction.decompose(msg)(formats)

  def write(msg: Message): String =
    compact(render(serialize(msg)))

  def deserialize[M <: Message : Manifest](json: JValue): M =
    Extraction.extract[M](json)(formats, manifest[M])

  def read[M <: Message : Manifest](s: String): M =
    deserialize[M](parse(s, useBigDecimalForDouble = true))
}
