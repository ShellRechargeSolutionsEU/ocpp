package com.thenewmotion.ocpp
package json.v16

import java.net.{URI, URISyntaxException}

import enums.reflection.EnumUtils.{Nameable, Enumerable}
import messages.Meter._
import org.json4s.MappingException

import scala.concurrent.duration._

object ConvertersV16 {
  def toV16(msg: messages.Message): Message = msg match {
    case messages.BootNotificationReq(chargePointVendor, chargePointModel, chargePointSerialNumber,
    chargeBoxSerialNumber, firmwareVersion, iccid, imsi, meterType,
    meterSerialNumber) =>
      BootNotificationReq(chargePointVendor, chargePointModel, chargePointSerialNumber, chargeBoxSerialNumber,
        firmwareVersion, iccid, imsi, meterType, meterSerialNumber)

    case messages.BootNotificationRes(registrationAccepted, currentTime, interval) =>
      com.thenewmotion.ocpp.json.v16.BootNotificationRes(registrationAccepted.toStatusString, currentTime, interval.toSeconds.toInt)

    case messages.AuthorizeReq(idTag) => AuthorizeReq(idTag)

    case messages.AuthorizeRes(idTag) => AuthorizeRes(idTag.toV16)

    case messages.StartTransactionReq(connector, idTag, timestamp, meterStart, reservationId) =>
      StartTransactionReq(connectorId = connector.toOcpp,
        idTag = idTag,
        timestamp = timestamp,
        meterStart = meterStart,
        reservationId = reservationId)

    case messages.StartTransactionRes(transactionId, idTagInfo) => StartTransactionRes(transactionId, idTagInfo.toV16)

    case messages.StopTransactionReq(transactionId, idTag, timestamp, meterStop, transactionData) =>
      StopTransactionReq(transactionId = transactionId,
        idTag = idTag,
        timestamp = timestamp,
        reason = "",
        meterStop = meterStop,
        transactionData = Some(transactionDataToV16(transactionData)))

    case messages.StopTransactionRes(idTagInfo) => StopTransactionRes(idTagInfo.map(_.toV16))

    case messages.UnlockConnectorReq(scope) => UnlockConnectorReq(scope.toOcpp)

    case messages.UnlockConnectorRes(accepted) => UnlockConnectorRes(accepted.toStatusString)

    case messages.ResetReq(resetType) => ResetReq(resetType.toString)

    case messages.ResetRes(accepted) => ResetRes(accepted.toStatusString)

    case messages.ChangeAvailabilityReq(scope, availabilityType) =>
      ChangeAvailabilityReq(connectorId = scope.toOcpp, `type` = availabilityType.toString)

    case messages.ChangeAvailabilityRes(status) => ChangeAvailabilityRes(status.toString)

    case messages.StatusNotificationReq(scope, status, timestamp, vendorId) =>

      val (ocppStatus, errorCode, info, vendorErrorCode) = status.toV16Fields

      StatusNotificationReq(scope.toOcpp, ocppStatus, errorCode, info, timestamp, vendorId, vendorErrorCode)

    case messages.StatusNotificationRes => StatusNotificationRes()

    //    case messages.RemoteStartTransactionReq(idTag, connector, chargingProfile) =>
    //      RemoteStartTransactionReq(idTag, connector.map(_.toOcpp), chargingProfile.map(_.toV16))

    case messages.RemoteStartTransactionRes(accepted) => RemoteStartTransactionRes(accepted.toStatusString)

    case messages.RemoteStopTransactionReq(transactionId) => RemoteStopTransactionReq(transactionId)

    case messages.RemoteStopTransactionRes(accepted) => RemoteStopTransactionRes(accepted.toStatusString)

    case messages.HeartbeatReq => HeartbeatReq()

    case messages.HeartbeatRes(currentTime) => HeartbeatRes(currentTime)

    case messages.UpdateFirmwareReq(retrieveDate, location, retries) =>
      UpdateFirmwareReq(retrieveDate, location.toASCIIString, retries.numberOfRetries, retries.intervalInSeconds)

    case messages.UpdateFirmwareRes => UpdateFirmwareRes()

    case messages.FirmwareStatusNotificationReq(status) => FirmwareStatusNotificationReq(status.toString)

    case messages.FirmwareStatusNotificationRes => FirmwareStatusNotificationRes()

    case messages.GetDiagnosticsReq(location, startTime, stopTime, retries) =>
      GetDiagnosticsReq(location.toASCIIString, startTime, stopTime, retries.numberOfRetries, retries.intervalInSeconds)

    case messages.GetDiagnosticsRes(filename) => GetDiagnosticsRes(filename)

    case messages.DiagnosticsStatusNotificationReq(uploaded) =>
      DiagnosticsStatusNotificationReq(uploaded.toUploadStatusString)

    case messages.DiagnosticsStatusNotificationRes =>
      DiagnosticsStatusNotificationRes()

    case messages.MeterValuesReq(scope, transactionId, meters) =>
      MeterValuesReq(scope.toOcpp, transactionId, meters.map(_.toV16))

    case messages.MeterValuesRes => MeterValuesRes()

    case messages.ChangeConfigurationReq(key, value) => ChangeConfigurationReq(key, value)

    case messages.ChangeConfigurationRes(status) => ChangeConfigurationRes(status.toString)

    case messages.ClearCacheReq => ClearCacheReq()

    case messages.ClearCacheRes(accepted) => ClearCacheRes(accepted.toStatusString)

    case messages.GetConfigurationReq(keys) => GetConfigurationReq(Some(keys))

    case messages.GetConfigurationRes(values, unknownKeys) =>
      GetConfigurationRes(configurationKey = Some(values.map(_.toV16)), unknownKey = Some(unknownKeys))

    case messages.GetLocalListVersionReq => GetLocalListVersionReq()

    case messages.GetLocalListVersionRes(authListVersion) => GetLocalListVersionRes(authListVersion.toV16)

    case messages.SendLocalListReq(updateType, authListVersion, authorisationData, hash) =>
      SendLocalListReq(updateType.toString, authListVersion.toV16, Some(authorisationData.map(_.toV16)))

    case messages.SendLocalListRes(status: messages.UpdateStatus.Value) =>
      val (ocppStatus, hash) = status.toV16AndHash
      SendLocalListRes(ocppStatus)

    case messages.ReserveNowReq(scope, expiryDate, idTag, parentIdTag, reservationId) =>
      ReserveNowReq(scope.toOcpp, expiryDate, idTag, parentIdTag, reservationId)

    case messages.ReserveNowRes(status) => ReserveNowRes(status.toString)

    case messages.CancelReservationReq(reservationId) => CancelReservationReq(reservationId)

    case messages.CancelReservationRes(accepted) => CancelReservationRes(accepted.toStatusString)

    case messages.CentralSystemDataTransferReq(_, _, _)
         | messages.CentralSystemDataTransferRes(_, _)
         | messages.ChargePointDataTransferReq(_, _, _)
         | messages.ChargePointDataTransferRes(_, _) =>
      unexpectedMessage(msg)
  }

