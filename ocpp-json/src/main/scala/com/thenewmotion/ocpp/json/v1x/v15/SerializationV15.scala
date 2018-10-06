package com.thenewmotion.ocpp
package json
package v1x
package v15

import messages.{v1x => messages}
import messages.{ConnectorScope, Scope, ChargePointStatus}
import org.json4s.MappingException

import scala.concurrent.duration._

object SerializationV15 extends SerializationCommon {

  implicit val AuthorizeReqV15Variant = OcppMessageSerializer.variantFor[messages.AuthorizeReq, Version.V15.type, AuthorizeReq](
    (msg: messages.AuthorizeReq) => AuthorizeReq(msg.idTag),
    (msg: AuthorizeReq) => messages.AuthorizeReq(msg.idTag)
  )

  implicit val AuthorizeResV15Variant = OcppMessageSerializer.variantFor[messages.AuthorizeRes, Version.V15.type, AuthorizeRes](
    (msg: messages.AuthorizeRes) => AuthorizeRes(msg.idTag.toV15),
    (msg: AuthorizeRes) => messages.AuthorizeRes(msg.idTagInfo.fromV15)
  )

  implicit val DataTransferReqV15Variant = OcppMessageSerializer.variantFor[messages.CentralSystemDataTransferReq, Version.V15.type, DataTransferReq](
    (msg: messages.CentralSystemDataTransferReq) => DataTransferReq(msg.vendorId, msg.messageId, msg.data),
    (msg: DataTransferReq) => messages.CentralSystemDataTransferReq(msg.vendorId, msg.messageId, msg.data)
  )

  implicit val DataTransferResV15Variant = OcppMessageSerializer.variantFor[messages.CentralSystemDataTransferRes, Version.V15.type, DataTransferRes](
    (msg: messages.CentralSystemDataTransferRes) => DataTransferRes(msg.status.name, msg.data),
    (msg: DataTransferRes) => messages.CentralSystemDataTransferRes(
      enumerableFromJsonString(messages.DataTransferStatus, msg.status),
      msg.data
    )
  )

  implicit val StartTransactionReqV15Variant = OcppMessageSerializer.variantFor[messages.StartTransactionReq, Version.V15.type, StartTransactionReq](
    (msg: messages.StartTransactionReq) => StartTransactionReq(
      connectorId = msg.connector.toOcpp,
      idTag = msg.idTag,
      timestamp = msg.timestamp,
      meterStart = msg.meterStart,
      reservationId = msg.reservationId
    ),
    (msg: StartTransactionReq) => messages.StartTransactionReq(
      ConnectorScope.fromOcpp(msg.connectorId),
      msg.idTag,
      msg.timestamp,
      msg.meterStart,
      msg.reservationId
    )
  )

  implicit val StartTransactionResV15Variant = OcppMessageSerializer.variantFor[messages.StartTransactionRes, Version.V15.type, StartTransactionRes](
    (msg: messages.StartTransactionRes) => StartTransactionRes(msg.transactionId, msg.idTag.toV15),
    (msg: StartTransactionRes) => messages.StartTransactionRes(msg.transactionId, msg.idTagInfo.fromV15)
  )

  implicit val StopTransactionReqV15Variant = OcppMessageSerializer.variantFor[messages.StopTransactionReq, Version.V15.type, StopTransactionReq](
    (msg: messages.StopTransactionReq) => StopTransactionReq(
      transactionId = msg.transactionId,
      idTag = msg.idTag,
      timestamp = msg.timestamp,
      meterStop = msg.meterStop,
      transactionData = noneIfEmpty(msg.meters).map { meters =>
        List(TransactionData(Some(meters.map(_.toV15))))
      }
    ),
    (msg: StopTransactionReq) => messages.StopTransactionReq(
      msg.transactionId,
      msg.idTag,
      msg.timestamp,
      msg.meterStop,
      messages.StopReason.default,
      emptyIfNone(msg.transactionData).flatMap { transactionData =>
        emptyIfNone(transactionData.values).map(meterFromV15)
      }
    )
  )

