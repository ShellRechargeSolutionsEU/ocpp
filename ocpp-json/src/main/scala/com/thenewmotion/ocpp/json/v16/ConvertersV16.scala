package com.thenewmotion.ocpp
package json
package v16

import org.json4s.MappingException

import scala.concurrent.duration._

object ConvertersV16 extends CommonSerialization {

  implicit val AuthorizeReqV16Variant = OcppMessageSerializer.variantFor[messages.AuthorizeReq, Version.V16.type, v16.AuthorizeReq](
    (msg: messages.AuthorizeReq) => AuthorizeReq(msg.idTag),
    (msg: AuthorizeReq) => messages.AuthorizeReq(msg.idTag)
  )

  implicit val AuthorizeResV16Variant = OcppMessageSerializer.variantFor[messages.AuthorizeRes, Version.V16.type, v16.AuthorizeRes](
    (msg: messages.AuthorizeRes) => AuthorizeRes(msg.idTag.toV16),
    (msg: AuthorizeRes) => messages.AuthorizeRes(msg.idTagInfo.fromV16)
  )

  implicit val StartTransactionReqV16Variant = OcppMessageSerializer.variantFor[messages.StartTransactionReq, Version.V16.type, v16.StartTransactionReq](
    (msg: messages.StartTransactionReq) => StartTransactionReq(
      connectorId = msg.connector.toOcpp,
      idTag = msg.idTag,
      timestamp = msg.timestamp,
      meterStart = msg.meterStart,
      reservationId = msg.reservationId
    ),
    (msg: v16.StartTransactionReq) => messages.StartTransactionReq(
      messages.ConnectorScope.fromOcpp(msg.connectorId),
      msg.idTag,
      msg.timestamp,
      msg.meterStart,
      msg.reservationId
    )
  )

  implicit val StartTransactionResV16Variant = OcppMessageSerializer.variantFor[messages.StartTransactionRes, Version.V16.type, v16.StartTransactionRes](
    (msg: messages.StartTransactionRes) => StartTransactionRes(msg.transactionId, msg.idTag.toV16),
    (msg: v16.StartTransactionRes) => messages.StartTransactionRes(msg.transactionId, msg.idTagInfo.fromV16)
  )

  implicit val StopTransactionReqV16Variant = OcppMessageSerializer.variantFor[messages.StopTransactionReq, Version.V16.type, v16.StopTransactionReq](
    (msg: messages.StopTransactionReq) => StopTransactionReq(
      transactionId = msg.transactionId,
      idTag = msg.idTag,
      timestamp = msg.timestamp,
      reason = noneIfDefault(messages.StopReason, msg.reason),
      meterStop = msg.meterStop,
      transactionData = noneIfEmpty(msg.meters.map(_.toV16))
    ),
    (msg: v16.StopTransactionReq) => messages.StopTransactionReq(
      msg.transactionId,
      msg.idTag,
      msg.timestamp,
      msg.meterStop,
      defaultIfNone(messages.StopReason, msg.reason),
      emptyIfNone(msg.transactionData).map(meterFromV16)
    )
  )

  implicit val StopTransactionResV16Variant = OcppMessageSerializer.variantFor[messages.StopTransactionRes, Version.V16.type, v16.StopTransactionRes](
    (msg: messages.StopTransactionRes) => StopTransactionRes(msg.idTag.map(_.toV16)),
    (msg: v16.StopTransactionRes) => messages.StopTransactionRes(msg.idTagInfo.map(_.fromV16))
  )

  implicit val HeartbeatReqV16Variant = OcppMessageSerializer.variantFor[messages.HeartbeatReq.type, Version.V16.type, v16.HeartbeatReq](
    (_: messages.HeartbeatReq.type) => HeartbeatReq(),
    (_: v16.HeartbeatReq) => messages.HeartbeatReq
  )

  implicit val HeartbeatResV16Variant = OcppMessageSerializer.variantFor[messages.HeartbeatRes, Version.V16.type, v16.HeartbeatRes](
    (msg: messages.HeartbeatRes) => HeartbeatRes(msg.currentTime),
    (msg: v16.HeartbeatRes) => messages.HeartbeatRes(msg.currentTime)
  )

