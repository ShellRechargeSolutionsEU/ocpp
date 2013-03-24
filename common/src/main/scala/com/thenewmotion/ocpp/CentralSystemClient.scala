package com.thenewmotion.ocpp

import com.thenewmotion.time.Imports._
import java.net.URI
import org.joda.time
import com.thenewmotion.{ocpp, Logging}
import scalaxb.{SoapClients, Fault}
import javax.xml.datatype.XMLGregorianCalendar

/**
 * @author Yaroslav Klymko
 */
trait CentralSystemClient extends CentralSystemService {

  //TODO duplication
  protected def rightOrException[T](x: Either[Fault[Any], T]) = x match {
    case Left(fault) => sys.error(fault.toString) // TODO
    case Right(t) => t
  }

  // todo move to scalax, why it doesn't work directly?
  protected implicit def fromOptionToOption[A, B](from: Option[A])(implicit conversion: A => B): Option[B] = from.map(conversion(_))

  // todo
  protected implicit def dateTime(x: Option[XMLGregorianCalendar]): Option[DateTime] = fromOptionToOption(x)

  protected implicit def xmlGregorianCalendarOption(x: Option[DateTime]): Option[XMLGregorianCalendar] = fromOptionToOption(x)
}

class CentralSystemClientV12(uri: URI, chargeBoxIdentity: String) extends CentralSystemClient with Logging {
  import v12._

  //TODO duplication
  private def id = chargeBoxIdentity

  val bindings = new CentralSystemServiceSoapBindings with SoapClients with FixedDispatchHttpClients {
    override def baseAddress = uri
  }

  private def ?[T](f: CentralSystemService => Either[scalaxb.Fault[Any], T]): T = rightOrException(f(bindings.service))

  private implicit def toIdTagInfo(x: IdTagInfo): ocpp.IdTagInfo = {
    val status = x.status match {
      case AcceptedValue7 => AuthorizationAccepted
      case Blocked => IdTagBlocked
      case Expired => IdTagExpired
      case Invalid => IdTagInvalid
      case ConcurrentTx => ocpp.ConcurrentTx
    }
    ocpp.IdTagInfo(status, x.expiryDate, x.parentIdTag)
  }

  def authorize(idTag: String) = ?[IdTagInfo](_.authorize(AuthorizeRequest(idTag), id))

  def logNotSupported(name: String, value: Any) {
    log.warn("%s is not supported in OCPP 1.2, value: %s".format(name, value))
  }

