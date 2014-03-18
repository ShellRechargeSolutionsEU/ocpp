package com.thenewmotion.ocpp
package json.v15

import com.thenewmotion.ocpp.messages
import com.thenewmotion.ocpp.messages.Meter._

private object ConvertersV15 {
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

    case messages.HeartbeatReq => HeartbeatReq

    case messages.UpdateFirmwareRes => UpdateFirmwareRes

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

  def fromV15(msg: Message): messages.Message = ???

  private implicit class RichIdTagInfo(i: messages.IdTagInfo) {
    def toV15: IdTagInfo = IdTagInfo(status = AuthorizationStatusConverters.enumToJson(i.status.toString),
                          expiryDate = i.expiryDate,
                          parentIdTag = i.parentIdTag)
  }

  private implicit class RichTransactionData(self: messages.TransactionData) {
    def toV15: MeterValues = MeterValues(values = Some(self.meters.map(_.toV15): List[MeterValueSample]))
  }

  private implicit class RichMeter(self: messages.Meter) {
    def toV15: MeterValueSample =
      MeterValueSample(timestamp = Some(self.timestamp),
                       values = self.values.map(valueToV15))

    def valueToV15(v: messages.Meter.Value): MeterValueItem =
      MeterValueItem(value = v.value,
                     measurand = noneIfDefault(Measurand.EnergyActiveImportRegister, v.measurand),
                     context = noneIfDefault(ReadingContext.SamplePeriodic, v.context),
                     format = noneIfDefault(ValueFormat.Raw, v.format),
                     location = noneIfDefault(Location.Outlet, v.location),
                     unit = noneIfDefault(UnitOfMeasure.Wh, v.unit))

    def noneIfDefault(default: Enumeration#Value, actual: Enumeration#Value): Option[String] =
      if (actual == default) None else Some(actual.toString)

    /*
    class MeterValueJsonFormat extends CustomDeserializerOnly[Meter.Value](format => {
      case jval: JObject => {
        Meter.Value(value = (jval \ "value").extract[String](format, manifest),
          measurand = getMeterValueProperty(jval \ "measurand", Measurand.EnergyActiveImportRegister, new EnumNameSerializer(Measurand)),
          context = getMeterValueProperty(jval \ "context", ReadingContext.SamplePeriodic, new EnumNameSerializer(ReadingContext)),
          format = getMeterValueProperty(jval \ "format", ValueFormat.Raw, new EnumNameSerializer(ValueFormat)),
          location = getMeterValueProperty(jval \ "location", Location.Outlet, new EnumNameSerializer(Location)),
          unit = getMeterValueProperty(jval \ "unit", UnitOfMeasure.Wh, new EnumNameSerializer(UnitOfMeasure)))
      }
    })
    */
  }

  object AuthorizationStatusConverters {
    val names = List(("Accepted", "Accepted"), ("IdTagBlocked", "Blocked"), ("IdTagExpired", "Expired"),
      ("IdTagInvalid", "Invalid"), ("ConcurrentTx", "ConcurrentTx"))
    val jsonToEnum = Map(names.map(_.swap): _*)
    val enumToJson = Map(names: _*)
  }

  private implicit class BooleanToStatusString(val b: Boolean) extends AnyVal {
    def toStatusString = if (b) "Accepted" else "Rejected"
  }

  private implicit class BooleanToUploadStatusString(val b: Boolean) extends AnyVal {
    def toUploadStatusString = if (b) "Uploaded" else "UploadFailed"
  }

  private implicit class RichKeyValue(val self: messages.KeyValue) {
    import self._

    def toV15: ConfigurationEntry = ConfigurationEntry(key, readonly, value)
  }

  private implicit class RichAuthListVersion(self: messages.AuthListVersion) {
    def toV15: Int = self match {
      case messages.AuthListNotSupported => -1
      case messages.AuthListSupported(i) => i
    }
  }

  private implicit class RichUpdateStatus(self: messages.UpdateStatus.Value) {
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
}
