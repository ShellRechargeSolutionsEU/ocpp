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

  protected type VersionSpecific

  protected val manifest: Manifest[VersionSpecific]

  protected implicit val formats = DefaultFormats + new ZonedDateTimeJsonFormat

  protected def to(msg: M): VersionSpecific
  protected def from(msg: VersionSpecific): M

  def serialize(msg: M): JValue = Extraction.decompose(to(msg))

  def deserialize(json: JValue): M = from(Extraction.extract[VersionSpecific](json)(formats, manifest))
}

object OcppMessageSerializer {
  def variantFor[M <: messages.Message, V <: Version, SpecM <: VersionSpecificMessage : Manifest](
    _to: M => SpecM,
    _from: SpecM => M): OcppMessageSerializer[M, V] =
    new OcppMessageSerializer[M, V] {

      protected type VersionSpecific = SpecM

      protected val manifest: Manifest[SpecM] = implicitly[Manifest[SpecM]]

      override protected def to(msg: M): SpecM = _to(msg)

      override protected def from(msg: SpecM): M = _from(msg)
  }
}

object OcppMessageSerializers {

  object Ocpp15 {
    implicit val AuthorizeReqV15Variant = OcppMessageSerializer.variantFor[messages.AuthorizeReq, Version.V15.type, v15.AuthorizeReq](
      (msg: messages.AuthorizeReq) => v15.ConvertersV15.toV15(msg).asInstanceOf[v15.AuthorizeReq],
      (msg: v15.AuthorizeReq) => v15.ConvertersV15.fromV15(msg).asInstanceOf[messages.AuthorizeReq]
    )

    implicit val AuthorizeResV15Variant = OcppMessageSerializer.variantFor[messages.AuthorizeRes, Version.V15.type, v15.AuthorizeRes](
      (msg: messages.AuthorizeRes) => v15.ConvertersV15.toV15(msg).asInstanceOf[v15.AuthorizeRes],
      (msg: v15.AuthorizeRes) => v15.ConvertersV15.fromV15(msg).asInstanceOf[messages.AuthorizeRes]
    )

    implicit val StartTransactionReqV15Variant = OcppMessageSerializer.variantFor[messages.StartTransactionReq, Version.V15.type, v15.StartTransactionReq](
      (msg: messages.StartTransactionReq) => v15.ConvertersV15.toV15(msg).asInstanceOf[v15.StartTransactionReq],
      (msg: v15.StartTransactionReq) => v15.ConvertersV15.fromV15(msg).asInstanceOf[messages.StartTransactionReq]
    )

    implicit val StartTransactionResV15Variant = OcppMessageSerializer.variantFor[messages.StartTransactionRes, Version.V15.type, v15.StartTransactionRes](
      (msg: messages.StartTransactionRes) => v15.ConvertersV15.toV15(msg).asInstanceOf[v15.StartTransactionRes],
      (msg: v15.StartTransactionRes) => v15.ConvertersV15.fromV15(msg).asInstanceOf[messages.StartTransactionRes]
    )

    implicit val StopTransactionReqV15Variant = OcppMessageSerializer.variantFor[messages.StopTransactionReq, Version.V15.type, v15.StopTransactionReq](
      (msg: messages.StopTransactionReq) => v15.ConvertersV15.toV15(msg).asInstanceOf[v15.StopTransactionReq],
      (msg: v15.StopTransactionReq) => v15.ConvertersV15.fromV15(msg).asInstanceOf[messages.StopTransactionReq]
    )

    implicit val StopTransactionResV15Variant = OcppMessageSerializer.variantFor[messages.StopTransactionRes, Version.V15.type, v15.StopTransactionRes](
      (msg: messages.StopTransactionRes) => v15.ConvertersV15.toV15(msg).asInstanceOf[v15.StopTransactionRes],
      (msg: v15.StopTransactionRes) => v15.ConvertersV15.fromV15(msg).asInstanceOf[messages.StopTransactionRes]
    )

    implicit val HeartbeatReqV15Variant = OcppMessageSerializer.variantFor[messages.HeartbeatReq.type, Version.V15.type, v15.HeartbeatReq](
      (msg: messages.HeartbeatReq.type) => v15.ConvertersV15.toV15(msg).asInstanceOf[v15.HeartbeatReq],
      (msg: v15.HeartbeatReq) => v15.ConvertersV15.fromV15(msg).asInstanceOf[messages.HeartbeatReq.type]
    )

    implicit val HeartbeatResV15Variant = OcppMessageSerializer.variantFor[messages.HeartbeatRes, Version.V15.type, v15.HeartbeatRes](
      (msg: messages.HeartbeatRes) => v15.ConvertersV15.toV15(msg).asInstanceOf[v15.HeartbeatRes],
      (msg: v15.HeartbeatRes) => v15.ConvertersV15.fromV15(msg).asInstanceOf[messages.HeartbeatRes]
    )

    implicit val MeterValuesReqV15Variant = OcppMessageSerializer.variantFor[messages.MeterValuesReq, Version.V15.type, v15.MeterValuesReq](
      (msg: messages.MeterValuesReq) => v15.ConvertersV15.toV15(msg).asInstanceOf[v15.MeterValuesReq],
      (msg: v15.MeterValuesReq) => v15.ConvertersV15.fromV15(msg).asInstanceOf[messages.MeterValuesReq]
    )

    implicit val MeterValuesResV15Variant = OcppMessageSerializer.variantFor[messages.MeterValuesRes.type, Version.V15.type, v15.MeterValuesRes](
      (msg: messages.MeterValuesRes.type) => v15.ConvertersV15.toV15(msg).asInstanceOf[v15.MeterValuesRes],
      (msg: v15.MeterValuesRes) => v15.ConvertersV15.fromV15(msg).asInstanceOf[messages.MeterValuesRes.type]
    )

    implicit val BootNotificationReqV15Variant = OcppMessageSerializer.variantFor[messages.BootNotificationReq, Version.V15.type, v15.BootNotificationReq](
      (msg: messages.BootNotificationReq) => v15.ConvertersV15.toV15(msg).asInstanceOf[v15.BootNotificationReq],
      (msg: v15.BootNotificationReq) => v15.ConvertersV15.fromV15(msg).asInstanceOf[messages.BootNotificationReq]
    )

    implicit val BootNotificationResV15Variant = OcppMessageSerializer.variantFor[messages.BootNotificationRes, Version.V15.type, v15.BootNotificationRes](
      (msg: messages.BootNotificationRes) => v15.ConvertersV15.toV15(msg).asInstanceOf[v15.BootNotificationRes],
      (msg: v15.BootNotificationRes) => v15.ConvertersV15.fromV15(msg).asInstanceOf[messages.BootNotificationRes]
    )