  def startTransaction(connectorId: Int,
                       idTag: IdTag,
                       timestamp: DateTime,
                       meterStart: Int,
                       reservationId: Option[Int]) = {
    reservationId.foreach(x => logNotSupported("startTransaction.reservationId", x))
    val req = StartTransactionRequest(connectorId, idTag, timestamp, meterStart)
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

  def meterValues(connectorId: Int, values: List[Meter.Value]) {
    // TODO need more details on this
    //    ?(_.meterValues())
    sys.error("TODO")
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

  // todo disallow passing Reserved status and error codes
  def statusNotification(connectorId: Int,
                         status: ocpp.ChargePointStatus,
                         timestamp: Option[time.DateTime],
                         vendorId: Option[String]) {

    def toErrorCode(x: ocpp.ChargePointErrorCode): ChargePointErrorCode = x match {
      case ocpp.ConnectorLockFailure => ConnectorLockFailure
      case ocpp.HighTemperature => HighTemperature
      case ocpp.Mode3Error => Mode3Error
      case ocpp.PowerMeterFailure => PowerMeterFailure
      case ocpp.PowerSwitchFailure => PowerSwitchFailure
      case ocpp.ReaderFailure => ReaderFailure
      case ocpp.ResetFailure => ResetFailure
      // since OCPP 1.5
      case ocpp.GroundFailure => sys.error("TODO")
      case ocpp.OverCurrentFailure => sys.error("TODO")
      case ocpp.UnderVoltage => sys.error("TODO")
      case ocpp.WeakSignal => sys.error("TODO")
      case ocpp.OtherError => sys.error("TODO")
    }

    def noError(status: ChargePointStatus) = (status, NoError)

    val (chargePointStatus, errorCode) = status match {
      case ocpp.Available => noError(Available)
      case ocpp.Occupied => noError(Occupied)
      case ocpp.Unavailable => noError(Unavailable)
      case ocpp.Reserved => sys.error("TODO")
      case ocpp.Faulted(code, info, vendorCode) =>
        info.foreach(x => logNotSupported("statusNotification.info", x))
        vendorCode.foreach(x => logNotSupported("statusNotification.vendorErrorCode", x))
        (Faulted, toErrorCode(code))
    }

    timestamp.foreach(x => logNotSupported("statusNotification.timestamp", x))
    vendorId.foreach(x => logNotSupported("statusNotification.vendorId", x))

    ?(_.statusNotification(StatusNotificationRequest(connectorId, chargePointStatus, errorCode), id))
  }

  def firmwareStatusNotification(status: ocpp.FirmwareStatus) {
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

  // since OCPP 1.5
  def dataTransfer(vendorId: String, messageId: Option[String], data: Option[String]) = sys.error("TODO")
}


class CentralSystemClientV15(uri: URI, chargeBoxIdentity: String) extends CentralSystemClient with Logging {
  import v15._

  //TODO duplication
  private def id = chargeBoxIdentity

  val bindings = new CentralSystemServiceSoapBindings with SoapClients with FixedDispatchHttpClients {
    override def baseAddress = uri
  }

  private def ?[T](f: CentralSystemService => Either[scalaxb.Fault[Any], T]): T = rightOrException(f(bindings.service))

  private implicit def toIdTagInfo(x: IdTagInfoType): ocpp.IdTagInfo = {
    val status = x.status match {
      case AcceptedValue13 => AuthorizationAccepted
      case BlockedValue => IdTagBlocked
      case ExpiredValue => IdTagExpired
      case InvalidValue => IdTagInvalid
      case ConcurrentTxValue => ocpp.ConcurrentTx
    }
    ocpp.IdTagInfo(status, x.expiryDate, x.parentIdTag)
  }

  def logNotSupported(name: String, value: Any) {
    log.warn("%s is not supported in OCPP 1.2, value: %s".format(name, value))
  }

  def authorize(idTag: String) = ?[IdTagInfoType](_.authorize(AuthorizeRequest(idTag), id))

  def startTransaction(connectorId: Int,
                       idTag: IdTag,
                       timestamp: DateTime,
                       meterStart: Int,
                       reservationId: Option[Int]) = {
    reservationId.foreach(x => logNotSupported("startTransaction.reservationId", x))
    val req = StartTransactionRequest(connectorId, idTag, timestamp, meterStart)
    val StartTransactionResponse(transactionId, idTagInfo) = ?(_.startTransaction(req, id))
    transactionId -> idTagInfo
  }

  def stopTransaction(transactionId: TransactionId,
                      idTag: Option[IdTag],
                      timestamp: DateTime,
                      meterStop: Int,
                      transactionData: List[ocpp.TransactionData]) = {

    def toReadingContext(x: Meter.ReadingContext): ReadingContext = x match {
      case Meter.InterruptionBegin => InterruptionBegin
      case Meter.InterruptionEnd => InterruptionEnd
      case Meter.SampleClock => SampleClock
      case Meter.SamplePeriodic => SamplePeriodic
      case Meter.TransactionBegin => TransactionBegin
      case Meter.TransactionEnd => TransactionEnd
    }

    def toValueFormat(x: Meter.ValueFormat): ValueFormat = x match {
      case Meter.RawFormat => Raw
      case Meter.SignedData => SignedData
    }

    def toMeasurand(x: Meter.Measurand): Measurand = x match {
      case Meter.EnergyActiveExportRegister => EnergyActiveExportRegister
      case Meter.EnergyActiveImportRegister => EnergyActiveImportRegister
      case Meter.EnergyReactiveExportRegister => EnergyReactiveExportRegister
      case Meter.EnergyReactiveImportRegister => EnergyReactiveImportRegister
      case Meter.EnergyActiveExportInterval => EnergyActiveExportInterval
      case Meter.EnergyActiveImportInterval => EnergyActiveImportInterval
      case Meter.EnergyReactiveExportInterval => EnergyReactiveExportInterval
      case Meter.EnergyReactiveImportInterval => EnergyReactiveImportInterval
      case Meter.PowerActiveExport => PowerActiveExport
      case Meter.PowerActiveImport => PowerActiveImport
      case Meter.PowerReactiveExport => PowerReactiveExport
      case Meter.PowerReactiveImport => PowerReactiveImport
      case Meter.CurrentExport => CurrentExport
      case Meter.CurrentImport => CurrentImport
      case Meter.Voltage => Voltage
      case Meter.Temperature => Temperature
    }

    def toLocation(x: Meter.Location): Location = x match {
      case Meter.Inlet => Inlet
      case Meter.Outlet => Outlet
      case Meter.Body => Body
    }

    def toUnit(x: Meter.UnitOfMeasure): UnitOfMeasure = x match {
      case Meter.Wh => Wh
      case Meter.KWh => KWh
      case Meter.Varh => Varh
      case Meter.Kvarh => Kvarh
      case Meter.W => W
      case Meter.KW => KW
      case Meter.Var => Var
      case Meter.Kvar => Kvar
      case Meter.Amp => Amp
      case Meter.Volt => Volt
      case Meter.Celsius => Celsius
    }

    def toValue(x: Meter.Value): Value = Value(
      value = x.value,
      context = x.context map toReadingContext,
      format = x.format map toValueFormat,
      measurand = x.measurand map toMeasurand,
      location = x.location map toLocation,
      unit = x.unit map toUnit)

    def toMeter(x: Meter): MeterValue = MeterValue(x.timestamp, x.values.map(toValue))

    def toTransactionData(x: ocpp.TransactionData): TransactionData = TransactionData(x.values.map(toMeter): _* )

    val req = StopTransactionRequest(transactionId, idTag, timestamp, meterStop, transactionData.map(toTransactionData))
    ?(_.stopTransaction(req, id)).idTagInfo.map(toIdTagInfo)
  }

  def heartbeat = xmlGregCalendar2DateTime(?(_.heartbeat(HeartbeatRequest(), id)))

  def meterValues(connectorId: Int, values: List[Meter.Value]) {
    // TODO need more details on this
    //    ?(_.meterValues())
    sys.error("TODO")
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

  // todo disallow passing Reserved status and error codes
  def statusNotification(connectorId: Int,
                         status: ocpp.ChargePointStatus,
                         timestamp: Option[time.DateTime],
                         vendorId: Option[String]) {

    def toErrorCode(x: ocpp.ChargePointErrorCode): ChargePointErrorCode = x match {
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

    def noError(status: ChargePointStatus) = (status, NoError, None, None)

    val (chargePointStatus, errorCode, errorInfo, vendorErrorCode) = status match {
      case ocpp.Available => noError(Available)
      case ocpp.Occupied => noError(OccupiedValue)
      case ocpp.Faulted(code, info, vendorCode) => (FaultedValue, toErrorCode(code), info, vendorCode)
      case ocpp.Unavailable => noError(UnavailableValue)
      case ocpp.Reserved => noError(Reserved)
    }

    val req = StatusNotificationRequest(
      connectorId, chargePointStatus, errorCode,
      errorInfo, timestamp, vendorId, vendorErrorCode)

    ?(_.statusNotification(req, id))
  }

  def firmwareStatusNotification(status: ocpp.FirmwareStatus) {
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

  // since OCPP 1.5
  def dataTransfer(vendorId: String, messageId: Option[String], data: Option[String]) = sys.error("TODO")
}