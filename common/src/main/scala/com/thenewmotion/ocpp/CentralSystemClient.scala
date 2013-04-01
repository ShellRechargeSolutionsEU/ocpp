package com.thenewmotion.ocpp

import com.thenewmotion.time.Imports._
import java.net.URI
import org.joda.time
import com.thenewmotion.{ocpp, Logging}
import scalaxb.SoapClients
import dispatch.Http

/**
 * @author Yaroslav Klymko
 */
trait CentralSystemClient extends CentralSystemService with Client

class CentralSystemClientV12(val chargeBoxIdentity: String, uri: URI, http: Http) extends CentralSystemClient with Logging {
  import v12._

  val bindings = new CustomDispatchHttpClients(http) with CentralSystemServiceSoapBindings with SoapClients {
    override def baseAddress = uri
  }

  private def ?[T](f: CentralSystemService => Either[scalaxb.Fault[Any], T]): T = rightOrException(f(bindings.service))

  private implicit def toIdTagInfo(x: IdTagInfo): ocpp.IdTagInfo = {
    val status = {
      import ocpp.{AuthorizationStatus => ocpp}
      x.status match {
        case AcceptedValue7 => ocpp.Accepted
        case Blocked => ocpp.IdTagBlocked
        case Expired => ocpp.IdTagExpired
        case Invalid => ocpp.IdTagInvalid
        case ConcurrentTx => ocpp.ConcurrentTx
      }
    }
    ocpp.IdTagInfo(status, x.expiryDate, x.parentIdTag)
  }

  def authorize(idTag: String) = ?[IdTagInfo](_.authorize(AuthorizeRequest(idTag), id))

  def startTransaction(connector: ConnectorScope,
                       idTag: IdTag,
                       timestamp: DateTime,
                       meterStart: Int,
                       reservationId: Option[Int]) = {
    reservationId.foreach(x => logNotSupported("startTransaction.reservationId", x))
    val req = StartTransactionRequest(connector.toOcpp, idTag, timestamp, meterStart)
    val StartTransactionResponse(transactionId, idTagInfo) = ?(_.startTransaction(req, id))
    transactionId -> idTagInfo
  }

  def stopTransaction(transactionId: TransactionId,
                      idTag: Option[IdTag],
                      timestamp: DateTime,
                      meterStop: Int,
                      transactionData: List[TransactionData]) = {
    if (transactionData.nonEmpty)
      logNotSupported("stopTransaction.transactionData", transactionData.mkString("\n", "\n", "\n"))

    val req = StopTransactionRequest(transactionId, idTag, timestamp, meterStop)
    ?(_.stopTransaction(req, id)).idTagInfo.map(toIdTagInfo)
  }

  def heartbeat = xmlGregCalendar2DateTime(?(_.heartbeat(HeartbeatRequest(), id)))

  def meterValues(scope: Scope, transactionId: Option[TransactionId], meters: List[Meter]) {
    def toMeter(x: Meter): Option[MeterValue] = {
      x.values.collectFirst {
        case Meter.DefaultValue(value) => MeterValue(x.timestamp, value)
      }
    }
    ?(_.meterValues(MeterValuesRequest(scope.toOcpp, meters.flatMap(toMeter)), id))
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

    val BootNotificationResponse(status, currentTime, heartbeatInterval) = ?(_.bootNotification(req, id))
    val accepted = status match {
      case AcceptedValue6 => true
      case RejectedValue6 => false
    }

    ocpp.BootNotificationResponse(
      accepted,
      currentTime.map(implicitly[DateTime](_)) getOrElse DateTime.now,
      heartbeatInterval getOrElse 900)
  }

  def statusNotification(scope: Scope,
                         status: ocpp.ChargePointStatus,
                         timestamp: Option[time.DateTime],
                         vendorId: Option[String]) {
    def toErrorCode(x: ocpp.ChargePointErrorCode.Value): ChargePointErrorCode = {
      import ocpp.{ChargePointErrorCode => ocpp}
      def notSupportedCode(x: ocpp.Value) = notSupported("ChargePointErrorCode.%s".format(x))
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
      case ocpp.Reserved => throw new NotSupportedOperationException("ChargePointStatus.Reserved is not supported in ocpp 1.2")
      case ocpp.Faulted(code, info, vendorCode) =>
        info.foreach(x => logNotSupported("statusNotification.info", x))
        vendorCode.foreach(x => logNotSupported("statusNotification.vendorErrorCode", x))
        (Faulted, code.map(toErrorCode) getOrElse NoError)
    }

    timestamp.foreach(x => logNotSupported("statusNotification.timestamp", x))
    vendorId.foreach(x => logNotSupported("statusNotification.vendorId", x))

    ?(_.statusNotification(StatusNotificationRequest(scope.toOcpp, chargePointStatus, errorCode), id))
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
    ?(_.firmwareStatusNotification(FirmwareStatusNotificationRequest(firmwareStatus), id))
  }

  def diagnosticsStatusNotification(uploaded: Boolean) {
    val status = if (uploaded) Uploaded else UploadFailed
    ?(_.diagnosticsStatusNotification(DiagnosticsStatusNotificationRequest(status), id))
  }

  // since OCPP 1.5
  def dataTransfer(vendorId: String, messageId: Option[String], data: Option[String]) =
    notSupported("CentralSystemClient.dataTransfer")

  private def logNotSupported(name: String, value: Any) {
    log.warn("%s is not supported in OCPP 1.2, value: %s".format(name, value))
  }

  private def notSupported(x: String): Nothing =
    throw new NotSupportedOperationException("%s is not supported in ocpp 1.2".format(x))
}

class CentralSystemClientV15(val chargeBoxIdentity: String, uri: URI, http: Http) extends CentralSystemClient with Logging {
  import v15._