    implicit val CentralSystemDataTransferReqV15Variant = OcppMessageSerializer.variantFor[messages.CentralSystemDataTransferReq, Version.V15.type, v15.DataTransferReq](
      (msg: messages.CentralSystemDataTransferReq) => v15.ConvertersV15.toV15(msg).asInstanceOf[v15.DataTransferReq],
      (msg: v15.DataTransferReq) => v15.ConvertersV15.fromV15(msg).asInstanceOf[messages.CentralSystemDataTransferReq]
    )

    implicit val CentralSystemDataTransferResV15Variant = OcppMessageSerializer.variantFor[messages.CentralSystemDataTransferRes, Version.V15.type, v15.DataTransferRes](
      (msg: messages.CentralSystemDataTransferRes) => v15.ConvertersV15.toV15(msg).asInstanceOf[v15.DataTransferRes],
      (msg: v15.DataTransferRes) => v15.ConvertersV15.fromV15(msg).asInstanceOf[messages.CentralSystemDataTransferRes]
    )

    implicit val StatusNotificationReqV15Variant = OcppMessageSerializer.variantFor[messages.StatusNotificationReq, Version.V15.type, v15.StatusNotificationReq](
      (msg: messages.StatusNotificationReq) => v15.ConvertersV15.toV15(msg).asInstanceOf[v15.StatusNotificationReq],
      (msg: v15.StatusNotificationReq) => v15.ConvertersV15.fromV15(msg).asInstanceOf[messages.StatusNotificationReq]
    )

    implicit val StatusNotificationResV15Variant = OcppMessageSerializer.variantFor[messages.StatusNotificationRes.type, Version.V15.type, v15.StatusNotificationRes](
      (msg: messages.StatusNotificationRes.type) => v15.ConvertersV15.toV15(msg).asInstanceOf[v15.StatusNotificationRes],
      (msg: v15.StatusNotificationRes) => v15.ConvertersV15.fromV15(msg).asInstanceOf[messages.StatusNotificationRes.type]
    )

    implicit val FirmwareStatusNotificationReqV15Variant = OcppMessageSerializer.variantFor[messages.FirmwareStatusNotificationReq, Version.V15.type, v15.FirmwareStatusNotificationReq](
      (msg: messages.FirmwareStatusNotificationReq) => v15.ConvertersV15.toV15(msg).asInstanceOf[v15.FirmwareStatusNotificationReq],
      (msg: v15.FirmwareStatusNotificationReq) => v15.ConvertersV15.fromV15(msg).asInstanceOf[messages.FirmwareStatusNotificationReq]
    )

    implicit val FirmwareStatusNotificationResV15Variant = OcppMessageSerializer.variantFor[messages.FirmwareStatusNotificationRes.type, Version.V15.type, v15.FirmwareStatusNotificationRes](
      (msg: messages.FirmwareStatusNotificationRes.type) => v15.ConvertersV15.toV15(msg).asInstanceOf[v15.FirmwareStatusNotificationRes],
      (msg: v15.FirmwareStatusNotificationRes) => v15.ConvertersV15.fromV15(msg).asInstanceOf[messages.FirmwareStatusNotificationRes.type]
    )

    implicit val DiagnosticsStatusNotificationReqV15Variant = OcppMessageSerializer.variantFor[messages.DiagnosticsStatusNotificationReq, Version.V15.type, v15.DiagnosticsStatusNotificationReq](
      (msg: messages.DiagnosticsStatusNotificationReq) => v15.ConvertersV15.toV15(msg).asInstanceOf[v15.DiagnosticsStatusNotificationReq],
      (msg: v15.DiagnosticsStatusNotificationReq) => v15.ConvertersV15.fromV15(msg).asInstanceOf[messages.DiagnosticsStatusNotificationReq]
    )

    implicit val DiagnosticsStatusNotificationResV15Variant = OcppMessageSerializer.variantFor[messages.DiagnosticsStatusNotificationRes.type, Version.V15.type, v15.DiagnosticsStatusNotificationRes](
      (msg: messages.DiagnosticsStatusNotificationRes.type) => v15.ConvertersV15.toV15(msg).asInstanceOf[v15.DiagnosticsStatusNotificationRes],
      (msg: v15.DiagnosticsStatusNotificationRes) => v15.ConvertersV15.fromV15(msg).asInstanceOf[messages.DiagnosticsStatusNotificationRes.type]
    )

    implicit val RemoteStartTransactionReqV15Variant = OcppMessageSerializer.variantFor[messages.RemoteStartTransactionReq, Version.V15.type, v15.RemoteStartTransactionReq](
      (msg: messages.RemoteStartTransactionReq) => v15.ConvertersV15.toV15(msg).asInstanceOf[v15.RemoteStartTransactionReq],
      (msg: v15.RemoteStartTransactionReq) => v15.ConvertersV15.fromV15(msg).asInstanceOf[messages.RemoteStartTransactionReq]
    )

    implicit val RemoteStartTransactionResV15Variant = OcppMessageSerializer.variantFor[messages.RemoteStartTransactionRes, Version.V15.type, v15.RemoteStartTransactionRes](
      (msg: messages.RemoteStartTransactionRes) => v15.ConvertersV15.toV15(msg).asInstanceOf[v15.RemoteStartTransactionRes],
      (msg: v15.RemoteStartTransactionRes) => v15.ConvertersV15.fromV15(msg).asInstanceOf[messages.RemoteStartTransactionRes]
    )

    implicit val RemoteStopTransactionReqV15Variant = OcppMessageSerializer.variantFor[messages.RemoteStopTransactionReq, Version.V15.type, v15.RemoteStopTransactionReq](
      (msg: messages.RemoteStopTransactionReq) => v15.ConvertersV15.toV15(msg).asInstanceOf[v15.RemoteStopTransactionReq],
      (msg: v15.RemoteStopTransactionReq) => v15.ConvertersV15.fromV15(msg).asInstanceOf[messages.RemoteStopTransactionReq]
    )

    implicit val RemoteStopTransactionResV15Variant = OcppMessageSerializer.variantFor[messages.RemoteStopTransactionRes, Version.V15.type, v15.RemoteStopTransactionRes](
      (msg: messages.RemoteStopTransactionRes) => v15.ConvertersV15.toV15(msg).asInstanceOf[v15.RemoteStopTransactionRes],
      (msg: v15.RemoteStopTransactionRes) => v15.ConvertersV15.fromV15(msg).asInstanceOf[messages.RemoteStopTransactionRes]
    )

    implicit val UnlockConnectorReqV15Variant = OcppMessageSerializer.variantFor[messages.UnlockConnectorReq, Version.V15.type, v15.UnlockConnectorReq](
      (msg: messages.UnlockConnectorReq) => v15.ConvertersV15.toV15(msg).asInstanceOf[v15.UnlockConnectorReq],
      (msg: v15.UnlockConnectorReq) => v15.ConvertersV15.fromV15(msg).asInstanceOf[messages.UnlockConnectorReq]
    )

