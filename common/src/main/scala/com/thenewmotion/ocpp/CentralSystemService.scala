package com.thenewmotion.ocpp

import org.joda.time.DateTime

/**
 * @author Yaroslav Klymko
 */
trait CentralSystemService {
  type TransactionId = Int
  type IdTag = String

  def authorize(idTag: String): IdTagInfo

  def startTransaction(connector: ConnectorScope,
                       idTag: IdTag,
                       timestamp: DateTime,
                       meterStart: Int,
                       reservationId: Option[Int]): (TransactionId, IdTagInfo)

  def stopTransaction(transactionId: TransactionId,
                      idTag: Option[IdTag],
                      timestamp: DateTime,
                      meterStop: Int,
                      transactionData: List[TransactionData]): Option[IdTagInfo]

  def heartbeat: DateTime

  def meterValues(scope: Scope, transactionId: Option[TransactionId], meters: List[Meter])

  def bootNotification(chargePointVendor: String,
                       chargePointModel: String,
                       chargePointSerialNumber: Option[String],
                       chargeBoxSerialNumber: Option[String],
                       firmwareVersion: Option[String],
                       iccid: Option[String],
                       imsi: Option[String],
                       meterType: Option[String],
                       meterSerialNumber: Option[String]): BootNotificationResponse

  @throws[ActionNotSupportedException]
  def statusNotification(scope: Scope, status: ChargePointStatus, timestamp: Option[DateTime], vendorId: Option[String])

  def firmwareStatusNotification(status: FirmwareStatus.Value)

  def diagnosticsStatusNotification(uploaded: Boolean)

  @throws[ActionNotSupportedException]
  def dataTransfer(vendorId: String, messageId: Option[String], data: Option[String]): DataTransferResponse
}

object AuthorizationStatus extends Enumeration {
  val Accepted,
  IdTagBlocked,
  IdTagExpired,
  IdTagInvalid,
  ConcurrentTx = Value
}

case class IdTagInfo(status: AuthorizationStatus.Value,
                     expiryDate: Option[DateTime] = None,
                     parentIdTag: Option[String] = None)

case class TransactionData(meters: List[Meter])

case class Meter(timestamp: DateTime, values: List[Meter.Value] = Nil)

object Meter {
  case class Value(value: String,
                   context: ReadingContext.Value,
                   format: ValueFormat.Value,
                   measurand: Measurand.Value,
                   location: Location.Value,
                   unit: UnitOfMeasure.Value)

  object DefaultValue {
    val readingContext = ReadingContext.SamplePeriodic
    val format = ValueFormat.Raw
    val measurand = Measurand.EnergyActiveImportRegister
    val location = Location.Outlet
    val unitOfMeasure = UnitOfMeasure.Wh

    def apply(value: Int): Value = Value(value.toString, readingContext, format, measurand, location, unitOfMeasure)

    def unapply(x: Value): Option[Int] = PartialFunction.condOpt(x) {
      case Value(value, `readingContext`, `format`, `measurand`, `location`, `unitOfMeasure`) => value.toFloat.round
    }
  }

  object Location extends Enumeration {
    val Inlet, Outlet, Body = Value
  }

  object Measurand extends Enumeration {
    val EnergyActiveExportRegister,
    EnergyActiveImportRegister,
    EnergyReactiveExportRegister,
    EnergyReactiveImportRegister,
    EnergyActiveExportInterval,
    EnergyActiveImportInterval,
    EnergyReactiveExportInterval,
    EnergyReactiveImportInterval,
    PowerActiveExport,
    PowerActiveImport,
    PowerReactiveExport,
    PowerReactiveImport,
    CurrentExport,
    CurrentImport,
    Voltage,
    Temperature = Value
  }

  object ValueFormat extends Enumeration {
    val Raw, Signed = Value
  }

  object ReadingContext extends Enumeration {
    val InterruptionBegin,
    InterruptionEnd,
    SampleClock,
    SamplePeriodic,
    TransactionBegin,
    TransactionEnd = Value
  }

  object UnitOfMeasure extends Enumeration {
    val Wh = Value("Wh")
    val Kwh = Value("kWh")
    val Varh = Value("varh")
    val Kvarh = Value("kvarh")
    val W = Value("W")
    val Kw = Value("kW")
    val Var = Value("var")
    val Kvar = Value("kvar")
    val Amp = Value("Amp")
    val Volt = Value("Volt")
    val Celsius = Value("Celsius")
  }
}

case class BootNotificationResponse(registrationAccepted: Boolean,
                                    currentTime: DateTime, // optional in OCPP 1.2
                                    heartbeatInterval: Int) // optional in OCPP 1.2

sealed trait ChargePointStatus
case object Available extends ChargePointStatus
case object Occupied extends ChargePointStatus
case class Faulted(errorCode: Option[ChargePointErrorCode.Value],
                   info: Option[String],
                   vendorErrorCode: Option[String]) extends ChargePointStatus
case object Unavailable extends ChargePointStatus
// since OCPP 1.5
case object Reserved extends ChargePointStatus

object ChargePointErrorCode extends Enumeration {
  val ConnectorLockFailure,
  HighTemperature,
  Mode3Error,
  PowerMeterFailure,
  PowerSwitchFailure,
  ReaderFailure,
  ResetFailure,
  GroundFailure /*since OCPP 1.5*/ ,
  OverCurrentFailure,
  UnderVoltage,
  WeakSignal,
  OtherError = Value
}

object FirmwareStatus extends Enumeration {
  val Downloaded,
  DownloadFailed,
  InstallationFailed,
  Installed = Value
}

case class DataTransferResponse(status: DataTransferResponse.Status.Value, data: Option[String] = None)

object DataTransferResponse {
  object Status extends Enumeration {
    val Accepted,
    Rejected,
    UnknownMessageId,
    UnknownVendorId = Value
  }
}