  def fromV16(msg: Message): messages.Message = msg match {
    case BootNotificationReq(vendor, model, chargePointSerial, chargeBoxSerial, firmwareVersion, iccid, imsi, meterType,
    meterSerial) =>
      messages.BootNotificationReq(vendor, model, chargePointSerial, chargeBoxSerial, firmwareVersion, iccid, imsi,
        meterType, meterSerial)

    case BootNotificationRes(registrationAccepted, currentTime, heartbeatInterval) =>
      messages.BootNotificationRes(registrationAccepted = statusStringToBoolean(registrationAccepted),
        currentTime = currentTime,
        FiniteDuration(heartbeatInterval, SECONDS))

    case AuthorizeReq(idTag) => messages.AuthorizeReq(idTag)

    case AuthorizeRes(idTagInfo) => messages.AuthorizeRes(idTagInfo.fromV16)

    case StartTransactionReq(connectorId, idTag, timestamp, meterStart, reservationId) =>
      messages.StartTransactionReq(messages.ConnectorScope.fromOcpp(connectorId), idTag, timestamp, meterStart,
        reservationId)

    case StartTransactionRes(transactionId, idTagInfo) => messages.StartTransactionRes(transactionId, idTagInfo.fromV16)

    case StopTransactionReq(transactionId, idTag, timestamp, meterStop, reason, transactionData) =>
      messages.StopTransactionReq(transactionId, idTag, timestamp, meterStop, transactionDataFromV16(transactionData))

    case StopTransactionRes(idTagInfo) => messages.StopTransactionRes(idTagInfo.map(_.fromV16))

    case UnlockConnectorReq(connectorId) => messages.UnlockConnectorReq(messages.ConnectorScope.fromOcpp(connectorId))

    case UnlockConnectorRes(status) => messages.UnlockConnectorRes(statusStringToBoolean(status))

    case ResetReq(resetType) => messages.ResetReq(enumFromJsonString(messages.ResetType, resetType))

    case ResetRes(status) => messages.ResetRes(statusStringToBoolean(status))

    case ChangeAvailabilityReq(connectorId, availabilityType) =>
      messages.ChangeAvailabilityReq(scope = messages.Scope.fromOcpp(connectorId),
        availabilityType = enumFromJsonString(messages.AvailabilityType, availabilityType))

    case ChangeAvailabilityRes(status) =>
      messages.ChangeAvailabilityRes(enumFromJsonString(messages.AvailabilityStatus, status))

    case StatusNotificationReq(connector, status, errorCode, info, timestamp, vendorId, vendorErrorCode) =>
      messages.StatusNotificationReq(messages.Scope.fromOcpp(connector),
        statusFieldsToOcppStatus(status, errorCode, info, vendorErrorCode),
        timestamp,
        vendorId)

    case StatusNotificationRes() => messages.StatusNotificationRes

    //    case RemoteStartTransactionReq(idTag, connector, chargingProfile) => messages.RemoteStartTransactionReq(idTag,
    //      connector.map(messages.ConnectorScope.fromOcpp))

    case RemoteStartTransactionRes(status) => messages.RemoteStartTransactionRes(statusStringToBoolean(status))

    case RemoteStopTransactionReq(transactionId) => messages.RemoteStopTransactionReq(transactionId)

    case RemoteStopTransactionRes(status) => messages.RemoteStopTransactionRes(statusStringToBoolean(status))

    case HeartbeatReq() => messages.HeartbeatReq

    case HeartbeatRes(currentTime) => messages.HeartbeatRes(currentTime)

    case UpdateFirmwareReq(retrieveDate, location, retries, retryInterval) =>
      messages.UpdateFirmwareReq(retrieveDate, parseURI(location), messages.Retries.fromInts(retries, retryInterval))

    case UpdateFirmwareRes() => messages.UpdateFirmwareRes

    case FirmwareStatusNotificationReq(status) =>
      messages.FirmwareStatusNotificationReq(enumFromJsonString(messages.FirmwareStatus, status))

    case FirmwareStatusNotificationRes() =>
      messages.FirmwareStatusNotificationRes

    case GetDiagnosticsReq(location, startTime, stopTime, retries, retryInterval) =>
      messages.GetDiagnosticsReq(parseURI(location), startTime, stopTime,
        messages.Retries.fromInts(retries, retryInterval))

    case GetDiagnosticsRes(filename) => messages.GetDiagnosticsRes(filename)

    case DiagnosticsStatusNotificationReq(status) =>
      messages.DiagnosticsStatusNotificationReq(uploadStatusStringToBoolean(status))

    case DiagnosticsStatusNotificationRes() => messages.DiagnosticsStatusNotificationRes

    case MeterValuesReq(connectorId, transactionId, values) =>
      val meters: List[messages.Meter] = values.map(meterFromV16)
      messages.MeterValuesReq(messages.Scope.fromOcpp(connectorId), transactionId, meters)

    case MeterValuesRes() => messages.MeterValuesRes

    case ChangeConfigurationReq(key, value) => messages.ChangeConfigurationReq(key, value)

    case ChangeConfigurationRes(status) =>
      messages.ChangeConfigurationRes(enumFromJsonString(messages.ConfigurationStatus, status))

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
        updateType = enumFromJsonString(messages.UpdateType, updateType),
        listVersion = messages.AuthListSupported(authListVersion),
        localAuthorisationList = authorizationData.getOrElse(Nil).map(_.fromV16),
        hash = None
      )

