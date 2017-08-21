package com.thenewmotion.ocpp
package json

import org.json4s.{DefaultFormats, JValue, Extraction}
import messages._

/**
 * A typeclass of all types that can be serialized to/from any supported OCPP-JSON version
 *
 * @tparam T The message type, for example com.thenewmotion.ocpp.messages.AuthorizeReq
 */
trait JsonDeserializable[T <: messages.Message] {
  type V15Type

  def fromV15(v15: V15Type): T

  def deserializeV15(json: JValue): T
}

class JsonDeserializableWithManifest[T <: messages.Message, V15T <: v15.Message : Manifest] extends JsonDeserializable[T] {
  implicit val formats = DefaultFormats + new ZonedDateTimeJsonFormat

  type V15Type = V15T

  // TODO Some kind of type witnesses should let us get rid of the asInstanceOf here
  def fromV15(msg: V15Type): T = v15.ConvertersV15.fromV15(msg).asInstanceOf[T]

  def deserializeV15(json: JValue): T = fromV15(Extraction.extract[V15Type](json))
}

object JsonDeserializable {
  def jsonDeserializable[T <: Message : JsonDeserializable]: JsonDeserializable[T] = implicitly[JsonDeserializable[T]]

  implicit val bootNotificationReq =
    new JsonDeserializableWithManifest[BootNotificationReq, v15.BootNotificationReq]

  implicit val bootNotificationRes =
    new JsonDeserializableWithManifest[BootNotificationRes, v15.BootNotificationRes]

  implicit val authorizeReq =
    new JsonDeserializableWithManifest[AuthorizeReq, v15.AuthorizeReq]

  implicit val authorizeRes =
    new JsonDeserializableWithManifest[AuthorizeRes, v15.AuthorizeRes]

  implicit val startTransactionReq =
    new JsonDeserializableWithManifest[StartTransactionReq, v15.StartTransactionReq]

  implicit val startTransactionRes =
    new JsonDeserializableWithManifest[StartTransactionRes, v15.StartTransactionRes]

  implicit val stopTransactionReq =
    new JsonDeserializableWithManifest[StopTransactionReq, v15.StopTransactionReq]

  implicit val stopTransactionRes =
    new JsonDeserializableWithManifest[StopTransactionRes, v15.StopTransactionRes]

  implicit val unlockConnectorReq =
    new JsonDeserializableWithManifest[UnlockConnectorReq, v15.UnlockConnectorReq]

  implicit val unlockConnectorRes =
    new JsonDeserializableWithManifest[UnlockConnectorRes, v15.UnlockConnectorRes]

  implicit val resetReq =
    new JsonDeserializableWithManifest[ResetReq, v15.ResetReq]

  implicit val resetRes =
    new JsonDeserializableWithManifest[ResetRes, v15.ResetRes]

  implicit val changeAvailabilityReq =
    new JsonDeserializableWithManifest[ChangeAvailabilityReq, v15.ChangeAvailabilityReq]

  implicit val changeAvailabilityRes =
    new JsonDeserializableWithManifest[ChangeAvailabilityRes, v15.ChangeAvailabilityRes]

  implicit val statusNotificationReq =
    new JsonDeserializableWithManifest[StatusNotificationReq, v15.StatusNotificationReq]

  implicit val statusNotificationRes =
    new JsonDeserializableWithManifest[StatusNotificationRes.type, v15.StatusNotificationRes]

  implicit val remoteStartTransactionReq =
    new JsonDeserializableWithManifest[RemoteStartTransactionReq, v15.RemoteStartTransactionReq]

  implicit val remoteStartTransactionRes =
    new JsonDeserializableWithManifest[RemoteStartTransactionRes, v15.RemoteStartTransactionRes]

  implicit val remoteStopTransactionReq =
    new JsonDeserializableWithManifest[RemoteStopTransactionReq, v15.RemoteStopTransactionReq]

