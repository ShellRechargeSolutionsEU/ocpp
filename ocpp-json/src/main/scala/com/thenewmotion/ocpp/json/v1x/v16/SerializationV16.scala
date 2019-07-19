package com.thenewmotion.ocpp
package json
package v1x
package v16

import scala.concurrent.duration._
import messages.v1x
import v1x.{Scope, ConnectorScope, ChargePointStatus, ChargingProfileKind}
import v1x.OccupancyKind._
import org.json4s.MappingException

object SerializationV16 extends SerializationCommon {

  implicit val AuthorizeReqV16Variant = OcppMessageSerializer.variantFor[v1x.AuthorizeReq, Version.V16.type, AuthorizeReq](
    (msg: v1x.AuthorizeReq) => AuthorizeReq(msg.idTag),
    (msg: AuthorizeReq) => v1x.AuthorizeReq(msg.idTag)
  )

  implicit val AuthorizeResV16Variant = OcppMessageSerializer.variantFor[v1x.AuthorizeRes, Version.V16.type, AuthorizeRes](
    (msg: v1x.AuthorizeRes) => AuthorizeRes(msg.idTag.toV16),
    (msg: AuthorizeRes) => v1x.AuthorizeRes(msg.idTagInfo.fromV16)
  )

  implicit val DataTransferReqV16Variant = OcppMessageSerializer.variantFor[v1x.CentralSystemDataTransferReq, Version.V16.type, DataTransferReq](
    (msg: v1x.CentralSystemDataTransferReq) => DataTransferReq(msg.vendorId, msg.messageId, msg.data),
    (msg: DataTransferReq) => v1x.CentralSystemDataTransferReq(msg.vendorId, msg.messageId, msg.data)
  )

  implicit val DataTransferResV16Variant = OcppMessageSerializer.variantFor[v1x.CentralSystemDataTransferRes, Version.V16.type, DataTransferRes](
    (msg: v1x.CentralSystemDataTransferRes) => DataTransferRes(msg.status.name, msg.data),
    (msg: DataTransferRes) => v1x.CentralSystemDataTransferRes(
      enumerableFromJsonString(v1x.DataTransferStatus, msg.status),
      msg.data
    )
  )

  implicit val StartTransactionReqV16Variant = OcppMessageSerializer.variantFor[v1x.StartTransactionReq, Version.V16.type, StartTransactionReq](
    (msg: v1x.StartTransactionReq) => StartTransactionReq(
      connectorId = msg.connector.toOcpp,
      idTag = msg.idTag,
      timestamp = msg.timestamp,
      meterStart = msg.meterStart,
      reservationId = msg.reservationId
    ),
    (msg: StartTransactionReq) => v1x.StartTransactionReq(
      ConnectorScope.fromOcpp(msg.connectorId),
      msg.idTag,
      msg.timestamp,
      msg.meterStart,
      msg.reservationId
    )
  )

  implicit val StartTransactionResV16Variant = OcppMessageSerializer.variantFor[v1x.StartTransactionRes, Version.V16.type, StartTransactionRes](
    (msg: v1x.StartTransactionRes) => StartTransactionRes(msg.transactionId, msg.idTag.toV16),
    (msg: StartTransactionRes) => v1x.StartTransactionRes(msg.transactionId, msg.idTagInfo.fromV16)
  )

  implicit val StopTransactionReqV16Variant = OcppMessageSerializer.variantFor[v1x.StopTransactionReq, Version.V16.type, StopTransactionReq](
    (msg: v1x.StopTransactionReq) => StopTransactionReq(
      transactionId = msg.transactionId,
      idTag = msg.idTag,
      timestamp = msg.timestamp,
      reason = noneIfDefault(v1x.StopReason, msg.reason),
      meterStop = msg.meterStop,
      transactionData = noneIfEmpty(msg.meters.map(_.toV16))
    ),
    (msg: StopTransactionReq) => v1x.StopTransactionReq(
      msg.transactionId,
      msg.idTag,
      msg.timestamp,
      msg.meterStop,
      defaultIfNone(v1x.StopReason, msg.reason),
      emptyIfNone(msg.transactionData).map(meterFromV16)
    )
  )

  implicit val StopTransactionResV16Variant = OcppMessageSerializer.variantFor[v1x.StopTransactionRes, Version.V16.type, StopTransactionRes](
    (msg: v1x.StopTransactionRes) => StopTransactionRes(msg.idTag.map(_.toV16)),
    (msg: StopTransactionRes) => v1x.StopTransactionRes(msg.idTagInfo.map(_.fromV16))
  )

  implicit val HeartbeatReqV16Variant = OcppMessageSerializer.variantFor[v1x.HeartbeatReq.type, Version.V16.type, HeartbeatReq](
    (_: v1x.HeartbeatReq.type) => HeartbeatReq(),
    (_: HeartbeatReq) => v1x.HeartbeatReq
  )

  implicit val HeartbeatResV16Variant = OcppMessageSerializer.variantFor[v1x.HeartbeatRes, Version.V16.type, HeartbeatRes](
    (msg: v1x.HeartbeatRes) => HeartbeatRes(msg.currentTime),
    (msg: HeartbeatRes) => v1x.HeartbeatRes(msg.currentTime)
  )