  implicit val StopTransactionResV15Variant = OcppMessageSerializer.variantFor[messages.StopTransactionRes, Version.V15.type, StopTransactionRes](
    (msg: messages.StopTransactionRes) => StopTransactionRes(msg.idTag.map(_.toV15)),
    (msg: StopTransactionRes) => messages.StopTransactionRes(msg.idTagInfo.map(_.fromV15))
  )

  implicit val HeartbeatReqV15Variant = OcppMessageSerializer.variantFor[messages.HeartbeatReq.type, Version.V15.type, HeartbeatReq](
    (_: messages.HeartbeatReq.type) => HeartbeatReq(),
    (_: HeartbeatReq) => messages.HeartbeatReq
  )

  implicit val HeartbeatResV15Variant = OcppMessageSerializer.variantFor[messages.HeartbeatRes, Version.V15.type, HeartbeatRes](
    (msg: messages.HeartbeatRes) => HeartbeatRes(msg.currentTime),
    (msg: HeartbeatRes) => messages.HeartbeatRes(msg.currentTime)
  )

  implicit val MeterValuesReqV15Variant = OcppMessageSerializer.variantFor[messages.MeterValuesReq, Version.V15.type, MeterValuesReq](
    (msg: messages.MeterValuesReq) => MeterValuesReq(
      msg.scope.toOcpp,
      msg.transactionId,
      noneIfEmpty(msg.meters.map(_.toV15))
    ),
    (msg: MeterValuesReq) => messages.MeterValuesReq(
      Scope.fromOcpp(msg.connectorId),
      msg.transactionId,
      emptyIfNone(msg.values).map(meterFromV15)
    )
  )

  implicit val MeterValuesResV15Variant = OcppMessageSerializer.variantFor[messages.MeterValuesRes.type, Version.V15.type, MeterValuesRes](
    (_: messages.MeterValuesRes.type) => MeterValuesRes(),
    (_: MeterValuesRes) => messages.MeterValuesRes
  )

