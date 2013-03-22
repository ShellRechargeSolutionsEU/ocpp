package com.thenewmotion.chargenetwork.ocpp

import com.thenewmotion.time.Imports._
import java.net.URI
import org.joda.time
import com.thenewmotion.chargenetwork.ocpp
import scalaxb.{SoapClients, Fault}
import javax.xml.datatype.XMLGregorianCalendar
import com.thenewmotion.Logging

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
                         errorCode: Option[ocpp.ChargePointErrorCode],
                         info: Option[String],
                         timestamp: Option[time.DateTime],
                         vendorId: Option[String],
                         vendorErrorCode: Option[String]) {

    val chargePointStatus: ChargePointStatus = status match {
      case ocpp.Available => Available
      case ocpp.Occupied => Occupied
      case ocpp.Faulted => Faulted
      case ocpp.Unavailable => Unavailable
      case Reserved => sys.error("TODO")
    }

    val code: Option[ChargePointErrorCode] = errorCode.map {
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

    info.foreach(x => logNotSupported("statusNotification.info", x))
    timestamp.foreach(x => logNotSupported("statusNotification.timestamp", x))
    vendorId.foreach(x => logNotSupported("statusNotification.vendorId", x))
    vendorErrorCode.foreach(x => logNotSupported("statusNotification.vendorErrorCode", x))

    ?(_.statusNotification(StatusNotificationRequest(connectorId, chargePointStatus, code getOrElse NoError), id))
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