    implicit val UnlockConnectorResV15Variant = OcppMessageSerializer.variantFor[messages.UnlockConnectorRes, Version.V15.type, v15.UnlockConnectorRes](
      (msg: messages.UnlockConnectorRes) => v15.ConvertersV15.toV15(msg).asInstanceOf[v15.UnlockConnectorRes],
      (msg: v15.UnlockConnectorRes) => v15.ConvertersV15.fromV15(msg).asInstanceOf[messages.UnlockConnectorRes]
    )

    implicit val GetDiagnosticsReqV15Variant = OcppMessageSerializer.variantFor[messages.GetDiagnosticsReq, Version.V15.type, v15.GetDiagnosticsReq](
      (msg: messages.GetDiagnosticsReq) => v15.ConvertersV15.toV15(msg).asInstanceOf[v15.GetDiagnosticsReq],
      (msg: v15.GetDiagnosticsReq) => v15.ConvertersV15.fromV15(msg).asInstanceOf[messages.GetDiagnosticsReq]
    )

    implicit val GetDiagnosticsResV15Variant = OcppMessageSerializer.variantFor[messages.GetDiagnosticsRes, Version.V15.type, v15.GetDiagnosticsRes](
      (msg: messages.GetDiagnosticsRes) => v15.ConvertersV15.toV15(msg).asInstanceOf[v15.GetDiagnosticsRes],
      (msg: v15.GetDiagnosticsRes) => v15.ConvertersV15.fromV15(msg).asInstanceOf[messages.GetDiagnosticsRes]
    )

    implicit val ChangeConfigurationReqV15Variant = OcppMessageSerializer.variantFor[messages.ChangeConfigurationReq, Version.V15.type, v15.ChangeConfigurationReq](
      (msg: messages.ChangeConfigurationReq) => v15.ConvertersV15.toV15(msg).asInstanceOf[v15.ChangeConfigurationReq],
      (msg: v15.ChangeConfigurationReq) => v15.ConvertersV15.fromV15(msg).asInstanceOf[messages.ChangeConfigurationReq]
    )

    implicit val ChangeConfigurationResV15Variant = OcppMessageSerializer.variantFor[messages.ChangeConfigurationRes, Version.V15.type, v15.ChangeConfigurationRes](
      (msg: messages.ChangeConfigurationRes) => v15.ConvertersV15.toV15(msg).asInstanceOf[v15.ChangeConfigurationRes],
      (msg: v15.ChangeConfigurationRes) => v15.ConvertersV15.fromV15(msg).asInstanceOf[messages.ChangeConfigurationRes]
    )

    implicit val GetConfigurationReqV15Variant = OcppMessageSerializer.variantFor[messages.GetConfigurationReq, Version.V15.type, v15.GetConfigurationReq](
      (msg: messages.GetConfigurationReq) => v15.ConvertersV15.toV15(msg).asInstanceOf[v15.GetConfigurationReq],
      (msg: v15.GetConfigurationReq) => v15.ConvertersV15.fromV15(msg).asInstanceOf[messages.GetConfigurationReq]
    )

    implicit val GetConfigurationResV15Variant = OcppMessageSerializer.variantFor[messages.GetConfigurationRes, Version.V15.type, v15.GetConfigurationRes](
      (msg: messages.GetConfigurationRes) => v15.ConvertersV15.toV15(msg).asInstanceOf[v15.GetConfigurationRes],
      (msg: v15.GetConfigurationRes) => v15.ConvertersV15.fromV15(msg).asInstanceOf[messages.GetConfigurationRes]
    )

    implicit val ChangeAvailabilityReqV15Variant = OcppMessageSerializer.variantFor[messages.ChangeAvailabilityReq, Version.V15.type, v15.ChangeAvailabilityReq](
      (msg: messages.ChangeAvailabilityReq) => v15.ConvertersV15.toV15(msg).asInstanceOf[v15.ChangeAvailabilityReq],
      (msg: v15.ChangeAvailabilityReq) => v15.ConvertersV15.fromV15(msg).asInstanceOf[messages.ChangeAvailabilityReq]
    )

    implicit val ChangeAvailabilityResV15Variant = OcppMessageSerializer.variantFor[messages.ChangeAvailabilityRes, Version.V15.type, v15.ChangeAvailabilityRes](
      (msg: messages.ChangeAvailabilityRes) => v15.ConvertersV15.toV15(msg).asInstanceOf[v15.ChangeAvailabilityRes],
      (msg: v15.ChangeAvailabilityRes) => v15.ConvertersV15.fromV15(msg).asInstanceOf[messages.ChangeAvailabilityRes]
    )

    implicit val ClearCacheReqV15Variant = OcppMessageSerializer.variantFor[messages.ClearCacheReq.type, Version.V15.type, v15.ClearCacheReq](
      (msg: messages.ClearCacheReq.type) => v15.ConvertersV15.toV15(msg).asInstanceOf[v15.ClearCacheReq],
      (msg: v15.ClearCacheReq) => v15.ConvertersV15.fromV15(msg).asInstanceOf[messages.ClearCacheReq.type]
    )

    implicit val ClearCacheResV15Variant = OcppMessageSerializer.variantFor[messages.ClearCacheRes, Version.V15.type, v15.ClearCacheRes](
      (msg: messages.ClearCacheRes) => v15.ConvertersV15.toV15(msg).asInstanceOf[v15.ClearCacheRes],
      (msg: v15.ClearCacheRes) => v15.ConvertersV15.fromV15(msg).asInstanceOf[messages.ClearCacheRes]
    )

    implicit val ResetReqV15Variant = OcppMessageSerializer.variantFor[messages.ResetReq, Version.V15.type, v15.ResetReq](
      (msg: messages.ResetReq) => v15.ConvertersV15.toV15(msg).asInstanceOf[v15.ResetReq],
      (msg: v15.ResetReq) => v15.ConvertersV15.fromV15(msg).asInstanceOf[messages.ResetReq]
    )

    implicit val ResetResV15Variant = OcppMessageSerializer.variantFor[messages.ResetRes, Version.V15.type, v15.ResetRes](
      (msg: messages.ResetRes) => v15.ConvertersV15.toV15(msg).asInstanceOf[v15.ResetRes],
      (msg: v15.ResetRes) => v15.ConvertersV15.fromV15(msg).asInstanceOf[messages.ResetRes]
    )

    implicit val UpdateFirmwareReqV15Variant = OcppMessageSerializer.variantFor[messages.UpdateFirmwareReq, Version.V15.type, v15.UpdateFirmwareReq](
      (msg: messages.UpdateFirmwareReq) => v15.ConvertersV15.toV15(msg).asInstanceOf[v15.UpdateFirmwareReq],
      (msg: v15.UpdateFirmwareReq) => v15.ConvertersV15.fromV15(msg).asInstanceOf[messages.UpdateFirmwareReq]
    )

