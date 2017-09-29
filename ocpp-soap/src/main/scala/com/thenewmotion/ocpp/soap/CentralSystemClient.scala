package com.thenewmotion.ocpp
package soap

import com.thenewmotion.ocpp.messages._
import dispatch.Http
import java.time.ZonedDateTime
import scala.concurrent.duration._
import scala.language.implicitConversions

trait CentralSystemClient extends SyncCentralSystem with Client

object CentralSystemClient {
  def apply(chargeBoxIdentity: String,
            version: Version,
            uri: Uri,
            http: Http,
            endpoint: Option[Uri] = None): CentralSystemClient = version match {
    case Version.V12 => new CentralSystemClientV12(chargeBoxIdentity, uri, http, endpoint)
    case Version.V15 => new CentralSystemClientV15(chargeBoxIdentity, uri, http, endpoint)
  }
}

class CentralSystemClientV12(val chargeBoxIdentity: String, uri: Uri, http: Http, endpoint: Option[Uri])
  extends CentralSystemClient with ScalaxbClient {
  import com.thenewmotion.ocpp
  import ocpp.messages
  import ocpp.v12._
  import ConvertersV12._

  def version = Version.V12

  type Service = CentralSystemService

  val service = new CustomDispatchHttpClients(http) with CentralSystemServiceSoapBindings with WsaAddressingSoapClients {
    override def baseAddress = uri
    def endpoint = CentralSystemClientV12.this.endpoint
  }.service


  def authorize(req: messages.AuthorizeReq) = messages.AuthorizeRes(?(_.authorize, AuthorizeRequest(req.idTag)).toOcpp)

  def startTransaction(x: messages.StartTransactionReq) = {
    import x._
    reservationId.foreach(x => logNotSupported("startTransaction.reservationId", x))
    val req = StartTransactionRequest(connector.toOcpp, idTag, timestamp.toXMLCalendar, meterStart)
    val StartTransactionResponse(transactionId, idTagInfo) = ?(_.startTransaction, req)
    messages.StartTransactionRes(transactionId, idTagInfo.toOcpp)
  }

  def stopTransaction(req: messages.StopTransactionReq) = {
    import req._
    if (meters.nonEmpty)
      logNotSupported("stopTransaction.transactionData", meters.mkString("\n", "\n", "\n"))

    val x = StopTransactionRequest(transactionId, idTag, timestamp.toXMLCalendar, meterStop)
    messages.StopTransactionRes(?(_.stopTransaction, x).idTagInfo.map(_.toOcpp))
  }

  def heartbeat = messages.HeartbeatRes(?(_.heartbeat, HeartbeatRequest()).toDateTime)

  def meterValues(req: messages.MeterValuesReq) {
    import req._
    def toMeter(x: messages.meter.Meter): Option[MeterValue] = {
      x.values.collectFirst {
        case messages.meter.DefaultValue(value) => MeterValue(x.timestamp.toXMLCalendar, value)
      }
    }
    ?(_.meterValues, MeterValuesRequest(scope.toOcpp, meters.flatMap(toMeter)))
  }

  def bootNotification(req: messages.BootNotificationReq) = {
    import req._
    val x = BootNotificationRequest(
      chargePointVendor,
      chargePointModel,
      chargePointSerialNumber,
      chargeBoxSerialNumber,
      firmwareVersion,
      iccid,
      imsi,
      meterType,
      meterSerialNumber)

    val BootNotificationResponse(status, currentTime, heartbeatInterval) = ?(_.bootNotification, x)
    val accepted = status match {
      case AcceptedValue7 => messages.RegistrationStatus.Accepted
      case RejectedValue6 => messages.RegistrationStatus.Rejected
    }

    messages.BootNotificationRes(
      accepted,
      currentTime.fold(ZonedDateTime.now())(_.toDateTime),
      heartbeatInterval.fold(15.minutes)(FiniteDuration(_, SECONDS)))
  }

  def statusNotification(req: messages.StatusNotificationReq) {
    import req._
    def toErrorCode(x: messages.ChargePointErrorCode): ChargePointErrorCode = {
      import messages.{ChargePointErrorCode => ocpp}

      x match {
        case ocpp.ConnectorLockFailure => ConnectorLockFailure
        case ocpp.HighTemperature => HighTemperature
        case ocpp.EVCommunicationError => Mode3Error
        case ocpp.PowerMeterFailure => PowerMeterFailure
        case ocpp.PowerSwitchFailure => PowerSwitchFailure
        case ocpp.ReaderFailure => ReaderFailure
        case ocpp.ResetFailure => ResetFailure
        case ocpp.GroundFailure => notSupportedCode(ocpp.GroundFailure)
        case ocpp.OverCurrentFailure => notSupportedCode(ocpp.OverCurrentFailure)
        case ocpp.UnderVoltage => notSupportedCode(ocpp.UnderVoltage)
        case ocpp.WeakSignal => notSupportedCode(ocpp.WeakSignal)
        case ocpp.OtherError => notSupportedCode(ocpp.OtherError)
        case ocpp.InternalError => notSupportedCode(ocpp.InternalError)
        case ocpp.OverVoltage => notSupportedCode(ocpp.OverVoltage)
        case ocpp.LocalListConflict => notSupportedCode(ocpp.LocalListConflict)
      }
    }

    def noError(status: ChargePointStatus) = (status, NoError)

    val (chargePointStatus, errorCode) = {
      import messages.ChargePointStatus
      status match {
        case ChargePointStatus.Available(_) => noError(Available)
        case ChargePointStatus.Occupied(_, _) => noError(Occupied)
        case ChargePointStatus.Unavailable(_) => noError(Unavailable)
        case ChargePointStatus.Reserved(_) => notSupported("statusNotification(Reserved)")
        case ChargePointStatus.Faulted(code, info, vendorCode) =>
          info.foreach(x => logNotSupported("statusNotification.info", x))
          vendorCode.foreach(x => logNotSupported("statusNotification.vendorErrorCode", x))
          (Faulted, code.map(toErrorCode) getOrElse NoError)
      }
    }

    timestamp.foreach(x => logNotSupported("statusNotification.timestamp", x))
    vendorId.foreach(x => logNotSupported("statusNotification.vendorId", x))

    ?(_.statusNotification, StatusNotificationRequest(scope.toOcpp, chargePointStatus, errorCode))
  }

  def firmwareStatusNotification(req: messages.FirmwareStatusNotificationReq) {
    val firmwareStatus = {
      import messages.{FirmwareStatus => ocpp}
      req.status match {
        case ocpp.Downloaded => Downloaded
        case ocpp.DownloadFailed => DownloadFailed
        case ocpp.InstallationFailed => InstallationFailed
        case ocpp.Installed => Installed
        case ocpp.Installing => notSupportedCode(ocpp.Installing)
        case ocpp.Idle => notSupportedCode(ocpp.Idle)
        case ocpp.Downloading => notSupportedCode(ocpp.Downloading)
      }
    }
    ?(_.firmwareStatusNotification, FirmwareStatusNotificationRequest(firmwareStatus))
  }

  def diagnosticsStatusNotification(req: messages.DiagnosticsStatusNotificationReq) = {
    val status = if (req.status == messages.DiagnosticsStatus.Uploaded) Uploaded else UploadFailed
    ?(_.diagnosticsStatusNotification, DiagnosticsStatusNotificationRequest(status))
    messages.DiagnosticsStatusNotificationRes
  }

  def dataTransfer(req: messages.CentralSystemDataTransferReq) = notSupported("dataTransfer")
}


