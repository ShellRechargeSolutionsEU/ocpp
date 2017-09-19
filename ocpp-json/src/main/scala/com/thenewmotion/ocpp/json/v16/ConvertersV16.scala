package com.thenewmotion.ocpp
package json
package v16

import java.net.URI
import java.net.URISyntaxException

import enums.reflection.EnumUtils.Enumerable
import enums.reflection.EnumUtils.Nameable
import org.json4s.MappingException

import scala.concurrent.duration._

object ConvertersV16 {

  implicit val AuthorizeReqV16Variant = OcppMessageSerializer.variantFor[messages.AuthorizeReq, Version.V16.type, v16.AuthorizeReq](
    (msg: messages.AuthorizeReq) => AuthorizeReq(msg.idTag),
    (msg: AuthorizeReq) => messages.AuthorizeReq(msg.idTag)
  )

  implicit val AuthorizeResV16Variant = OcppMessageSerializer.variantFor[messages.AuthorizeRes, Version.V16.type, v16.AuthorizeRes](
    (msg: messages.AuthorizeRes) => AuthorizeRes(msg.idTag.toV16),
    (msg: AuthorizeRes) => messages.AuthorizeRes(msg.idTagInfo.fromV16)
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
    (msg: messages.BootNotificationReq) =>
      BootNotificationReq(
        msg.chargePointVendor,
        msg.chargePointModel,
        msg.chargePointSerialNumber,
        msg.chargeBoxSerialNumber,
        msg.firmwareVersion,
        msg.iccid,
        msg.imsi,
        msg.meterType,
        msg.meterSerialNumber
      ),
    (msg: v16.BootNotificationReq) =>
      messages.BootNotificationReq(
        msg.chargePointVendor,
        msg.chargePointModel,
        msg.chargePointSerialNumber,
        msg.chargeBoxSerialNumber,
        msg.firmwareVersion,
        msg.iccid,
        msg.imsi,
        msg.meterType,
        msg.meterSerialNumber
      )
  )

