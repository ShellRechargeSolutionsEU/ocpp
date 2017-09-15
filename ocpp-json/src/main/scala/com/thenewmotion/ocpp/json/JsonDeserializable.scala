package com.thenewmotion.ocpp
package json

import com.thenewmotion.ocpp.messages.Message
import org.json4s.{DefaultFormats, Extraction, JValue, MappingException}

/**
 * A typeclass of all types that can be serialized to/from any supported OCPP-JSON version
 *
 * @tparam T The message type, for example com.thenewmotion.ocpp.messages.AuthorizeReq
 */
trait JsonDeserializable[T <: messages.Message] {

  def fromVersionSpecific(msg: VersionSpecificMessage): T

  def deserialize(json: JValue): T
}

abstract class VersionVariant[N <: VersionSpecificMessage : Manifest, V <: Version] {
  implicit val formats = DefaultFormats + new ZonedDateTimeJsonFormat

  def to(msg: messages.Message): N
  def from(msg: N): messages.Message

}


object JsonDeserializable {
//  def jsonDeserializable[T <: Message : JsonDeserializable]: JsonDeserializable[T] = implicitly[JsonDeserializable[T]]

  implicit object V15Variant extends VersionVariant[v15.Message, Version.V15.type] {
    def to(msg: messages.Message): v15.Message = v15.ConvertersV15.toV15(msg)

    def from(msg:v15.Message): messages.Message = {
      v15.ConvertersV15.fromV15(msg)
    }
  }

  implicit object V16Variant extends VersionVariant[v16.Message, Version.V16.type ] {

    def to(msg: messages.Message):v16.Message = v16.ConvertersV16.toV16(msg)

    def from(msg: v16.Message):messages.Message = {
      v16.ConvertersV16.fromV16(msg)
    }
  }
}