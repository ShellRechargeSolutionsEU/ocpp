package com.thenewmotion.ocpp
package json.v15

import java.net.URI
import java.net.URISyntaxException

import enums.reflection.EnumUtils.Enumerable
import enums.reflection.EnumUtils.Nameable
import org.json4s.MappingException

import scala.concurrent.duration._

object ConvertersV15 {
  def toV15(msg: messages.Message): Message = msg match {
    case messages.BootNotificationReq(chargePointVendor, chargePointModel, chargePointSerialNumber,
    chargeBoxSerialNumber, firmwareVersion, iccid, imsi, meterType, meterSerialNumber) =>
      BootNotificationReq(
        chargePointVendor,
        chargePointModel,
        chargePointSerialNumber,
        chargeBoxSerialNumber,
        firmwareVersion,
        iccid,
        imsi,
        meterType,
        meterSerialNumber
      )

    case messages.BootNotificationRes(registrationAccepted, currentTime, heartbeatInterval) =>
      BootNotificationRes(registrationAccepted.name, currentTime, heartbeatInterval.toSeconds.toInt)

    case messages.AuthorizeReq(idTag) => AuthorizeReq(idTag)

    case messages.AuthorizeRes(idTag) => AuthorizeRes(idTag.toV15)

    case messages.StartTransactionReq(connector, idTag, timestamp, meterStart, reservationId) =>
      StartTransactionReq(
        connectorId = connector.toOcpp,
        idTag = idTag,
        timestamp = timestamp,
        meterStart = meterStart,
        reservationId = reservationId
      )

    case messages.StartTransactionRes(transactionId, idTagInfo) => StartTransactionRes(transactionId, idTagInfo.toV15)

    case messages.StopTransactionReq(transactionId, idTag, timestamp, meterStop, _, meters) =>
      StopTransactionReq(
        transactionId = transactionId,
        idTag = idTag,
        timestamp = timestamp,
        meterStop = meterStop,
        transactionData = Some(
          List(TransactionData(Some(meters.map(_.toV15))))
        )
      )

    case messages.StopTransactionRes(idTagInfo) => StopTransactionRes(idTagInfo.map(_.toV15))

    case messages.UnlockConnectorReq(scope) => UnlockConnectorReq(scope.toOcpp)

    case messages.UnlockConnectorRes(accepted) => UnlockConnectorRes {
      if (accepted == messages.UnlockStatus.Unlocked) "Accepted"
      else "Rejected"
    }

    case messages.ResetReq(resetType) => ResetReq(resetType.name)

    case messages.ResetRes(accepted) => ResetRes(accepted.toStatusString)

    case messages.ChangeAvailabilityReq(scope, availabilityType) =>
      ChangeAvailabilityReq(connectorId = scope.toOcpp, `type` = availabilityType.name)

    case messages.ChangeAvailabilityRes(status) => ChangeAvailabilityRes(status.name)

    case messages.StatusNotificationReq(scope, status, timestamp, vendorId) =>
      val (ocppStatus, errorCode, info, vendorErrorCode) = status.toV15Fields
      StatusNotificationReq(scope.toOcpp, ocppStatus, errorCode, info, timestamp, vendorId, vendorErrorCode)

    case messages.StatusNotificationRes => StatusNotificationRes()

    case messages.RemoteStartTransactionReq(idTag, connector, _) =>
      RemoteStartTransactionReq(idTag, connector.map(_.toOcpp))

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
      MeterValuesReq(scope.toOcpp, transactionId, Some(meters.map(_.toV15)))

    case messages.MeterValuesRes => MeterValuesRes()

    case messages.ChangeConfigurationReq(key, value) => ChangeConfigurationReq(key, value)

    case messages.ChangeConfigurationRes(status) => ChangeConfigurationRes(status.name)

    case messages.ClearCacheReq => ClearCacheReq()

    case messages.ClearCacheRes(accepted) => ClearCacheRes(accepted.toStatusString)

    case messages.GetConfigurationReq(keys) => GetConfigurationReq(Some(keys))

    case messages.GetConfigurationRes(values, unknownKeys) =>
      GetConfigurationRes(configurationKey = Some(values.map(_.toV15)), unknownKey = Some(unknownKeys))

    case messages.GetLocalListVersionReq => GetLocalListVersionReq()

    case messages.GetLocalListVersionRes(authListVersion) => GetLocalListVersionRes(authListVersion.toV15)

    case messages.SendLocalListReq(updateType, authListVersion, authorisationData, hash) =>
      SendLocalListReq(updateType.name, authListVersion.toV15, Some(authorisationData.map(_.toV15)), hash)

    case messages.SendLocalListRes(status: messages.UpdateStatus) =>
      SendLocalListRes.tupled(status.toV15Fields)

    case messages.ReserveNowReq(scope, expiryDate, idTag, parentIdTag, reservationId) =>
      ReserveNowReq(scope.toOcpp, expiryDate, idTag, parentIdTag, reservationId)

    case messages.ReserveNowRes(status) => ReserveNowRes(status.name)

    case messages.CancelReservationReq(reservationId) => CancelReservationReq(reservationId)

    case messages.CancelReservationRes(accepted) => CancelReservationRes(accepted.toStatusString)

    case messages.CentralSystemDataTransferReq(_, _, _)
         | messages.CentralSystemDataTransferRes(_, _)
         | messages.ChargePointDataTransferReq(_, _, _)
         | messages.ChargePointDataTransferRes(_, _)
         | messages.ClearChargingProfileReq(_, _, _, _)
         | messages.ClearChargingProfileRes(_)
         | messages.GetCompositeScheduleReq(_, _, _)
         | messages.GetCompositeScheduleRes(_)
         | messages.SetChargingProfileReq(_, _)
         | messages.SetChargingProfileRes(_)
         | messages.TriggerMessageReq(_)
         | messages.TriggerMessageRes(_) =>
      unexpectedMessage(msg)
  }

