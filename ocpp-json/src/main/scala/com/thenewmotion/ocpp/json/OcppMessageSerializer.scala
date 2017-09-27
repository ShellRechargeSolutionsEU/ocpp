package com.thenewmotion.ocpp
package json

import org.json4s.{DefaultFormats, Extraction, JValue}

/**
 * A typeclass of all types that can be serialized to/from any supported OCPP-JSON version
 *
 * @tparam M The message type, for example com.thenewmotion.ocpp.messages.AuthorizeReq
 * @tparam V The version to/from which the message can be serialized
 */
trait OcppMessageSerializer[M <: messages.Message, V <: Version] {

  protected[json] type VersionSpecific

  protected val manifest: Manifest[VersionSpecific]

  protected implicit val formats = DefaultFormats + new ZonedDateTimeJsonFormat

  protected[json] def to(msg: M): VersionSpecific
  protected[json] def from(msg: VersionSpecific): M

  def serialize(msg: M): JValue = Extraction.decompose(to(msg))

  def deserialize(json: JValue): M = from(Extraction.extract[VersionSpecific](json)(formats, manifest))
}

object OcppMessageSerializer {
  def variantFor[M <: messages.Message, V <: Version, SpecM <: VersionSpecificMessage : Manifest](
    _to: M => SpecM,
    _from: SpecM => M): OcppMessageSerializer[M, V] =
    new OcppMessageSerializer[M, V] {

      protected[json] type VersionSpecific = SpecM

      protected val manifest: Manifest[SpecM] = implicitly[Manifest[SpecM]]

      protected[json] def to(msg: M): SpecM = _to(msg)

      protected[json] def from(msg: SpecM): M = _from(msg)
  }
}