    implicit val UpdateFirmwareResV15Variant = OcppMessageSerializer.variantFor[messages.UpdateFirmwareRes.type, Version.V15.type, v15.UpdateFirmwareRes](
      (msg: messages.UpdateFirmwareRes.type) => v15.ConvertersV15.toV15(msg).asInstanceOf[v15.UpdateFirmwareRes],
      (msg: v15.UpdateFirmwareRes) => v15.ConvertersV15.fromV15(msg).asInstanceOf[messages.UpdateFirmwareRes.type]
    )

    implicit val SendLocalListReqV15Variant = OcppMessageSerializer.variantFor[messages.SendLocalListReq, Version.V15.type, v15.SendLocalListReq](
      (msg: messages.SendLocalListReq) => v15.ConvertersV15.toV15(msg).asInstanceOf[v15.SendLocalListReq],
      (msg: v15.SendLocalListReq) => v15.ConvertersV15.fromV15(msg).asInstanceOf[messages.SendLocalListReq]
    )

    implicit val SendLocalListResV15Variant = OcppMessageSerializer.variantFor[messages.SendLocalListRes, Version.V15.type, v15.SendLocalListRes](
      (msg: messages.SendLocalListRes) => v15.ConvertersV15.toV15(msg).asInstanceOf[v15.SendLocalListRes],
      (msg: v15.SendLocalListRes) => v15.ConvertersV15.fromV15(msg).asInstanceOf[messages.SendLocalListRes]
    )

    implicit val GetLocalListVersionReqV15Variant = OcppMessageSerializer.variantFor[messages.GetLocalListVersionReq.type, Version.V15.type, v15.GetLocalListVersionReq](
      (msg: messages.GetLocalListVersionReq.type) => v15.ConvertersV15.toV15(msg).asInstanceOf[v15.GetLocalListVersionReq],
      (msg: v15.GetLocalListVersionReq) => v15.ConvertersV15.fromV15(msg).asInstanceOf[messages.GetLocalListVersionReq.type]
    )

    implicit val GetLocalListVersionResV15Variant = OcppMessageSerializer.variantFor[messages.GetLocalListVersionRes, Version.V15.type, v15.GetLocalListVersionRes](
      (msg: messages.GetLocalListVersionRes) => v15.ConvertersV15.toV15(msg).asInstanceOf[v15.GetLocalListVersionRes],
      (msg: v15.GetLocalListVersionRes) => v15.ConvertersV15.fromV15(msg).asInstanceOf[messages.GetLocalListVersionRes]
    )

    implicit val ChargePointDataTransferReqV15Variant = OcppMessageSerializer.variantFor[messages.ChargePointDataTransferReq, Version.V15.type, v15.DataTransferReq](
      (msg: messages.ChargePointDataTransferReq) => v15.ConvertersV15.toV15(msg).asInstanceOf[v15.DataTransferReq],
      (msg: v15.DataTransferReq) => v15.ConvertersV15.fromV15(msg).asInstanceOf[messages.ChargePointDataTransferReq]
    )

    implicit val ChargePointDataTransferResV15Variant = OcppMessageSerializer.variantFor[messages.ChargePointDataTransferRes, Version.V15.type, v15.DataTransferRes](
      (msg: messages.ChargePointDataTransferRes) => v15.ConvertersV15.toV15(msg).asInstanceOf[v15.DataTransferRes],
      (msg: v15.DataTransferRes) => v15.ConvertersV15.fromV15(msg).asInstanceOf[messages.ChargePointDataTransferRes]
    )

    implicit val ReserveNowReqV15Variant = OcppMessageSerializer.variantFor[messages.ReserveNowReq, Version.V15.type, v15.ReserveNowReq](
      (msg: messages.ReserveNowReq) => v15.ConvertersV15.toV15(msg).asInstanceOf[v15.ReserveNowReq],
      (msg: v15.ReserveNowReq) => v15.ConvertersV15.fromV15(msg).asInstanceOf[messages.ReserveNowReq]
    )

    implicit val ReserveNowResV15Variant = OcppMessageSerializer.variantFor[messages.ReserveNowRes, Version.V15.type, v15.ReserveNowRes](
      (msg: messages.ReserveNowRes) => v15.ConvertersV15.toV15(msg).asInstanceOf[v15.ReserveNowRes],
      (msg: v15.ReserveNowRes) => v15.ConvertersV15.fromV15(msg).asInstanceOf[messages.ReserveNowRes]
    )

    implicit val CancelReservationReqV15Variant = OcppMessageSerializer.variantFor[messages.CancelReservationReq, Version.V15.type, v15.CancelReservationReq](
      (msg: messages.CancelReservationReq) => v15.ConvertersV15.toV15(msg).asInstanceOf[v15.CancelReservationReq],
      (msg: v15.CancelReservationReq) => v15.ConvertersV15.fromV15(msg).asInstanceOf[messages.CancelReservationReq]
    )

    implicit val CancelReservationResV15Variant = OcppMessageSerializer.variantFor[messages.CancelReservationRes, Version.V15.type, v15.CancelReservationRes](
      (msg: messages.CancelReservationRes) => v15.ConvertersV15.toV15(msg).asInstanceOf[v15.CancelReservationRes],
      (msg: v15.CancelReservationRes) => v15.ConvertersV15.fromV15(msg).asInstanceOf[messages.CancelReservationRes]
    )
  }

  object Ocpp16 {

    implicit val AuthorizeReqV16Variant = OcppMessageSerializer.variantFor[messages.AuthorizeReq, Version.V16.type, v16.AuthorizeReq](
      (msg: messages.AuthorizeReq) => v16.ConvertersV16.toV16(msg).asInstanceOf[v16.AuthorizeReq],
      (msg: v16.AuthorizeReq) => v16.ConvertersV16.fromV16(msg).asInstanceOf[messages.AuthorizeReq]
    )

    implicit val AuthorizeResV16Variant = OcppMessageSerializer.variantFor[messages.AuthorizeRes, Version.V16.type, v16.AuthorizeRes](
      (msg: messages.AuthorizeRes) => v16.ConvertersV16.toV16(msg).asInstanceOf[v16.AuthorizeRes],
      (msg: v16.AuthorizeRes) => v16.ConvertersV16.fromV16(msg).asInstanceOf[messages.AuthorizeRes]
    )

    implicit val StartTransactionReqV16Variant = OcppMessageSerializer.variantFor[messages.StartTransactionReq, Version.V16.type, v16.StartTransactionReq](
      (msg: messages.StartTransactionReq) => v16.ConvertersV16.toV16(msg).asInstanceOf[v16.StartTransactionReq],
      (msg: v16.StartTransactionReq) => v16.ConvertersV16.fromV16(msg).asInstanceOf[messages.StartTransactionReq]
    )