  def fromV15(msg: Message): messages.Message = msg match {
    case BootNotificationReq(vendor, model, chargePointSerial, chargeBoxSerial, firmwareVersion, iccid, imsi, meterType, meterSerial) =>
      messages.BootNotificationReq(
        vendor,
        model,
        chargePointSerial,
        chargeBoxSerial,
        firmwareVersion,
        iccid,
        imsi,
        meterType,
        meterSerial
      )

    case BootNotificationRes(statusString, currentTime, heartbeatInterval) =>
      messages.BootNotificationRes(
        status = enumerableFromJsonString(messages.RegistrationStatus, statusString),
        currentTime = currentTime,
        FiniteDuration(heartbeatInterval, SECONDS)
      )

    case AuthorizeReq(idTag) => messages.AuthorizeReq(idTag)

    case AuthorizeRes(idTagInfo) => messages.AuthorizeRes(idTagInfo.fromV15)

    case StartTransactionReq(connectorId, idTag, timestamp, meterStart, reservationId) =>
      messages.StartTransactionReq(
        messages.ConnectorScope.fromOcpp(connectorId),
        idTag,
        timestamp,
        meterStart,
        reservationId
      )

    case StartTransactionRes(transactionId, idTagInfo) => messages.StartTransactionRes(transactionId, idTagInfo.fromV15)

    case StopTransactionReq(transactionId, idTag, timestamp, meterStop, transactionData) =>
      messages.StopTransactionReq(
        transactionId,
        idTag,
        timestamp,
        meterStop,
        messages.StopReason.Local,
        transactionDataFromV15(transactionData)
      )

    case StopTransactionRes(idTagInfo) => messages.StopTransactionRes(idTagInfo.map(_.fromV15))

    case UnlockConnectorReq(connectorId) => messages.UnlockConnectorReq(messages.ConnectorScope.fromOcpp(connectorId))

    case UnlockConnectorRes(statusString) => messages.UnlockConnectorRes(
      if (statusString == "Accepted") messages.UnlockStatus.Unlocked
      else messages.UnlockStatus.UnlockFailed
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

    case RemoteStartTransactionReq(idTag, connector) =>
      messages.RemoteStartTransactionReq(
        idTag,
        connector.map(messages.ConnectorScope.fromOcpp),
        None
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
      val meters: List[messages.Meter] = values.fold(List.empty[messages.Meter])(_.map(meterFromV15))
      messages.MeterValuesReq(messages.Scope.fromOcpp(connectorId), transactionId, meters)

    case MeterValuesRes() => messages.MeterValuesRes

    case ChangeConfigurationReq(key, value) => messages.ChangeConfigurationReq(key, value)

    case ChangeConfigurationRes(status) =>
      messages.ChangeConfigurationRes(enumerableFromJsonString(messages.ConfigurationStatus, status))

    case ClearCacheReq() => messages.ClearCacheReq

    case ClearCacheRes(status) => messages.ClearCacheRes(statusStringToBoolean(status))

    case GetConfigurationReq(keys) => messages.GetConfigurationReq(keys getOrElse Nil)

    case GetConfigurationRes(values, unknownKeys) =>
      messages.GetConfigurationRes(values.fold(List.empty[messages.KeyValue])(_.map(_.fromV15)),
        unknownKeys getOrElse Nil)

    case GetLocalListVersionReq() => messages.GetLocalListVersionReq

    case GetLocalListVersionRes(v) => messages.GetLocalListVersionRes(messages.AuthListVersion(v))

    case SendLocalListReq(updateType, authListVersion, authorizationData, hash) =>
      messages.SendLocalListReq(
        updateType = enumerableFromJsonString(messages.UpdateType, updateType),
        listVersion = messages.AuthListSupported(authListVersion),
        localAuthorisationList = authorizationData.getOrElse(Nil).map(_.fromV15),
        hash = hash
      )

    case SendLocalListRes(status, hash) => messages.SendLocalListRes(updateStatusFromV15(status, hash))

    case ReserveNowReq(connectorId, expiryDate, idTag, parentIdTag, reservationId) =>
      messages.ReserveNowReq(messages.Scope.fromOcpp(connectorId), expiryDate, idTag, parentIdTag, reservationId)

    case ReserveNowRes(status) => messages.ReserveNowRes(enumerableFromJsonString(messages.Reservation, status))

    case CancelReservationReq(reservationId) => messages.CancelReservationReq(reservationId)

    case CancelReservationRes(status) => messages.CancelReservationRes(statusStringToBoolean(status))

    case DataTransferReq(_, _, _) | DataTransferRes(_, _) => unexpectedMessage(msg)
  }

  private def unexpectedMessage(msg: Any) =
    throw new Exception(s"Couldn't convert unexpected OCPP message $msg")

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

  private implicit class RichChargePointStatus(self: messages.ChargePointStatus) {

    import RichChargePointStatus.defaultErrorCode

    def toV15Fields: (String, String, Option[String], Option[String]) = {
      def simpleStatus(name: String) = (name, defaultErrorCode, self.info, None)

      import messages.ChargePointStatus
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
    vendorErrorCode: Option[String]): messages.ChargePointStatus = {
    import messages.ChargePointStatus
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
          else
            Some(enumerableFromJsonString(messages.ChargePointErrorCode, errorCode))
        ChargePointStatus.Faulted(errorCodeString, info, vendorErrorCode)
    }
  }

  private implicit class RichMeter(self: messages.Meter) {
    def toV15: Meter = Meter(
      timestamp = self.timestamp,
      values = self.values.map(valueToV15)
    )

    def valueToV15(v: messages.Meter.Value): MeterValue = {
      import messages.Meter._
      MeterValue(
        value = v.value,
        measurand = noneIfDefault(Measurand.EnergyActiveImportRegister, v.measurand),
        context = noneIfDefault(ReadingContext.SamplePeriodic, v.context),
        format = noneIfDefault(ValueFormat.Raw, v.format),
        location = noneIfDefault(Location.Outlet, v.location),
        unit = noneIfDefault(UnitOfMeasure.Wh, v.unit)
      )
    }

    def noneIfDefault(default: Nameable, actual: Nameable): Option[String] =
      if (actual == default) None else Some(actual.name)
  }

  private def transactionDataFromV15(v15td: Option[List[TransactionData]]): List[messages.Meter] =
    v15td.fold(List.empty[messages.Meter])(metersFromV15)

  private def metersFromV15(v15mv: List[TransactionData]): List[messages.Meter] =
    v15mv.flatMap(_.values.fold(List.empty[messages.Meter])(_.map(meterFromV15)))

  private def meterFromV15(v15m: Meter): messages.Meter = {
    messages.Meter(v15m.timestamp, v15m.values.map(meterValueFromV15))
  }

  private def meterValueFromV15(v15m: MeterValue): messages.Meter.Value = {
    import messages.Meter._
    import v15m._

    Value(
      value = value,
      measurand = getMeterValueProperty(measurand, Measurand, Measurand.EnergyActiveImportRegister),
      phase = None,
      context = getMeterValueProperty(context, ReadingContext, ReadingContext.SamplePeriodic),
      format = getMeterValueProperty(format, ValueFormat, ValueFormat.Raw),
      location = getMeterValueProperty(location, Location, Location.Outlet),
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
    import messages.UpdateStatusWithoutHash
    import messages.UpdateStatusWithHash
    UpdateStatusWithoutHash.withName(status) match {
      case Some(updateStatus) => updateStatus
      case None => status match {
        case "Accepted" => UpdateStatusWithHash.Accepted(hash)
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

  /**
   * Parses a URI and throws a lift-json MappingException if the syntax is wrong
   */
  private def parseURI(s: String) = try {
    new URI(s)
  } catch {
    case e: URISyntaxException => throw MappingException(s"Invalid URL $s in OCPP-JSON message", e)
  }
}