  implicit val BootNotificationReqV15Variant = OcppMessageSerializer.variantFor[messages.BootNotificationReq, Version.V15.type, BootNotificationReq](
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
    (msg: BootNotificationReq) =>
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

  implicit val BootNotificationResV15Variant = OcppMessageSerializer.variantFor[messages.BootNotificationRes, Version.V15.type, BootNotificationRes](
    (msg: messages.BootNotificationRes) =>
      BootNotificationRes(msg.status.name, msg.currentTime, msg.interval.toSeconds.toInt),

    (msg: BootNotificationRes) =>
      messages.BootNotificationRes(
        status = enumerableFromJsonString(messages.RegistrationStatus, msg.status),
        currentTime = msg.currentTime,
        interval = msg.heartbeatInterval.seconds
      )
  )

  implicit val StatusNotificationReqV15Variant = OcppMessageSerializer.variantFor[messages.StatusNotificationReq, Version.V15.type, StatusNotificationReq](
    (msg: messages.StatusNotificationReq) => {
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
    (msg: StatusNotificationReq) => messages.StatusNotificationReq(
      Scope.fromOcpp(msg.connectorId),
      statusFieldsToOcppStatus(msg.status, msg.errorCode, msg.info, msg.vendorErrorCode),
      msg.timestamp,
      msg.vendorId
    )
  )

  implicit val StatusNotificationResV15Variant = OcppMessageSerializer.variantFor[messages.StatusNotificationRes.type, Version.V15.type, StatusNotificationRes](
    (_: messages.StatusNotificationRes.type) => StatusNotificationRes(),
    (_: StatusNotificationRes) => messages.StatusNotificationRes
  )

  implicit val FirmwareStatusNotificationReqV15Variant = OcppMessageSerializer.variantFor[messages.FirmwareStatusNotificationReq, Version.V15.type, FirmwareStatusNotificationReq](
    (msg: messages.FirmwareStatusNotificationReq) => FirmwareStatusNotificationReq(msg.status.name),
    (msg: FirmwareStatusNotificationReq) => messages.FirmwareStatusNotificationReq(
      enumerableFromJsonString(messages.FirmwareStatus, msg.status)
    )
  )

  implicit val FirmwareStatusNotificationResV15Variant = OcppMessageSerializer.variantFor[messages.FirmwareStatusNotificationRes.type, Version.V15.type, FirmwareStatusNotificationRes](
    (_: messages.FirmwareStatusNotificationRes.type) => FirmwareStatusNotificationRes(),
    (_: FirmwareStatusNotificationRes) => messages.FirmwareStatusNotificationRes
  )

  implicit val DiagnosticsStatusNotificationReqV15Variant = OcppMessageSerializer.variantFor[messages.DiagnosticsStatusNotificationReq, Version.V15.type, DiagnosticsStatusNotificationReq](
    (msg: messages.DiagnosticsStatusNotificationReq) => DiagnosticsStatusNotificationReq(msg.status.name),
    (msg: DiagnosticsStatusNotificationReq) => messages.DiagnosticsStatusNotificationReq(
      enumerableFromJsonString(messages.DiagnosticsStatus, msg.status)
    )
  )

  implicit val DiagnosticsStatusNotificationResV15Variant = OcppMessageSerializer.variantFor[messages.DiagnosticsStatusNotificationRes.type, Version.V15.type, DiagnosticsStatusNotificationRes](
    (_: messages.DiagnosticsStatusNotificationRes.type) => DiagnosticsStatusNotificationRes(),
    (_: DiagnosticsStatusNotificationRes) => messages.DiagnosticsStatusNotificationRes
  )

  implicit val RemoteStartTransactionReqV15Variant = OcppMessageSerializer.variantFor[messages.RemoteStartTransactionReq, Version.V15.type, RemoteStartTransactionReq](
    (msg: messages.RemoteStartTransactionReq) => RemoteStartTransactionReq(
      msg.idTag,
      msg.connector.map(_.toOcpp)
    ),
    (msg: RemoteStartTransactionReq) => messages.RemoteStartTransactionReq(
      msg.idTag,
      msg.connectorId.map(ConnectorScope.fromOcpp),
      chargingProfile = None
    )
  )

  implicit val RemoteStartTransactionResV15Variant = OcppMessageSerializer.variantFor[messages.RemoteStartTransactionRes, Version.V15.type, RemoteStartTransactionRes](
    (msg: messages.RemoteStartTransactionRes) => RemoteStartTransactionRes(msg.accepted.toStatusString),
    (msg: RemoteStartTransactionRes) => messages.RemoteStartTransactionRes(statusStringToBoolean(msg.status))
  )

  implicit val RemoteStopTransactionReqV15Variant = OcppMessageSerializer.variantFor[messages.RemoteStopTransactionReq, Version.V15.type, RemoteStopTransactionReq](
    (msg: messages.RemoteStopTransactionReq) => RemoteStopTransactionReq(msg.transactionId),
    (msg: RemoteStopTransactionReq) => messages.RemoteStopTransactionReq(msg.transactionId)
  )

  implicit val RemoteStopTransactionResV15Variant = OcppMessageSerializer.variantFor[messages.RemoteStopTransactionRes, Version.V15.type, RemoteStopTransactionRes](
    (msg: messages.RemoteStopTransactionRes) => RemoteStopTransactionRes(msg.accepted.toStatusString),
    (msg: RemoteStopTransactionRes) => messages.RemoteStopTransactionRes(statusStringToBoolean(msg.status))
  )

  implicit val UnlockConnectorReqV15Variant = OcppMessageSerializer.variantFor[messages.UnlockConnectorReq, Version.V15.type, UnlockConnectorReq](
    (msg: messages.UnlockConnectorReq) => UnlockConnectorReq(msg.connector.toOcpp),
    (msg: UnlockConnectorReq) => messages.UnlockConnectorReq(ConnectorScope.fromOcpp(msg.connectorId))
  )

  implicit val UnlockConnectorResV15Variant = OcppMessageSerializer.variantFor[messages.UnlockConnectorRes, Version.V15.type, UnlockConnectorRes](
    (msg: messages.UnlockConnectorRes) => UnlockConnectorRes {
      if (msg.status == messages.UnlockStatus.Unlocked) "Accepted"
      else "Rejected"
    },
    (msg: UnlockConnectorRes) => messages.UnlockConnectorRes(
      if (msg.status == "Accepted") messages.UnlockStatus.Unlocked
      else messages.UnlockStatus.UnlockFailed
    )
  )

  implicit val GetDiagnosticsReqV15Variant = OcppMessageSerializer.variantFor[messages.GetDiagnosticsReq, Version.V15.type, GetDiagnosticsReq](
    (msg: messages.GetDiagnosticsReq) => GetDiagnosticsReq(
      msg.location.toASCIIString,
      msg.startTime,
      msg.stopTime,
      msg.retries.numberOfRetries,
      msg.retries.intervalInSeconds
    ),
    (msg: GetDiagnosticsReq) => messages.GetDiagnosticsReq(
      parseURI(msg.location),
      msg.startTime,
      msg.stopTime,
      messages.Retries.fromInts(msg.retries, msg.retryInterval)
    )
  )

  implicit val GetDiagnosticsResV15Variant = OcppMessageSerializer.variantFor[messages.GetDiagnosticsRes, Version.V15.type, GetDiagnosticsRes](
    (msg: messages.GetDiagnosticsRes) => GetDiagnosticsRes(msg.fileName),
    (msg: GetDiagnosticsRes) => messages.GetDiagnosticsRes(msg.fileName)
  )

  implicit val ChangeConfigurationReqV15Variant = OcppMessageSerializer.variantFor[messages.ChangeConfigurationReq, Version.V15.type, ChangeConfigurationReq](
    (msg: messages.ChangeConfigurationReq) => ChangeConfigurationReq(msg.key, msg.value),
    (msg: ChangeConfigurationReq) => messages.ChangeConfigurationReq(msg.key, msg.value)
  )

  implicit val ChangeConfigurationResV15Variant = OcppMessageSerializer.variantFor[messages.ChangeConfigurationRes, Version.V15.type, ChangeConfigurationRes](
    (msg: messages.ChangeConfigurationRes) => ChangeConfigurationRes(msg.status.name),
    (msg: ChangeConfigurationRes) => messages.ChangeConfigurationRes(
      enumerableFromJsonString(messages.ConfigurationStatus, msg.status)
    )
  )

  implicit val GetConfigurationReqV15Variant = OcppMessageSerializer.variantFor[messages.GetConfigurationReq, Version.V15.type, GetConfigurationReq](
    (msg: messages.GetConfigurationReq) => GetConfigurationReq(noneIfEmpty(msg.keys)),
    (msg: GetConfigurationReq) => messages.GetConfigurationReq(emptyIfNone(msg.key))
  )

  implicit val GetConfigurationResV15Variant = OcppMessageSerializer.variantFor[messages.GetConfigurationRes, Version.V15.type, GetConfigurationRes](
    (msg: messages.GetConfigurationRes) => GetConfigurationRes(
      noneIfEmpty(msg.values.map(_.toV15)),
      noneIfEmpty(msg.unknownKeys)
    ),
    (msg: GetConfigurationRes) => messages.GetConfigurationRes(
      emptyIfNone(msg.configurationKey).map(_.fromV15),
      emptyIfNone(msg.unknownKey)
    )
  )

  implicit val ChangeAvailabilityReqV15Variant = OcppMessageSerializer.variantFor[messages.ChangeAvailabilityReq, Version.V15.type, ChangeAvailabilityReq](
    (msg: messages.ChangeAvailabilityReq) => ChangeAvailabilityReq(
      msg.scope.toOcpp,
      msg.availabilityType.name
    ),
    (msg: ChangeAvailabilityReq) => messages.ChangeAvailabilityReq(
      Scope.fromOcpp(msg.connectorId),
      enumerableFromJsonString(messages.AvailabilityType, msg.`type`)
    )
  )

  implicit val ChangeAvailabilityResV15Variant = OcppMessageSerializer.variantFor[messages.ChangeAvailabilityRes, Version.V15.type, ChangeAvailabilityRes](
    (msg: messages.ChangeAvailabilityRes) => ChangeAvailabilityRes(msg.status.name),
    (msg: ChangeAvailabilityRes) => messages.ChangeAvailabilityRes(
      enumerableFromJsonString(messages.AvailabilityStatus, msg.status)
    )
  )

  implicit val ClearCacheReqV15Variant = OcppMessageSerializer.variantFor[messages.ClearCacheReq.type, Version.V15.type, ClearCacheReq](
    (_: messages.ClearCacheReq.type) => ClearCacheReq(),
    (_: ClearCacheReq) => messages.ClearCacheReq
  )

  implicit val ClearCacheResV15Variant = OcppMessageSerializer.variantFor[messages.ClearCacheRes, Version.V15.type, ClearCacheRes](
    (msg: messages.ClearCacheRes) => ClearCacheRes(msg.accepted.toStatusString),
    (msg: ClearCacheRes) => messages.ClearCacheRes(statusStringToBoolean(msg.status))
  )

  implicit val ResetReqV15Variant = OcppMessageSerializer.variantFor[messages.ResetReq, Version.V15.type, ResetReq](
    (msg: messages.ResetReq) => ResetReq(msg.resetType.name),
    (msg: ResetReq) => messages.ResetReq(enumerableFromJsonString(messages.ResetType, msg.`type`))
  )

  implicit val ResetResV15Variant = OcppMessageSerializer.variantFor[messages.ResetRes, Version.V15.type, ResetRes](
    (msg: messages.ResetRes) => ResetRes(msg.accepted.toStatusString),
    (msg: ResetRes) => messages.ResetRes(statusStringToBoolean(msg.status))
  )

  implicit val UpdateFirmwareReqV15Variant = OcppMessageSerializer.variantFor[messages.UpdateFirmwareReq, Version.V15.type, UpdateFirmwareReq](
    (msg: messages.UpdateFirmwareReq) => UpdateFirmwareReq(
      msg.retrieveDate,
      msg.location.toASCIIString,
      msg.retries.numberOfRetries,
      msg.retries.intervalInSeconds
    ),
    (msg: UpdateFirmwareReq) => messages.UpdateFirmwareReq(
      msg.retrieveDate,
      parseURI(msg.location),
      messages.Retries.fromInts(msg.retries, msg.retryInterval)
    )
  )

  implicit val UpdateFirmwareResV15Variant = OcppMessageSerializer.variantFor[messages.UpdateFirmwareRes.type, Version.V15.type, UpdateFirmwareRes](
    (_: messages.UpdateFirmwareRes.type) => UpdateFirmwareRes(),
    (_: UpdateFirmwareRes) => messages.UpdateFirmwareRes
  )

  implicit val SendLocalListReqV15Variant = OcppMessageSerializer.variantFor[messages.SendLocalListReq, Version.V15.type, SendLocalListReq](
    (msg: messages.SendLocalListReq) => SendLocalListReq(
      msg.updateType.name,
      msg.listVersion.toV15,
      Some(msg.localAuthorisationList.map(_.toV15)),
      msg.hash
    ),
    (msg: SendLocalListReq) => messages.SendLocalListReq(
      enumerableFromJsonString(messages.UpdateType, msg.updateType),
      messages.AuthListSupported(msg.listVersion),
      emptyIfNone(msg.localAuthorisationList).map(_.fromV15),
      msg.hash
    )
  )

  implicit val SendLocalListResV15Variant = OcppMessageSerializer.variantFor[messages.SendLocalListRes, Version.V15.type, SendLocalListRes](
    (msg: messages.SendLocalListRes) => SendLocalListRes.tupled(msg.status.toV15Fields),
    (msg: SendLocalListRes) => messages.SendLocalListRes(updateStatusFromV15(msg.status, msg.hash))
  )

  implicit val GetLocalListVersionReqV15Variant = OcppMessageSerializer.variantFor[messages.GetLocalListVersionReq.type, Version.V15.type, GetLocalListVersionReq](
    (_: messages.GetLocalListVersionReq.type) => GetLocalListVersionReq(),
    (_: GetLocalListVersionReq) => messages.GetLocalListVersionReq
  )

  implicit val GetLocalListVersionResV15Variant = OcppMessageSerializer.variantFor[messages.GetLocalListVersionRes, Version.V15.type, GetLocalListVersionRes](
    (msg: messages.GetLocalListVersionRes) => GetLocalListVersionRes(msg.version.toV15),
    (msg: GetLocalListVersionRes) => messages.GetLocalListVersionRes(messages.AuthListVersion(msg.listVersion))
  )

  implicit val ReserveNowReqV15Variant = OcppMessageSerializer.variantFor[messages.ReserveNowReq, Version.V15.type, ReserveNowReq](
    (msg: messages.ReserveNowReq) => ReserveNowReq(
      msg.connector.toOcpp,
      msg.expiryDate,
      msg.idTag,
      msg.parentIdTag,
      msg.reservationId
    ),
    (msg: ReserveNowReq) => messages.ReserveNowReq(
      Scope.fromOcpp(msg.connectorId),
      msg.expiryDate,
      msg.idTag,
      msg.parentIdTag,
      msg.reservationId
    )
  )

  implicit val ReserveNowResV15Variant = OcppMessageSerializer.variantFor[messages.ReserveNowRes, Version.V15.type, ReserveNowRes](
    (msg: messages.ReserveNowRes) => ReserveNowRes(msg.status.name),
    (msg: ReserveNowRes) => messages.ReserveNowRes(
      enumerableFromJsonString(messages.Reservation, msg.status)
    )
  )

  implicit val CancelReservationReqV15Variant = OcppMessageSerializer.variantFor[messages.CancelReservationReq, Version.V15.type, CancelReservationReq](
    (msg: messages.CancelReservationReq) => CancelReservationReq(msg.reservationId),
    (msg: CancelReservationReq) => messages.CancelReservationReq(msg.reservationId)
  )

  implicit val CancelReservationResV15Variant = OcppMessageSerializer.variantFor[messages.CancelReservationRes, Version.V15.type, CancelReservationRes](
    (msg: messages.CancelReservationRes) => CancelReservationRes(msg.accepted.toStatusString),
    (msg: CancelReservationRes) => messages.CancelReservationRes(statusStringToBoolean(msg.status))
  )

  private implicit class RichIdTagInfo(idTagInfo: messages.IdTagInfo) {
    def toV15: IdTagInfo = IdTagInfo(
      status = idTagInfo.status.name,
      expiryDate = idTagInfo.expiryDate,
      parentIdTag = idTagInfo.parentIdTag
    )
  }

  private implicit class RichV15IdTagInfo(self: IdTagInfo) {
    def fromV15: messages.IdTagInfo = messages.IdTagInfo(
      status = enumerableFromJsonString(messages.AuthorizationStatus, self.status),
      expiryDate = self.expiryDate,
      parentIdTag = self.parentIdTag
    )
  }

  private object RichChargePointStatus {
    val defaultErrorCode = "NoError"
  }

  private implicit class RichChargePointStatus(self: ChargePointStatus) {

    import RichChargePointStatus.defaultErrorCode

    def toV15Fields: (String, String, Option[String], Option[String]) = {
      def simpleStatus(name: String) = (name, defaultErrorCode, self.info, None)

      self match {
        case ChargePointStatus.Available(_) => simpleStatus("Available")
        case ChargePointStatus.Occupied(_, _) => simpleStatus("Occupied")
        case ChargePointStatus.Unavailable(_) => simpleStatus("Unavailable")
        case ChargePointStatus.Reserved(_) => simpleStatus("Reserved")
        case ChargePointStatus.Faulted(errCode, inf, vendorErrCode) =>
          ("Faulted", errCode.map(_.name).getOrElse(defaultErrorCode), inf, vendorErrCode)
      }
    }
  }

  private def statusFieldsToOcppStatus(status: String, errorCode: String, info: Option[String],
    vendorErrorCode: Option[String]): ChargePointStatus = {
    import RichChargePointStatus.defaultErrorCode

    status match {
      case "Available" => ChargePointStatus.Available(info)
      case "Occupied" => ChargePointStatus.Occupied(kind = None, info)
      case "Unavailable" => ChargePointStatus.Unavailable(info)
      case "Reserved" => ChargePointStatus.Reserved(info)
      case "Faulted" =>
        val errorCodeString =
          if (errorCode == defaultErrorCode)
            None
          else if (errorCode == "Mode3Error") // this got renamed in ocpp 1.6 and as we match on the protocol agnostic messages a translation needs to be made
            Some(enumerableFromJsonString(messages.ChargePointErrorCode, "EVCommunicationError"))
          else
            Some(enumerableFromJsonString(messages.ChargePointErrorCode, errorCode))
        ChargePointStatus.Faulted(errorCodeString, info, vendorErrorCode)
    }
  }

  private implicit class RichMeter(self: messages.meter.Meter) {
    def toV15: Meter = Meter(
      timestamp = self.timestamp,
      values = self.values.map(valueToV15)
    )

    def valueToV15(v: messages.meter.Value): MeterValue = {
      import messages.meter._
      MeterValue(
        value = v.value,
        measurand = noneIfDefault(Measurand, v.measurand),
        context = noneIfDefault(ReadingContext, v.context),
        format = noneIfDefault(ValueFormat, v.format),
        location = noneIfDefault(Location, v.location),
        unit = noneIfDefault(UnitOfMeasure, v.unit)
      )
    }
  }

  private def meterFromV15(v15m: Meter): messages.meter.Meter = {
    messages.meter.Meter(v15m.timestamp, v15m.values.map(meterValueFromV15))
  }

  private def meterValueFromV15(v15m: MeterValue): messages.meter.Value = {
    import messages.meter._
    import v15m._

    Value(
      value = value,
      measurand = defaultIfNone(Measurand, measurand),
      phase = None,
      context = defaultIfNone(ReadingContext, context),
      format = defaultIfNone(ValueFormat, format),
      location = defaultIfNone(Location, location),
      unit = unit.fold[UnitOfMeasure](UnitOfMeasure.Wh) { unitString =>
        UnitOfMeasure.withName(unitString) match {
          case Some(unitOfMeasure) => unitOfMeasure
          case None => unitString match {
            case "Amp" => UnitOfMeasure.Amp
            case "Volt" => UnitOfMeasure.Volt
            case _ => throw new MappingException(s"Value $unitString is not valid for UnitOfMeasure")
          }
        }
      }
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

    def toV15: ConfigurationEntry = ConfigurationEntry(key, readonly, value)
  }

  private implicit class RichConfigurationEntry(self: ConfigurationEntry) {

    import self._

    def fromV15: messages.KeyValue = messages.KeyValue(key, readonly, value)
  }

  private implicit class RichAuthListVersion(self: messages.AuthListVersion) {
    def toV15: Int = self match {
      case messages.AuthListNotSupported => -1
      case messages.AuthListSupported(i) => i
    }
  }

  private implicit class RichAuthorisationData(self: messages.AuthorisationData) {
    def toV15: AuthorisationData = {
      val v15IdTagInfo = self match {
        case messages.AuthorisationAdd(_, idTagInfo) => Some(idTagInfo.toV15)
        case messages.AuthorisationRemove(_) => None
      }

      AuthorisationData(self.idTag, v15IdTagInfo)
    }
  }

  private implicit class RichV15AuthorisationData(self: AuthorisationData) {
    def fromV15: messages.AuthorisationData = messages.AuthorisationData(
      self.idTag, self.idTagInfo.map(_.fromV15)
    )
  }

  private implicit class RichUpdateStatus(self: messages.UpdateStatus) {
    def toV15Fields: (String, Option[String]) = self match {
      case updateStatus: messages.UpdateStatusWithoutHash => (updateStatus.name, None)
      case messages.UpdateStatusWithHash.Accepted(hash) => ("Accepted", hash)
    }
  }

  private def updateStatusFromV15(status: String, hash: Option[String]): messages.UpdateStatus = {
    messages.UpdateStatusWithoutHash.withName(status) match {
      case Some(updateStatus) => updateStatus
      case None => status match {
        case "Accepted" => messages.UpdateStatusWithHash.Accepted(hash)
        case _ => throw new MappingException(s"Value $status is not valid for UpdateStatus")
      }
    }
  }
}