    implicit val StartTransactionResV16Variant = OcppMessageSerializer.variantFor[messages.StartTransactionRes, Version.V16.type, v16.StartTransactionRes](
      (msg: messages.StartTransactionRes) => v16.ConvertersV16.toV16(msg).asInstanceOf[v16.StartTransactionRes],
      (msg: v16.StartTransactionRes) => v16.ConvertersV16.fromV16(msg).asInstanceOf[messages.StartTransactionRes]
    )

    implicit val StopTransactionReqV16Variant = OcppMessageSerializer.variantFor[messages.StopTransactionReq, Version.V16.type, v16.StopTransactionReq](
      (msg: messages.StopTransactionReq) => v16.ConvertersV16.toV16(msg).asInstanceOf[v16.StopTransactionReq],
      (msg: v16.StopTransactionReq) => v16.ConvertersV16.fromV16(msg).asInstanceOf[messages.StopTransactionReq]
    )

    implicit val StopTransactionResV16Variant = OcppMessageSerializer.variantFor[messages.StopTransactionRes, Version.V16.type, v16.StopTransactionRes](
      (msg: messages.StopTransactionRes) => v16.ConvertersV16.toV16(msg).asInstanceOf[v16.StopTransactionRes],
      (msg: v16.StopTransactionRes) => v16.ConvertersV16.fromV16(msg).asInstanceOf[messages.StopTransactionRes]
    )

    implicit val HeartbeatReqV16Variant = OcppMessageSerializer.variantFor[messages.HeartbeatReq.type, Version.V16.type, v16.HeartbeatReq](
      (msg: messages.HeartbeatReq.type) => v16.ConvertersV16.toV16(msg).asInstanceOf[v16.HeartbeatReq],
      (msg: v16.HeartbeatReq) => v16.ConvertersV16.fromV16(msg).asInstanceOf[messages.HeartbeatReq.type]
    )

    implicit val HeartbeatResV16Variant = OcppMessageSerializer.variantFor[messages.HeartbeatRes, Version.V16.type, v16.HeartbeatRes](
      (msg: messages.HeartbeatRes) => v16.ConvertersV16.toV16(msg).asInstanceOf[v16.HeartbeatRes],
      (msg: v16.HeartbeatRes) => v16.ConvertersV16.fromV16(msg).asInstanceOf[messages.HeartbeatRes]
    )

    implicit val MeterValuesReqV16Variant = OcppMessageSerializer.variantFor[messages.MeterValuesReq, Version.V16.type, v16.MeterValuesReq](
      (msg: messages.MeterValuesReq) => v16.ConvertersV16.toV16(msg).asInstanceOf[v16.MeterValuesReq],
      (msg: v16.MeterValuesReq) => v16.ConvertersV16.fromV16(msg).asInstanceOf[messages.MeterValuesReq]
    )

    implicit val MeterValuesResV16Variant = OcppMessageSerializer.variantFor[messages.MeterValuesRes.type, Version.V16.type, v16.MeterValuesRes](
      (msg: messages.MeterValuesRes.type) => v16.ConvertersV16.toV16(msg).asInstanceOf[v16.MeterValuesRes],
      (msg: v16.MeterValuesRes) => v16.ConvertersV16.fromV16(msg).asInstanceOf[messages.MeterValuesRes.type]
    )

    implicit val BootNotificationReqV16Variant = OcppMessageSerializer.variantFor[messages.BootNotificationReq, Version.V16.type, v16.BootNotificationReq](
      (msg: messages.BootNotificationReq) => v16.ConvertersV16.toV16(msg).asInstanceOf[v16.BootNotificationReq],
      (msg: v16.BootNotificationReq) => v16.ConvertersV16.fromV16(msg).asInstanceOf[messages.BootNotificationReq]
    )

    implicit val BootNotificationResV16Variant = OcppMessageSerializer.variantFor[messages.BootNotificationRes, Version.V16.type, v16.BootNotificationRes](
      (msg: messages.BootNotificationRes) => v16.ConvertersV16.toV16(msg).asInstanceOf[v16.BootNotificationRes],
      (msg: v16.BootNotificationRes) => v16.ConvertersV16.fromV16(msg).asInstanceOf[messages.BootNotificationRes]
    )

    implicit val CentralSystemDataTransferReqV16Variant = OcppMessageSerializer.variantFor[messages.CentralSystemDataTransferReq, Version.V16.type, v16.DataTransferReq](
      (msg: messages.CentralSystemDataTransferReq) => v16.ConvertersV16.toV16(msg).asInstanceOf[v16.DataTransferReq],
      (msg: v16.DataTransferReq) => v16.ConvertersV16.fromV16(msg).asInstanceOf[messages.CentralSystemDataTransferReq]
    )

    implicit val CentralSystemDataTransferResV16Variant = OcppMessageSerializer.variantFor[messages.CentralSystemDataTransferRes, Version.V16.type, v16.DataTransferRes](
      (msg: messages.CentralSystemDataTransferRes) => v16.ConvertersV16.toV16(msg).asInstanceOf[v16.DataTransferRes],
      (msg: v16.DataTransferRes) => v16.ConvertersV16.fromV16(msg).asInstanceOf[messages.CentralSystemDataTransferRes]
    )

    implicit val StatusNotificationReqV16Variant = OcppMessageSerializer.variantFor[messages.StatusNotificationReq, Version.V16.type, v16.StatusNotificationReq](
      (msg: messages.StatusNotificationReq) => v16.ConvertersV16.toV16(msg).asInstanceOf[v16.StatusNotificationReq],
      (msg: v16.StatusNotificationReq) => v16.ConvertersV16.fromV16(msg).asInstanceOf[messages.StatusNotificationReq]
    )

    implicit val StatusNotificationResV16Variant = OcppMessageSerializer.variantFor[messages.StatusNotificationRes.type, Version.V16.type, v16.StatusNotificationRes](
      (msg: messages.StatusNotificationRes.type) => v16.ConvertersV16.toV16(msg).asInstanceOf[v16.StatusNotificationRes],
      (msg: v16.StatusNotificationRes) => v16.ConvertersV16.fromV16(msg).asInstanceOf[messages.StatusNotificationRes.type]
    )

    implicit val FirmwareStatusNotificationReqV16Variant = OcppMessageSerializer.variantFor[messages.FirmwareStatusNotificationReq, Version.V16.type, v16.FirmwareStatusNotificationReq](
      (msg: messages.FirmwareStatusNotificationReq) => v16.ConvertersV16.toV16(msg).asInstanceOf[v16.FirmwareStatusNotificationReq],
      (msg: v16.FirmwareStatusNotificationReq) => v16.ConvertersV16.fromV16(msg).asInstanceOf[messages.FirmwareStatusNotificationReq]
    )

