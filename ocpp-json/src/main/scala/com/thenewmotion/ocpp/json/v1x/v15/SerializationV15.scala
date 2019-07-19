package com.thenewmotion.ocpp
package json
package v1x
package v15

import messages.v1x
import v1x.{ConnectorScope, Scope, ChargePointStatus}
import org.json4s.MappingException

import scala.concurrent.duration._

object SerializationV15 extends SerializationCommon {

  implicit val AuthorizeReqV15Variant = OcppMessageSerializer.variantFor[v1x.AuthorizeReq, Version.V15.type, AuthorizeReq](
    (msg: v1x.AuthorizeReq) => AuthorizeReq(msg.idTag),
    (msg: AuthorizeReq) => v1x.AuthorizeReq(msg.idTag)
  )

  implicit val AuthorizeResV15Variant = OcppMessageSerializer.variantFor[v1x.AuthorizeRes, Version.V15.type, AuthorizeRes](
    (msg: v1x.AuthorizeRes) => AuthorizeRes(msg.idTag.toV15),
    (msg: AuthorizeRes) => v1x.AuthorizeRes(msg.idTagInfo.fromV15)
  )

  implicit val DataTransferReqV15Variant = OcppMessageSerializer.variantFor[v1x.CentralSystemDataTransferReq, Version.V15.type, DataTransferReq](
    (msg: v1x.CentralSystemDataTransferReq) => DataTransferReq(msg.vendorId, msg.messageId, msg.data),
    (msg: DataTransferReq) => v1x.CentralSystemDataTransferReq(msg.vendorId, msg.messageId, msg.data)
  )

  implicit val DataTransferResV15Variant = OcppMessageSerializer.variantFor[v1x.CentralSystemDataTransferRes, Version.V15.type, DataTransferRes](
    (msg: v1x.CentralSystemDataTransferRes) => DataTransferRes(msg.status.name, msg.data),
    (msg: DataTransferRes) => v1x.CentralSystemDataTransferRes(
      enumerableFromJsonString(v1x.DataTransferStatus, msg.status),
      msg.data
    )
  )

