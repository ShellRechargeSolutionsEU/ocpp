package com.thenewmotion.ocpp

import org.joda.time.DateTime

/**
 * @author Yaroslav Klymko
 */
trait CentralSystemService {
  type TransactionId = Int
  type IdTag = String

  def authorize(idTag: String): IdTagInfo

  def startTransaction(connectorId: Int,
                       idTag: IdTag,
                       timestamp: DateTime,
                       meterStart: Int,
                       reservationId: Option[Int] = None): (TransactionId, IdTagInfo)

  def stopTransaction(transactionId: TransactionId,
                      idTag: Option[IdTag] = None,
                      timestamp: DateTime,
                      meterStop: Int,
                      transactionData: List[TransactionData] = Nil): Option[IdTagInfo]

  def heartbeat: DateTime

  def meterValues(connectorId: Int, values: List[Meter.Value] = Nil)

  def bootNotification(chargePointVendor: String,
                       chargePointModel: String,
                       chargePointSerialNumber: Option[String] = None,
                       chargeBoxSerialNumber: Option[String] = None,
                       firmwareVersion: Option[String] = None,
                       iccid: Option[String] = None,
                       imsi: Option[String] = None,
                       meterType: Option[String] = None,
                       meterSerialNumber: Option[String] = None): BootNotificationResponse

  def statusNotification(connectorId: Int,
                         status: ChargePointStatus,
                         errorCode: Option[ChargePointErrorCode],
                         info: Option[String] = None,
                         timestamp: Option[DateTime] = None,
                         vendorId: Option[String] = None,
                         vendorErrorCode: Option[String] = None)

  def firmwareStatusNotification(status: FirmwareStatus)

  def diagnosticsStatusNotification(uploaded: Boolean)

  // since OCPP 1.5
  def dataTransfer(vendorId: String,
                   messageId: Option[String] = None,
                   data: Option[String] = None): DataTransferResponse
}

sealed trait AuthorizationStatus
case object AuthorizationAccepted extends AuthorizationStatus
case object IdTagBlocked extends AuthorizationStatus
case object IdTagExpired extends AuthorizationStatus
case object IdTagInvalid extends AuthorizationStatus
case object ConcurrentTx extends AuthorizationStatus

case class IdTagInfo(status: AuthorizationStatus,
                     expiryDate: Option[DateTime] = None,
                     parentIdTag: Option[String] = None)

case class TransactionData(values: List[Meter])

case class Meter(timestamp: DateTime, values: List[Meter.Value] = Nil)

object Meter {

  case class Value(value: String,
                   context: Option[ReadingContext] = None,
                   format: Option[ValueFormat] = None,
                   measurand: Option[Measurand] = None,
                   location: Option[Location] = None,
                   unit: Option[UnitOfMeasure] = None)

  sealed trait Location
  case object Inlet extends Location
  case object Outlet extends Location
  case object Body extends Location

  sealed trait Measurand
  case object EnergyActiveExportRegister extends Measurand
  case object EnergyActiveImportRegister extends Measurand
  case object EnergyReactiveExportRegister extends Measurand
  case object EnergyReactiveImportRegister extends Measurand
  case object EnergyActiveExportInterval extends Measurand
  case object EnergyActiveImportInterval extends Measurand
  case object EnergyReactiveExportInterval extends Measurand
  case object EnergyReactiveImportInterval extends Measurand
  case object PowerActiveExport extends Measurand
  case object PowerActiveImport extends Measurand
  case object PowerReactiveExport extends Measurand
  case object PowerReactiveImport extends Measurand
  case object CurrentExport extends Measurand
  case object CurrentImport extends Measurand
  case object Voltage extends Measurand
  case object Temperature extends Measurand

  sealed trait ValueFormat
  case object RawFormat extends ValueFormat
  case object SignedData extends ValueFormat

  sealed trait ReadingContext
  case object InterruptionBegin extends ReadingContext
  case object InterruptionEnd extends ReadingContext
  case object SampleClock extends ReadingContext
  case object SamplePeriodic extends ReadingContext
  case object TransactionBegin extends ReadingContext
  case object TransactionEnd extends ReadingContext

  sealed trait UnitOfMeasure
  case object Wh extends UnitOfMeasure
  case object KWh extends UnitOfMeasure
  case object Varh extends UnitOfMeasure
  case object Kvarh extends UnitOfMeasure
  case object W extends UnitOfMeasure
  case object KW extends UnitOfMeasure
  case object Var extends UnitOfMeasure
  case object Kvar extends UnitOfMeasure
  case object Amp extends UnitOfMeasure
  case object Volt extends UnitOfMeasure
  case object Celsius extends UnitOfMeasure
}

case class BootNotificationResponse(registrationAccepted: Boolean,
                                    currentTime: DateTime, // optional in OCPP 1.2
                                    heartbeatInterval: Int) // optional in OCPP 1.2

sealed trait ChargePointStatus
case object Available extends ChargePointStatus
case object Occupied extends ChargePointStatus
case object Faulted extends ChargePointStatus
case object Unavailable extends ChargePointStatus
// since OCPP 1.5
case object Reserved extends ChargePointStatus

sealed trait ChargePointErrorCode
case object ConnectorLockFailure extends ChargePointErrorCode
case object HighTemperature extends ChargePointErrorCode
case object Mode3Error extends ChargePointErrorCode
case object PowerMeterFailure extends ChargePointErrorCode
case object PowerSwitchFailure extends ChargePointErrorCode
case object ReaderFailure extends ChargePointErrorCode
case object ResetFailure extends ChargePointErrorCode
// since OCPP 1.5
case object GroundFailure extends ChargePointErrorCode
case object OverCurrentFailure extends ChargePointErrorCode
case object UnderVoltage extends ChargePointErrorCode
case object WeakSignal extends ChargePointErrorCode
case object OtherError extends ChargePointErrorCode

sealed trait FirmwareStatus
case object Downloaded extends FirmwareStatus
case object DownloadFailed extends FirmwareStatus
case object InstallationFailed extends FirmwareStatus
case object Installed extends FirmwareStatus

case class DataTransferResponse(status: DataTransferResponse.Status, data: Option[String] = None)

object DataTransferResponse {
  sealed trait Status
  case object Accepted extends Status
  case object Rejected extends Status
  case object UnknownMessageId extends Status
  case object UnknownVendorId extends Status
}