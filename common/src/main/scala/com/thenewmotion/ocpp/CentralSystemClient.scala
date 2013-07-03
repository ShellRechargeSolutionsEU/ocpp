package com.thenewmotion.ocpp

import org.joda.time
import com.thenewmotion.ocpp
import dispatch.Http
import org.joda.time.DateTime
import scala.concurrent.duration.{FiniteDuration, SECONDS, MINUTES}
import scala.language.implicitConversions

/**
 * @author Yaroslav Klymko
 */
trait CentralSystemClient extends CentralSystemService with Client

object CentralSystemClient {
  def apply(chargeBoxIdentity: String,
            version: Version.Value,
            uri: Uri,
            http: Http,
            endpoint: Option[Uri] = None): CentralSystemClient = version match {
    case Version.V12 => new CentralSystemClientV12(chargeBoxIdentity, uri, http, endpoint)
    case Version.V15 => new CentralSystemClientV15(chargeBoxIdentity, uri, http, endpoint)
  }
}

private[ocpp] class CentralSystemClientV12(val chargeBoxIdentity: String, uri: Uri, http: Http, endpoint: Option[Uri])
  extends CentralSystemClient with ScalaxbClient {
  import v12._
  import ConvertersV12._

  def version = Version.V12

  type Service = CentralSystemService

  val service = new CustomDispatchHttpClients(http) with CentralSystemServiceSoapBindings with WsaAddressingSoapClients {
    override def baseAddress = uri
    def endpoint = CentralSystemClientV12.this.endpoint
  }.service

  def authorize(idTag: String) = ?(_.authorize, AuthorizeRequest(idTag)).toOcpp

  def startTransaction(connector: ConnectorScope,
                       idTag: IdTag,
                       timestamp: DateTime,
                       meterStart: Int,
                       reservationId: Option[Int]) = {
    reservationId.foreach(x => logNotSupported("startTransaction.reservationId", x))
    val req = StartTransactionRequest(connector.toOcpp, idTag, timestamp.toXMLCalendar, meterStart)
    val StartTransactionResponse(transactionId, idTagInfo) = ?(_.startTransaction, req)
    transactionId -> idTagInfo.toOcpp
  }

  def stopTransaction(transactionId: TransactionId,
                      idTag: Option[IdTag],
                      timestamp: DateTime,
                      meterStop: Int,
                      transactionData: List[TransactionData]) = {
    if (transactionData.nonEmpty)
      logNotSupported("stopTransaction.transactionData", transactionData.mkString("\n", "\n", "\n"))

    val req = StopTransactionRequest(transactionId, idTag, timestamp.toXMLCalendar, meterStop)
    ?(_.stopTransaction, req).idTagInfo.map(_.toOcpp)
  }

  def heartbeat = ?(_.heartbeat, HeartbeatRequest()).toDateTime

  def meterValues(scope: Scope, transactionId: Option[TransactionId], meters: List[Meter]) {
    def toMeter(x: Meter): Option[MeterValue] = {
      x.values.collectFirst {
        case Meter.DefaultValue(value) => MeterValue(x.timestamp.toXMLCalendar, value)
      }
    }
    ?(_.meterValues, MeterValuesRequest(scope.toOcpp, meters.flatMap(toMeter)))
  }

  def bootNotification(chargePointVendor: String,
                       chargePointModel: String,
                       chargePointSerialNumber: Option[String],
                       chargeBoxSerialNumber: Option[String],
                       firmwareVersion: Option[String],
                       iccid: Option[String],
                       imsi: Option[String],
                       meterType: Option[String],
                       meterSerialNumber: Option[String]) = {
    val req = BootNotificationRequest(
      chargePointVendor,
      chargePointModel,
      chargePointSerialNumber,
      chargeBoxSerialNumber,
      firmwareVersion,
      iccid,
      imsi,
      meterType,
      meterSerialNumber)

    val BootNotificationResponse(status, currentTime, heartbeatInterval) = ?(_.bootNotification, req)
    val accepted = status match {
      case AcceptedValue7 => true
      case RejectedValue6 => false
    }

    ocpp.BootNotificationResponse(
      accepted,
      currentTime.fold(DateTime.now)(_.toDateTime),
      heartbeatInterval.fold(FiniteDuration(15, MINUTES))(FiniteDuration(_, SECONDS)))
  }

  def statusNotification(scope: Scope,
                         status: ocpp.ChargePointStatus,
                         timestamp: Option[time.DateTime],
                         vendorId: Option[String]) {
    def toErrorCode(x: ocpp.ChargePointErrorCode.Value): ChargePointErrorCode = {
      import ocpp.{ChargePointErrorCode => ocpp}
      def notSupportedCode(x: ocpp.Value) = notSupported("statusNotification(ChargePointErrorCode.%s)".format(x))
      x match {
        case ocpp.ConnectorLockFailure => ConnectorLockFailure
        case ocpp.HighTemperature => HighTemperature
        case ocpp.Mode3Error => Mode3Error
        case ocpp.PowerMeterFailure => PowerMeterFailure
        case ocpp.PowerSwitchFailure => PowerSwitchFailure
        case ocpp.ReaderFailure => ReaderFailure
        case ocpp.ResetFailure => ResetFailure
        case ocpp.GroundFailure => notSupportedCode(ocpp.GroundFailure)
        case ocpp.OverCurrentFailure => notSupportedCode(ocpp.OverCurrentFailure)
        case ocpp.UnderVoltage => notSupportedCode(ocpp.UnderVoltage)
        case ocpp.WeakSignal => notSupportedCode(ocpp.WeakSignal)
        case ocpp.OtherError => notSupportedCode(ocpp.OtherError)
      }
    }

    def noError(status: ChargePointStatus) = (status, NoError)

    val (chargePointStatus, errorCode) = status match {
      case ocpp.Available => noError(Available)
      case ocpp.Occupied => noError(Occupied)
      case ocpp.Unavailable => noError(Unavailable)
      case ocpp.Reserved => notSupported("statusNotification(Reserved)")
      case ocpp.Faulted(code, info, vendorCode) =>
        info.foreach(x => logNotSupported("statusNotification.info", x))
        vendorCode.foreach(x => logNotSupported("statusNotification.vendorErrorCode", x))
        (Faulted, code.map(toErrorCode) getOrElse NoError)
    }

    timestamp.foreach(x => logNotSupported("statusNotification.timestamp", x))
    vendorId.foreach(x => logNotSupported("statusNotification.vendorId", x))

    ?(_.statusNotification, StatusNotificationRequest(scope.toOcpp, chargePointStatus, errorCode))
  }

  def firmwareStatusNotification(status: ocpp.FirmwareStatus.Value) {
    val firmwareStatus = {
      import ocpp.{FirmwareStatus => ocpp}
      status match {
        case ocpp.Downloaded => Downloaded
        case ocpp.DownloadFailed => DownloadFailed
        case ocpp.InstallationFailed => InstallationFailed
        case ocpp.Installed => Installed
      }
    }
    ?(_.firmwareStatusNotification, FirmwareStatusNotificationRequest(firmwareStatus))
  }

  def diagnosticsStatusNotification(uploaded: Boolean) {
    val status = if (uploaded) Uploaded else UploadFailed
    ?(_.diagnosticsStatusNotification, DiagnosticsStatusNotificationRequest(status))
  }

  def dataTransfer(vendorId: String, messageId: Option[String], data: Option[String]) = notSupported("dataTransfer")
}

