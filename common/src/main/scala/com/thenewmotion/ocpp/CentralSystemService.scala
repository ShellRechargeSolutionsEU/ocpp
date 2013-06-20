package com.thenewmotion.ocpp

import org.joda.time.DateTime
import scala.concurrent.duration.FiniteDuration

/**
 * @author Yaroslav Klymko
 */
trait CentralSystemService {
  type TransactionId = Int

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
    val Inlet = Value(0)
    val Outlet = Value(1)
    val Body = Value(2)
  }

  object Measurand extends Enumeration {
    val EnergyActiveExportRegister = Value(0)
    val EnergyActiveImportRegister = Value(1)
    val EnergyReactiveExportRegister = Value(2)
    val EnergyReactiveImportRegister = Value(3)
    val EnergyActiveExportInterval = Value(4)
    val EnergyActiveImportInterval = Value(5)
    val EnergyReactiveExportInterval = Value(6)
    val EnergyReactiveImportInterval = Value(7)
    val PowerActiveExport = Value(8)
    val PowerActiveImport = Value(9)
    val PowerReactiveExport = Value(10)
    val PowerReactiveImport = Value(11)
    val CurrentExport = Value(12)
    val CurrentImport = Value(13)
    val Voltage = Value(14)
    val Temperature = Value(15)
  }

  object ValueFormat extends Enumeration {
    val Raw = Value(0)
    val Signed = Value(1)
  }

  object ReadingContext extends Enumeration {
    val InterruptionBegin = Value(0)
    val InterruptionEnd= Value(1)
    val SampleClock= Value(2)
    val SamplePeriodic= Value(3)
    val TransactionBegin= Value(4)
    val TransactionEnd = Value(5)
  }

  object UnitOfMeasure extends Enumeration {
    val Wh = Value(0, "Wh")
    val Kwh = Value(1, "kWh")
    val Varh = Value(2, "varh")
    val Kvarh = Value(3, "kvarh")
    val W = Value(4, "W")
    val Kw = Value(5, "kW")
    val Var = Value(6, "var")
    val Kvar = Value(7, "kvar")
    val Amp = Value(8, "Amp")
    val Volt = Value(9, "Volt")
    val Celsius = Value(10, "Celsius")
  }
}

case class BootNotificationResponse(registrationAccepted: Boolean,
                                    currentTime: DateTime, // optional in OCPP 1.2
                                    heartbeatInterval: FiniteDuration) // optional in OCPP 1.2

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

case class DataTransferResponse(status: DataTransferStatus.Value, data: Option[String] = None)

object DataTransferStatus extends Enumeration {
  val Accepted,
  Rejected,
  UnknownMessageId,
  UnknownVendorId = Value
}