  implicit val BootNotificationResV16Variant = OcppMessageSerializer.variantFor[messages.BootNotificationRes, Version.V16.type, v16.BootNotificationRes](
    (msg: messages.BootNotificationRes) =>
      BootNotificationRes(msg.status.name, msg.currentTime, msg.interval.toSeconds.toInt),

    (msg: v16.BootNotificationRes) =>
      messages.BootNotificationRes(
        status = enumerableFromJsonString(messages.RegistrationStatus, msg.status),
        currentTime = msg.currentTime,
        interval = msg.interval.seconds
      )
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

  private def toV16(msg: messages.Message): Message = msg match {


    case messages.StartTransactionReq(connector, idTag, timestamp, meterStart, reservationId) =>
      StartTransactionReq(
        connectorId = connector.toOcpp,
        idTag = idTag,
        timestamp = timestamp,
        meterStart = meterStart,
        reservationId = reservationId
      )

    case messages.StartTransactionRes(transactionId, idTagInfo) => StartTransactionRes(transactionId, idTagInfo.toV16)

    case messages.StopTransactionReq(transactionId, idTag, timestamp, meterStop, stopReason, meters) =>
      StopTransactionReq(
        transactionId = transactionId,
        idTag = idTag,
        timestamp = timestamp,
        reason = noneIfDefault(messages.StopReason, stopReason),
        meterStop = meterStop,
        transactionData = noneIfEmpty(meters.map(_.toV16))
      )

    case messages.StopTransactionRes(idTagInfo) => StopTransactionRes(idTagInfo.map(_.toV16))

    case messages.UnlockConnectorReq(scope) => UnlockConnectorReq(scope.toOcpp)

    case messages.UnlockConnectorRes(status) => UnlockConnectorRes(status.name)

    case messages.ResetReq(resetType) => ResetReq(resetType.name)

    case messages.ResetRes(accepted) => ResetRes(accepted.toStatusString)

    case messages.ChangeAvailabilityReq(scope, availabilityType) =>
      ChangeAvailabilityReq(connectorId = scope.toOcpp, `type` = availabilityType.name)

    case messages.ChangeAvailabilityRes(status) => ChangeAvailabilityRes(status.name)

    case messages.StatusNotificationReq(scope, status, timestamp, vendorId) =>
      val (ocppStatus, errorCode, info, vendorErrorCode) = status.toV16Fields
      StatusNotificationReq(scope.toOcpp, ocppStatus, errorCode, info, timestamp, vendorId, vendorErrorCode)

    case messages.StatusNotificationRes => StatusNotificationRes()

    case messages.RemoteStartTransactionReq(idTag, connector, chargingProfile) =>
      RemoteStartTransactionReq(idTag, connector.map(_.toOcpp), chargingProfile.map(_.toV16))

    case messages.RemoteStartTransactionRes(accepted) => RemoteStartTransactionRes(accepted.toStatusString)

    case messages.RemoteStopTransactionReq(transactionId) => RemoteStopTransactionReq(transactionId)

    case messages.RemoteStopTransactionRes(accepted) => RemoteStopTransactionRes(accepted.toStatusString)

    case messages.HeartbeatReq => HeartbeatReq()

    case messages.HeartbeatRes(currentTime) => HeartbeatRes(currentTime)

    case messages.UpdateFirmwareReq(retrieveDate, location, retries) =>
      UpdateFirmwareReq(retrieveDate, location.toASCIIString, retries.numberOfRetries, retries.intervalInSeconds)

    case messages.UpdateFirmwareRes => UpdateFirmwareRes()

    case messages.FirmwareStatusNotificationReq(status) => FirmwareStatusNotificationReq(status.name)

    case messages.FirmwareStatusNotificationRes => FirmwareStatusNotificationRes()

    case messages.GetDiagnosticsReq(location, startTime, stopTime, retries) =>
      GetDiagnosticsReq(location.toASCIIString, startTime, stopTime, retries.numberOfRetries, retries.intervalInSeconds)

    case messages.GetDiagnosticsRes(filename) => GetDiagnosticsRes(filename)

    case messages.DiagnosticsStatusNotificationReq(uploaded) =>
      DiagnosticsStatusNotificationReq(uploaded.name)

    case messages.DiagnosticsStatusNotificationRes =>
      DiagnosticsStatusNotificationRes()

    case messages.MeterValuesReq(scope, transactionId, meters) =>
      MeterValuesReq(scope.toOcpp, transactionId, meters.map(_.toV16))

    case messages.MeterValuesRes => MeterValuesRes()

    case messages.ChangeConfigurationReq(key, value) => ChangeConfigurationReq(key, value)

    case messages.ChangeConfigurationRes(status) => ChangeConfigurationRes(status.name)

    case messages.ClearCacheReq => ClearCacheReq()

    case messages.ClearCacheRes(accepted) => ClearCacheRes(accepted.toStatusString)

    case messages.GetConfigurationReq(keys) => GetConfigurationReq(noneIfEmpty(keys))

    case messages.GetConfigurationRes(values, unknownKeys) =>
      GetConfigurationRes(configurationKey = noneIfEmpty(values.map(_.toV16)), unknownKey = noneIfEmpty(unknownKeys))

    case messages.GetLocalListVersionReq => GetLocalListVersionReq()

    case messages.GetLocalListVersionRes(authListVersion) => GetLocalListVersionRes(authListVersion.toV16)

    case messages.SendLocalListReq(updateType, authListVersion, authorisationData, _) =>
      SendLocalListReq(updateType.name, authListVersion.toV16, Some(authorisationData.map(_.toV16)))

    case messages.SendLocalListRes(status: messages.UpdateStatus) =>
      SendLocalListRes(status.toV16)

    case messages.ReserveNowReq(scope, expiryDate, idTag, parentIdTag, reservationId) =>
      ReserveNowReq(scope.toOcpp, expiryDate, idTag, parentIdTag, reservationId)

    case messages.ReserveNowRes(status) => ReserveNowRes(status.name)

    case messages.CancelReservationReq(reservationId) => CancelReservationReq(reservationId)

    case messages.CancelReservationRes(accepted) => CancelReservationRes(accepted.toStatusString)

    case messages.ClearChargingProfileReq(id, connectorId, chargingProfilePurpose, stackLevel) =>
      ClearChargingProfileReq(
        id,
        connectorId.map(_.toOcpp),
        chargingProfilePurpose.map(_.name),
        stackLevel
      )

    case messages.ClearChargingProfileRes(status) => ClearChargingProfileRes(status.name)

    case messages.GetCompositeScheduleReq(connectorId, duration, chargingRateUnit) =>
      GetCompositeScheduleReq(
        connectorId.toOcpp,
        duration.toSeconds.toInt,
        chargingRateUnit.map(_.name)
      )

    case messages.GetCompositeScheduleRes(status) =>
      import messages.GetCompositeScheduleStatus._
      GetCompositeScheduleRes.tupled {
        status match {
          case Accepted(connector, scheduleStart, chargingSchedule) =>
            ("Accepted", Some(connector.toOcpp), scheduleStart, chargingSchedule.map(_.toV16))
          case Rejected => ("Rejected", None, None, None)
        }
      }

    case messages.SetChargingProfileReq(connectorId, csChargingProfiles) =>
      SetChargingProfileReq(
        connectorId.toOcpp,
        csChargingProfiles.toV16
      )

    case messages.SetChargingProfileRes(status) => SetChargingProfileRes(status.name)

    case messages.TriggerMessageReq(requestedMessage) =>
      import messages.MessageTriggerWithoutScope
      import messages.MessageTriggerWithScope
      TriggerMessageReq.tupled {
        requestedMessage match {
          case messageTrigger: MessageTriggerWithoutScope =>
            (messageTrigger.name, None)
          case MessageTriggerWithScope.MeterValues(connectorId) =>
            ("MeterValues", connectorId.map(_.toOcpp))
          case MessageTriggerWithScope.StatusNotification(connectorId) =>
            ("StatusNotification", connectorId.map(_.toOcpp))
        }
      }

    case messages.TriggerMessageRes(status) => TriggerMessageRes(status.name)

    case messages.CentralSystemDataTransferReq(_, _, _)
         | messages.CentralSystemDataTransferRes(_, _)
         | messages.ChargePointDataTransferReq(_, _, _)
         | messages.ChargePointDataTransferRes(_, _) =>
      unexpectedMessage(msg)
  }

  private def fromV16(msg: Message): messages.Message = msg match {


    case StartTransactionReq(connectorId, idTag, timestamp, meterStart, reservationId) =>
      messages.StartTransactionReq(
        messages.ConnectorScope.fromOcpp(connectorId),
        idTag,
        timestamp,
        meterStart,
        reservationId
      )

    case StartTransactionRes(transactionId, idTagInfo) => messages.StartTransactionRes(transactionId, idTagInfo.fromV16)

    case StopTransactionReq(transactionId, idTag, timestamp, meterStop, stopReason, meters) =>
      messages.StopTransactionReq(
        transactionId,
        idTag,
        timestamp,
        meterStop,
        defaultIfNone(messages.StopReason, stopReason),
        meters.fold(List.empty[messages.meter.Meter])(_.map(meterFromV16))
      )

    case StopTransactionRes(idTagInfo) => messages.StopTransactionRes(idTagInfo.map(_.fromV16))

    case UnlockConnectorReq(connectorId) => messages.UnlockConnectorReq(messages.ConnectorScope.fromOcpp(connectorId))

    case UnlockConnectorRes(statusString) => messages.UnlockConnectorRes(
      status = enumerableFromJsonString(messages.UnlockStatus, statusString)
    )

    case ResetReq(resetType) => messages.ResetReq(enumerableFromJsonString(messages.ResetType, resetType))

    case ResetRes(status) => messages.ResetRes(statusStringToBoolean(status))

    case ChangeAvailabilityReq(connectorId, availabilityType) =>
      messages.ChangeAvailabilityReq(
        scope = messages.Scope.fromOcpp(connectorId),
        availabilityType = enumerableFromJsonString(messages.AvailabilityType, availabilityType)
      )

    case ChangeAvailabilityRes(status) =>
      messages.ChangeAvailabilityRes(enumerableFromJsonString(messages.AvailabilityStatus, status))

    case StatusNotificationReq(connector, status, errorCode, info, timestamp, vendorId, vendorErrorCode) =>
      messages.StatusNotificationReq(
        messages.Scope.fromOcpp(connector),
        statusFieldsToOcppStatus(status, errorCode, info, vendorErrorCode),
        timestamp,
        vendorId
      )

    case StatusNotificationRes() => messages.StatusNotificationRes

    case RemoteStartTransactionReq(idTag, connector, chargingProfile) =>
      messages.RemoteStartTransactionReq(
        idTag,
        connector.map(messages.ConnectorScope.fromOcpp),
        chargingProfile.map(cp => chargingProfileFromV16(cp))
      )

    case RemoteStartTransactionRes(status) => messages.RemoteStartTransactionRes(statusStringToBoolean(status))

    case RemoteStopTransactionReq(transactionId) => messages.RemoteStopTransactionReq(transactionId)

    case RemoteStopTransactionRes(status) => messages.RemoteStopTransactionRes(statusStringToBoolean(status))

    case HeartbeatReq() => messages.HeartbeatReq

    case HeartbeatRes(currentTime) => messages.HeartbeatRes(currentTime)

    case UpdateFirmwareReq(retrieveDate, location, retries, retryInterval) =>
      messages.UpdateFirmwareReq(retrieveDate, parseURI(location), messages.Retries.fromInts(retries, retryInterval))

    case UpdateFirmwareRes() => messages.UpdateFirmwareRes

    case FirmwareStatusNotificationReq(status) =>
      messages.FirmwareStatusNotificationReq(enumerableFromJsonString(messages.FirmwareStatus, status))

    case FirmwareStatusNotificationRes() =>
      messages.FirmwareStatusNotificationRes

    case GetDiagnosticsReq(location, startTime, stopTime, retries, retryInterval) =>
      messages.GetDiagnosticsReq(
        parseURI(location),
        startTime,
        stopTime,
        messages.Retries.fromInts(retries, retryInterval)
      )

    case GetDiagnosticsRes(filename) => messages.GetDiagnosticsRes(filename)

    case DiagnosticsStatusNotificationReq(statusString) =>
      messages.DiagnosticsStatusNotificationReq(
        status = enumerableFromJsonString(messages.DiagnosticsStatus, statusString)
      )

    case DiagnosticsStatusNotificationRes() => messages.DiagnosticsStatusNotificationRes

    case MeterValuesReq(connectorId, transactionId, values) =>
      val meters: List[messages.meter.Meter] = values.map(meterFromV16)
      messages.MeterValuesReq(messages.Scope.fromOcpp(connectorId), transactionId, meters)

    case MeterValuesRes() => messages.MeterValuesRes

    case ChangeConfigurationReq(key, value) => messages.ChangeConfigurationReq(key, value)

    case ChangeConfigurationRes(status) =>
      messages.ChangeConfigurationRes(enumerableFromJsonString(messages.ConfigurationStatus, status))

    case ClearCacheReq() => messages.ClearCacheReq

    case ClearCacheRes(status) => messages.ClearCacheRes(statusStringToBoolean(status))

    case GetConfigurationReq(keys) => messages.GetConfigurationReq(keys getOrElse Nil)

    case GetConfigurationRes(values, unknownKeys) =>
      messages.GetConfigurationRes(values.fold(List.empty[messages.KeyValue])(_.map(_.fromV16)),
        unknownKeys getOrElse Nil)

    case GetLocalListVersionReq() => messages.GetLocalListVersionReq

    case GetLocalListVersionRes(v) => messages.GetLocalListVersionRes(messages.AuthListVersion(v))

    case SendLocalListReq(updateType, authListVersion, authorizationData) =>
      messages.SendLocalListReq(
        updateType = enumerableFromJsonString(messages.UpdateType, updateType),
        listVersion = messages.AuthListSupported(authListVersion),
        localAuthorisationList = authorizationData.getOrElse(Nil).map(_.fromV16),
        hash = None
      )

    case SendLocalListRes(status) => messages.SendLocalListRes(updateStatusFromV16(status))

    case ReserveNowReq(connectorId, expiryDate, idTag, parentIdTag, reservationId) =>
      messages.ReserveNowReq(messages.Scope.fromOcpp(connectorId), expiryDate, idTag, parentIdTag, reservationId)

    case ReserveNowRes(status) => messages.ReserveNowRes(enumerableFromJsonString(messages.Reservation, status))

    case CancelReservationReq(reservationId) => messages.CancelReservationReq(reservationId)

    case CancelReservationRes(status) => messages.CancelReservationRes(statusStringToBoolean(status))

    case ClearChargingProfileReq(id, connectorId, chargingProfilePurpose, stackLevel) =>
      messages.ClearChargingProfileReq(
        id,
        connectorId.map(messages.Scope.fromOcpp),
        chargingProfilePurpose.map(enumerableFromJsonString(messages.ChargingProfilePurpose, _)),
        stackLevel
      )

    case ClearChargingProfileRes(status) => messages.ClearChargingProfileRes(
      enumerableFromJsonString(messages.ClearChargingProfileStatus, status)
    )

    case GetCompositeScheduleReq(connectorId, duration, chargingRateUnit) =>
      messages.GetCompositeScheduleReq(
        messages.Scope.fromOcpp(connectorId),
        duration.seconds,
        chargingRateUnit.map(enumerableFromJsonString(messages.UnitOfChargeRate, _))
      )

    case GetCompositeScheduleRes(status, connectorId, scheduleStart, chargingSchedule) =>
      messages.GetCompositeScheduleRes(
        status match {
          case "Accepted" =>
            messages.GetCompositeScheduleStatus.Accepted(
              messages.Scope.fromOcpp(connectorId.getOrElse {
                throw new MappingException("Missing connector id")
              }),
              scheduleStart,
              chargingSchedule.map(chargingScheduleFromV16)
            )
          case "Rejected" =>
            messages.GetCompositeScheduleStatus.Rejected
        }
      )

    case SetChargingProfileReq(connectorId, csChargingProfiles) =>
      messages.SetChargingProfileReq(
        messages.Scope.fromOcpp(connectorId),
        chargingProfileFromV16(csChargingProfiles)
      )

    case SetChargingProfileRes(status) => messages.SetChargingProfileRes(
      enumerableFromJsonString(messages.ChargingProfileStatus, status)
    )

    case triggerMessageReq: TriggerMessageReq =>
      triggerFromV16(triggerMessageReq)

    case TriggerMessageRes(status) => messages.TriggerMessageRes(
      enumerableFromJsonString(messages.TriggerMessageStatus, status)
    )

    case DataTransferReq(_, _, _) | DataTransferRes(_, _) => unexpectedMessage(msg)
  }

  private def unexpectedMessage(msg: Any) =
    throw new Exception(s"Couldn't convert unexpected OCPP message $msg")

  private implicit class RichIdTagInfo(idTagInfo: messages.IdTagInfo) {
    def toV16: IdTagInfo = IdTagInfo(
      status = idTagInfo.status.name,
      expiryDate = idTagInfo.expiryDate,
      parentIdTag = idTagInfo.parentIdTag
    )
  }

  private implicit class RichV16IdTagInfo(self: IdTagInfo) {
    def fromV16: messages.IdTagInfo = messages.IdTagInfo(
      status = enumerableFromJsonString(messages.AuthorizationStatus, self.status),
      expiryDate = self.expiryDate,
      parentIdTag = self.parentIdTag
    )
  }

  private object RichChargePointStatus {
    val defaultErrorCode = "NoError"
  }

  private implicit class RichChargePointStatus(self: messages.ChargePointStatus) {

    import RichChargePointStatus.defaultErrorCode

    def toV16Fields: (String, String, Option[String], Option[String]) = {
      def simpleStatus(name: String) = (name, defaultErrorCode, self.info, None)
      import messages.ChargePointStatus
      self match {
        case ChargePointStatus.Available(_) => simpleStatus("Available")
        case ChargePointStatus.Occupied(kind, _) => simpleStatus(
          kind.getOrElse(throw new MappingException("Missing occupancy kind")).name
        )
        case ChargePointStatus.Unavailable(_) => simpleStatus("Unavailable")
        case ChargePointStatus.Reserved(_) => simpleStatus("Reserved")
        case ChargePointStatus.Faulted(errCode, inf, vendorErrCode) =>
          ("Faulted", errCode.map(_.name).getOrElse(defaultErrorCode), inf, vendorErrCode)
      }
    }
  }

  private def statusFieldsToOcppStatus(status: String, errorCode: String, info: Option[String],
    vendorErrorCode: Option[String]): messages.ChargePointStatus = {
    import messages.{ChargePointStatus, OccupancyKind}
    import OccupancyKind._

    import RichChargePointStatus.defaultErrorCode
    status match {
      case "Available" => ChargePointStatus.Available(info)
      case "Preparing" => ChargePointStatus.Occupied(Some(Preparing))
      case "Charging"  => ChargePointStatus.Occupied(Some(Charging))
      case "SuspendedEV" => ChargePointStatus.Occupied(Some(SuspendedEV))
      case "SuspendedEVSE" => ChargePointStatus.Occupied(Some(SuspendedEVSE))
      case "Finishing" => ChargePointStatus.Occupied(Some(Finishing))
      case "Unavailable" => ChargePointStatus.Unavailable(info)
      case "Reserved" => ChargePointStatus.Reserved(info)
      case "Faulted" =>
        val errorCodeString =
          if (errorCode == defaultErrorCode)
            None
          else
            Some(enumerableFromJsonString(messages.ChargePointErrorCode, errorCode))
        ChargePointStatus.Faulted(errorCodeString, info, vendorErrorCode)
    }
  }

  private implicit class RichMeter(self: messages.meter.Meter) {
    def toV16: Meter = Meter(
      timestamp = self.timestamp,
      sampledValue = self.values.map(valueToV16)
    )

    def valueToV16(v: messages.meter.Value): MeterValue = {
      import messages.meter._
      MeterValue(
        value = v.value,
        measurand = noneIfDefault(Measurand, v.measurand),
        phase = v.phase.map(_.name),
        context = noneIfDefault(ReadingContext, v.context),
        format = noneIfDefault(ValueFormat, v.format),
        location = noneIfDefault(Location, v.location),
        unit = noneIfDefault(UnitOfMeasure, v.unit)
      )
    }
  }

  private def meterFromV16(v16m: Meter): messages.meter.Meter = {
    messages.meter.Meter(v16m.timestamp, v16m.sampledValue.map(meterValueFromV16))
  }

  private def meterValueFromV16(v16m: MeterValue): messages.meter.Value = {
    import messages.meter._
    import v16m._

    Value(
      value = value,
      measurand = defaultIfNone(Measurand, measurand),
      phase = phase.map(enumerableFromJsonString(Phase, _)),
      context = defaultIfNone(ReadingContext, context),
      format = defaultIfNone(ValueFormat, format),
      location = defaultIfNone(Location, location),
      unit = defaultIfNone(UnitOfMeasure, unit)
    )
  }

  private def getMeterValueProperty[V <: Nameable](
    property: Option[String], enum: Enumerable[V], default: V
  ): V = property.fold(default)(s => enumerableFromJsonString(enum, s))

  private implicit class BooleanToStatusString(val b: Boolean) extends AnyVal {
    def toStatusString = if (b) "Accepted" else "Rejected"
  }

  private def statusStringToBoolean(statusString: String) = statusString match {
    case "Accepted" => true
    case "Rejected" => false
    case _ => throw new MappingException(
      s"Did not recognize status $statusString (expected 'Accepted' or 'Rejected')"
    )
  }

  private implicit class RichKeyValue(val self: messages.KeyValue) {

    import self._

    def toV16: ConfigurationEntry = ConfigurationEntry(key, readonly, value)
  }

  private implicit class RichConfigurationEntry(self: ConfigurationEntry) {

    import self._

    def fromV16: messages.KeyValue = messages.KeyValue(key, readonly, value)
  }

  private implicit class RichAuthListVersion(self: messages.AuthListVersion) {
    def toV16: Int = self match {
      case messages.AuthListNotSupported => -1
      case messages.AuthListSupported(i) => i
    }
  }

  private implicit class RichAuthorisationData(self: messages.AuthorisationData) {
    def toV16: AuthorisationData = {
      val v16IdTagInfo = self match {
        case messages.AuthorisationAdd(_, idTagInfo) => Some(idTagInfo.toV16)
        case messages.AuthorisationRemove(_) => None
      }

      AuthorisationData(self.idTag, v16IdTagInfo)
    }
  }

  private implicit class RichV16AuthorisationData(self: AuthorisationData) {
    def fromV16: messages.AuthorisationData = messages.AuthorisationData(
      self.idTag, self.idTagInfo.map(_.fromV16)
    )
  }

  private implicit class RichUpdateStatus(self: messages.UpdateStatus) {
    def toV16: String = self match {
      case updateStatus: messages.UpdateStatusWithoutHash => updateStatus.name
      case messages.UpdateStatusWithHash.Accepted(_) => "Accepted"
    }
  }

  private def updateStatusFromV16(status: String): messages.UpdateStatus = {
    messages.UpdateStatusWithoutHash.withName(status) match {
      case Some(updateStatus) => updateStatus
      case None => status match {
        case "Accepted" => messages.UpdateStatusWithHash.Accepted(hash = None)
        case _ => throw new MappingException(s"Value $status is not valid for UpdateStatus")
      }
    }
  }

  /**
   * Tries to get select the enumerable value whose name is equal to the given string. If no such enum value exists,
   * throws a net.liftweb.json.MappingException.
   */
  private def enumerableFromJsonString[T <: Nameable](enum: Enumerable[T], s: String): T =
    enum.withName(s) match {
      case None =>
        throw new MappingException(s"Value $s is not valid for ${enum.getClass.getSimpleName}")
      case Some(v) => v
    }

  private def noneIfDefault[T <: Nameable](enumerable: messages.EnumerableWithDefault[T], actual: T): Option[String] =
    if (actual == enumerable.default) None else Some(actual.name)

  private def defaultIfNone[T <: Nameable](enumerable: messages.EnumerableWithDefault[T], str: Option[String]): T =
    str.map(enumerableFromJsonString(enumerable, _)).getOrElse(enumerable.default)

  private def noneIfEmpty[T](l: List[T]): Option[List[T]] =
    if (l.isEmpty) None else Some(l)

  /**
   * Parses a URI and throws a lift-json MappingException if the syntax is wrong
   */
  private def parseURI(s: String) = try {
    new URI(s)
  } catch {
    case e: URISyntaxException => throw MappingException(s"Invalid URL $s in OCPP-JSON message", e)
  }

  private def triggerFromV16(v16t: TriggerMessageReq): messages.TriggerMessageReq =
    messages.TriggerMessageReq {
      import messages.ConnectorScope.fromOcpp
      import messages.MessageTriggerWithScope
      import messages.MessageTriggerWithoutScope
      v16t match {
        case TriggerMessageReq(requestedMessage, connectorId) =>
          MessageTriggerWithoutScope.withName(requestedMessage) match {
            case Some(messageTrigger) => messageTrigger
            case None => requestedMessage match {
              case "MeterValues" =>
                MessageTriggerWithScope.MeterValues(connectorId.map(fromOcpp))
              case "StatusNotification" =>
                MessageTriggerWithScope.StatusNotification(connectorId.map(fromOcpp))
              case _ => throw new MappingException(
                s"Value $requestedMessage is not valid for MessageTrigger"
              )
            }
          }
      }
    }

  private implicit class RichChargingSchedule(cs: messages.ChargingSchedule) {
    def toV16: ChargingSchedule =
      ChargingSchedule(
        cs.chargingRateUnit.name,
        periodToV16,
        cs.duration.map(_.toSeconds.toInt),
        cs.startsAt,
        cs.minChargingRate.map(_.toFloat)
      )

    def periodToV16: List[ChargingSchedulePeriod] =
      cs.chargingSchedulePeriods.map { csp =>
        ChargingSchedulePeriod(
          csp.startOffset.toSeconds.toInt,
          csp.amperesLimit.toFloat,
          csp.numberPhases
        )
      }
  }

  private def periodFromV16(v16sp: ChargingSchedulePeriod): messages.ChargingSchedulePeriod =
    messages.ChargingSchedulePeriod(v16sp.startPeriod.seconds, v16sp.limit.toDouble, v16sp.numberPhases)

  private implicit class RichChargingProfile(cp: messages.ChargingProfile) {

    import messages.ChargingProfileKind._
    def toV16: ChargingProfile = ChargingProfile(
      cp.id,
      cp.stackLevel,
      cp.chargingProfilePurpose.name,
      cp.chargingProfileKind match {
        case Recurring(_) => "Recurring"
        case k            => k.toString
      },
      cp.chargingSchedule.toV16,
      cp.transactionId,
      cp.chargingProfileKind match {
        case Recurring(recKind) => Some(recKind.name)
        case _                  => None
      },
      cp.validFrom,
      cp.validTo
    )
  }

  private def chargingProfileFromV16(v16p: ChargingProfile): messages.ChargingProfile =
    messages.ChargingProfile(
      v16p.chargingProfileId,
      v16p.stackLevel,
      enumerableFromJsonString(messages.ChargingProfilePurpose, v16p.chargingProfilePurpose),
      stringToProfileKind(v16p.chargingProfileKind, v16p.recurrencyKind),
      chargingScheduleFromV16(v16p.chargingSchedule),
      v16p.transactionId,
      v16p.validFrom,
      v16p.validTo
    )

  private def stringToProfileKind(v16cpk: String, v16rk: Option[String]): messages.ChargingProfileKind = {
    import messages.ChargingProfileKind._
    import messages.RecurrencyKind._

    (v16cpk, v16rk) match {
      case ("Absolute", _)               => Absolute
      case ("Relative", _)               => Relative
      case ("Recurring", Some("Weekly")) => Recurring(Weekly)
      case ("Recurring", Some("Daily"))  => Recurring(Daily)
      case _ => throw new MappingException(s"Unrecognized values ($v16cpk, $v16rk) for OCPP profile/recurrency kind")
    }
  }

  private def chargingScheduleFromV16(v16cs: ChargingSchedule): messages.ChargingSchedule =
    messages.ChargingSchedule(
      enumerableFromJsonString(messages.UnitOfChargeRate, v16cs.chargingRateUnit),
      v16cs.chargingSchedulePeriod.map(periodFromV16),
      v16cs.minChargingRate.map(_.toDouble),
      v16cs.startSchedule,
      v16cs.duration.map(_.seconds)
    )
}