class CentralSystemClientV15(val chargeBoxIdentity: String, uri: Uri, http: Http, endpoint: Option[Uri])
  extends CentralSystemClient with ScalaxbClient {
  import com.thenewmotion.ocpp
  import ocpp.messages
  import ocpp.v15._

  def version = Version.V15

  type Service = CentralSystemService

  val service = new CustomDispatchHttpClients(http) with CentralSystemServiceSoapBindings with WsaAddressingSoapClients {
    override def baseAddress = uri
    def endpoint = CentralSystemClientV15.this.endpoint
  }.service


  private implicit def toIdTagInfo(x: IdTagInfoType): messages.IdTagInfo = {
    val status = {
      import messages.{AuthorizationStatus => ocpp}
      x.status match {
        case AcceptedValue12 => ocpp.Accepted
        case BlockedValue => ocpp.IdTagBlocked
        case ExpiredValue => ocpp.IdTagExpired
        case InvalidValue => ocpp.IdTagInvalid
        case ConcurrentTxValue => ocpp.ConcurrentTx
      }
    }
    messages.IdTagInfo(status, x.expiryDate.map(_.toDateTime), x.parentIdTag)
  }

  def authorize(req: messages.AuthorizeReq) =
    messages.AuthorizeRes(?[AuthorizeRequest, IdTagInfoType](_.authorize, AuthorizeRequest(req.idTag)))

  def startTransaction(req: messages.StartTransactionReq) = {
    import req._
    reservationId.foreach(x => logNotSupported("startTransaction.reservationId", x))
    val x = StartTransactionRequest(connector.toOcpp, idTag, timestamp.toXMLCalendar, meterStart)
    val StartTransactionResponse(transactionId, idTagInfo) = ?(_.startTransaction, x)
    messages.StartTransactionRes(transactionId, idTagInfo)
  }

  def stopTransaction(req: messages.StopTransactionReq) = {
    import req._

    val x = StopTransactionRequest(transactionId, idTag, timestamp.toXMLCalendar, meterStop, Seq(TransactionData(req.meters.map(toMeterValue))))
    messages.StopTransactionRes(?(_.stopTransaction, x).idTagInfo.map(toIdTagInfo))
  }

  def heartbeat = messages.HeartbeatRes(?(_.heartbeat, HeartbeatRequest()).toDateTime)

  def meterValues(req: messages.MeterValuesReq) {
    import req._
    val x = MeterValuesRequest(scope.toOcpp, transactionId, meters.map(toMeterValue))
    ?(_.meterValues, x)
  }

  def bootNotification(req: messages.BootNotificationReq) = {
    import req._
    val x = BootNotificationRequest(
      chargePointVendor,
      chargePointModel,
      chargePointSerialNumber,
      chargeBoxSerialNumber,
      firmwareVersion,
      iccid,
      imsi,
      meterType,
      meterSerialNumber)

    val BootNotificationResponse(status, currentTime, heartbeatInterval) = ?(_.bootNotification, x)
    val accepted = status match {
      case AcceptedValue11 => messages.RegistrationStatus.Accepted
      case RejectedValue9 => messages.RegistrationStatus.Rejected
    }

    messages.BootNotificationRes(accepted, currentTime.toDateTime, FiniteDuration(heartbeatInterval, SECONDS))
  }

  def statusNotification(req: messages.StatusNotificationReq) {
    import req._
    def toErrorCode(x: messages.ChargePointErrorCode): ChargePointErrorCode = {
      import messages.{ChargePointErrorCode => ocpp}
      x match {
        case ocpp.ConnectorLockFailure => ConnectorLockFailure
        case ocpp.HighTemperature => HighTemperature
        case ocpp.EVCommunicationError => Mode3Error
        case ocpp.PowerMeterFailure => PowerMeterFailure
        case ocpp.PowerSwitchFailure => PowerSwitchFailure
        case ocpp.ReaderFailure => ReaderFailure
        case ocpp.ResetFailure => ResetFailure
        case ocpp.GroundFailure => GroundFailure
        case ocpp.OverCurrentFailure => OverCurrentFailure
        case ocpp.UnderVoltage => UnderVoltage
        case ocpp.WeakSignal => WeakSignal
        case ocpp.OtherError => OtherError
        case ocpp.InternalError => notSupportedCode(ocpp.InternalError)
        case ocpp.OverVoltage => notSupportedCode(ocpp.OverVoltage)
        case ocpp.LocalListConflict => notSupportedCode(ocpp.LocalListConflict)
      }
    }

    def noError(status: ChargePointStatus, info: Option[String]) = (status, NoError, info, None)

    val (chargePointStatus, errorCode, errorInfo, vendorErrorCode) = {
      import messages.ChargePointStatus
      status match {
        case ChargePointStatus.Available(info) => noError(Available, info)
        case ChargePointStatus.Occupied(_, info) => noError(OccupiedValue, info)
        case ChargePointStatus.Faulted(code, info, vendorCode) =>
          (FaultedValue, code.map(toErrorCode) getOrElse NoError, info, vendorCode)
        case ChargePointStatus.Unavailable(info) => noError(UnavailableValue, info)
        case ChargePointStatus.Reserved(info) => noError(Reserved, info)
      }
    }

    val x = StatusNotificationRequest(
      scope.toOcpp, chargePointStatus, errorCode,
      errorInfo, timestamp.map(_.toXMLCalendar), vendorId, vendorErrorCode)

    ?(_.statusNotification, x)
  }

  def firmwareStatusNotification(req: messages.FirmwareStatusNotificationReq) {
    import req._
    import messages.{FirmwareStatus => ocpp}
    val firmwareStatus = status match {
      case ocpp.Downloaded => Downloaded
      case ocpp.DownloadFailed => DownloadFailed
      case ocpp.InstallationFailed => InstallationFailed
      case ocpp.Installed => Installed
      case ocpp.Installing => notSupportedCode(ocpp.Installing)
      case ocpp.Idle => notSupportedCode(ocpp.Idle)
      case ocpp.Downloading => notSupportedCode(ocpp.Downloading)
    }
    ?(_.firmwareStatusNotification, FirmwareStatusNotificationRequest(firmwareStatus))
  }

  def diagnosticsStatusNotification(req: messages.DiagnosticsStatusNotificationReq) {
    val status = if (req.status == messages.DiagnosticsStatus.Uploaded) Uploaded else UploadFailed
    ?(_.diagnosticsStatusNotification, DiagnosticsStatusNotificationRequest(status))
  }

  def dataTransfer(req: messages.CentralSystemDataTransferReq) = {
    import req._
    val res = ?(_.dataTransfer, DataTransferRequestType(vendorId, messageId, data))
    val status = {
      import messages.{DataTransferStatus => ocpp}
      res.status match {
        case AcceptedValue13 => ocpp.Accepted
        case RejectedValue10 => ocpp.Rejected
        case UnknownMessageIdValue => ocpp.UnknownMessageId
        case UnknownVendorIdValue => ocpp.UnknownVendorId
      }
    }
    messages.CentralSystemDataTransferRes(status, stringOption(res.data))
  }


  def toMeterValue(x: messages.meter.Meter): MeterValue = {
    implicit def toReadingContext(x: messages.meter.ReadingContext): ReadingContext = {
      import messages.meter.{ReadingContext => ocpp}
      x match {
        case ocpp.InterruptionBegin => Interruptionu46Begin
        case ocpp.InterruptionEnd => Interruptionu46End
        case ocpp.SampleClock => Sampleu46Clock
        case ocpp.SamplePeriodic => Sampleu46Periodic
        case ocpp.TransactionBegin => Transactionu46Begin
        case ocpp.TransactionEnd => Transactionu46End
        case ocpp.Trigger => notSupportedCode(ocpp.Trigger)
        case ocpp.Other => notSupportedCode(ocpp.Other)
      }
    }

    implicit def toValueFormat(x: messages.meter.ValueFormat): ValueFormat = {
      import messages.meter.{ValueFormat => ocpp}
      x match {
        case ocpp.Raw => Raw
        case ocpp.Signed => SignedData
      }
    }

    implicit def toMeasurand(x: messages.meter.Measurand): Measurand = {
      import messages.meter.{Measurand => ocpp}
      x match {
        case ocpp.EnergyActiveExportRegister => Energyu46Activeu46Exportu46Register
        case ocpp.EnergyActiveImportRegister => Energyu46Activeu46Importu46Register
        case ocpp.EnergyReactiveExportRegister => Energyu46Reactiveu46Exportu46Register
        case ocpp.EnergyReactiveImportRegister => Energyu46Reactiveu46Importu46Register
        case ocpp.EnergyActiveExportInterval => Energyu46Activeu46Exportu46Interval
        case ocpp.EnergyActiveImportInterval => Energyu46Activeu46Importu46Interval
        case ocpp.EnergyReactiveExportInterval => Energyu46Reactiveu46Exportu46Interval
        case ocpp.EnergyReactiveImportInterval => Energyu46Reactiveu46Importu46Interval
        case ocpp.PowerActiveExport => Poweru46Activeu46Export
        case ocpp.PowerActiveImport => Poweru46Activeu46Import
        case ocpp.PowerReactiveExport => Poweru46Reactiveu46Export
        case ocpp.PowerReactiveImport => Poweru46Reactiveu46Import
        case ocpp.CurrentExport => Currentu46Export
        case ocpp.CurrentImport => Currentu46Import
        case ocpp.Voltage => Voltage
        case ocpp.Temperature => Temperature
        case ocpp.CurrentOffered => notSupportedCode(ocpp.CurrentOffered)
        case ocpp.FanSpeedInRotationsPerMinute => notSupportedCode(ocpp.FanSpeedInRotationsPerMinute)
        case ocpp.Frequency => notSupportedCode(ocpp.Frequency)
        case ocpp.PowerFactor => notSupportedCode(ocpp.PowerFactor)
        case ocpp.PowerOffered => notSupportedCode(ocpp.PowerOffered)
        case ocpp.StateOfChargeInPercentage => notSupportedCode(ocpp.StateOfChargeInPercentage)
      }
    }

    implicit def toLocation(x: meter.Location): Location = {
      import meter.{Location => ocpp}
      x match {
        case ocpp.Inlet => Inlet
        case ocpp.Outlet => Outlet
        case ocpp.Body => Body
        case ocpp.Cable => notSupportedCode(ocpp.Cable)
        case ocpp.Ev => notSupportedCode(ocpp.Ev)
      }
    }

    implicit def toUnit(x: messages.meter.UnitOfMeasure): UnitOfMeasure = {
      import messages.meter.{UnitOfMeasure => ocpp}
      x match {
        case ocpp.Wh => Wh
        case ocpp.Kwh => KWh
        case ocpp.Varh => Varh
        case ocpp.Kvarh => Kvarh
        case ocpp.W => W
        case ocpp.Kw => KW
        case ocpp.Var => Var
        case ocpp.Kvar => Kvar
        case ocpp.Amp => Amp
        case ocpp.Volt => Volt
        case ocpp.Celsius => Celsius
        case ocpp.Fahrenheit => notSupportedCode(ocpp.Fahrenheit)
        case ocpp.Kelvin => notSupportedCode(ocpp.Kelvin)
        case ocpp.Kva => notSupportedCode(ocpp.Kva)
        case ocpp.Percent => notSupportedCode(ocpp.Percent)
        case ocpp.Va => notSupportedCode(ocpp.Va)
      }
    }

    def toValue(x: messages.meter.Value): Value = Value(
      x.value,
      Map(
      "context" -> scalaxb.DataRecord(x.context.name),
      "format" -> scalaxb.DataRecord(x.format.name),
      "measurand" -> scalaxb.DataRecord(x.measurand.name),
      "location" -> scalaxb.DataRecord(x.location.name),
      "unit" -> scalaxb.DataRecord(x.unit.name)))

    MeterValue(x.timestamp.toXMLCalendar, x.values.map(toValue))
  }
}
