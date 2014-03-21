package com.thenewmotion.ocpp
package json.v15

import com.thenewmotion.ocpp.messages
import com.thenewmotion.ocpp.messages.Meter._
import net.liftweb.json.MappingException

object ConvertersV15 {
  def toV15(msg: messages.Message): Message = msg match {
    case messages.BootNotificationReq(chargePointVendor, chargePointModel, chargePointSerialNumber,
                                      chargeBoxSerialNumber, firmwareVersion, iccid, imsi, meterType,
                                      meterSerialNumber) =>
      BootNotificationReq(chargePointVendor, chargePointModel, chargePointSerialNumber, chargeBoxSerialNumber,
                          firmwareVersion, iccid, imsi, meterType, meterSerialNumber)

    case messages.AuthorizeReq(idTag) => AuthorizeReq(idTag)

    case messages.AuthorizeRes(idTag) => AuthorizeRes(idTag.toV15)

    case messages.StartTransactionReq(connector, idTag, timestamp, meterStart, reservationId) =>
      StartTransactionReq(connectorId = connector.toOcpp,
                          idTag = idTag,
                          timestamp = timestamp,
                          meterStart = meterStart,
                          reservationId = reservationId)

    case messages.StartTransactionRes(transactionId, idTagInfo) => StartTransactionRes(transactionId, idTagInfo.toV15)

    case messages.StopTransactionReq(transactionId, idTag, timestamp, meterStop, transactionData) =>
      StopTransactionReq(transactionId = transactionId,
                         idTag = idTag,
                         timestamp = timestamp,
                         meterStop = meterStop,
                         transactionData = Some(transactionData.map(_.toV15)))

    case messages.StopTransactionRes(idTagInfo) => StopTransactionRes(idTagInfo.map(_.toV15))

    case messages.UnlockConnectorRes(accepted) => UnlockConnectorRes(accepted.toStatusString)

    case messages.ResetRes(accepted) => ResetRes(accepted.toStatusString)

    case messages.ChangeAvailabilityRes(status) => ChangeAvailabilityRes(status.toString)

    case messages.StatusNotificationReq(scope, status, timestamp, vendorId) =>

      def statusToOcppFields(status: messages.ChargePointStatus): (String, String, Option[String], Option[String]) = {
        def simpleStatus(name: String) = (name, "NoError", None, None)
        status match {
          case messages.Available => simpleStatus("Available")
          case messages.Occupied => simpleStatus("Occupied")
          case messages.Unavailable => simpleStatus("Unavailable")
          case messages.Reserved => simpleStatus("Reserved")
          case messages.Faulted(errCode, inf, vendorErrCode) =>
            ("Faulted", errCode.map(_.toString).getOrElse("OtherError"), inf, vendorErrCode)
        }
      }

      val (ocppStatus, errorCode, info, vendorErrorCode) = statusToOcppFields(status)

      StatusNotificationReq(scope.toOcpp, ocppStatus, errorCode, info, timestamp, vendorId, vendorErrorCode)

    case messages.RemoteStartTransactionRes(accepted) => RemoteStartTransactionRes(accepted.toStatusString)

    case messages.RemoteStopTransactionRes(accepted) => RemoteStopTransactionRes(accepted.toStatusString)

    case messages.HeartbeatReq => HeartbeatReq()

    case messages.UpdateFirmwareRes => UpdateFirmwareRes()

    case messages.FirmwareStatusNotificationReq(status) => FirmwareStatusNotificationReq(status.toString)

    case messages.GetDiagnosticsRes(filename) => GetDiagnosticsRes(filename)

    case messages.DiagnosticsStatusNotificationReq(uploaded) =>
      DiagnosticsStatusNotificationReq(uploaded.toUploadStatusString)

    case messages.MeterValuesReq(scope, transactionId, meters) =>
      MeterValuesReq(scope.toOcpp, transactionId, Some(meters.map(_.toV15)))

    case messages.ChangeConfigurationRes(status) => ChangeConfigurationRes(status.toString)

    case messages.ClearCacheRes(accepted) => ClearCacheRes(accepted.toStatusString)

    case messages.GetConfigurationRes(values, unknownKeys) =>
      GetConfigurationRes(configurationKey = Some(values.map(_.toV15)), unknownKey = Some(unknownKeys))

    case messages.GetLocalListVersionRes(authListVersion) => GetLocalListVersionRes(authListVersion.toV15)

    case messages.SendLocalListRes(status: messages.UpdateStatus.Value) =>
      val (ocppStatus, hash) = status.toV15AndHash
      SendLocalListRes(ocppStatus, hash)

    case messages.ReserveNowRes(status) => ReserveNowRes(status.toString)

    case messages.CancelReservationRes(accepted) => CancelReservationRes(accepted.toStatusString)
  }