  implicit val MeterValuesReqV16Variant = OcppMessageSerializer.variantFor[v1x.MeterValuesReq, Version.V16.type, MeterValuesReq](
    (msg: v1x.MeterValuesReq) => MeterValuesReq(
      msg.scope.toOcpp,
      msg.transactionId,
      msg.meters.map(_.toV16)
    ),
    (msg: MeterValuesReq) => v1x.MeterValuesReq(
      Scope.fromOcpp(msg.connectorId),
      msg.transactionId,
      msg.meterValue.map(meterFromV16)
    )
  )

  implicit val MeterValuesResV16Variant = OcppMessageSerializer.variantFor[v1x.MeterValuesRes.type, Version.V16.type, MeterValuesRes](
    (_: v1x.MeterValuesRes.type) => MeterValuesRes(),
    (_: MeterValuesRes) => v1x.MeterValuesRes
  )

  implicit val BootNotificationReqV16Variant = OcppMessageSerializer.variantFor[v1x.BootNotificationReq, Version.V16.type, BootNotificationReq](
    (msg: v1x.BootNotificationReq) =>
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
    (msg: BootNotificationReq) =>
      v1x.BootNotificationReq(
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

  implicit val BootNotificationResV16Variant = OcppMessageSerializer.variantFor[v1x.BootNotificationRes, Version.V16.type, BootNotificationRes](
    (msg: v1x.BootNotificationRes) =>
      BootNotificationRes(msg.status.name, msg.currentTime, msg.interval.toSeconds.toInt),

    (msg: BootNotificationRes) =>
      v1x.BootNotificationRes(
        status = enumerableFromJsonString(v1x.RegistrationStatus, msg.status),
        currentTime = msg.currentTime,
        interval = msg.interval.seconds
      )
  )

  implicit val StatusNotificationReqV16Variant = OcppMessageSerializer.variantFor[v1x.StatusNotificationReq, Version.V16.type, StatusNotificationReq](
    (msg: v1x.StatusNotificationReq) => {
      val (ocppStatus, errorCode, info, vendorErrorCode) = msg.status.toV16Fields
      StatusNotificationReq(
        msg.scope.toOcpp,
        ocppStatus,
        errorCode,
        info,
        msg.timestamp,
        msg.vendorId,
        vendorErrorCode
      )
    },
    (msg: StatusNotificationReq) => v1x.StatusNotificationReq(
      Scope.fromOcpp(msg.connectorId),
      statusFieldsToOcppStatus(msg.status, msg.errorCode, msg.info, msg.vendorErrorCode),
      msg.timestamp,
      msg.vendorId
    )
  )

  implicit val StatusNotificationResV16Variant = OcppMessageSerializer.variantFor[v1x.StatusNotificationRes.type, Version.V16.type, StatusNotificationRes](
    (_: v1x.StatusNotificationRes.type) => StatusNotificationRes(),
    (_: StatusNotificationRes) => v1x.StatusNotificationRes
  )

  implicit val FirmwareStatusNotificationReqV16Variant = OcppMessageSerializer.variantFor[v1x.FirmwareStatusNotificationReq, Version.V16.type, FirmwareStatusNotificationReq](
    (msg: v1x.FirmwareStatusNotificationReq) => FirmwareStatusNotificationReq(msg.status.name),
    (msg: FirmwareStatusNotificationReq) => v1x.FirmwareStatusNotificationReq(
      enumerableFromJsonString(v1x.FirmwareStatus, msg.status)
    )
  )

  implicit val FirmwareStatusNotificationResV16Variant = OcppMessageSerializer.variantFor[v1x.FirmwareStatusNotificationRes.type, Version.V16.type, FirmwareStatusNotificationRes](
    (_: v1x.FirmwareStatusNotificationRes.type) => FirmwareStatusNotificationRes(),
    (_: FirmwareStatusNotificationRes) => v1x.FirmwareStatusNotificationRes
  )

  implicit val DiagnosticsStatusNotificationReqV16Variant = OcppMessageSerializer.variantFor[v1x.DiagnosticsStatusNotificationReq, Version.V16.type, DiagnosticsStatusNotificationReq](
    (msg: v1x.DiagnosticsStatusNotificationReq) => DiagnosticsStatusNotificationReq(msg.status.name),
    (msg: DiagnosticsStatusNotificationReq) => v1x.DiagnosticsStatusNotificationReq(
      enumerableFromJsonString(v1x.DiagnosticsStatus, msg.status)
    )
  )

  implicit val DiagnosticsStatusNotificationResV16Variant = OcppMessageSerializer.variantFor[v1x.DiagnosticsStatusNotificationRes.type, Version.V16.type, DiagnosticsStatusNotificationRes](
    (_: v1x.DiagnosticsStatusNotificationRes.type) => DiagnosticsStatusNotificationRes(),
    (_: DiagnosticsStatusNotificationRes) => v1x.DiagnosticsStatusNotificationRes
  )

  implicit val RemoteStartTransactionReqV16Variant = OcppMessageSerializer.variantFor[v1x.RemoteStartTransactionReq, Version.V16.type, RemoteStartTransactionReq](
    (msg: v1x.RemoteStartTransactionReq) => RemoteStartTransactionReq(
      msg.idTag,
      msg.connector.map(_.toOcpp),
      msg.chargingProfile.map(_.toV16)
    ),
    (msg: RemoteStartTransactionReq) => v1x.RemoteStartTransactionReq(
      msg.idTag,
      msg.connectorId.map(ConnectorScope.fromOcpp),
      msg.chargingProfile.map(chargingProfileFromV16)
    )
  )

  implicit val RemoteStartTransactionResV16Variant = OcppMessageSerializer.variantFor[v1x.RemoteStartTransactionRes, Version.V16.type, RemoteStartTransactionRes](
    (msg: v1x.RemoteStartTransactionRes) => RemoteStartTransactionRes(msg.accepted.toStatusString),
    (msg: RemoteStartTransactionRes) => v1x.RemoteStartTransactionRes(statusStringToBoolean(msg.status))
  )

  implicit val RemoteStopTransactionReqV16Variant = OcppMessageSerializer.variantFor[v1x.RemoteStopTransactionReq, Version.V16.type, RemoteStopTransactionReq](
    (msg: v1x.RemoteStopTransactionReq) => RemoteStopTransactionReq(msg.transactionId),
    (msg: RemoteStopTransactionReq) => v1x.RemoteStopTransactionReq(msg.transactionId)
  )

  implicit val RemoteStopTransactionResV16Variant = OcppMessageSerializer.variantFor[v1x.RemoteStopTransactionRes, Version.V16.type, RemoteStopTransactionRes](
    (msg: v1x.RemoteStopTransactionRes) => RemoteStopTransactionRes(msg.accepted.toStatusString),
    (msg: RemoteStopTransactionRes) => v1x.RemoteStopTransactionRes(statusStringToBoolean(msg.status))
  )

  implicit val UnlockConnectorReqV16Variant = OcppMessageSerializer.variantFor[v1x.UnlockConnectorReq, Version.V16.type, UnlockConnectorReq](
    (msg: v1x.UnlockConnectorReq) => UnlockConnectorReq(msg.connector.toOcpp),
    (msg: UnlockConnectorReq) => v1x.UnlockConnectorReq(ConnectorScope.fromOcpp(msg.connectorId))
  )

  implicit val UnlockConnectorResV16Variant = OcppMessageSerializer.variantFor[v1x.UnlockConnectorRes, Version.V16.type, UnlockConnectorRes](
    (msg: v1x.UnlockConnectorRes) => UnlockConnectorRes(msg.status.name),
    (msg: UnlockConnectorRes) => v1x.UnlockConnectorRes(
      enumerableFromJsonString(v1x.UnlockStatus, msg.status)
    )
  )

  implicit val GetDiagnosticsReqV16Variant = OcppMessageSerializer.variantFor[v1x.GetDiagnosticsReq, Version.V16.type, GetDiagnosticsReq](
    (msg: v1x.GetDiagnosticsReq) => GetDiagnosticsReq(
      msg.location.toASCIIString,
      msg.startTime,
      msg.stopTime,
      msg.retries.numberOfRetries,
      msg.retries.intervalInSeconds
    ),
    (msg: GetDiagnosticsReq) => v1x.GetDiagnosticsReq(
      parseURI(msg.location),
      msg.startTime,
      msg.stopTime,
      v1x.Retries.fromInts(msg.retries, msg.retryInterval)
    )
  )

  implicit val GetDiagnosticsResV16Variant = OcppMessageSerializer.variantFor[v1x.GetDiagnosticsRes, Version.V16.type, GetDiagnosticsRes](
    (msg: v1x.GetDiagnosticsRes) => GetDiagnosticsRes(msg.fileName),
    (msg: GetDiagnosticsRes) => v1x.GetDiagnosticsRes(msg.fileName)
  )

  implicit val ChangeConfigurationReqV16Variant = OcppMessageSerializer.variantFor[v1x.ChangeConfigurationReq, Version.V16.type, ChangeConfigurationReq](
    (msg: v1x.ChangeConfigurationReq) => ChangeConfigurationReq(msg.key, msg.value),
    (msg: ChangeConfigurationReq) => v1x.ChangeConfigurationReq(msg.key, msg.value)
  )

  implicit val ChangeConfigurationResV16Variant = OcppMessageSerializer.variantFor[v1x.ChangeConfigurationRes, Version.V16.type, ChangeConfigurationRes](
    (msg: v1x.ChangeConfigurationRes) => ChangeConfigurationRes(msg.status.name),
    (msg: ChangeConfigurationRes) => v1x.ChangeConfigurationRes(
      enumerableFromJsonString(v1x.ConfigurationStatus, msg.status)
    )
  )

  implicit val GetConfigurationReqV16Variant = OcppMessageSerializer.variantFor[v1x.GetConfigurationReq, Version.V16.type, GetConfigurationReq](
    (msg: v1x.GetConfigurationReq) => GetConfigurationReq(noneIfEmpty(msg.keys)),
    (msg: GetConfigurationReq) => v1x.GetConfigurationReq(emptyIfNone(msg.key))
  )

  implicit val GetConfigurationResV16Variant = OcppMessageSerializer.variantFor[v1x.GetConfigurationRes, Version.V16.type, GetConfigurationRes](
    (msg: v1x.GetConfigurationRes) => GetConfigurationRes(
      noneIfEmpty(msg.values.map(_.toV16)),
      noneIfEmpty(msg.unknownKeys)
    ),
    (msg: GetConfigurationRes) => v1x.GetConfigurationRes(
      emptyIfNone(msg.configurationKey).map(_.fromV16),
      emptyIfNone(msg.unknownKey)
    )
  )

  implicit val ChangeAvailabilityReqV16Variant = OcppMessageSerializer.variantFor[v1x.ChangeAvailabilityReq, Version.V16.type, ChangeAvailabilityReq](
    (msg: v1x.ChangeAvailabilityReq) => ChangeAvailabilityReq(
      msg.scope.toOcpp,
      msg.availabilityType.name
    ),
    (msg: ChangeAvailabilityReq) => v1x.ChangeAvailabilityReq(
      Scope.fromOcpp(msg.connectorId),
      enumerableFromJsonString(v1x.AvailabilityType, msg.`type`)
    )
  )

  implicit val ChangeAvailabilityResV16Variant = OcppMessageSerializer.variantFor[v1x.ChangeAvailabilityRes, Version.V16.type, ChangeAvailabilityRes](
    (msg: v1x.ChangeAvailabilityRes) => ChangeAvailabilityRes(msg.status.name),
    (msg: ChangeAvailabilityRes) => v1x.ChangeAvailabilityRes(
      enumerableFromJsonString(v1x.AvailabilityStatus, msg.status)
    )
  )

  implicit val ClearCacheReqV16Variant = OcppMessageSerializer.variantFor[v1x.ClearCacheReq.type, Version.V16.type, ClearCacheReq](
    (_: v1x.ClearCacheReq.type) => ClearCacheReq(),
    (_: ClearCacheReq) => v1x.ClearCacheReq
  )

  implicit val ClearCacheResV16Variant = OcppMessageSerializer.variantFor[v1x.ClearCacheRes, Version.V16.type, ClearCacheRes](
    (msg: v1x.ClearCacheRes) => ClearCacheRes(msg.accepted.toStatusString),
    (msg: ClearCacheRes) => v1x.ClearCacheRes(statusStringToBoolean(msg.status))
  )

  implicit val ResetReqV16Variant = OcppMessageSerializer.variantFor[v1x.ResetReq, Version.V16.type, ResetReq](
    (msg: v1x.ResetReq) => ResetReq(msg.resetType.name),
    (msg: ResetReq) => v1x.ResetReq(enumerableFromJsonString(v1x.ResetType, msg.`type`))
  )

  implicit val ResetResV16Variant = OcppMessageSerializer.variantFor[v1x.ResetRes, Version.V16.type, ResetRes](
    (msg: v1x.ResetRes) => ResetRes(msg.accepted.toStatusString),
    (msg: ResetRes) => v1x.ResetRes(statusStringToBoolean(msg.status))
  )

  implicit val UpdateFirmwareReqV16Variant = OcppMessageSerializer.variantFor[v1x.UpdateFirmwareReq, Version.V16.type, UpdateFirmwareReq](
    (msg: v1x.UpdateFirmwareReq) => UpdateFirmwareReq(
      msg.retrieveDate,
      msg.location.toASCIIString,
      msg.retries.numberOfRetries,
      msg.retries.intervalInSeconds
    ),
    (msg: UpdateFirmwareReq) => v1x.UpdateFirmwareReq(
      msg.retrieveDate,
      parseURI(msg.location),
      v1x.Retries.fromInts(msg.retries, msg.retryInterval)
    )
  )

  implicit val UpdateFirmwareResV16Variant = OcppMessageSerializer.variantFor[v1x.UpdateFirmwareRes.type, Version.V16.type, UpdateFirmwareRes](
    (_: v1x.UpdateFirmwareRes.type) => UpdateFirmwareRes(),
    (_: UpdateFirmwareRes) => v1x.UpdateFirmwareRes
  )

  implicit val SendLocalListReqV16Variant = OcppMessageSerializer.variantFor[v1x.SendLocalListReq, Version.V16.type, SendLocalListReq](
    (msg: v1x.SendLocalListReq) => SendLocalListReq(
      msg.updateType.name,
      msg.listVersion.toV16,
      Some(msg.localAuthorisationList.map(_.toV16))
    ),
    (msg: SendLocalListReq) => v1x.SendLocalListReq(
      enumerableFromJsonString(v1x.UpdateType, msg.updateType),
      v1x.AuthListSupported(msg.listVersion),
      emptyIfNone(msg.localAuthorizationList).map(_.fromV16),
      hash = None
    )
  )

  implicit val SendLocalListResV16Variant = OcppMessageSerializer.variantFor[v1x.SendLocalListRes, Version.V16.type, SendLocalListRes](
    (msg: v1x.SendLocalListRes) => SendLocalListRes(msg.status.toV16),
    (msg: SendLocalListRes) => v1x.SendLocalListRes(updateStatusFromV16(msg.status))
  )

  implicit val GetLocalListVersionReqV16Variant = OcppMessageSerializer.variantFor[v1x.GetLocalListVersionReq.type, Version.V16.type, GetLocalListVersionReq](
    (_: v1x.GetLocalListVersionReq.type) => GetLocalListVersionReq(),
    (_: GetLocalListVersionReq) => v1x.GetLocalListVersionReq
  )

  implicit val GetLocalListVersionResV16Variant = OcppMessageSerializer.variantFor[v1x.GetLocalListVersionRes, Version.V16.type, GetLocalListVersionRes](
    (msg: v1x.GetLocalListVersionRes) => GetLocalListVersionRes(msg.version.toV16),
    (msg: GetLocalListVersionRes) => v1x.GetLocalListVersionRes(v1x.AuthListVersion(msg.listVersion))
  )

  implicit val ReserveNowReqV16Variant = OcppMessageSerializer.variantFor[v1x.ReserveNowReq, Version.V16.type, ReserveNowReq](
    (msg: v1x.ReserveNowReq) => ReserveNowReq(
      msg.connector.toOcpp,
      msg.expiryDate,
      msg.idTag,
      msg.parentIdTag,
      msg.reservationId
    ),
    (msg: ReserveNowReq) => v1x.ReserveNowReq(
      Scope.fromOcpp(msg.connectorId),
      msg.expiryDate,
      msg.idTag,
      msg.parentIdTag,
      msg.reservationId
    )
  )

  implicit val ReserveNowResV16Variant = OcppMessageSerializer.variantFor[v1x.ReserveNowRes, Version.V16.type, ReserveNowRes](
    (msg: v1x.ReserveNowRes) => ReserveNowRes(msg.status.name),
    (msg: ReserveNowRes) => v1x.ReserveNowRes(
      enumerableFromJsonString(v1x.Reservation, msg.status)
    )
  )

  implicit val CancelReservationReqV16Variant = OcppMessageSerializer.variantFor[v1x.CancelReservationReq, Version.V16.type, CancelReservationReq](
    (msg: v1x.CancelReservationReq) => CancelReservationReq(msg.reservationId),
    (msg: CancelReservationReq) => v1x.CancelReservationReq(msg.reservationId)
  )

  implicit val CancelReservationResV16Variant = OcppMessageSerializer.variantFor[v1x.CancelReservationRes, Version.V16.type, CancelReservationRes](
    (msg: v1x.CancelReservationRes) => CancelReservationRes(msg.accepted.toStatusString),
    (msg: CancelReservationRes) => v1x.CancelReservationRes(statusStringToBoolean(msg.status))
  )

  implicit val SetChargingProfileReqV16Variant = OcppMessageSerializer.variantFor[v1x.SetChargingProfileReq, Version.V16.type, SetChargingProfileReq](
    (msg: v1x.SetChargingProfileReq) => SetChargingProfileReq(
      msg.connector.toOcpp,
      msg.chargingProfile.toV16
    )
    ,
    (msg: SetChargingProfileReq) => v1x.SetChargingProfileReq(
      Scope.fromOcpp(msg.connectorId),
      chargingProfileFromV16(msg.csChargingProfiles)
    )
  )

  implicit val SetChargingProfileResV16Variant = OcppMessageSerializer.variantFor[v1x.SetChargingProfileRes, Version.V16.type, SetChargingProfileRes](
    (msg: v1x.SetChargingProfileRes) => SetChargingProfileRes(msg.status.name),
    (msg: SetChargingProfileRes) => v1x.SetChargingProfileRes(
      enumerableFromJsonString(v1x.ChargingProfileStatus, msg.status)
    )
  )

  implicit val ClearChargingProfileReqV16Variant = OcppMessageSerializer.variantFor[v1x.ClearChargingProfileReq, Version.V16.type, ClearChargingProfileReq](
    (msg: v1x.ClearChargingProfileReq) => ClearChargingProfileReq(
      msg.id,
      msg.connector.map(_.toOcpp),
      msg.chargingProfilePurpose.map(_.name),
      msg.stackLevel
    ),
    (msg: ClearChargingProfileReq) => v1x.ClearChargingProfileReq(
      msg.id,
      msg.connectorId.map(Scope.fromOcpp),
      msg.chargingProfilePurpose.map(enumerableFromJsonString(v1x.ChargingProfilePurpose, _)),
      msg.stackLevel
    )
  )

  implicit val ClearChargingProfileResV16Variant = OcppMessageSerializer.variantFor[v1x.ClearChargingProfileRes, Version.V16.type, ClearChargingProfileRes](
    (msg: v1x.ClearChargingProfileRes) => ClearChargingProfileRes(msg.status.name),
    (msg: ClearChargingProfileRes) => v1x.ClearChargingProfileRes(
      enumerableFromJsonString(v1x.ClearChargingProfileStatus, msg.status)
    )
  )

  implicit val GetCompositeScheduleReqV16Variant = OcppMessageSerializer.variantFor[v1x.GetCompositeScheduleReq, Version.V16.type, GetCompositeScheduleReq](
    (msg: v1x.GetCompositeScheduleReq) => GetCompositeScheduleReq(
      msg.connector.toOcpp,
      msg.duration.toSeconds.toInt,
      msg.chargingRateUnit.map(_.name)
    ),
    (msg: GetCompositeScheduleReq) => v1x.GetCompositeScheduleReq(
      Scope.fromOcpp(msg.connectorId),
      msg.duration.seconds,
      msg.chargingRateUnit.map(enumerableFromJsonString(v1x.UnitOfChargingRate, _))
    )
  )

  implicit val ChargePointDataTransferReqV16Variant = OcppMessageSerializer.variantFor[v1x.ChargePointDataTransferReq, Version.V16.type, DataTransferReq](
    (msg: v1x.ChargePointDataTransferReq) => DataTransferReq(msg.vendorId, msg.messageId, msg.data),
    (msg: DataTransferReq) => v1x.ChargePointDataTransferReq(msg.vendorId, msg.messageId, msg.data)
  )

  implicit val ChargePointDataTransferResV16Variant = OcppMessageSerializer.variantFor[v1x.ChargePointDataTransferRes, Version.V16.type, DataTransferRes](
    (msg: v1x.ChargePointDataTransferRes) => DataTransferRes(msg.status.name, msg.data),
    (msg: DataTransferRes) => v1x.ChargePointDataTransferRes(
      enumerableFromJsonString(v1x.DataTransferStatus, msg.status),
      msg.data
    )
  )

  implicit val GetCompositeScheduleResV16Variant = OcppMessageSerializer.variantFor[v1x.GetCompositeScheduleRes, Version.V16.type, GetCompositeScheduleRes](
    (msg: v1x.GetCompositeScheduleRes) => msg.toV16,
    (msg: GetCompositeScheduleRes) => v1x.GetCompositeScheduleRes(compositeStatusFromV16(msg))
  )

  implicit val TriggerMessageReqV16Variant = OcppMessageSerializer.variantFor[v1x.TriggerMessageReq, Version.V16.type, TriggerMessageReq](
    (msg: v1x.TriggerMessageReq) => msg.toV16,
    (msg: TriggerMessageReq) => triggerMessageReqFromV16(msg)

  )

  implicit val TriggerMessageResV16Variant = OcppMessageSerializer.variantFor[v1x.TriggerMessageRes, Version.V16.type, TriggerMessageRes](
    (msg: v1x.TriggerMessageRes) => TriggerMessageRes(msg.status.name),
    (msg: TriggerMessageRes) => v1x.TriggerMessageRes(
      enumerableFromJsonString(v1x.TriggerMessageStatus, msg.status)
    )
  )

  private implicit class RichIdTagInfo(idTagInfo: v1x.IdTagInfo) {
    def toV16: IdTagInfo = IdTagInfo(
      status = idTagInfo.status.name,
      expiryDate = idTagInfo.expiryDate,
      parentIdTag = idTagInfo.parentIdTag
    )
  }

  private implicit class RichV16IdTagInfo(self: IdTagInfo) {
    def fromV16: v1x.IdTagInfo = v1x.IdTagInfo(
      status = enumerableFromJsonString(v1x.AuthorizationStatus, self.status),
      expiryDate = self.expiryDate,
      parentIdTag = self.parentIdTag
    )
  }

  private object RichChargePointStatus {
    val defaultErrorCode = "NoError"
  }

  private implicit class RichChargePointStatus(self: ChargePointStatus) {

    def toV16Fields: (String, String, Option[String], Option[String]) = {

      def simpleStatus(name: String) =
        (name, RichChargePointStatus.defaultErrorCode, self.info, None)

      self match {
        case ChargePointStatus.Available(_) => simpleStatus("Available")
        case ChargePointStatus.Occupied(kind, _) => simpleStatus(
          kind.getOrElse(throw new MappingException("Missing occupancy kind")).name
        )
        case ChargePointStatus.Unavailable(_) => simpleStatus("Unavailable")
        case ChargePointStatus.Reserved(_) => simpleStatus("Reserved")
        case ChargePointStatus.Faulted(errCode, inf, vendorErrCode) =>
          ("Faulted", errCode.map(_.name).getOrElse(RichChargePointStatus.defaultErrorCode), inf, vendorErrCode)
      }
    }
  }

  private def statusFieldsToOcppStatus(status: String, errorCode: String, info: Option[String],
    vendorErrorCode: Option[String]): ChargePointStatus = {

    status match {
      case "Available" => ChargePointStatus.Available(info)
      case "Preparing" => ChargePointStatus.Occupied(Some(Preparing))
      case "Charging" => ChargePointStatus.Occupied(Some(Charging))
      case "SuspendedEV" => ChargePointStatus.Occupied(Some(SuspendedEV))
      case "SuspendedEVSE" => ChargePointStatus.Occupied(Some(SuspendedEVSE))
      case "Finishing" => ChargePointStatus.Occupied(Some(Finishing))
      case "Unavailable" => ChargePointStatus.Unavailable(info)
      case "Reserved" => ChargePointStatus.Reserved(info)
      case "Faulted" =>
        val errorCodeString =
          if (errorCode == RichChargePointStatus.defaultErrorCode)
            None
          else
            Some(enumerableFromJsonString(v1x.ChargePointErrorCode, errorCode))

        ChargePointStatus.Faulted(errorCodeString, info, vendorErrorCode)
    }
  }

  private implicit class RichMeter(self: v1x.meter.Meter) {
    def toV16: Meter = Meter(
      timestamp = self.timestamp,
      sampledValue = self.values.map(valueToV16)
    )

    def valueToV16(v: v1x.meter.Value): MeterValue = {
      MeterValue(
        value = v.value,
        measurand = noneIfDefault(v1x.meter.Measurand, v.measurand),
        phase = v.phase.map(_.name),
        context = noneIfDefault(v1x.meter.ReadingContext, v.context),
        format = noneIfDefault(v1x.meter.ValueFormat, v.format),
        location = noneIfDefault(v1x.meter.Location, v.location),
        unit = noneIfDefault(v1x.meter.UnitOfMeasure, v.unit)
      )
    }
  }

  private def meterFromV16(v16m: Meter): v1x.meter.Meter = {
    v1x.meter.Meter(v16m.timestamp, v16m.sampledValue.map(meterValueFromV16))
  }

  private def meterValueFromV16(v16m: MeterValue): v1x.meter.Value = {
    v1x.meter.Value(
      value = v16m.value,
      measurand = defaultIfNone(v1x.meter.Measurand, v16m.measurand),
      phase = v16m.phase.map(enumerableFromJsonString(v1x.meter.Phase, _)),
      context = defaultIfNone(v1x.meter.ReadingContext, v16m.context),
      format = defaultIfNone(v1x.meter.ValueFormat, v16m.format),
      location = defaultIfNone(v1x.meter.Location, v16m.location),
      unit = defaultIfNone(v1x.meter.UnitOfMeasure, v16m.unit)
    )
  }

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

  private implicit class RichKeyValue(val self: v1x.KeyValue) {

    def toV16: ConfigurationEntry = ConfigurationEntry(self.key, self.readonly, self.value)
  }

  private implicit class RichConfigurationEntry(self: ConfigurationEntry) {

    def fromV16: v1x.KeyValue = v1x.KeyValue(self.key, self.readonly, self.value)
  }

  private implicit class RichAuthListVersion(self: v1x.AuthListVersion) {
    def toV16: Int = self match {
      case v1x.AuthListNotSupported => -1
      case v1x.AuthListSupported(i) => i
    }
  }

  private implicit class RichAuthorisationData(self: v1x.AuthorisationData) {
    def toV16: AuthorisationData = {
      val v16IdTagInfo = self match {
        case v1x.AuthorisationAdd(_, idTagInfo) => Some(idTagInfo.toV16)
        case v1x.AuthorisationRemove(_) => None
      }

      AuthorisationData(self.idTag, v16IdTagInfo)
    }
  }

  private implicit class RichV16AuthorisationData(self: AuthorisationData) {
    def fromV16: v1x.AuthorisationData = v1x.AuthorisationData(
      self.idTag, self.idTagInfo.map(_.fromV16)
    )
  }

  private implicit class RichUpdateStatus(self: v1x.UpdateStatus) {
    def toV16: String = self match {
      case updateStatus: v1x.UpdateStatusWithoutHash => updateStatus.name
      case v1x.UpdateStatusWithHash.Accepted(_) => "Accepted"
    }
  }

  private def updateStatusFromV16(status: String): v1x.UpdateStatus = {
    v1x.UpdateStatusWithoutHash.withName(status) match {
      case Some(updateStatus) => updateStatus
      case None => status match {
        case "Accepted" => v1x.UpdateStatusWithHash.Accepted(hash = None)
        case _ => throw new MappingException(s"Value $status is not valid for UpdateStatus")
      }
    }
  }

  private implicit class RichTriggerMessageReq(self: v1x.TriggerMessageReq) {
    def toV16: TriggerMessageReq = TriggerMessageReq.tupled {
      self.requestedMessage match {
        case messageTrigger: v1x.MessageTriggerWithoutScope =>
          (messageTrigger.name, None)
        case v1x.MessageTriggerWithScope.MeterValues(connectorId) =>
          ("MeterValues", connectorId.map(_.toOcpp))
        case v1x.MessageTriggerWithScope.StatusNotification(connectorId) =>
          ("StatusNotification", connectorId.map(_.toOcpp))
      }
    }
  }

  private def triggerMessageReqFromV16(v16t: TriggerMessageReq): v1x.TriggerMessageReq =
    v1x.TriggerMessageReq {
      import ConnectorScope.fromOcpp
      v16t match {
        case TriggerMessageReq(requestedMessage, connectorId) =>
          v1x.MessageTriggerWithoutScope.withName(requestedMessage) match {
            case Some(messageTrigger) => messageTrigger
            case None => requestedMessage match {
              case "MeterValues" =>
                v1x.MessageTriggerWithScope.MeterValues(connectorId.map(fromOcpp))
              case "StatusNotification" =>
                v1x.MessageTriggerWithScope.StatusNotification(connectorId.map(fromOcpp))
              case _ => throw new MappingException(
                s"Value $requestedMessage is not valid for MessageTrigger"
              )
            }
          }
      }
    }

  private implicit class RichChargingSchedule(cs: v1x.ChargingSchedule) {
    def toV16: ChargingSchedule =
      ChargingSchedule(
        cs.chargingRateUnit.name,
        periodToV16,
        cs.duration.map(_.toSeconds.toInt),
        cs.startsAt,
        cs.minChargingRate.map(toOneDigitFraction)
      )

    def periodToV16: List[ChargingSchedulePeriod] =
      cs.chargingSchedulePeriods.map { csp =>
        ChargingSchedulePeriod(
          csp.startOffset.toSeconds.toInt,
          toOneDigitFraction(csp.amperesLimit),
          csp.numberPhases
        )
      }
  }

  private def periodFromV16(v16sp: ChargingSchedulePeriod): v1x.ChargingSchedulePeriod =
    v1x.ChargingSchedulePeriod(v16sp.startPeriod.seconds, v16sp.limit.toDouble, v16sp.numberPhases)

  private def toOneDigitFraction(v: Double): Float = (v * 10).round.toFloat / 10

  private implicit class RichChargingProfile(cp: v1x.ChargingProfile) {

    import ChargingProfileKind._

    def toV16: ChargingProfile = ChargingProfile(
      cp.id,
      cp.stackLevel,
      cp.chargingProfilePurpose.name,
      cp.chargingProfileKind match {
        case Recurring(_) => "Recurring"
        case k => k.toString
      },
      cp.chargingSchedule.toV16,
      cp.transactionId,
      cp.chargingProfileKind match {
        case Recurring(recKind) => Some(recKind.name)
        case _ => None
      },
      cp.validFrom,
      cp.validTo
    )
  }

  private def chargingProfileFromV16(v16p: ChargingProfile): v1x.ChargingProfile =
    v1x.ChargingProfile(
      v16p.chargingProfileId,
      v16p.stackLevel,
      enumerableFromJsonString(v1x.ChargingProfilePurpose, v16p.chargingProfilePurpose),
      stringToProfileKind(v16p.chargingProfileKind, v16p.recurrencyKind),
      chargingScheduleFromV16(v16p.chargingSchedule),
      v16p.transactionId,
      v16p.validFrom,
      v16p.validTo
    )

  private def stringToProfileKind(v16cpk: String, v16rk: Option[String]): ChargingProfileKind = {
    import ChargingProfileKind._
    import v1x.RecurrencyKind._

    (v16cpk, v16rk) match {
      case ("Absolute", _) => Absolute
      case ("Relative", _) => Relative
      case ("Recurring", Some("Weekly")) => Recurring(Weekly)
      case ("Recurring", Some("Daily")) => Recurring(Daily)
      case _ => throw new MappingException(s"Unrecognized values ($v16cpk, $v16rk) for OCPP profile/recurrency kind")
    }
  }

  private def chargingScheduleFromV16(v16cs: ChargingSchedule): v1x.ChargingSchedule =
    v1x.ChargingSchedule(
      enumerableFromJsonString(v1x.UnitOfChargingRate, v16cs.chargingRateUnit),
      v16cs.chargingSchedulePeriod.map(periodFromV16),
      v16cs.minChargingRate.map(_.toDouble),
      v16cs.startSchedule,
      v16cs.duration.map(_.seconds)
    )

  private implicit class RichGetCompositeScheduleRes(self: v1x.GetCompositeScheduleRes) {
    def toV16: GetCompositeScheduleRes = GetCompositeScheduleRes.tupled {
      self.status match {
        case v1x.CompositeScheduleStatus.Accepted(connector, scheduleStart, chargingSchedule) =>
          ("Accepted", Some(connector.toOcpp), scheduleStart, chargingSchedule.map(_.toV16))
        case v1x.CompositeScheduleStatus.Rejected => ("Rejected", None, None, None)
      }
    }
  }

  private def compositeStatusFromV16(req: GetCompositeScheduleRes): v1x.CompositeScheduleStatus = {
    req.status match {
      case "Accepted" =>
        v1x.CompositeScheduleStatus.Accepted(
          Scope.fromOcpp(req.connectorId.getOrElse {
            throw new MappingException("Missing connector id")
          }),
          req.scheduleStart,
          req.chargingSchedule.map(chargingScheduleFromV16)
        )
      case "Rejected" =>
        v1x.CompositeScheduleStatus.Rejected
    }
  }
}