    implicit val FirmwareStatusNotificationResV16Variant = OcppMessageSerializer.variantFor[messages.FirmwareStatusNotificationRes.type, Version.V16.type, v16.FirmwareStatusNotificationRes](
      (msg: messages.FirmwareStatusNotificationRes.type) => v16.ConvertersV16.toV16(msg).asInstanceOf[v16.FirmwareStatusNotificationRes],
      (msg: v16.FirmwareStatusNotificationRes) => v16.ConvertersV16.fromV16(msg).asInstanceOf[messages.FirmwareStatusNotificationRes.type]
    )

    implicit val DiagnosticsStatusNotificationReqV16Variant = OcppMessageSerializer.variantFor[messages.DiagnosticsStatusNotificationReq, Version.V16.type, v16.DiagnosticsStatusNotificationReq](
      (msg: messages.DiagnosticsStatusNotificationReq) => v16.ConvertersV16.toV16(msg).asInstanceOf[v16.DiagnosticsStatusNotificationReq],
      (msg: v16.DiagnosticsStatusNotificationReq) => v16.ConvertersV16.fromV16(msg).asInstanceOf[messages.DiagnosticsStatusNotificationReq]
    )

    implicit val DiagnosticsStatusNotificationResV16Variant = OcppMessageSerializer.variantFor[messages.DiagnosticsStatusNotificationRes.type, Version.V16.type, v16.DiagnosticsStatusNotificationRes](
      (msg: messages.DiagnosticsStatusNotificationRes.type) => v16.ConvertersV16.toV16(msg).asInstanceOf[v16.DiagnosticsStatusNotificationRes],
      (msg: v16.DiagnosticsStatusNotificationRes) => v16.ConvertersV16.fromV16(msg).asInstanceOf[messages.DiagnosticsStatusNotificationRes.type]
    )

    implicit val SetChargingProfileReqV16Variant = OcppMessageSerializer.variantFor[messages.SetChargingProfileReq, Version.V16.type, v16.SetChargingProfileReq](
      (msg: messages.SetChargingProfileReq) => v16.ConvertersV16.toV16(msg).asInstanceOf[v16.SetChargingProfileReq],
      (msg: v16.SetChargingProfileReq) => v16.ConvertersV16.fromV16(msg).asInstanceOf[messages.SetChargingProfileReq]
    )

    implicit val SetChargingProfileResV16Variant = OcppMessageSerializer.variantFor[messages.SetChargingProfileRes, Version.V16.type, v16.SetChargingProfileRes](
      (msg: messages.SetChargingProfileRes) => v16.ConvertersV16.toV16(msg).asInstanceOf[v16.SetChargingProfileRes],
      (msg: v16.SetChargingProfileRes) => v16.ConvertersV16.fromV16(msg).asInstanceOf[messages.SetChargingProfileRes]
    )


    implicit val ClearChargingProfileReqV16Variant = OcppMessageSerializer.variantFor[messages.ClearChargingProfileReq, Version.V16.type, v16.ClearChargingProfileReq](
      (msg: messages.ClearChargingProfileReq) => v16.ConvertersV16.toV16(msg).asInstanceOf[v16.ClearChargingProfileReq],
      (msg: v16.ClearChargingProfileReq) => v16.ConvertersV16.fromV16(msg).asInstanceOf[messages.ClearChargingProfileReq]
    )

    implicit val ClearChargingProfileResV16Variant = OcppMessageSerializer.variantFor[messages.ClearChargingProfileRes, Version.V16.type, v16.ClearChargingProfileRes](
      (msg: messages.ClearChargingProfileRes) => v16.ConvertersV16.toV16(msg).asInstanceOf[v16.ClearChargingProfileRes],
      (msg: v16.ClearChargingProfileRes) => v16.ConvertersV16.fromV16(msg).asInstanceOf[messages.ClearChargingProfileRes]
    )

    implicit val GetCompositeScheduleReqV16Variant = OcppMessageSerializer.variantFor[messages.GetCompositeScheduleReq, Version.V16.type, v16.GetCompositeScheduleReq](
      (msg: messages.GetCompositeScheduleReq) => v16.ConvertersV16.toV16(msg).asInstanceOf[v16.GetCompositeScheduleReq],
      (msg: v16.GetCompositeScheduleReq) => v16.ConvertersV16.fromV16(msg).asInstanceOf[messages.GetCompositeScheduleReq]
    )

    implicit val GetCompositeScheduleResV16Variant = OcppMessageSerializer.variantFor[messages.GetCompositeScheduleRes, Version.V16.type, v16.GetCompositeScheduleRes](
      (msg: messages.GetCompositeScheduleRes) => v16.ConvertersV16.toV16(msg).asInstanceOf[v16.GetCompositeScheduleRes],
      (msg: v16.GetCompositeScheduleRes) => v16.ConvertersV16.fromV16(msg).asInstanceOf[messages.GetCompositeScheduleRes]
    )

    implicit val TriggerMessageReqV16Variant = OcppMessageSerializer.variantFor[messages.TriggerMessageReq, Version.V16.type, v16.TriggerMessageReq](
      (msg: messages.TriggerMessageReq) => v16.ConvertersV16.toV16(msg).asInstanceOf[v16.TriggerMessageReq],
      (msg: v16.TriggerMessageReq) => v16.ConvertersV16.fromV16(msg).asInstanceOf[messages.TriggerMessageReq]
    )

    implicit val TriggerMessageResV16Variant = OcppMessageSerializer.variantFor[messages.TriggerMessageRes, Version.V16.type, v16.TriggerMessageRes](
      (msg: messages.TriggerMessageRes) => v16.ConvertersV16.toV16(msg).asInstanceOf[v16.TriggerMessageRes],
      (msg: v16.TriggerMessageRes) => v16.ConvertersV16.fromV16(msg).asInstanceOf[messages.TriggerMessageRes]
    )

    implicit val RemoteStartTransactionReqV16Variant = OcppMessageSerializer.variantFor[messages.RemoteStartTransactionReq, Version.V16.type, v16.RemoteStartTransactionReq](
      (msg: messages.RemoteStartTransactionReq) => v16.ConvertersV16.toV16(msg).asInstanceOf[v16.RemoteStartTransactionReq],
      (msg: v16.RemoteStartTransactionReq) => v16.ConvertersV16.fromV16(msg).asInstanceOf[messages.RemoteStartTransactionReq]
    )

    implicit val RemoteStartTransactionResV16Variant = OcppMessageSerializer.variantFor[messages.RemoteStartTransactionRes, Version.V16.type, v16.RemoteStartTransactionRes](
      (msg: messages.RemoteStartTransactionRes) => v16.ConvertersV16.toV16(msg).asInstanceOf[v16.RemoteStartTransactionRes],
      (msg: v16.RemoteStartTransactionRes) => v16.ConvertersV16.fromV16(msg).asInstanceOf[messages.RemoteStartTransactionRes]
    )

