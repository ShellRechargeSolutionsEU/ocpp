package com.thenewmotion.ocpp
package json

import net.liftweb.json.{DefaultFormats, JValue, Extraction}
import messages._

/**
 * A typeclass of all types that can be serialized to/from any supported OCPP-JSON version
 *
 * @tparam T The message type, for example com.thenewmotion.ocpp.messages.AuthorizeReq
 */
trait JsonSerializable[T <: messages.Message] {
  type V15Type

  def toV15(msg: T): V15Type

  def fromV15(v15: V15Type): T

  def serializeV15(msg: T): JValue

  def deserializeV15(json: JValue): T
}

class JsonSerializableWithManifest[T <: messages.Message, V15T <: v15.Message : Manifest] extends JsonSerializable[T] {
  implicit val formats = DefaultFormats + new OcppMessageJsonSerializers.JodaDateTimeJsonFormat

  type V15Type = V15T

  // TODO Some kind of type witnesses should let us get rid of the asInstanceOf here
  def toV15(msg: T): V15Type = v15.ConvertersV15.toV15(msg).asInstanceOf[V15Type]

  def fromV15(msg: V15Type): T = v15.ConvertersV15.fromV15(msg).asInstanceOf[T]

  def serializeV15(msg: T): JValue = Extraction.decompose(toV15(msg))

  def deserializeV15(json: JValue): T = fromV15(Extraction.extract[V15Type](json))
}

object JsonSerializable {
  def jsonSerializable[T <: Message : JsonSerializable]: JsonSerializable[T] = implicitly[JsonSerializable[T]]

  implicit val bootNotificationReq =
    new JsonSerializableWithManifest[BootNotificationReq, v15.BootNotificationReq]

  implicit val authorizeReq =
    new JsonSerializableWithManifest[AuthorizeReq, v15.AuthorizeReq]

  implicit val authorizeRes =
    new JsonSerializableWithManifest[AuthorizeRes, v15.AuthorizeRes]

  implicit val startTransactionReq =
    new JsonSerializableWithManifest[StartTransactionReq, v15.StartTransactionReq]

  implicit val startTransactionRes =
    new JsonSerializableWithManifest[StartTransactionRes, v15.StartTransactionRes]

  implicit val stopTransactionReq =
    new JsonSerializableWithManifest[StopTransactionReq, v15.StopTransactionReq]

  implicit val stopTransactionRes =
    new JsonSerializableWithManifest[StopTransactionRes, v15.StopTransactionRes]

  implicit val unlockConnectorRes =
    new JsonSerializableWithManifest[UnlockConnectorRes, v15.UnlockConnectorRes]

  implicit val resetRes =
    new JsonSerializableWithManifest[ResetRes, v15.ResetRes]

  implicit val changeAvailabilityRes =
    new JsonSerializableWithManifest[ChangeAvailabilityRes, v15.ChangeAvailabilityRes]

  implicit val statusNotificationReq =
    new JsonSerializableWithManifest[StatusNotificationReq, v15.StatusNotificationReq]

  implicit val remoteStartTransactionRes =
    new JsonSerializableWithManifest[RemoteStartTransactionRes, v15.RemoteStartTransactionRes]

  implicit val remoteStopTransactionRes =
    new JsonSerializableWithManifest[RemoteStopTransactionRes, v15.RemoteStopTransactionRes]

  implicit val heartbeatReq =
    new JsonSerializableWithManifest[HeartbeatReq.type, v15.HeartbeatReq]

  implicit val updateFirmwareRes =
    new JsonSerializableWithManifest[UpdateFirmwareRes.type, v15.UpdateFirmwareRes]

  implicit val firmwareStatusNotificationReq =
    new JsonSerializableWithManifest[FirmwareStatusNotificationReq, v15.FirmwareStatusNotificationReq]

  implicit val getDiagnosticsRes =
    new JsonSerializableWithManifest[GetDiagnosticsRes, v15.GetDiagnosticsRes]

  implicit val diagnosticsStatusNotificationReq =
    new JsonSerializableWithManifest[DiagnosticsStatusNotificationReq, v15.DiagnosticsStatusNotificationReq]

  implicit val meterValuesReq =
    new JsonSerializableWithManifest[MeterValuesReq, v15.MeterValuesReq]

  implicit val changeConfigurationRes =
    new JsonSerializableWithManifest[ChangeConfigurationRes, v15.ChangeConfigurationRes]

  implicit val clearCacheRes =
    new JsonSerializableWithManifest[ClearCacheRes, v15.ClearCacheRes]

  implicit val getConfigurationRes =
    new JsonSerializableWithManifest[GetConfigurationRes, v15.GetConfigurationRes]

  implicit val getLocalListVersionRes =
    new JsonSerializableWithManifest[GetLocalListVersionRes, v15.GetLocalListVersionRes]

  implicit val sendLocalListRes =
    new JsonSerializableWithManifest[SendLocalListRes, v15.SendLocalListRes]

  implicit val reserveNowRes =
    new JsonSerializableWithManifest[ReserveNowRes, v15.ReserveNowRes]

  implicit val cancelReservationRes =
    new JsonSerializableWithManifest[CancelReservationRes, v15.CancelReservationRes]
}
