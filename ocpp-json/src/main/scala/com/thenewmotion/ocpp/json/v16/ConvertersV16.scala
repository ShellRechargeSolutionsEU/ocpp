package com.thenewmotion.ocpp
package json.v16

import java.net.URI
import java.net.URISyntaxException

import enums.reflection.EnumUtils.Enumerable
import enums.reflection.EnumUtils.Nameable
import org.json4s.MappingException

import scala.concurrent.duration._

object ConvertersV16 {
  def toV16(msg: messages.Message): Message = msg match {
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

    case messages.BootNotificationRes(registrationAccepted, currentTime, interval) =>
      BootNotificationRes(registrationAccepted.name, currentTime, interval.toSeconds.toInt)

    case messages.AuthorizeReq(idTag) => AuthorizeReq(idTag)

    case messages.AuthorizeRes(idTag) => AuthorizeRes(idTag.toV16)

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
        transactionData = Some(meters.map(_.toV16))
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

    case messages.GetConfigurationReq(keys) => GetConfigurationReq(Some(keys))

    case messages.GetConfigurationRes(values, unknownKeys) =>
      GetConfigurationRes(configurationKey = Some(values.map(_.toV16)), unknownKey = Some(unknownKeys))

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
      import messages.MessageTriggerWithoutConnector
      import messages.MessageTriggerWithConnector
      TriggerMessageReq.tupled {
        requestedMessage match {
          case messageTrigger: MessageTriggerWithoutConnector =>
            (messageTrigger.name, None)
          case MessageTriggerWithConnector.MeterValues(connectorId) =>
            ("Metervalues", connectorId.map(_.toOcpp))
          case MessageTriggerWithConnector.StatusNotification(connectorId) =>
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

  def fromV16(msg: Message): messages.Message = msg match {
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

    case BootNotificationRes(statusString, currentTime, interval) =>
      messages.BootNotificationRes(
        status = enumerableFromJsonString(messages.RegistrationStatus, statusString),
        currentTime = currentTime,
        FiniteDuration(interval, SECONDS)
      )

    case AuthorizeReq(idTag) => messages.AuthorizeReq(idTag)

    case AuthorizeRes(idTagInfo) => messages.AuthorizeRes(idTagInfo.fromV16)

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
        meters.fold(List.empty[messages.Meter])(_.map(meterFromV16))
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
      val meters: List[messages.Meter] = values.map(meterFromV16)
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
    def toV16: Meter = Meter(
      timestamp = self.timestamp,
      sampledValue = self.values.map(valueToV16)
    )

    def valueToV16(v: messages.Meter.Value): MeterValue = {
      import messages.Meter._
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

  private def meterFromV16(v16m: Meter): messages.Meter = {
    messages.Meter(v16m.timestamp, v16m.sampledValue.map(meterValueFromV16))
  }

  private def meterValueFromV16(v16m: MeterValue): messages.Meter.Value = {
    import messages.Meter._
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

  /**
   * Parses a URI and throws a lift-json MappingException if the syntax is wrong
   */
  private def parseURI(s: String) = try {
    new URI(s)
  } catch {
    case e: URISyntaxException => throw MappingException(s"Invalid URL $s in OCPP-JSON message", e)
  }

  private implicit class RichTriggerMessage(self: messages.TriggerMessageReq) {

    import messages.MessageTriggerWithConnector
    import messages.MessageTriggerWithoutConnector

    def toV16: TriggerMessageReq = {
      self.requestedMessage match {
        case messageTrigger: MessageTriggerWithoutConnector =>
          TriggerMessageReq(messageTrigger.name, None)
        case MessageTriggerWithConnector.MeterValues(connector) =>
          TriggerMessageReq("MeterValues", connector.map(_.toOcpp))
        case MessageTriggerWithConnector.StatusNotification(connector) =>
          TriggerMessageReq("StatusNotification", connector.map(_.toOcpp))
      }
    }
  }

  private def triggerFromV16(v16t: TriggerMessageReq): messages.TriggerMessageReq =
    messages.TriggerMessageReq {
      import messages.ConnectorScope.fromOcpp
      import messages.MessageTriggerWithConnector
      import messages.MessageTriggerWithoutConnector
      v16t match {
        case TriggerMessageReq(requestedMessage, connectorId) =>
          MessageTriggerWithoutConnector.withName(requestedMessage) match {
            case Some(messageTrigger) => messageTrigger
            case None => requestedMessage match {
              case "MeterValues" =>
                MessageTriggerWithConnector.MeterValues(connectorId.map(fromOcpp))
              case "StatusNotification" =>
                MessageTriggerWithConnector.StatusNotification(connectorId.map(fromOcpp))
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
        cs.chargingRateUnit.toString,
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
    def toV16: ChargingProfile = ChargingProfile(
      cp.id,
      cp.stackLevel,
      cp.chargingProfilePurpose.toString,
      cp.chargingProfileKind.toString,
      cp.chargingSchedule.toV16,
      cp.transactionId,
      None,
      cp.validFrom,
      cp.validTo
    )
  }

  private def chargingProfileFromV16(v16p: ChargingProfile): messages.ChargingProfile =
    messages.ChargingProfile(
      v16p.chargingProfileId,
      v16p.stackLevel,
      enumerableFromJsonString(messages.ChargingProfilePurpose, v16p.chargingProfilePurpose),
      stringToProfileKind(v16p.chargingProfileKind),
      chargingScheduleFromV16(v16p.chargingSchedule),
      v16p.transactionId,
      v16p.validFrom,
      v16p.validTo
    )

  private def stringToProfileKind(v16cpk: String): messages.ChargingProfileKind = {
    import messages.ChargingProfileKind._
    import messages.RecurrencyKind._

    v16cpk match {
      case "Absolute" => Absolute
      case "Relative" => Relative
      case "Weekly" => Recurring(Weekly)
      case "Daily" => Recurring(Daily)
      case _ => throw new MappingException(s"Unrecognized value $v16cpk for OCPP profile kind")
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