  implicit val MeterValuesReqV16Variant = OcppMessageSerializer.variantFor[messages.MeterValuesReq, Version.V16.type, v16.MeterValuesReq](
    (msg: messages.MeterValuesReq) => MeterValuesReq(
      msg.scope.toOcpp,
      msg.transactionId,
      msg.meters.map(_.toV16)
    ),
    (msg: v16.MeterValuesReq) => messages.MeterValuesReq(
      messages.Scope.fromOcpp(msg.connectorId),
      msg.transactionId,
      msg.meterValue.map(meterFromV16)
    )
  )

  implicit val MeterValuesResV16Variant = OcppMessageSerializer.variantFor[messages.MeterValuesRes.type, Version.V16.type, v16.MeterValuesRes](
    (_: messages.MeterValuesRes.type) => MeterValuesRes(),
    (_: v16.MeterValuesRes) => messages.MeterValuesRes
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

  implicit val StatusNotificationReqV16Variant = OcppMessageSerializer.variantFor[messages.StatusNotificationReq, Version.V16.type, v16.StatusNotificationReq](
    (msg: messages.StatusNotificationReq) => {
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
    (msg: v16.StatusNotificationReq) => messages.StatusNotificationReq(
      messages.Scope.fromOcpp(msg.connectorId),
      statusFieldsToOcppStatus(msg.status, msg.errorCode, msg.info, msg.vendorErrorCode),
      msg.timestamp,
      msg.vendorId
    )
  )

  implicit val StatusNotificationResV16Variant = OcppMessageSerializer.variantFor[messages.StatusNotificationRes.type, Version.V16.type, v16.StatusNotificationRes](
    (_: messages.StatusNotificationRes.type) => StatusNotificationRes(),
    (_: v16.StatusNotificationRes) => messages.StatusNotificationRes
  )

  implicit val FirmwareStatusNotificationReqV16Variant = OcppMessageSerializer.variantFor[messages.FirmwareStatusNotificationReq, Version.V16.type, v16.FirmwareStatusNotificationReq](
    (msg: messages.FirmwareStatusNotificationReq) => FirmwareStatusNotificationReq(msg.status.name),
    (msg: v16.FirmwareStatusNotificationReq) => messages.FirmwareStatusNotificationReq(
      enumerableFromJsonString(messages.FirmwareStatus, msg.status)
    )
  )

  implicit val FirmwareStatusNotificationResV16Variant = OcppMessageSerializer.variantFor[messages.FirmwareStatusNotificationRes.type, Version.V16.type, v16.FirmwareStatusNotificationRes](
    (_: messages.FirmwareStatusNotificationRes.type) => FirmwareStatusNotificationRes(),
    (_: v16.FirmwareStatusNotificationRes) => messages.FirmwareStatusNotificationRes
  )

  implicit val DiagnosticsStatusNotificationReqV16Variant = OcppMessageSerializer.variantFor[messages.DiagnosticsStatusNotificationReq, Version.V16.type, v16.DiagnosticsStatusNotificationReq](
    (msg: messages.DiagnosticsStatusNotificationReq) => DiagnosticsStatusNotificationReq(msg.status.name),
    (msg: v16.DiagnosticsStatusNotificationReq) => messages.DiagnosticsStatusNotificationReq(
      enumerableFromJsonString(messages.DiagnosticsStatus, msg.status)
    )
  )

  implicit val DiagnosticsStatusNotificationResV16Variant = OcppMessageSerializer.variantFor[messages.DiagnosticsStatusNotificationRes.type, Version.V16.type, v16.DiagnosticsStatusNotificationRes](
    (_: messages.DiagnosticsStatusNotificationRes.type) => DiagnosticsStatusNotificationRes(),
    (_: v16.DiagnosticsStatusNotificationRes) => messages.DiagnosticsStatusNotificationRes
  )

  implicit val RemoteStartTransactionReqV16Variant = OcppMessageSerializer.variantFor[messages.RemoteStartTransactionReq, Version.V16.type, v16.RemoteStartTransactionReq](
    (msg: messages.RemoteStartTransactionReq) => RemoteStartTransactionReq(
      msg.idTag,
      msg.connector.map(_.toOcpp),
      msg.chargingProfile.map(_.toV16)
    ),
    (msg: v16.RemoteStartTransactionReq) => messages.RemoteStartTransactionReq(
      msg.idTag,
      msg.connectorId.map(messages.ConnectorScope.fromOcpp),
      msg.chargingProfile.map(chargingProfileFromV16)
    )
  )

  implicit val RemoteStartTransactionResV16Variant = OcppMessageSerializer.variantFor[messages.RemoteStartTransactionRes, Version.V16.type, v16.RemoteStartTransactionRes](
    (msg: messages.RemoteStartTransactionRes) => RemoteStartTransactionRes(msg.accepted.toStatusString),
    (msg: v16.RemoteStartTransactionRes) => messages.RemoteStartTransactionRes(statusStringToBoolean(msg.status))
  )

  implicit val RemoteStopTransactionReqV16Variant = OcppMessageSerializer.variantFor[messages.RemoteStopTransactionReq, Version.V16.type, v16.RemoteStopTransactionReq](
    (msg: messages.RemoteStopTransactionReq) => RemoteStopTransactionReq(msg.transactionId),
    (msg: v16.RemoteStopTransactionReq) => messages.RemoteStopTransactionReq(msg.transactionId)
  )

  implicit val RemoteStopTransactionResV16Variant = OcppMessageSerializer.variantFor[messages.RemoteStopTransactionRes, Version.V16.type, v16.RemoteStopTransactionRes](
    (msg: messages.RemoteStopTransactionRes) => RemoteStopTransactionRes(msg.accepted.toStatusString),
    (msg: v16.RemoteStopTransactionRes) => messages.RemoteStopTransactionRes(statusStringToBoolean(msg.status))
  )

  implicit val UnlockConnectorReqV16Variant = OcppMessageSerializer.variantFor[messages.UnlockConnectorReq, Version.V16.type, v16.UnlockConnectorReq](
    (msg: messages.UnlockConnectorReq) => UnlockConnectorReq(msg.connector.toOcpp),
    (msg: v16.UnlockConnectorReq) => messages.UnlockConnectorReq(messages.ConnectorScope.fromOcpp(msg.connectorId))
  )

  implicit val UnlockConnectorResV16Variant = OcppMessageSerializer.variantFor[messages.UnlockConnectorRes, Version.V16.type, v16.UnlockConnectorRes](
    (msg: messages.UnlockConnectorRes) => UnlockConnectorRes(msg.status.name),
    (msg: v16.UnlockConnectorRes) => messages.UnlockConnectorRes(
      enumerableFromJsonString(messages.UnlockStatus, msg.status)
    )
  )

  implicit val GetDiagnosticsReqV16Variant = OcppMessageSerializer.variantFor[messages.GetDiagnosticsReq, Version.V16.type, v16.GetDiagnosticsReq](
    (msg: messages.GetDiagnosticsReq) => GetDiagnosticsReq(
      msg.location.toASCIIString,
      msg.startTime,
      msg.stopTime,
      msg.retries.numberOfRetries,
      msg.retries.intervalInSeconds
    ),
    (msg: v16.GetDiagnosticsReq) => messages.GetDiagnosticsReq(
      parseURI(msg.location),
      msg.startTime,
      msg.stopTime,
      messages.Retries.fromInts(msg.retries, msg.retryInterval)
    )
  )

  implicit val GetDiagnosticsResV16Variant = OcppMessageSerializer.variantFor[messages.GetDiagnosticsRes, Version.V16.type, v16.GetDiagnosticsRes](
    (msg: messages.GetDiagnosticsRes) => GetDiagnosticsRes(msg.fileName),
    (msg: v16.GetDiagnosticsRes) => messages.GetDiagnosticsRes(msg.fileName)
  )

  implicit val ChangeConfigurationReqV16Variant = OcppMessageSerializer.variantFor[messages.ChangeConfigurationReq, Version.V16.type, v16.ChangeConfigurationReq](
    (msg: messages.ChangeConfigurationReq) => ChangeConfigurationReq(msg.key, msg.value),
    (msg: v16.ChangeConfigurationReq) => messages.ChangeConfigurationReq(msg.key, msg.value)
  )

  implicit val ChangeConfigurationResV16Variant = OcppMessageSerializer.variantFor[messages.ChangeConfigurationRes, Version.V16.type, v16.ChangeConfigurationRes](
    (msg: messages.ChangeConfigurationRes) => ChangeConfigurationRes(msg.status.name),
    (msg: v16.ChangeConfigurationRes) => messages.ChangeConfigurationRes(
      enumerableFromJsonString(messages.ConfigurationStatus, msg.status)
    )
  )

  implicit val GetConfigurationReqV16Variant = OcppMessageSerializer.variantFor[messages.GetConfigurationReq, Version.V16.type, v16.GetConfigurationReq](
    (msg: messages.GetConfigurationReq) => GetConfigurationReq(noneIfEmpty(msg.keys)),
    (msg: v16.GetConfigurationReq) => messages.GetConfigurationReq(emptyIfNone(msg.key))
  )

  implicit val GetConfigurationResV16Variant = OcppMessageSerializer.variantFor[messages.GetConfigurationRes, Version.V16.type, v16.GetConfigurationRes](
    (msg: messages.GetConfigurationRes) => GetConfigurationRes(
      noneIfEmpty(msg.values.map(_.toV16)),
      noneIfEmpty(msg.unknownKeys)
    ),
    (msg: v16.GetConfigurationRes) => messages.GetConfigurationRes(
      emptyIfNone(msg.configurationKey).map(_.fromV16),
      emptyIfNone(msg.unknownKey)
    )
  )

  implicit val ChangeAvailabilityReqV16Variant = OcppMessageSerializer.variantFor[messages.ChangeAvailabilityReq, Version.V16.type, v16.ChangeAvailabilityReq](
    (msg: messages.ChangeAvailabilityReq) => ChangeAvailabilityReq(
      msg.scope.toOcpp,
      msg.availabilityType.name
    ),
    (msg: v16.ChangeAvailabilityReq) => messages.ChangeAvailabilityReq(
      messages.Scope.fromOcpp(msg.connectorId),
      enumerableFromJsonString(messages.AvailabilityType, msg.`type`)
    )
  )

  implicit val ChangeAvailabilityResV16Variant = OcppMessageSerializer.variantFor[messages.ChangeAvailabilityRes, Version.V16.type, v16.ChangeAvailabilityRes](
    (msg: messages.ChangeAvailabilityRes) => ChangeAvailabilityRes(msg.status.name),
    (msg: v16.ChangeAvailabilityRes) => messages.ChangeAvailabilityRes(
      enumerableFromJsonString(messages.AvailabilityStatus, msg.status)
    )
  )

  implicit val ClearCacheReqV16Variant = OcppMessageSerializer.variantFor[messages.ClearCacheReq.type, Version.V16.type, v16.ClearCacheReq](
    (_: messages.ClearCacheReq.type) => ClearCacheReq(),
    (_: v16.ClearCacheReq) => messages.ClearCacheReq
  )

  implicit val ClearCacheResV16Variant = OcppMessageSerializer.variantFor[messages.ClearCacheRes, Version.V16.type, v16.ClearCacheRes](
    (msg: messages.ClearCacheRes) => ClearCacheRes(msg.accepted.toStatusString),
    (msg: v16.ClearCacheRes) => messages.ClearCacheRes(statusStringToBoolean(msg.status))
  )

  implicit val ResetReqV16Variant = OcppMessageSerializer.variantFor[messages.ResetReq, Version.V16.type, v16.ResetReq](
    (msg: messages.ResetReq) => ResetReq(msg.resetType.name),
    (msg: v16.ResetReq) => messages.ResetReq(enumerableFromJsonString(messages.ResetType, msg.`type`))
  )

  implicit val ResetResV16Variant = OcppMessageSerializer.variantFor[messages.ResetRes, Version.V16.type, v16.ResetRes](
    (msg: messages.ResetRes) => ResetRes(msg.accepted.toStatusString),
    (msg: v16.ResetRes) => messages.ResetRes(statusStringToBoolean(msg.status))
  )

  implicit val UpdateFirmwareReqV16Variant = OcppMessageSerializer.variantFor[messages.UpdateFirmwareReq, Version.V16.type, v16.UpdateFirmwareReq](
    (msg: messages.UpdateFirmwareReq) => UpdateFirmwareReq(
      msg.retrieveDate,
      msg.location.toASCIIString,
      msg.retries.numberOfRetries,
      msg.retries.intervalInSeconds
    ),
    (msg: v16.UpdateFirmwareReq) => messages.UpdateFirmwareReq(
      msg.retrieveDate,
      parseURI(msg.location),
      messages.Retries.fromInts(msg.retries, msg.retryInterval)
    )
  )

  implicit val UpdateFirmwareResV16Variant = OcppMessageSerializer.variantFor[messages.UpdateFirmwareRes.type, Version.V16.type, v16.UpdateFirmwareRes](
    (_: messages.UpdateFirmwareRes.type) => UpdateFirmwareRes(),
    (_: v16.UpdateFirmwareRes) => messages.UpdateFirmwareRes
  )

  implicit val SendLocalListReqV16Variant = OcppMessageSerializer.variantFor[messages.SendLocalListReq, Version.V16.type, v16.SendLocalListReq](
    (msg: messages.SendLocalListReq) => SendLocalListReq(
      msg.updateType.name,
      msg.listVersion.toV16,
      Some(msg.localAuthorisationList.map(_.toV16))
    ),
    (msg: v16.SendLocalListReq) => messages.SendLocalListReq(
      enumerableFromJsonString(messages.UpdateType, msg.updateType),
      messages.AuthListSupported(msg.listVersion),
      emptyIfNone(msg.localAuthorisationList).map(_.fromV16),
      hash = None
    )
  )

  implicit val SendLocalListResV16Variant = OcppMessageSerializer.variantFor[messages.SendLocalListRes, Version.V16.type, v16.SendLocalListRes](
    (msg: messages.SendLocalListRes) => SendLocalListRes(msg.status.toV16),
    (msg: v16.SendLocalListRes) => messages.SendLocalListRes(updateStatusFromV16(msg.status))
  )

  implicit val GetLocalListVersionReqV16Variant = OcppMessageSerializer.variantFor[messages.GetLocalListVersionReq.type, Version.V16.type, v16.GetLocalListVersionReq](
    (_: messages.GetLocalListVersionReq.type) => GetLocalListVersionReq(),
    (_: v16.GetLocalListVersionReq) => messages.GetLocalListVersionReq
  )

  implicit val GetLocalListVersionResV16Variant = OcppMessageSerializer.variantFor[messages.GetLocalListVersionRes, Version.V16.type, v16.GetLocalListVersionRes](
    (msg: messages.GetLocalListVersionRes) => GetLocalListVersionRes(msg.version.toV16),
    (msg: v16.GetLocalListVersionRes) => messages.GetLocalListVersionRes(messages.AuthListVersion(msg.listVersion))
  )

  implicit val ReserveNowReqV16Variant = OcppMessageSerializer.variantFor[messages.ReserveNowReq, Version.V16.type, v16.ReserveNowReq](
    (msg: messages.ReserveNowReq) => ReserveNowReq(
      msg.connector.toOcpp,
      msg.expiryDate,
      msg.idTag,
      msg.parentIdTag,
      msg.reservationId
    ),
    (msg: v16.ReserveNowReq) => messages.ReserveNowReq(
      messages.Scope.fromOcpp(msg.connectorId),
      msg.expiryDate,
      msg.idTag,
      msg.parentIdTag,
      msg.reservationId
    )
  )

  implicit val ReserveNowResV16Variant = OcppMessageSerializer.variantFor[messages.ReserveNowRes, Version.V16.type, v16.ReserveNowRes](
    (msg: messages.ReserveNowRes) => ReserveNowRes(msg.status.name),
    (msg: v16.ReserveNowRes) => messages.ReserveNowRes(
      enumerableFromJsonString(messages.Reservation, msg.status)
    )
  )

  implicit val CancelReservationReqV16Variant = OcppMessageSerializer.variantFor[messages.CancelReservationReq, Version.V16.type, v16.CancelReservationReq](
    (msg: messages.CancelReservationReq) => CancelReservationReq(msg.reservationId),
    (msg: v16.CancelReservationReq) => messages.CancelReservationReq(msg.reservationId)
  )

  implicit val CancelReservationResV16Variant = OcppMessageSerializer.variantFor[messages.CancelReservationRes, Version.V16.type, v16.CancelReservationRes](
    (msg: messages.CancelReservationRes) => CancelReservationRes(msg.accepted.toStatusString),
    (msg: v16.CancelReservationRes) => messages.CancelReservationRes(statusStringToBoolean(msg.status))
  )

  implicit val SetChargingProfileReqV16Variant = OcppMessageSerializer.variantFor[messages.SetChargingProfileReq, Version.V16.type, v16.SetChargingProfileReq](
    (msg: messages.SetChargingProfileReq) => SetChargingProfileReq(
      msg.connector.toOcpp,
      msg.chargingProfile.toV16
    )
    ,
    (msg: v16.SetChargingProfileReq) => messages.SetChargingProfileReq(
      messages.Scope.fromOcpp(msg.connectorId),
      chargingProfileFromV16(msg.csChargingProfiles)
    )
  )

  implicit val SetChargingProfileResV16Variant = OcppMessageSerializer.variantFor[messages.SetChargingProfileRes, Version.V16.type, v16.SetChargingProfileRes](
    (msg: messages.SetChargingProfileRes) => SetChargingProfileRes(msg.status.name),
    (msg: v16.SetChargingProfileRes) => messages.SetChargingProfileRes(
      enumerableFromJsonString(messages.ChargingProfileStatus, msg.status)
    )
  )

  implicit val ClearChargingProfileReqV16Variant = OcppMessageSerializer.variantFor[messages.ClearChargingProfileReq, Version.V16.type, v16.ClearChargingProfileReq](
    (msg: messages.ClearChargingProfileReq) => ClearChargingProfileReq(
      msg.id,
      msg.connector.map(_.toOcpp),
      msg.chargingProfilePurpose.map(_.name),
      msg.stackLevel
    ),
    (msg: v16.ClearChargingProfileReq) => messages.ClearChargingProfileReq(
      msg.id,
      msg.connectorId.map(messages.Scope.fromOcpp),
      msg.chargingProfilePurpose.map(enumerableFromJsonString(messages.ChargingProfilePurpose, _)),
      msg.stackLevel
    )
  )

  implicit val ClearChargingProfileResV16Variant = OcppMessageSerializer.variantFor[messages.ClearChargingProfileRes, Version.V16.type, v16.ClearChargingProfileRes](
    (msg: messages.ClearChargingProfileRes) => ClearChargingProfileRes(msg.status.name),
    (msg: v16.ClearChargingProfileRes) => messages.ClearChargingProfileRes(
      enumerableFromJsonString(messages.ClearChargingProfileStatus, msg.status)
    )
  )

  implicit val GetCompositeScheduleReqV16Variant = OcppMessageSerializer.variantFor[messages.GetCompositeScheduleReq, Version.V16.type, v16.GetCompositeScheduleReq](
    (msg: messages.GetCompositeScheduleReq) => GetCompositeScheduleReq(
      msg.connector.toOcpp,
      msg.duration.toSeconds.toInt,
      msg.chargingRateUnit.map(_.name)
    ),
    (msg: v16.GetCompositeScheduleReq) => messages.GetCompositeScheduleReq(
      messages.Scope.fromOcpp(msg.connectorId),
      msg.duration.seconds,
      msg.chargingRateUnit.map(enumerableFromJsonString(messages.UnitOfChargingRate, _))
    )
  )

  implicit val GetCompositeScheduleResV16Variant = OcppMessageSerializer.variantFor[messages.GetCompositeScheduleRes, Version.V16.type, v16.GetCompositeScheduleRes](
    (msg: messages.GetCompositeScheduleRes) => msg.toV16,
    (msg: v16.GetCompositeScheduleRes) => messages.GetCompositeScheduleRes(compositeStatusFromV16(msg))
  )

  implicit val TriggerMessageReqV16Variant = OcppMessageSerializer.variantFor[messages.TriggerMessageReq, Version.V16.type, v16.TriggerMessageReq](
    (msg: messages.TriggerMessageReq) => msg.toV16,
    (msg: v16.TriggerMessageReq) => triggerMessageReqFromV16(msg)

  )

  implicit val TriggerMessageResV16Variant = OcppMessageSerializer.variantFor[messages.TriggerMessageRes, Version.V16.type, v16.TriggerMessageRes](
    (msg: messages.TriggerMessageRes) => TriggerMessageRes(msg.status.name),
    (msg: v16.TriggerMessageRes) => messages.TriggerMessageRes(
      enumerableFromJsonString(messages.TriggerMessageStatus, msg.status)
    )
  )

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

    import messages.ChargePointStatus
    import messages.OccupancyKind
    import OccupancyKind._
    import RichChargePointStatus.defaultErrorCode

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

  private implicit class RichTriggerMessageReq(self: messages.TriggerMessageReq) {
    def toV16: v16.TriggerMessageReq = TriggerMessageReq.tupled {
      self.requestedMessage match {
        case messageTrigger: messages.MessageTriggerWithoutScope =>
          (messageTrigger.name, None)
        case messages.MessageTriggerWithScope.MeterValues(connectorId) =>
          ("MeterValues", connectorId.map(_.toOcpp))
        case messages.MessageTriggerWithScope.StatusNotification(connectorId) =>
          ("StatusNotification", connectorId.map(_.toOcpp))
      }
    }
  }

  private def triggerMessageReqFromV16(v16t: TriggerMessageReq): messages.TriggerMessageReq =
    messages.TriggerMessageReq {
      import messages.ConnectorScope.fromOcpp
      v16t match {
        case TriggerMessageReq(requestedMessage, connectorId) =>
          messages.MessageTriggerWithoutScope.withName(requestedMessage) match {
            case Some(messageTrigger) => messageTrigger
            case None => requestedMessage match {
              case "MeterValues" =>
                messages.MessageTriggerWithScope.MeterValues(connectorId.map(fromOcpp))
              case "StatusNotification" =>
                messages.MessageTriggerWithScope.StatusNotification(connectorId.map(fromOcpp))
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

  private def periodFromV16(v16sp: ChargingSchedulePeriod): messages.ChargingSchedulePeriod =
    messages.ChargingSchedulePeriod(v16sp.startPeriod.seconds, v16sp.limit.toDouble, v16sp.numberPhases)

  private def toOneDigitFraction(v: Double): Float = (v * 10).round.toFloat / 10

  private implicit class RichChargingProfile(cp: messages.ChargingProfile) {

    import messages.ChargingProfileKind._

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
      case ("Absolute", _) => Absolute
      case ("Relative", _) => Relative
      case ("Recurring", Some("Weekly")) => Recurring(Weekly)
      case ("Recurring", Some("Daily")) => Recurring(Daily)
      case _ => throw new MappingException(s"Unrecognized values ($v16cpk, $v16rk) for OCPP profile/recurrency kind")
    }
  }

  private def chargingScheduleFromV16(v16cs: ChargingSchedule): messages.ChargingSchedule =
    messages.ChargingSchedule(
      enumerableFromJsonString(messages.UnitOfChargingRate, v16cs.chargingRateUnit),
      v16cs.chargingSchedulePeriod.map(periodFromV16),
      v16cs.minChargingRate.map(_.toDouble),
      v16cs.startSchedule,
      v16cs.duration.map(_.seconds)
    )

  private implicit class RichGetCompositeScheduleRes(self: messages.GetCompositeScheduleRes) {
    def toV16: v16.GetCompositeScheduleRes = GetCompositeScheduleRes.tupled {
      self.status match {
        case messages.CompositeScheduleStatus.Accepted(connector, scheduleStart, chargingSchedule) =>
          ("Accepted", Some(connector.toOcpp), scheduleStart, chargingSchedule.map(_.toV16))
        case messages.CompositeScheduleStatus.Rejected => ("Rejected", None, None, None)
      }
    }
  }

  private def compositeStatusFromV16(req: GetCompositeScheduleRes): messages.CompositeScheduleStatus = {
    req.status match {
      case "Accepted" =>
        messages.CompositeScheduleStatus.Accepted(
          messages.Scope.fromOcpp(req.connectorId.getOrElse {
            throw new MappingException("Missing connector id")
          }),
          req.scheduleStart,
          req.chargingSchedule.map(chargingScheduleFromV16)
        )
      case "Rejected" =>
        messages.CompositeScheduleStatus.Rejected
    }
  }
}