  val bindings = new CustomDispatchHttpClients(http) with CentralSystemServiceSoapBindings with SoapClients {
    override def baseAddress = uri
  }

  private def ?[T](f: CentralSystemService => Either[scalaxb.Fault[Any], T]): T = rightOrException(f(bindings.service))

  private implicit def toIdTagInfo(x: IdTagInfoType): ocpp.IdTagInfo = {
    val status = {
      import ocpp.{AuthorizationStatus => ocpp}
      x.status match {
        case AcceptedValue13 => ocpp.Accepted
        case BlockedValue => ocpp.IdTagBlocked
        case ExpiredValue => ocpp.IdTagExpired
        case InvalidValue => ocpp.IdTagInvalid
        case ConcurrentTxValue => ocpp.ConcurrentTx
      }
    }
    ocpp.IdTagInfo(status, x.expiryDate, x.parentIdTag)
  }

  def logNotSupported(name: String, value: Any) {
    log.warn("%s is not supported in OCPP 1.2, value: %s".format(name, value))
  }

  def authorize(idTag: String) = ?[IdTagInfoType](_.authorize(AuthorizeRequest(idTag), id))

  def startTransaction(connector: ConnectorScope,
                       idTag: IdTag,
                       timestamp: DateTime,
                       meterStart: Int,
                       reservationId: Option[Int]) = {
    reservationId.foreach(x => logNotSupported("startTransaction.reservationId", x))
    val req = StartTransactionRequest(connector.toOcpp, idTag, timestamp, meterStart)
    val StartTransactionResponse(transactionId, idTagInfo) = ?(_.startTransaction(req, id))
    transactionId -> idTagInfo
  }

  def stopTransaction(transactionId: TransactionId,
                      idTag: Option[IdTag],
                      timestamp: DateTime,
                      meterStop: Int,
                      transactionData: List[ocpp.TransactionData]) = {

    def toTransactionData(x: ocpp.TransactionData): TransactionData = TransactionData(x.meters.map(toMeter): _*)

    val req = StopTransactionRequest(transactionId, idTag, timestamp, meterStop, transactionData.map(toTransactionData))
    ?(_.stopTransaction(req, id)).idTagInfo.map(toIdTagInfo)
  }

  def heartbeat = xmlGregCalendar2DateTime(?(_.heartbeat(HeartbeatRequest(), id)))

  def meterValues(scope: Scope, transactionId: Option[TransactionId], meters: List[Meter]) {
    val req = MeterValuesRequest(scope.toOcpp, transactionId, meters.map(toMeter))
    ?(_.meterValues(req, id))
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

    val BootNotificationResponse(status, currentTime, heartbeatInterval) = ?(_.bootNotification(req, id))
    val accepted = status match {
      case AcceptedValue12 => true
      case RejectedValue10 => false
    }

    ocpp.BootNotificationResponse(accepted, currentTime, heartbeatInterval)
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
      errorInfo, timestamp, vendorId, vendorErrorCode)

    ?(_.statusNotification(req, id))
  }

  def firmwareStatusNotification(status: ocpp.FirmwareStatus.Value) {
    import ocpp.{FirmwareStatus => ocpp}
    val firmwareStatus = status match {
      case ocpp.Downloaded => Downloaded
      case ocpp.DownloadFailed => DownloadFailed
      case ocpp.InstallationFailed => InstallationFailed
      case ocpp.Installed => Installed
    }
    ?(_.firmwareStatusNotification(FirmwareStatusNotificationRequest(firmwareStatus), id))
  }

  def diagnosticsStatusNotification(uploaded: Boolean) {
    val status = if (uploaded) Uploaded else UploadFailed
    ?(_.diagnosticsStatusNotification(DiagnosticsStatusNotificationRequest(status), id))
  }

  def dataTransfer(vendorId: String, messageId: Option[String], data: Option[String]) = {
    val res = ?(_.dataTransfer(DataTransferRequestType(vendorId, messageId, data), id))
    val status = {
      import ocpp.DataTransferResponse.{Status => ocpp}
      res.status match {
        case AcceptedValue11 => ocpp.Accepted
        case RejectedValue9 => ocpp.Rejected
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
        case ocpp.InterruptionBegin => InterruptionBegin
        case ocpp.InterruptionEnd => InterruptionEnd
        case ocpp.SampleClock => SampleClock
        case ocpp.SamplePeriodic => SamplePeriodic
        case ocpp.TransactionBegin => TransactionBegin
        case ocpp.TransactionEnd => TransactionEnd
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
        case ocpp.EnergyActiveExportRegister => EnergyActiveExportRegister
        case ocpp.EnergyActiveImportRegister => EnergyActiveImportRegister
        case ocpp.EnergyReactiveExportRegister => EnergyReactiveExportRegister
        case ocpp.EnergyReactiveImportRegister => EnergyReactiveImportRegister
        case ocpp.EnergyActiveExportInterval => EnergyActiveExportInterval
        case ocpp.EnergyActiveImportInterval => EnergyActiveImportInterval
        case ocpp.EnergyReactiveExportInterval => EnergyReactiveExportInterval
        case ocpp.EnergyReactiveImportInterval => EnergyReactiveImportInterval
        case ocpp.PowerActiveExport => PowerActiveExport
        case ocpp.PowerActiveImport => PowerActiveImport
        case ocpp.PowerReactiveExport => PowerReactiveExport
        case ocpp.PowerReactiveImport => PowerReactiveImport
        case ocpp.CurrentExport => CurrentExport
        case ocpp.CurrentImport => CurrentImport
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

    MeterValue(x.timestamp, x.values.map(toValue))
  }
}

class NotSupportedOperationException(msg: String) extends RuntimeException(msg)