  def fromV15(msg: Message): messages.Message = msg match {
    case BootNotificationReq(vendor, model, chargePointSerial, chargeBoxSerial, firmwareVersion, iccid, imsi, meterType,
                             meterSerial) =>
      messages.BootNotificationReq(vendor, model, chargePointSerial, chargeBoxSerial, firmwareVersion, iccid, imsi,
                                   meterType, meterSerial)

    case AuthorizeReq(idTag) => messages.AuthorizeReq(idTag)

    case AuthorizeRes(idTagInfo) => messages.AuthorizeRes(idTagInfo.fromV15)

    case StartTransactionReq(connectorId, idTag, timestamp, meterStart, reservationId) =>
      messages.StartTransactionReq(messages.ConnectorScope.fromOcpp(connectorId), idTag, timestamp, meterStart,
                                   reservationId)

    case StartTransactionRes(transactionId, idTagInfo) => messages.StartTransactionRes(transactionId, idTagInfo.fromV15)

    case StopTransactionReq(transactionId, idTag, timestamp, meterStop, transactionData) =>
      messages.StopTransactionReq(transactionId, idTag, timestamp, meterStop, transactionDataFromV15(transactionData))

    case StopTransactionRes(idTagInfo) => messages.StopTransactionRes(idTagInfo.map(_.fromV15))

    case UnlockConnectorRes(status) => messages.UnlockConnectorRes(statusStringToBoolean(status))

    case ResetRes(status) => messages.ResetRes(statusStringToBoolean(status))

    // TODO catch NSEE
    case ChangeAvailabilityRes(status) => messages.ChangeAvailabilityRes(messages.AvailabilityStatus.withName(status))

    case StatusNotificationReq(connector, status, errorCode, info, timestamp, vendorId, vendorErrorCode) =>
      def statusFieldsToOcppStatus(status: String, errorCode: String, info: Option[String],
                                   vendorErrorCode: Option[String]): messages.ChargePointStatus = status match {
          case "Available" => messages.Available
          case "Occupied" => messages.Occupied
          case "Unavailable" => messages.Unavailable
          case "Reserved" => messages.Reserved
            // TODO test for NoError
          case "Faulted" => messages.Faulted(if (errorCode == "NoError")
                                               None
                                             else
                                               Some(enumFromJsonString(messages.ChargePointErrorCode, errorCode)),
                                             info,
                                             vendorErrorCode)
        }

      messages.StatusNotificationReq(messages.Scope.fromOcpp(connector),
                                     statusFieldsToOcppStatus(status, errorCode, info, vendorErrorCode),
                                     timestamp,
                                     vendorId)

    case RemoteStartTransactionRes(status) => messages.RemoteStartTransactionRes(statusStringToBoolean(status))

    case RemoteStopTransactionRes(status) => messages.RemoteStopTransactionRes(statusStringToBoolean(status))

    case HeartbeatReq() => messages.HeartbeatReq

    case UpdateFirmwareRes() => messages.UpdateFirmwareRes

    case FirmwareStatusNotificationReq(status) =>
      messages.FirmwareStatusNotificationReq(enumFromJsonString(messages.FirmwareStatus, status))

    case GetDiagnosticsRes(filename) => messages.GetDiagnosticsRes(filename)

    case DiagnosticsStatusNotificationReq(status) =>
      messages.DiagnosticsStatusNotificationReq(uploadStatusStringToBoolean(status))

    case MeterValuesReq(connectorId, transactionId, values) =>
      val meters: List[messages.Meter] = values.fold(List.empty[messages.Meter])(_.map(meterFromV15))
      messages.MeterValuesReq(messages.Scope.fromOcpp(connectorId), transactionId, meters)

    case ChangeConfigurationRes(status) =>
      messages.ChangeConfigurationRes(enumFromJsonString(messages.ConfigurationStatus, status))

    case ClearCacheRes(status) => messages.ClearCacheRes(statusStringToBoolean(status))

    case GetConfigurationRes(values, unknownKeys) =>
      messages.GetConfigurationRes(values.fold(List.empty[messages.KeyValue])(_.map(_.fromV15)),
                                   unknownKeys getOrElse Nil)

    case GetLocalListVersionRes(v) => messages.GetLocalListVersionRes(messages.AuthListVersion(v))

    case SendLocalListRes(status, hash) =>
      import messages.UpdateStatus._

      val ocppStatus = status match {
        case "Accepted" => UpdateAccepted(hash)
        case "Failed" => UpdateFailed
        case "HashError" => HashError
        case "NotSupportedValue" => NotSupportedValue
        case "VersionMismatch" => VersionMismatch
        case _ => throw new MappingException(s"Unrecognized value '$status' for OCPP update status")
      }

      messages.SendLocalListRes(ocppStatus)

    case ReserveNowRes(status) => messages.ReserveNowRes(enumFromJsonString(messages.Reservation, status))

    case CancelReservationRes(status) => messages.CancelReservationRes(statusStringToBoolean(status))
  }

  implicit class RichIdTagInfo(i: messages.IdTagInfo) {
    def toV15: IdTagInfo = IdTagInfo(status = AuthorizationStatusConverters.enumToJson(i.status.toString),
                          expiryDate = i.expiryDate,
                          parentIdTag = i.parentIdTag)
  }