  implicit val StartTransactionReqV15Variant = OcppMessageSerializer.variantFor[v1x.StartTransactionReq, Version.V15.type, StartTransactionReq](
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

  implicit val StartTransactionResV15Variant = OcppMessageSerializer.variantFor[v1x.StartTransactionRes, Version.V15.type, StartTransactionRes](
    (msg: v1x.StartTransactionRes) => StartTransactionRes(msg.transactionId, msg.idTag.toV15),
    (msg: StartTransactionRes) => v1x.StartTransactionRes(msg.transactionId, msg.idTagInfo.fromV15)
  )

  implicit val StopTransactionReqV15Variant = OcppMessageSerializer.variantFor[v1x.StopTransactionReq, Version.V15.type, StopTransactionReq](
    (msg: v1x.StopTransactionReq) => StopTransactionReq(
      transactionId = msg.transactionId,
      idTag = msg.idTag,
      timestamp = msg.timestamp,
      meterStop = msg.meterStop,
      transactionData = noneIfEmpty(msg.meters).map { meters =>
        List(TransactionData(Some(meters.map(_.toV15))))
      }
    ),
    (msg: StopTransactionReq) => v1x.StopTransactionReq(
      msg.transactionId,
      msg.idTag,
      msg.timestamp,
      msg.meterStop,
      v1x.StopReason.default,
      emptyIfNone(msg.transactionData).flatMap { transactionData =>
        emptyIfNone(transactionData.values).map(meterFromV15)
      }
    )
  )

  implicit val StopTransactionResV15Variant = OcppMessageSerializer.variantFor[v1x.StopTransactionRes, Version.V15.type, StopTransactionRes](
    (msg: v1x.StopTransactionRes) => StopTransactionRes(msg.idTag.map(_.toV15)),
    (msg: StopTransactionRes) => v1x.StopTransactionRes(msg.idTagInfo.map(_.fromV15))
  )

  implicit val HeartbeatReqV15Variant = OcppMessageSerializer.variantFor[v1x.HeartbeatReq.type, Version.V15.type, HeartbeatReq](
    (_: v1x.HeartbeatReq.type) => HeartbeatReq(),
    (_: HeartbeatReq) => v1x.HeartbeatReq
  )

  implicit val HeartbeatResV15Variant = OcppMessageSerializer.variantFor[v1x.HeartbeatRes, Version.V15.type, HeartbeatRes](
    (msg: v1x.HeartbeatRes) => HeartbeatRes(msg.currentTime),
    (msg: HeartbeatRes) => v1x.HeartbeatRes(msg.currentTime)
  )

  implicit val MeterValuesReqV15Variant = OcppMessageSerializer.variantFor[v1x.MeterValuesReq, Version.V15.type, MeterValuesReq](
    (msg: v1x.MeterValuesReq) => MeterValuesReq(
      msg.scope.toOcpp,
      msg.transactionId,
      noneIfEmpty(msg.meters.map(_.toV15))
    ),
    (msg: MeterValuesReq) => v1x.MeterValuesReq(
      Scope.fromOcpp(msg.connectorId),
      msg.transactionId,
      emptyIfNone(msg.values).map(meterFromV15)
    )
  )

  implicit val MeterValuesResV15Variant = OcppMessageSerializer.variantFor[v1x.MeterValuesRes.type, Version.V15.type, MeterValuesRes](
    (_: v1x.MeterValuesRes.type) => MeterValuesRes(),
    (_: MeterValuesRes) => v1x.MeterValuesRes
  )

  implicit val BootNotificationReqV15Variant = OcppMessageSerializer.variantFor[v1x.BootNotificationReq, Version.V15.type, BootNotificationReq](
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

  implicit val BootNotificationResV15Variant = OcppMessageSerializer.variantFor[v1x.BootNotificationRes, Version.V15.type, BootNotificationRes](
    (msg: v1x.BootNotificationRes) =>
      BootNotificationRes(msg.status.name, msg.currentTime, msg.interval.toSeconds.toInt),

    (msg: BootNotificationRes) =>
      v1x.BootNotificationRes(
        status = enumerableFromJsonString(v1x.RegistrationStatus, msg.status),
        currentTime = msg.currentTime,
        interval = msg.heartbeatInterval.seconds
      )
  )

  implicit val StatusNotificationReqV15Variant = OcppMessageSerializer.variantFor[v1x.StatusNotificationReq, Version.V15.type, StatusNotificationReq](
    (msg: v1x.StatusNotificationReq) => {
      val (ocppStatus, errorCode, info, vendorErrorCode) = msg.status.toV15Fields
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

  implicit val StatusNotificationResV15Variant = OcppMessageSerializer.variantFor[v1x.StatusNotificationRes.type, Version.V15.type, StatusNotificationRes](
    (_: v1x.StatusNotificationRes.type) => StatusNotificationRes(),
    (_: StatusNotificationRes) => v1x.StatusNotificationRes
  )

  implicit val FirmwareStatusNotificationReqV15Variant = OcppMessageSerializer.variantFor[v1x.FirmwareStatusNotificationReq, Version.V15.type, FirmwareStatusNotificationReq](
    (msg: v1x.FirmwareStatusNotificationReq) => FirmwareStatusNotificationReq(msg.status.name),
    (msg: FirmwareStatusNotificationReq) => v1x.FirmwareStatusNotificationReq(
      enumerableFromJsonString(v1x.FirmwareStatus, msg.status)
    )
  )

  implicit val FirmwareStatusNotificationResV15Variant = OcppMessageSerializer.variantFor[v1x.FirmwareStatusNotificationRes.type, Version.V15.type, FirmwareStatusNotificationRes](
    (_: v1x.FirmwareStatusNotificationRes.type) => FirmwareStatusNotificationRes(),
    (_: FirmwareStatusNotificationRes) => v1x.FirmwareStatusNotificationRes
  )

  implicit val DiagnosticsStatusNotificationReqV15Variant = OcppMessageSerializer.variantFor[v1x.DiagnosticsStatusNotificationReq, Version.V15.type, DiagnosticsStatusNotificationReq](
    (msg: v1x.DiagnosticsStatusNotificationReq) => DiagnosticsStatusNotificationReq(msg.status.name),
    (msg: DiagnosticsStatusNotificationReq) => v1x.DiagnosticsStatusNotificationReq(
      enumerableFromJsonString(v1x.DiagnosticsStatus, msg.status)
    )
  )

  implicit val DiagnosticsStatusNotificationResV15Variant = OcppMessageSerializer.variantFor[v1x.DiagnosticsStatusNotificationRes.type, Version.V15.type, DiagnosticsStatusNotificationRes](
    (_: v1x.DiagnosticsStatusNotificationRes.type) => DiagnosticsStatusNotificationRes(),
    (_: DiagnosticsStatusNotificationRes) => v1x.DiagnosticsStatusNotificationRes
  )

  implicit val RemoteStartTransactionReqV15Variant = OcppMessageSerializer.variantFor[v1x.RemoteStartTransactionReq, Version.V15.type, RemoteStartTransactionReq](
    (msg: v1x.RemoteStartTransactionReq) => RemoteStartTransactionReq(
      msg.idTag,
      msg.connector.map(_.toOcpp)
    ),
    (msg: RemoteStartTransactionReq) => v1x.RemoteStartTransactionReq(
      msg.idTag,
      msg.connectorId.map(ConnectorScope.fromOcpp),
      chargingProfile = None
    )
  )

  implicit val RemoteStartTransactionResV15Variant = OcppMessageSerializer.variantFor[v1x.RemoteStartTransactionRes, Version.V15.type, RemoteStartTransactionRes](
    (msg: v1x.RemoteStartTransactionRes) => RemoteStartTransactionRes(msg.accepted.toStatusString),
    (msg: RemoteStartTransactionRes) => v1x.RemoteStartTransactionRes(statusStringToBoolean(msg.status))
  )

  implicit val RemoteStopTransactionReqV15Variant = OcppMessageSerializer.variantFor[v1x.RemoteStopTransactionReq, Version.V15.type, RemoteStopTransactionReq](
    (msg: v1x.RemoteStopTransactionReq) => RemoteStopTransactionReq(msg.transactionId),
    (msg: RemoteStopTransactionReq) => v1x.RemoteStopTransactionReq(msg.transactionId)
  )

  implicit val RemoteStopTransactionResV15Variant = OcppMessageSerializer.variantFor[v1x.RemoteStopTransactionRes, Version.V15.type, RemoteStopTransactionRes](
    (msg: v1x.RemoteStopTransactionRes) => RemoteStopTransactionRes(msg.accepted.toStatusString),
    (msg: RemoteStopTransactionRes) => v1x.RemoteStopTransactionRes(statusStringToBoolean(msg.status))
  )

  implicit val UnlockConnectorReqV15Variant = OcppMessageSerializer.variantFor[v1x.UnlockConnectorReq, Version.V15.type, UnlockConnectorReq](
    (msg: v1x.UnlockConnectorReq) => UnlockConnectorReq(msg.connector.toOcpp),
    (msg: UnlockConnectorReq) => v1x.UnlockConnectorReq(ConnectorScope.fromOcpp(msg.connectorId))
  )

  implicit val UnlockConnectorResV15Variant = OcppMessageSerializer.variantFor[v1x.UnlockConnectorRes, Version.V15.type, UnlockConnectorRes](
    (msg: v1x.UnlockConnectorRes) => UnlockConnectorRes {
      if (msg.status == v1x.UnlockStatus.Unlocked) "Accepted"
      else "Rejected"
    },
    (msg: UnlockConnectorRes) => v1x.UnlockConnectorRes(
      if (msg.status == "Accepted") v1x.UnlockStatus.Unlocked
      else v1x.UnlockStatus.UnlockFailed
    )
  )

  implicit val GetDiagnosticsReqV15Variant = OcppMessageSerializer.variantFor[v1x.GetDiagnosticsReq, Version.V15.type, GetDiagnosticsReq](
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

  implicit val GetDiagnosticsResV15Variant = OcppMessageSerializer.variantFor[v1x.GetDiagnosticsRes, Version.V15.type, GetDiagnosticsRes](
    (msg: v1x.GetDiagnosticsRes) => GetDiagnosticsRes(msg.fileName),
    (msg: GetDiagnosticsRes) => v1x.GetDiagnosticsRes(msg.fileName)
  )

  implicit val ChangeConfigurationReqV15Variant = OcppMessageSerializer.variantFor[v1x.ChangeConfigurationReq, Version.V15.type, ChangeConfigurationReq](
    (msg: v1x.ChangeConfigurationReq) => ChangeConfigurationReq(msg.key, msg.value),
    (msg: ChangeConfigurationReq) => v1x.ChangeConfigurationReq(msg.key, msg.value)
  )

  implicit val ChangeConfigurationResV15Variant = OcppMessageSerializer.variantFor[v1x.ChangeConfigurationRes, Version.V15.type, ChangeConfigurationRes](
    (msg: v1x.ChangeConfigurationRes) => ChangeConfigurationRes(msg.status.name),
    (msg: ChangeConfigurationRes) => v1x.ChangeConfigurationRes(
      enumerableFromJsonString(v1x.ConfigurationStatus, msg.status)
    )
  )

  implicit val GetConfigurationReqV15Variant = OcppMessageSerializer.variantFor[v1x.GetConfigurationReq, Version.V15.type, GetConfigurationReq](
    (msg: v1x.GetConfigurationReq) => GetConfigurationReq(noneIfEmpty(msg.keys)),
    (msg: GetConfigurationReq) => v1x.GetConfigurationReq(emptyIfNone(msg.key))
  )

  implicit val GetConfigurationResV15Variant = OcppMessageSerializer.variantFor[v1x.GetConfigurationRes, Version.V15.type, GetConfigurationRes](
    (msg: v1x.GetConfigurationRes) => GetConfigurationRes(
      noneIfEmpty(msg.values.map(_.toV15)),
      noneIfEmpty(msg.unknownKeys)
    ),
    (msg: GetConfigurationRes) => v1x.GetConfigurationRes(
      emptyIfNone(msg.configurationKey).map(_.fromV15),
      emptyIfNone(msg.unknownKey)
    )
  )

  implicit val ChangeAvailabilityReqV15Variant = OcppMessageSerializer.variantFor[v1x.ChangeAvailabilityReq, Version.V15.type, ChangeAvailabilityReq](
    (msg: v1x.ChangeAvailabilityReq) => ChangeAvailabilityReq(
      msg.scope.toOcpp,
      msg.availabilityType.name
    ),
    (msg: ChangeAvailabilityReq) => v1x.ChangeAvailabilityReq(
      Scope.fromOcpp(msg.connectorId),
      enumerableFromJsonString(v1x.AvailabilityType, msg.`type`)
    )
  )

  implicit val ChangeAvailabilityResV15Variant = OcppMessageSerializer.variantFor[v1x.ChangeAvailabilityRes, Version.V15.type, ChangeAvailabilityRes](
    (msg: v1x.ChangeAvailabilityRes) => ChangeAvailabilityRes(msg.status.name),
    (msg: ChangeAvailabilityRes) => v1x.ChangeAvailabilityRes(
      enumerableFromJsonString(v1x.AvailabilityStatus, msg.status)
    )
  )

  implicit val ChargePointDataTransferReqV15Variant = OcppMessageSerializer.variantFor[v1x.ChargePointDataTransferReq, Version.V15.type, DataTransferReq](
    (msg: v1x.ChargePointDataTransferReq) => DataTransferReq(msg.vendorId, msg.messageId, msg.data),
    (msg: DataTransferReq) => v1x.ChargePointDataTransferReq(msg.vendorId, msg.messageId, msg.data)
  )

  implicit val ChargePointDataTransferResV15Variant = OcppMessageSerializer.variantFor[v1x.ChargePointDataTransferRes, Version.V15.type, DataTransferRes](
    (msg: v1x.ChargePointDataTransferRes) => DataTransferRes(msg.status.name, msg.data),
    (msg: DataTransferRes) => v1x.ChargePointDataTransferRes(
      enumerableFromJsonString(v1x.DataTransferStatus, msg.status),
      msg.data
    )
  )

  implicit val ClearCacheReqV15Variant = OcppMessageSerializer.variantFor[v1x.ClearCacheReq.type, Version.V15.type, ClearCacheReq](
    (_: v1x.ClearCacheReq.type) => ClearCacheReq(),
    (_: ClearCacheReq) => v1x.ClearCacheReq
  )

  implicit val ClearCacheResV15Variant = OcppMessageSerializer.variantFor[v1x.ClearCacheRes, Version.V15.type, ClearCacheRes](
    (msg: v1x.ClearCacheRes) => ClearCacheRes(msg.accepted.toStatusString),
    (msg: ClearCacheRes) => v1x.ClearCacheRes(statusStringToBoolean(msg.status))
  )

  implicit val ResetReqV15Variant = OcppMessageSerializer.variantFor[v1x.ResetReq, Version.V15.type, ResetReq](
    (msg: v1x.ResetReq) => ResetReq(msg.resetType.name),
    (msg: ResetReq) => v1x.ResetReq(enumerableFromJsonString(v1x.ResetType, msg.`type`))
  )

  implicit val ResetResV15Variant = OcppMessageSerializer.variantFor[v1x.ResetRes, Version.V15.type, ResetRes](
    (msg: v1x.ResetRes) => ResetRes(msg.accepted.toStatusString),
    (msg: ResetRes) => v1x.ResetRes(statusStringToBoolean(msg.status))
  )

  implicit val UpdateFirmwareReqV15Variant = OcppMessageSerializer.variantFor[v1x.UpdateFirmwareReq, Version.V15.type, UpdateFirmwareReq](
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

  implicit val UpdateFirmwareResV15Variant = OcppMessageSerializer.variantFor[v1x.UpdateFirmwareRes.type, Version.V15.type, UpdateFirmwareRes](
    (_: v1x.UpdateFirmwareRes.type) => UpdateFirmwareRes(),
    (_: UpdateFirmwareRes) => v1x.UpdateFirmwareRes
  )

  implicit val SendLocalListReqV15Variant = OcppMessageSerializer.variantFor[v1x.SendLocalListReq, Version.V15.type, SendLocalListReq](
    (msg: v1x.SendLocalListReq) => SendLocalListReq(
      msg.updateType.name,
      msg.listVersion.toV15,
      Some(msg.localAuthorisationList.map(_.toV15)),
      msg.hash
    ),
    (msg: SendLocalListReq) => v1x.SendLocalListReq(
      enumerableFromJsonString(v1x.UpdateType, msg.updateType),
      v1x.AuthListSupported(msg.listVersion),
      emptyIfNone(msg.localAuthorisationList).map(_.fromV15),
      msg.hash
    )
  )

  implicit val SendLocalListResV15Variant = OcppMessageSerializer.variantFor[v1x.SendLocalListRes, Version.V15.type, SendLocalListRes](
    (msg: v1x.SendLocalListRes) => SendLocalListRes.tupled(msg.status.toV15Fields),
    (msg: SendLocalListRes) => v1x.SendLocalListRes(updateStatusFromV15(msg.status, msg.hash))
  )

  implicit val GetLocalListVersionReqV15Variant = OcppMessageSerializer.variantFor[v1x.GetLocalListVersionReq.type, Version.V15.type, GetLocalListVersionReq](
    (_: v1x.GetLocalListVersionReq.type) => GetLocalListVersionReq(),
    (_: GetLocalListVersionReq) => v1x.GetLocalListVersionReq
  )

  implicit val GetLocalListVersionResV15Variant = OcppMessageSerializer.variantFor[v1x.GetLocalListVersionRes, Version.V15.type, GetLocalListVersionRes](
    (msg: v1x.GetLocalListVersionRes) => GetLocalListVersionRes(msg.version.toV15),
    (msg: GetLocalListVersionRes) => v1x.GetLocalListVersionRes(v1x.AuthListVersion(msg.listVersion))
  )

  implicit val ReserveNowReqV15Variant = OcppMessageSerializer.variantFor[v1x.ReserveNowReq, Version.V15.type, ReserveNowReq](
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

  implicit val ReserveNowResV15Variant = OcppMessageSerializer.variantFor[v1x.ReserveNowRes, Version.V15.type, ReserveNowRes](
    (msg: v1x.ReserveNowRes) => ReserveNowRes(msg.status.name),
    (msg: ReserveNowRes) => v1x.ReserveNowRes(
      enumerableFromJsonString(v1x.Reservation, msg.status)
    )
  )

  implicit val CancelReservationReqV15Variant = OcppMessageSerializer.variantFor[v1x.CancelReservationReq, Version.V15.type, CancelReservationReq](
    (msg: v1x.CancelReservationReq) => CancelReservationReq(msg.reservationId),
    (msg: CancelReservationReq) => v1x.CancelReservationReq(msg.reservationId)
  )

  implicit val CancelReservationResV15Variant = OcppMessageSerializer.variantFor[v1x.CancelReservationRes, Version.V15.type, CancelReservationRes](
    (msg: v1x.CancelReservationRes) => CancelReservationRes(msg.accepted.toStatusString),
    (msg: CancelReservationRes) => v1x.CancelReservationRes(statusStringToBoolean(msg.status))
  )

  private implicit class RichIdTagInfo(idTagInfo: v1x.IdTagInfo) {
    def toV15: IdTagInfo = IdTagInfo(
      status = idTagInfo.status.name,
      expiryDate = idTagInfo.expiryDate,
      parentIdTag = idTagInfo.parentIdTag
    )
  }

  private implicit class RichV15IdTagInfo(self: IdTagInfo) {
    def fromV15: v1x.IdTagInfo = v1x.IdTagInfo(
      status = enumerableFromJsonString(v1x.AuthorizationStatus, self.status),
      expiryDate = self.expiryDate,
      parentIdTag = self.parentIdTag
    )
  }

  private object RichChargePointStatus {
    val defaultErrorCode = "NoError"
  }

  private implicit class RichChargePointStatus(self: ChargePointStatus) {

    def toV15Fields: (String, String, Option[String], Option[String]) = {
      def simpleStatus(name: String) =
        (name, RichChargePointStatus.defaultErrorCode, self.info, None)

      self match {
        case ChargePointStatus.Available(_) => simpleStatus("Available")
        case ChargePointStatus.Occupied(_, _) => simpleStatus("Occupied")
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
      case "Occupied" => ChargePointStatus.Occupied(kind = None, info)
      case "Unavailable" => ChargePointStatus.Unavailable(info)
      case "Reserved" => ChargePointStatus.Reserved(info)
      case "Faulted" =>
        val errorCodeString = errorCode match {
          case RichChargePointStatus.`defaultErrorCode` =>
            None
          case "Mode3Error" =>
            // this got renamed in ocpp 1.6 and as we match on the protocol agnostic messages a translation needs to be made
            Some(enumerableFromJsonString (v1x.ChargePointErrorCode, "EVCommunicationError"))
          case _ =>
            Some(enumerableFromJsonString (v1x.ChargePointErrorCode, errorCode))
        }

        ChargePointStatus.Faulted(errorCodeString, info, vendorErrorCode)
    }
  }

  private implicit class RichMeter(self: v1x.meter.Meter) {
    def toV15: Meter = Meter(
      timestamp = self.timestamp,
      values = self.values.map(valueToV15)
    )

    def valueToV15(v: v1x.meter.Value): MeterValue = {
      MeterValue(
        value = v.value,
        measurand = noneIfDefault(v1x.meter.Measurand, v.measurand),
        context = noneIfDefault(v1x.meter.ReadingContext, v.context),
        format = noneIfDefault(v1x.meter.ValueFormat, v.format),
        location = noneIfDefault(v1x.meter.Location, v.location),
        unit = noneIfDefault(v1x.meter.UnitOfMeasure, v.unit)
      )
    }
  }

  private def meterFromV15(v15m: Meter): v1x.meter.Meter = {
    v1x.meter.Meter(v15m.timestamp, v15m.values.map(meterValueFromV15))
  }

  private def meterValueFromV15(v15m: MeterValue): v1x.meter.Value =
    v1x.meter.Value(
      value = v15m.value,
      measurand = defaultIfNone(v1x.meter.Measurand, v15m.measurand),
      phase = None,
      context = defaultIfNone(v1x.meter.ReadingContext, v15m.context),
      format = defaultIfNone(v1x.meter.ValueFormat, v15m.format),
      location = defaultIfNone(v1x.meter.Location, v15m.location),
      unit = v15m.unit.fold[v1x.meter.UnitOfMeasure](v1x.meter.UnitOfMeasure.Wh) { unitString =>
        v1x.meter.UnitOfMeasure.withName(unitString) match {
          case Some(unitOfMeasure) => unitOfMeasure
          case None => unitString match {
            case "Amp" => v1x.meter.UnitOfMeasure.Amp
            case "Volt" => v1x.meter.UnitOfMeasure.Volt
            case _ => throw new MappingException(s"Value $unitString is not valid for UnitOfMeasure")
          }
        }
      }
    )

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

    def toV15: ConfigurationEntry = ConfigurationEntry(self.key, self.readonly, self.value)
  }

  private implicit class RichConfigurationEntry(self: ConfigurationEntry) {

    def fromV15: v1x.KeyValue = v1x.KeyValue(self.key, self.readonly, self.value)
  }

  private implicit class RichAuthListVersion(self: v1x.AuthListVersion) {
    def toV15: Int = self match {
      case v1x.AuthListNotSupported => -1
      case v1x.AuthListSupported(i) => i
    }
  }

  private implicit class RichAuthorisationData(self: v1x.AuthorisationData) {
    def toV15: AuthorisationData = {
      val v15IdTagInfo = self match {
        case v1x.AuthorisationAdd(_, idTagInfo) => Some(idTagInfo.toV15)
        case v1x.AuthorisationRemove(_) => None
      }

      AuthorisationData(self.idTag, v15IdTagInfo)
    }
  }

  private implicit class RichV15AuthorisationData(self: AuthorisationData) {
    def fromV15: v1x.AuthorisationData = v1x.AuthorisationData(
      self.idTag, self.idTagInfo.map(_.fromV15)
    )
  }

  private implicit class RichUpdateStatus(self: v1x.UpdateStatus) {
    def toV15Fields: (String, Option[String]) = self match {
      case updateStatus: v1x.UpdateStatusWithoutHash => (updateStatus.name, None)
      case v1x.UpdateStatusWithHash.Accepted(hash) => ("Accepted", hash)
    }
  }

  private def updateStatusFromV15(status: String, hash: Option[String]): v1x.UpdateStatus = {
    v1x.UpdateStatusWithoutHash.withName(status) match {
      case Some(updateStatus) => updateStatus
      case None => status match {
        case "Accepted" => v1x.UpdateStatusWithHash.Accepted(hash)
        case _ => throw new MappingException(s"Value $status is not valid for UpdateStatus")
      }
    }
  }
}