  implicit val remoteStopTransactionRes =
    new JsonDeserializableWithManifest[RemoteStopTransactionRes, v15.RemoteStopTransactionRes]

  implicit val heartbeatReq =
    new JsonDeserializableWithManifest[HeartbeatReq.type, v15.HeartbeatReq]

  implicit val heartbeatRes =
    new JsonDeserializableWithManifest[HeartbeatRes, v15.HeartbeatRes]

  implicit val updateFirmwareReq =
    new JsonDeserializableWithManifest[UpdateFirmwareReq, v15.UpdateFirmwareReq]

  implicit val updateFirmwareRes =
    new JsonDeserializableWithManifest[UpdateFirmwareRes.type, v15.UpdateFirmwareRes]

  implicit val firmwareStatusNotificationReq =
    new JsonDeserializableWithManifest[FirmwareStatusNotificationReq, v15.FirmwareStatusNotificationReq]

  implicit val firmwareStatusNotificationRes =
    new JsonDeserializableWithManifest[FirmwareStatusNotificationRes.type, v15.FirmwareStatusNotificationRes]

  implicit val getDiagnosticsReq =
    new JsonDeserializableWithManifest[GetDiagnosticsReq, v15.GetDiagnosticsReq]

  implicit val getDiagnosticsRes =
    new JsonDeserializableWithManifest[GetDiagnosticsRes, v15.GetDiagnosticsRes]

  implicit val diagnosticsStatusNotificationReq =
    new JsonDeserializableWithManifest[DiagnosticsStatusNotificationReq, v15.DiagnosticsStatusNotificationReq]

  implicit val diagnosticsStatusNotificationRes =
    new JsonDeserializableWithManifest[DiagnosticsStatusNotificationRes.type, v15.DiagnosticsStatusNotificationRes]

  implicit val meterValuesReq =
    new JsonDeserializableWithManifest[MeterValuesReq, v15.MeterValuesReq]

  implicit val meterValuesRes =
    new JsonDeserializableWithManifest[MeterValuesRes.type, v15.MeterValuesRes]

  implicit val changeConfigurationReq =
    new JsonDeserializableWithManifest[ChangeConfigurationReq, v15.ChangeConfigurationReq]

  implicit val changeConfigurationRes =
    new JsonDeserializableWithManifest[ChangeConfigurationRes, v15.ChangeConfigurationRes]

  implicit val clearCacheReq =
    new JsonDeserializableWithManifest[ClearCacheReq.type, v15.ClearCacheReq]

  implicit val clearCacheRes =
    new JsonDeserializableWithManifest[ClearCacheRes, v15.ClearCacheRes]

  implicit val getConfigurationReq =
    new JsonDeserializableWithManifest[GetConfigurationReq, v15.GetConfigurationReq]

  implicit val getConfigurationRes =
    new JsonDeserializableWithManifest[GetConfigurationRes, v15.GetConfigurationRes]

  implicit val getLocalListVersionReq =
    new JsonDeserializableWithManifest[GetLocalListVersionReq.type, v15.GetLocalListVersionReq]

  implicit val getLocalListVersionRes =
    new JsonDeserializableWithManifest[GetLocalListVersionRes, v15.GetLocalListVersionRes]

  implicit val sendLocalListReq =
    new JsonDeserializableWithManifest[SendLocalListReq, v15.SendLocalListReq]

  implicit val sendLocalListRes =
    new JsonDeserializableWithManifest[SendLocalListRes, v15.SendLocalListRes]

  implicit val reserveNowReq =
    new JsonDeserializableWithManifest[ReserveNowReq, v15.ReserveNowReq]

  implicit val reserveNowRes =
    new JsonDeserializableWithManifest[ReserveNowRes, v15.ReserveNowRes]

  implicit val cancelReservationReq =
    new JsonDeserializableWithManifest[CancelReservationReq, v15.CancelReservationReq]

  implicit val cancelReservationRes =
    new JsonDeserializableWithManifest[CancelReservationRes, v15.CancelReservationRes]
}