  implicit class RichV15IdTagInfo(self: IdTagInfo) {
    def fromV15: messages.IdTagInfo =
      messages.IdTagInfo(status = messages.AuthorizationStatus.withName(AuthorizationStatusConverters.jsonToEnum(self.status)),
                         expiryDate = self.expiryDate,
                         parentIdTag = self.parentIdTag)
  }

  implicit class RichTransactionData(self: messages.TransactionData) {
    def toV15: TransactionData = TransactionData(values = Some(self.meters.map(_.toV15): List[Meter]))
  }

  implicit class RichMeter(self: messages.Meter) {
    def toV15: Meter =
      Meter(timestamp = self.timestamp,
                       values = self.values.map(valueToV15))

    def valueToV15(v: messages.Meter.Value): MeterValue =
      MeterValue(value = v.value,
                     measurand = noneIfDefault(Measurand.EnergyActiveImportRegister, v.measurand),
                     context = noneIfDefault(ReadingContext.SamplePeriodic, v.context),
                     format = noneIfDefault(ValueFormat.Raw, v.format),
                     location = noneIfDefault(Location.Outlet, v.location),
                     unit = noneIfDefault(UnitOfMeasure.Wh, v.unit))

    def noneIfDefault(default: Enumeration#Value, actual: Enumeration#Value): Option[String] =
      if (actual == default) None else Some(actual.toString)
  }

  def transactionDataFromV15(v15td: Option[List[TransactionData]]): List[messages.TransactionData] =
    v15td.fold(List.empty[messages.TransactionData])(_.map(metersFromV15))

  def metersFromV15(v15mv: TransactionData): messages.TransactionData =
    messages.TransactionData(v15mv.values getOrElse List.empty[Meter] map meterFromV15)

  def meterFromV15(v15m: Meter): messages.Meter = {
    messages.Meter(v15m.timestamp, v15m.values.map(meterValueFromV15))
  }

  def meterValueFromV15(v15m: MeterValue): messages.Meter.Value = {
    import v15m._
    import messages.Meter._

    Value(value = value,
      measurand = getMeterValueProperty(measurand, Measurand, Measurand.EnergyActiveImportRegister),
      context = getMeterValueProperty(context, ReadingContext, ReadingContext.SamplePeriodic),
      format = getMeterValueProperty(format, ValueFormat, ValueFormat.Raw),
      location = getMeterValueProperty(location, Location, Location.Outlet),
      unit = getMeterValueProperty(unit, UnitOfMeasure, UnitOfMeasure.Wh))
  }


  def getMeterValueProperty[T <: Enumeration](inJson: Option[String], enumeration: T, default: T#Value): T#Value =
    try {
      inJson.fold(default)(s => enumeration.withName(s))
    } catch {
      // TODO or don't catch and report error?
      case _: NoSuchElementException => default
    }

  object AuthorizationStatusConverters {
    val names = List(("Accepted", "Accepted"), ("IdTagBlocked", "Blocked"), ("IdTagExpired", "Expired"),
      ("IdTagInvalid", "Invalid"), ("ConcurrentTx", "ConcurrentTx"))
    val jsonToEnum = Map(names.map(_.swap): _*)
    val enumToJson = Map(names: _*)
  }

  implicit class BooleanToStatusString(val b: Boolean) extends AnyVal {
    def toStatusString = if (b) "Accepted" else "Rejected"
  }

  private def statusStringToBoolean(statusString: String) = statusString match {
    case "Accepted" => true
    case "Rejected" => false
    case _          =>
      throw new MappingException(s"Did not recognize status '$statusString' (expected 'Accepted' or 'Rejected')")
  }

  implicit class BooleanToUploadStatusString(val b: Boolean) extends AnyVal {
    def toUploadStatusString = if (b) "Uploaded" else "UploadFailed"
  }

  private def uploadStatusStringToBoolean(s: String): Boolean = s match {
    case "Uploaded" => true
    case "UploadFailed" => false
    case _ => throw new MappingException(s"'$s' is not a valid OCPP upload status")
  }

  implicit class RichKeyValue(val self: messages.KeyValue) {
    import self._

    def toV15: ConfigurationEntry = ConfigurationEntry(key, readonly, value)
  }

  implicit class RichConfigurationEntry(self: ConfigurationEntry) {
    import self._

    def fromV15: messages.KeyValue = messages.KeyValue(key, readonly, value)
  }

  implicit class RichAuthListVersion(self: messages.AuthListVersion) {
    def toV15: Int = self match {
      case messages.AuthListNotSupported => -1
      case messages.AuthListSupported(i) => i
    }
  }

  implicit class RichUpdateStatus(self: messages.UpdateStatus.Value) {
    import messages.UpdateStatus._

    def toV15AndHash: (String, Option[String]) = {
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

  object UpdateStatusConverters {
    val names = List("UpdateAccepted" -> "Accepted",
                     "UpdateFailed" -> "Failed",
                     "HashError" -> "HashError",
                     "NotSupported" -> "NotSupportedValue",
                     "VersionMismatch" -> "VersionMismatch")
    val jsonToEnum = Map(names.map(_.swap): _*)
    val enumToJson = Map(names: _*)
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
}