    implicit val RemoteStopTransactionReqV16Variant = OcppMessageSerializer.variantFor[messages.RemoteStopTransactionReq, Version.V16.type, v16.RemoteStopTransactionReq](
      (msg: messages.RemoteStopTransactionReq) => v16.ConvertersV16.toV16(msg).asInstanceOf[v16.RemoteStopTransactionReq],
      (msg: v16.RemoteStopTransactionReq) => v16.ConvertersV16.fromV16(msg).asInstanceOf[messages.RemoteStopTransactionReq]
    )

    implicit val RemoteStopTransactionResV16Variant = OcppMessageSerializer.variantFor[messages.RemoteStopTransactionRes, Version.V16.type, v16.RemoteStopTransactionRes](
      (msg: messages.RemoteStopTransactionRes) => v16.ConvertersV16.toV16(msg).asInstanceOf[v16.RemoteStopTransactionRes],
      (msg: v16.RemoteStopTransactionRes) => v16.ConvertersV16.fromV16(msg).asInstanceOf[messages.RemoteStopTransactionRes]
    )

    implicit val UnlockConnectorReqV16Variant = OcppMessageSerializer.variantFor[messages.UnlockConnectorReq, Version.V16.type, v16.UnlockConnectorReq](
      (msg: messages.UnlockConnectorReq) => v16.ConvertersV16.toV16(msg).asInstanceOf[v16.UnlockConnectorReq],
      (msg: v16.UnlockConnectorReq) => v16.ConvertersV16.fromV16(msg).asInstanceOf[messages.UnlockConnectorReq]
    )

    implicit val UnlockConnectorResV16Variant = OcppMessageSerializer.variantFor[messages.UnlockConnectorRes, Version.V16.type, v16.UnlockConnectorRes](
      (msg: messages.UnlockConnectorRes) => v16.ConvertersV16.toV16(msg).asInstanceOf[v16.UnlockConnectorRes],
      (msg: v16.UnlockConnectorRes) => v16.ConvertersV16.fromV16(msg).asInstanceOf[messages.UnlockConnectorRes]
    )

    implicit val GetDiagnosticsReqV16Variant = OcppMessageSerializer.variantFor[messages.GetDiagnosticsReq, Version.V16.type, v16.GetDiagnosticsReq](
      (msg: messages.GetDiagnosticsReq) => v16.ConvertersV16.toV16(msg).asInstanceOf[v16.GetDiagnosticsReq],
      (msg: v16.GetDiagnosticsReq) => v16.ConvertersV16.fromV16(msg).asInstanceOf[messages.GetDiagnosticsReq]
    )

    implicit val GetDiagnosticsResV16Variant = OcppMessageSerializer.variantFor[messages.GetDiagnosticsRes, Version.V16.type, v16.GetDiagnosticsRes](
      (msg: messages.GetDiagnosticsRes) => v16.ConvertersV16.toV16(msg).asInstanceOf[v16.GetDiagnosticsRes],
      (msg: v16.GetDiagnosticsRes) => v16.ConvertersV16.fromV16(msg).asInstanceOf[messages.GetDiagnosticsRes]
    )

    implicit val ChangeConfigurationReqV16Variant = OcppMessageSerializer.variantFor[messages.ChangeConfigurationReq, Version.V16.type, v16.ChangeConfigurationReq](
      (msg: messages.ChangeConfigurationReq) => v16.ConvertersV16.toV16(msg).asInstanceOf[v16.ChangeConfigurationReq],
      (msg: v16.ChangeConfigurationReq) => v16.ConvertersV16.fromV16(msg).asInstanceOf[messages.ChangeConfigurationReq]
    )

    implicit val ChangeConfigurationResV16Variant = OcppMessageSerializer.variantFor[messages.ChangeConfigurationRes, Version.V16.type, v16.ChangeConfigurationRes](
      (msg: messages.ChangeConfigurationRes) => v16.ConvertersV16.toV16(msg).asInstanceOf[v16.ChangeConfigurationRes],
      (msg: v16.ChangeConfigurationRes) => v16.ConvertersV16.fromV16(msg).asInstanceOf[messages.ChangeConfigurationRes]
    )

    implicit val GetConfigurationReqV16Variant = OcppMessageSerializer.variantFor[messages.GetConfigurationReq, Version.V16.type, v16.GetConfigurationReq](
      (msg: messages.GetConfigurationReq) => v16.ConvertersV16.toV16(msg).asInstanceOf[v16.GetConfigurationReq],
      (msg: v16.GetConfigurationReq) => v16.ConvertersV16.fromV16(msg).asInstanceOf[messages.GetConfigurationReq]
    )

    implicit val GetConfigurationResV16Variant = OcppMessageSerializer.variantFor[messages.GetConfigurationRes, Version.V16.type, v16.GetConfigurationRes](
      (msg: messages.GetConfigurationRes) => v16.ConvertersV16.toV16(msg).asInstanceOf[v16.GetConfigurationRes],
      (msg: v16.GetConfigurationRes) => v16.ConvertersV16.fromV16(msg).asInstanceOf[messages.GetConfigurationRes]
    )

    implicit val ChangeAvailabilityReqV16Variant = OcppMessageSerializer.variantFor[messages.ChangeAvailabilityReq, Version.V16.type, v16.ChangeAvailabilityReq](
      (msg: messages.ChangeAvailabilityReq) => v16.ConvertersV16.toV16(msg).asInstanceOf[v16.ChangeAvailabilityReq],
      (msg: v16.ChangeAvailabilityReq) => v16.ConvertersV16.fromV16(msg).asInstanceOf[messages.ChangeAvailabilityReq]
    )

    implicit val ChangeAvailabilityResV16Variant = OcppMessageSerializer.variantFor[messages.ChangeAvailabilityRes, Version.V16.type, v16.ChangeAvailabilityRes](
      (msg: messages.ChangeAvailabilityRes) => v16.ConvertersV16.toV16(msg).asInstanceOf[v16.ChangeAvailabilityRes],
      (msg: v16.ChangeAvailabilityRes) => v16.ConvertersV16.fromV16(msg).asInstanceOf[messages.ChangeAvailabilityRes]
    )

    implicit val ClearCacheReqV16Variant = OcppMessageSerializer.variantFor[messages.ClearCacheReq.type, Version.V16.type, v16.ClearCacheReq](
      (msg: messages.ClearCacheReq.type) => v16.ConvertersV16.toV16(msg).asInstanceOf[v16.ClearCacheReq],
      (msg: v16.ClearCacheReq) => v16.ConvertersV16.fromV16(msg).asInstanceOf[messages.ClearCacheReq.type]
    )

    implicit val ClearCacheResV16Variant = OcppMessageSerializer.variantFor[messages.ClearCacheRes, Version.V16.type, v16.ClearCacheRes](
      (msg: messages.ClearCacheRes) => v16.ConvertersV16.toV16(msg).asInstanceOf[v16.ClearCacheRes],
      (msg: v16.ClearCacheRes) => v16.ConvertersV16.fromV16(msg).asInstanceOf[messages.ClearCacheRes]
    )

