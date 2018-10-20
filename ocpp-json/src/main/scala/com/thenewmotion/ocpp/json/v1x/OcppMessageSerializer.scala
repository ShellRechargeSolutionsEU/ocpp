package com.thenewmotion.ocpp
package json
package v1x

import org.json4s.{DefaultFormats, Extraction, JValue}

/**
 * A typeclass of all types that can be serialized to/from any supported OCPP-JSON version
 *
 * @tparam M The message type, for example com.thenewmotion.ocpp.messages.AuthorizeReq
 * @tparam V The version to/from which the message can be serialized
 */
trait OcppMessageSerializer[M <: messages.v1x.Message, V <: Version] {

  protected[json] type VersionSpecific

  protected val manifest: Manifest[VersionSpecific]

  protected implicit val formats = DefaultFormats + new ZonedDateTimeJsonFormat

  protected[json] def to(msg: M): VersionSpecific
  protected[json] def from(msg: VersionSpecific): M

  def serialize(msg: M): JValue = Extraction.decompose(to(msg))

  def deserialize(json: JValue): M = from(Extraction.extract[VersionSpecific](json)(formats, manifest))
}

object OcppMessageSerializer {
  def variantFor[M <: messages.v1x.Message, V <: Version, SpecM <: VersionSpecificMessage : Manifest](
    toF: M => SpecM,
    fromF: SpecM => M): OcppMessageSerializer[M, V] =
    new OcppMessageSerializer[M, V] {

      protected[json] type VersionSpecific = SpecM

      protected val manifest: Manifest[SpecM] = implicitly[Manifest[SpecM]]

      protected[json] def to(msg: M): SpecM = toF(msg)

      protected[json] def from(msg: SpecM): M = fromF(msg)
  }
}