    case SendLocalListRes(status) => messages.SendLocalListRes(stringToUpdateStatus(status))

    case ReserveNowReq(connectorId, expiryDate, idTag, parentIdTag, reservationId) =>
      messages.ReserveNowReq(messages.Scope.fromOcpp(connectorId), expiryDate, idTag, parentIdTag, reservationId)

    case ReserveNowRes(status) => messages.ReserveNowRes(enumFromJsonString(messages.Reservation, status))

    case CancelReservationReq(reservationId) => messages.CancelReservationReq(reservationId)

    case CancelReservationRes(status) => messages.CancelReservationRes(statusStringToBoolean(status))

    case DataTransferReq(_, _, _) | DataTransferRes(_, _) => unexpectedMessage(msg)
  }

  private def unexpectedMessage(msg: Any) =
    throw new Exception(s"Couldn't convert unexpected OCPP message $msg")

  private implicit class RichIdTagInfo(i: messages.IdTagInfo) {
    def toV16: IdTagInfo = IdTagInfo(status = AuthorizationStatusConverters.enumToJson(i.status.toString),
      expiryDate = i.expiryDate,
      parentIdTag = i.parentIdTag)
  }

  private implicit class RichV16IdTagInfo(self: IdTagInfo) {
    def fromV16: messages.IdTagInfo =
      try {
        messages.IdTagInfo(
          status = messages.AuthorizationStatus.withName(AuthorizationStatusConverters.jsonToEnum(self.status)),
          expiryDate = self.expiryDate,
          parentIdTag = self.parentIdTag)
      } catch {
        case e: NoSuchElementException =>
          throw new MappingException(s"Unrecognized authorization status ${self.status} in OCPP-JSON message")
      }
  }

  private implicit class RichChargePointStatus(self: messages.ChargePointStatus) {
    def toV16Fields: (String, String, Option[String], Option[String]) = {
      def simpleStatus(name: String) = (name, "NoError", self.info, None)
      self match {
        case messages.Available(_) => simpleStatus("Available")
        case messages.Occupied(_) => simpleStatus("Occupied")
        case messages.Unavailable(_) => simpleStatus("Unavailable")
        case messages.Reserved(_) => simpleStatus("Reserved")
        case messages.Faulted(errCode, inf, vendorErrCode) =>
          ("Faulted", errCode.map(_.toString).getOrElse(RichChargePointStatus.defaultErrorCode), inf, vendorErrCode)
      }
    }
  }

  private object RichChargePointStatus {
    val defaultErrorCode = "NoError"
  }

  private def statusFieldsToOcppStatus(status: String, errorCode: String, info: Option[String],
                                       vendorErrorCode: Option[String]): messages.ChargePointStatus = status match {
    case "Available" => messages.Available(info)
    case "Occupied" => messages.Occupied(info)
    case "Unavailable" => messages.Unavailable(info)
    case "Reserved" => messages.Reserved(info)
    case "Faulted" =>
  val errorCodeString =
  if (errorCode == RichChargePointStatus.defaultErrorCode)
  None
  else
  Some(enumFromJsonString(messages.ChargePointErrorCode, errorCode))
  messages.Faulted(errorCodeString, info, vendorErrorCode)
  }

  //  private implicit class RichCharginProfile(cp: messages.ChargingProfile) {
  //    def toV16: ChargingProfile =
  //      ChargingProfile(cp.id, cp.stackLevel, cp.chargingProfilePurpose.toString, cp.chargingProfileKind.toString,
  //        scheduleToV16(cp.chargingSchedule), cp.transactionId, None, cp.validFrom, cp.validTo)
  //
  //    def scheduleToV16(cs: messages.ChargingSchedule): ChargingSchedule =
  //      ChargingSchedule(cs.chargingRateUnit.toString, cs.chargingSchedulePeriod, cs.duration, cs.startsAt, cs.minChargingRate)
  //
  //    def periodToV16(csp: messages.ChargingSchedulePeriod): ChargingSchedulePeriod =
  //      ChargingSchedulePeriod(csp.startOffset.length, csp.amperesLimit, csp.numberPhases)
  //  }

  private def transactionDataToV16(tsList: List[messages.TransactionData]): List[Meter] = {
    tsList.flatMap( ts => ts.meters.map(_.toV16) )
  }

  private implicit class RichMeter(self: messages.Meter) {
    def toV16: Meter =
      Meter(timestamp = self.timestamp,
        sampledValue = self.values.map(valueToV16))

    def valueToV16(v: messages.Meter.Value): MeterValue =
      MeterValue(value = v.value,
        measurand = noneIfDefault(Measurand.EnergyActiveImportRegister, v.measurand),
        phase = None,
        context = noneIfDefault(ReadingContext.SamplePeriodic, v.context),
        format = noneIfDefault(ValueFormat.Raw, v.format),
        location = noneIfDefault(Location.Outlet, v.location),
        unit = noneIfDefault(UnitOfMeasure.Wh, v.unit))

    def noneIfDefault(default: Any, actual: Any): Option[String] =
      if (actual == default) None else Some(actual.toString)
  }

  private def transactionDataFromV16(v16td: Option[List[Meter]]): List[messages.TransactionData] =
    messages.TransactionData(v16td.getOrElse(List.empty[Meter]).map(meterFromV16)) :: Nil

  private def meterFromV16(v16m: Meter): messages.Meter = {
    messages.Meter(v16m.timestamp, v16m.sampledValue.map(meterValueFromV16))
  }

  private def meterValueFromV16(v16m: MeterValue): messages.Meter.Value = {
    import messages.Meter._
    import v16m._

    Value(value = value,
      measurand = getMeterValueProperty(measurand, Measurand, Measurand.EnergyActiveImportRegister),
      context = getMeterValueProperty(context, ReadingContext, ReadingContext.SamplePeriodic),
      format = getMeterValueProperty(format, ValueFormat, ValueFormat.Raw),
      location = getMeterValueProperty(location, Location, Location.Outlet),
      unit = getMeterValueProperty(unit, UnitOfMeasure, UnitOfMeasure.Wh))
  }

  private def getMeterValueProperty[V <: Nameable](inJson: Option[String], enumeration: Enumerable[V], default: V): V =
    inJson.fold(Option(default))(s => enumeration.withName(s)) match {
      case None =>
        throw new MappingException(s"Uknown meter value property $inJson in OCPP-JSON message")
      case Some(v) => v
    }

  private object AuthorizationStatusConverters {
    val names = List(("Accepted", "Accepted"), ("IdTagBlocked", "Blocked"), ("IdTagExpired", "Expired"),
    ("IdTagInvalid", "Invalid"), ("ConcurrentTx", "ConcurrentTx"))
    val jsonToEnum = Map(names.map(_.swap): _*)
    val enumToJson = Map(names: _*)
  }

  private implicit class BooleanToStatusString(val b: Boolean) extends AnyVal {
    def toStatusString = if (b) "Accepted" else "Rejected"
  }

  private def statusStringToBoolean(statusString: String) = statusString match {
    case "Accepted" => true
    case "Rejected" => false
    case _          =>
      throw new MappingException(s"Did not recognize status $statusString (expected 'Accepted' or 'Rejected')")
  }

  private implicit class BooleanToUploadStatusString(val b: Boolean) extends AnyVal {
    def toUploadStatusString = if (b) "Uploaded" else "UploadFailed"
  }

  private def uploadStatusStringToBoolean(s: String): Boolean = s match {
    case "Uploaded" => true
    case "UploadFailed" => false
    case _ => throw new MappingException(s"$s is not a valid OCPP upload status")
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
    def fromV16: messages.AuthorisationData = messages.AuthorisationData(self.idTag, self.idTagInfo.map(_.fromV16))
  }

  private implicit class RichUpdateStatus(self: messages.UpdateStatus.Value) {
    import messages.UpdateStatus._

    def toV16AndHash: (String, Option[String]) = {
      val hashString = self match {
        case UpdateAccepted(hash) => hash
        case _ => None
      }

      (enumToName(self), hashString)
    }

    def enumToName(v: messages.UpdateStatus.Value): String = v match {
      case UpdateAccepted(_) => "Accepted"
      case UpdateFailed => "Failed"
      case x => x.toString
    }
  }

  private def stringToUpdateStatus(status: String) = {
    import messages.UpdateStatus._
    status match {
      case "Accepted" => UpdateAccepted(None)
      case "Failed" => UpdateFailed
      case "HashError" => HashError
      case "NotSupportedValue" => NotSupportedValue
      case "VersionMismatch" => VersionMismatch
      case _ => throw new MappingException(s"Unrecognized value $status for OCPP update status")
    }
  }

  /**
    * Tries to get select the enum value whose name is equal to the given string. If no such enum value exists, throws
    * a net.liftweb.json.MappingException.
    */
  private def enumFromJsonString[T <: Enumeration](enum: T, s: String) = try {
    enum.withName(s)
  } catch {
    case e: NoSuchElementException =>
      throw new MappingException(s"Value $s is not valid for ${enum.getClass.getSimpleName}", e)
  }

  /**
    * Parses a URI and throws a lift-json MappingException if the syntax is wrong
    */
  private def parseURI(s: String) = try {
    new URI(s)
  } catch {
    case e: URISyntaxException => throw new MappingException(s"Invalid URL $s in OCPP-JSON message", e)
  }
}