private[ocpp] class CentralSystemClientV15(val chargeBoxIdentity: String, uri: Uri, http: Http, endpoint: Option[Uri])
  extends CentralSystemClient with ScalaxbClient {
  import v15._

  def version = Version.V15

  type Service = CentralSystemService

  val service = new CustomDispatchHttpClients(http) with CentralSystemServiceSoapBindings with WsaAddressingSoapClients {
    override def baseAddress = uri
    def endpoint = CentralSystemClientV15.this.endpoint
  }.service

  private implicit def toIdTagInfo(x: IdTagInfoType): ocpp.IdTagInfo = {
    val status = {
      import ocpp.{AuthorizationStatus => ocpp}
      x.status match {
        case AcceptedValue12 => ocpp.Accepted
        case BlockedValue => ocpp.IdTagBlocked
        case ExpiredValue => ocpp.IdTagExpired
        case InvalidValue => ocpp.IdTagInvalid
        case ConcurrentTxValue => ocpp.ConcurrentTx
      }
    }
    ocpp.IdTagInfo(status, x.expiryDate.map(_.toDateTime), x.parentIdTag)
  }

  def authorize(idTag: String) = ?[AuthorizeRequest, IdTagInfoType](_.authorize, AuthorizeRequest(idTag))

  def startTransaction(connector: ConnectorScope,
                       idTag: IdTag,
                       timestamp: DateTime,
                       meterStart: Int,
                       reservationId: Option[Int]) = {
    reservationId.foreach(x => logNotSupported("startTransaction.reservationId", x))
    val req = StartTransactionRequest(connector.toOcpp, idTag, timestamp.toXMLCalendar, meterStart)
    val StartTransactionResponse(transactionId, idTagInfo) = ?(_.startTransaction, req)
    transactionId -> idTagInfo
  }

  def stopTransaction(transactionId: TransactionId,
                      idTag: Option[IdTag],
                      timestamp: DateTime,
                      meterStop: Int,
                      transactionData: List[ocpp.TransactionData]) = {

    def toTransactionData(x: ocpp.TransactionData): TransactionData = TransactionData(x.meters.map(toMeter): _*)

    val req = StopTransactionRequest(transactionId, idTag, timestamp.toXMLCalendar, meterStop, transactionData.map(toTransactionData))
    ?(_.stopTransaction, req).idTagInfo.map(toIdTagInfo)
  }

  def heartbeat = ?(_.heartbeat, HeartbeatRequest()).toDateTime

  def meterValues(scope: Scope, transactionId: Option[TransactionId], meters: List[Meter]) {
    val req = MeterValuesRequest(scope.toOcpp, transactionId, meters.map(toMeter))
    ?(_.meterValues, req)
  }

  def bootNotification(chargePointVendor: String,
                       chargePointModel: String,
                       chargePointSerialNumber: Option[String],
                       chargeBoxSerialNumber: Option[String],
                       firmwareVersion: Option[String],
                       iccid: Option[String],
                       imsi: Option[String],
                       meterType: Option[String],
                       meterSerialNumber: Option[String]) = {
    val req = BootNotificationRequest(
      chargePointVendor,
      chargePointModel,
      chargePointSerialNumber,
      chargeBoxSerialNumber,
      firmwareVersion,
      iccid,
      imsi,
      meterType,
      meterSerialNumber)

    val BootNotificationResponse(status, currentTime, heartbeatInterval) = ?(_.bootNotification, req)
    val accepted = status match {
      case AcceptedValue11 => true
      case RejectedValue9 => false
    }

    ocpp.BootNotificationResponse(accepted, currentTime.toDateTime, FiniteDuration(heartbeatInterval, SECONDS))
  }

  def statusNotification(scope: Scope,
                         status: ocpp.ChargePointStatus,
                         timestamp: Option[time.DateTime],
                         vendorId: Option[String]) {
    def toErrorCode(x: ocpp.ChargePointErrorCode.Value): ChargePointErrorCode = {
      import ocpp.{ChargePointErrorCode => ocpp}
      x match {
        case ocpp.ConnectorLockFailure => ConnectorLockFailure
        case ocpp.HighTemperature => HighTemperature
        case ocpp.Mode3Error => Mode3Error
        case ocpp.PowerMeterFailure => PowerMeterFailure
        case ocpp.PowerSwitchFailure => PowerSwitchFailure
        case ocpp.ReaderFailure => ReaderFailure
        case ocpp.ResetFailure => ResetFailure
        case ocpp.GroundFailure => GroundFailure
        case ocpp.OverCurrentFailure => OverCurrentFailure
        case ocpp.UnderVoltage => UnderVoltage
        case ocpp.WeakSignal => WeakSignal
        case ocpp.OtherError => OtherError
      }
    }

    def noError(status: ChargePointStatus) = (status, NoError, None, None)

    val (chargePointStatus, errorCode, errorInfo, vendorErrorCode) = status match {
      case ocpp.Available => noError(Available)
      case ocpp.Occupied => noError(OccupiedValue)
      case ocpp.Faulted(code, info, vendorCode) =>
        (FaultedValue, code.map(toErrorCode) getOrElse NoError, info, vendorCode)
      case ocpp.Unavailable => noError(UnavailableValue)
      case ocpp.Reserved => noError(Reserved)
    }

    val req = StatusNotificationRequest(
      scope.toOcpp, chargePointStatus, errorCode,
      errorInfo, timestamp.map(_.toXMLCalendar), vendorId, vendorErrorCode)

    ?(_.statusNotification, req)
  }

  def firmwareStatusNotification(status: ocpp.FirmwareStatus.Value) {
    import ocpp.{FirmwareStatus => ocpp}
    val firmwareStatus = status match {
      case ocpp.Downloaded => Downloaded
      case ocpp.DownloadFailed => DownloadFailed
      case ocpp.InstallationFailed => InstallationFailed
      case ocpp.Installed => Installed
    }
    ?(_.firmwareStatusNotification, FirmwareStatusNotificationRequest(firmwareStatus))
  }

  def diagnosticsStatusNotification(uploaded: Boolean) {
    val status = if (uploaded) Uploaded else UploadFailed
    ?(_.diagnosticsStatusNotification, DiagnosticsStatusNotificationRequest(status))
  }

  def dataTransfer(vendorId: String, messageId: Option[String], data: Option[String]) = {
    val res = ?(_.dataTransfer,DataTransferRequestType(vendorId, messageId, data))
    val status = {
      import ocpp.{DataTransferStatus => ocpp}
      res.status match {
        case AcceptedValue13 => ocpp.Accepted
        case RejectedValue10 => ocpp.Rejected
        case UnknownMessageIdValue => ocpp.UnknownMessageId
        case UnknownVendorIdValue => ocpp.UnknownVendorId
      }
    }
    ocpp.DataTransferResponse(status, data)
  }

  def toMeter(x: Meter): MeterValue = {
    implicit def toReadingContext(x: Meter.ReadingContext.Value): ReadingContext = {
      import Meter.{ReadingContext => ocpp}
      x match {
        case ocpp.InterruptionBegin => Interruptionu46Begin
        case ocpp.InterruptionEnd => Interruptionu46End
        case ocpp.SampleClock => Sampleu46Clock
        case ocpp.SamplePeriodic => Sampleu46Periodic
        case ocpp.TransactionBegin => Transactionu46Begin
        case ocpp.TransactionEnd => Transactionu46End
      }
    }

    implicit def toValueFormat(x: Meter.ValueFormat.Value): ValueFormat = {
      import Meter.{ValueFormat => ocpp}
      x match {
        case ocpp.Raw => Raw
        case ocpp.Signed => SignedData
      }
    }

    implicit def toMeasurand(x: Meter.Measurand.Value): Measurand = {
      import Meter.{Measurand => ocpp}
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
      }
    }

    implicit def toLocation(x: Meter.Location.Value): Location = {
      import Meter.{Location => ocpp}
      x match {
        case ocpp.Inlet => Inlet
        case ocpp.Outlet => Outlet
        case ocpp.Body => Body
      }
    }

    implicit def toUnit(x: Meter.UnitOfMeasure.Value): UnitOfMeasure = {
      import Meter.{UnitOfMeasure => ocpp}
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
      }
    }

    def toValue(x: Meter.Value): Value = Value(
      x.value,
      Some(x.context),
      Some(x.format),
      Some(x.measurand),
      Some(x.location),
      Some(x.unit))

    MeterValue(x.timestamp.toXMLCalendar, x.values.map(toValue))
  }
}