    implicit val ResetReqV16Variant = OcppMessageSerializer.variantFor[messages.ResetReq, Version.V16.type, v16.ResetReq](
      (msg: messages.ResetReq) => v16.ConvertersV16.toV16(msg).asInstanceOf[v16.ResetReq],
      (msg: v16.ResetReq) => v16.ConvertersV16.fromV16(msg).asInstanceOf[messages.ResetReq]
    )

    implicit val ResetResV16Variant = OcppMessageSerializer.variantFor[messages.ResetRes, Version.V16.type, v16.ResetRes](
      (msg: messages.ResetRes) => v16.ConvertersV16.toV16(msg).asInstanceOf[v16.ResetRes],
      (msg: v16.ResetRes) => v16.ConvertersV16.fromV16(msg).asInstanceOf[messages.ResetRes]
    )

    implicit val UpdateFirmwareReqV16Variant = OcppMessageSerializer.variantFor[messages.UpdateFirmwareReq, Version.V16.type, v16.UpdateFirmwareReq](
      (msg: messages.UpdateFirmwareReq) => v16.ConvertersV16.toV16(msg).asInstanceOf[v16.UpdateFirmwareReq],
      (msg: v16.UpdateFirmwareReq) => v16.ConvertersV16.fromV16(msg).asInstanceOf[messages.UpdateFirmwareReq]
    )

    implicit val UpdateFirmwareResV16Variant = OcppMessageSerializer.variantFor[messages.UpdateFirmwareRes.type, Version.V16.type, v16.UpdateFirmwareRes](
      (msg: messages.UpdateFirmwareRes.type) => v16.ConvertersV16.toV16(msg).asInstanceOf[v16.UpdateFirmwareRes],
      (msg: v16.UpdateFirmwareRes) => v16.ConvertersV16.fromV16(msg).asInstanceOf[messages.UpdateFirmwareRes.type]
    )

    implicit val SendLocalListReqV16Variant = OcppMessageSerializer.variantFor[messages.SendLocalListReq, Version.V16.type, v16.SendLocalListReq](
      (msg: messages.SendLocalListReq) => v16.ConvertersV16.toV16(msg).asInstanceOf[v16.SendLocalListReq],
      (msg: v16.SendLocalListReq) => v16.ConvertersV16.fromV16(msg).asInstanceOf[messages.SendLocalListReq]
    )

    implicit val SendLocalListResV16Variant = OcppMessageSerializer.variantFor[messages.SendLocalListRes, Version.V16.type, v16.SendLocalListRes](
      (msg: messages.SendLocalListRes) => v16.ConvertersV16.toV16(msg).asInstanceOf[v16.SendLocalListRes],
      (msg: v16.SendLocalListRes) => v16.ConvertersV16.fromV16(msg).asInstanceOf[messages.SendLocalListRes]
    )

    implicit val GetLocalListVersionReqV16Variant = OcppMessageSerializer.variantFor[messages.GetLocalListVersionReq.type, Version.V16.type, v16.GetLocalListVersionReq](
      (msg: messages.GetLocalListVersionReq.type) => v16.ConvertersV16.toV16(msg).asInstanceOf[v16.GetLocalListVersionReq],
      (msg: v16.GetLocalListVersionReq) => v16.ConvertersV16.fromV16(msg).asInstanceOf[messages.GetLocalListVersionReq.type]
    )

    implicit val GetLocalListVersionResV16Variant = OcppMessageSerializer.variantFor[messages.GetLocalListVersionRes, Version.V16.type, v16.GetLocalListVersionRes](
      (msg: messages.GetLocalListVersionRes) => v16.ConvertersV16.toV16(msg).asInstanceOf[v16.GetLocalListVersionRes],
      (msg: v16.GetLocalListVersionRes) => v16.ConvertersV16.fromV16(msg).asInstanceOf[messages.GetLocalListVersionRes]
    )

    implicit val ChargePointDataTransferReqV16Variant = OcppMessageSerializer.variantFor[messages.ChargePointDataTransferReq, Version.V16.type, v16.DataTransferReq](
      (msg: messages.ChargePointDataTransferReq) => v16.ConvertersV16.toV16(msg).asInstanceOf[v16.DataTransferReq],
      (msg: v16.DataTransferReq) => v16.ConvertersV16.fromV16(msg).asInstanceOf[messages.ChargePointDataTransferReq]
    )

    implicit val ChargePointDataTransferResV16Variant = OcppMessageSerializer.variantFor[messages.ChargePointDataTransferRes, Version.V16.type, v16.DataTransferRes](
      (msg: messages.ChargePointDataTransferRes) => v16.ConvertersV16.toV16(msg).asInstanceOf[v16.DataTransferRes],
      (msg: v16.DataTransferRes) => v16.ConvertersV16.fromV16(msg).asInstanceOf[messages.ChargePointDataTransferRes]
    )

    implicit val ReserveNowReqV16Variant = OcppMessageSerializer.variantFor[messages.ReserveNowReq, Version.V16.type, v16.ReserveNowReq](
      (msg: messages.ReserveNowReq) => v16.ConvertersV16.toV16(msg).asInstanceOf[v16.ReserveNowReq],
      (msg: v16.ReserveNowReq) => v16.ConvertersV16.fromV16(msg).asInstanceOf[messages.ReserveNowReq]
    )

    implicit val ReserveNowResV16Variant = OcppMessageSerializer.variantFor[messages.ReserveNowRes, Version.V16.type, v16.ReserveNowRes](
      (msg: messages.ReserveNowRes) => v16.ConvertersV16.toV16(msg).asInstanceOf[v16.ReserveNowRes],
      (msg: v16.ReserveNowRes) => v16.ConvertersV16.fromV16(msg).asInstanceOf[messages.ReserveNowRes]
    )

    implicit val CancelReservationReqV16Variant = OcppMessageSerializer.variantFor[messages.CancelReservationReq, Version.V16.type, v16.CancelReservationReq](
      (msg: messages.CancelReservationReq) => v16.ConvertersV16.toV16(msg).asInstanceOf[v16.CancelReservationReq],
      (msg: v16.CancelReservationReq) => v16.ConvertersV16.fromV16(msg).asInstanceOf[messages.CancelReservationReq]
    )

    implicit val CancelReservationResV16Variant = OcppMessageSerializer.variantFor[messages.CancelReservationRes, Version.V16.type, v16.CancelReservationRes](
      (msg: messages.CancelReservationRes) => v16.ConvertersV16.toV16(msg).asInstanceOf[v16.CancelReservationRes],
      (msg: v16.CancelReservationRes) => v16.ConvertersV16.fromV16(msg).asInstanceOf[messages.CancelReservationRes]
    )
  }
}