package com.thenewmotion.ocpp.messages

import scala.concurrent.duration._
import java.net.URI
import java.time.ZonedDateTime

sealed trait Message
sealed trait Req extends Message
sealed trait Res extends Message

@SerialVersionUID(0)
sealed trait CentralSystemMessage extends Message
sealed trait CentralSystemReq extends CentralSystemMessage with Req
sealed trait CentralSystemRes extends CentralSystemMessage with Res


case class AuthorizeReq(idTag: String) extends CentralSystemReq
case class AuthorizeRes(idTag: IdTagInfo) extends CentralSystemRes


case class StartTransactionReq(connector: ConnectorScope,
                               idTag: IdTag,
                               timestamp: ZonedDateTime,
                               meterStart: Int,
                               reservationId: Option[Int]) extends CentralSystemReq
case class StartTransactionRes(transactionId: Int, idTag: IdTagInfo) extends CentralSystemRes


case class StopTransactionReq(transactionId: Int,
                              idTag: Option[IdTag],
                              timestamp: ZonedDateTime,
                              meterStop: Int,
                              transactionData: List[TransactionData]) extends CentralSystemReq
case class StopTransactionRes(idTag: Option[IdTagInfo]) extends CentralSystemRes


case object HeartbeatReq extends CentralSystemReq
case class HeartbeatRes(currentTime: ZonedDateTime) extends CentralSystemRes


case class MeterValuesReq(scope: Scope, transactionId: Option[Int], meters: List[Meter]) extends CentralSystemReq
case object MeterValuesRes extends CentralSystemRes


case class BootNotificationReq(chargePointVendor: String,
                               chargePointModel: String,
                               chargePointSerialNumber: Option[String],
                               chargeBoxSerialNumber: Option[String],
                               firmwareVersion: Option[String],
                               iccid: Option[String],
                               imsi: Option[String],
                               meterType: Option[String],
                               meterSerialNumber: Option[String]) extends CentralSystemReq
case class BootNotificationRes(registrationAccepted: Boolean,
                               currentTime: ZonedDateTime /*optional in OCPP 1.2*/ ,
                               heartbeatInterval: FiniteDuration /*optional in OCPP 1.2*/) extends CentralSystemRes

case class CentralSystemDataTransferReq(vendorId: String, messageId: Option[String], data: Option[String])
  extends CentralSystemReq

case class CentralSystemDataTransferRes(status: DataTransferStatus.Value, data: Option[String] = None)
  extends CentralSystemRes

case class StatusNotificationReq(scope: Scope,
                                 status: ChargePointStatus,
                                 timestamp: Option[ZonedDateTime],
                                 vendorId: Option[String]) extends CentralSystemReq
case object StatusNotificationRes extends CentralSystemRes


case class FirmwareStatusNotificationReq(status: FirmwareStatus.Value) extends CentralSystemReq
case object FirmwareStatusNotificationRes extends CentralSystemRes


case class DiagnosticsStatusNotificationReq(uploaded: Boolean) extends CentralSystemReq
case object DiagnosticsStatusNotificationRes extends CentralSystemRes



case class TransactionData(meters: List[Meter])

case class Meter(timestamp: ZonedDateTime, values: List[Meter.Value] = Nil)

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
    val Inlet = Value(0, "Inlet")
    val Outlet = Value(1, "Outlet")
    val Body = Value(2, "Body")
  }

  object Measurand extends Enumeration {
    val EnergyActiveExportRegister = Value(0, "Energy.Active.Export.Register")
    val EnergyActiveImportRegister = Value(1, "Energy.Active.Import.Register")
    val EnergyReactiveExportRegister = Value(2, "Energy.Reactive.Export.Register")
    val EnergyReactiveImportRegister = Value(3, "Energy.Reactive.Import.Register")
    val EnergyActiveExportInterval = Value(4, "Energy.Active.Export.Interval")
    val EnergyActiveImportInterval = Value(5, "Energy.Active.Import.Interval")
    val EnergyReactiveExportInterval = Value(6, "Energy.Reactive.Export.Interval")
    val EnergyReactiveImportInterval = Value(7, "Energy.Reactive.Import.Interval")
    val PowerActiveExport = Value(8, "Power.Active.Export")
    val PowerActiveImport = Value(9, "Power.Active.Import")
    val PowerReactiveExport = Value(10, "Power.Reactive.Export")
    val PowerReactiveImport = Value(11, "Power.Reactive.Import")
    val CurrentExport = Value(12, "Current.Export")
    val CurrentImport = Value(13, "Current.Import")
    val Voltage = Value(14, "Voltage")
    val Temperature = Value(15, "Temperature")
  }

  object ValueFormat extends Enumeration {
    val Raw = Value(0, "Raw")
    val Signed = Value(1, "Signed")
  }

  object ReadingContext extends Enumeration {
    val InterruptionBegin = Value(0, "Interruption.Begin")
    val InterruptionEnd = Value(1, "Interruption.End")
    val SampleClock = Value(2, "Sample.Clock")
    val SamplePeriodic = Value(3, "Sample.Periodic")
    val TransactionBegin = Value(4, "Transaction.Begin")
    val TransactionEnd = Value(5, "Transaction.End")
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

sealed trait ChargePointStatus {
  def info: Option[String]
}
case class Available(info:Option[String]=None) extends ChargePointStatus
case class Occupied(info:Option[String]=None) extends ChargePointStatus
case class Faulted(errorCode: Option[ChargePointErrorCode.Value],
                   info: Option[String]=None,
                   vendorErrorCode: Option[String]) extends ChargePointStatus
case class Unavailable(info:Option[String]=None) extends ChargePointStatus
// since OCPP 1.5
case class Reserved(info:Option[String]=None) extends ChargePointStatus

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

@SerialVersionUID(0)
sealed trait ChargePointMessage extends Message
sealed trait ChargePointReq extends ChargePointMessage with Req
sealed trait ChargePointRes extends ChargePointMessage with Res


case class RemoteStartTransactionReq(idTag: IdTag, connector: Option[ConnectorScope]) extends ChargePointReq
case class RemoteStartTransactionRes(accepted: Boolean) extends ChargePointRes


case class RemoteStopTransactionReq(transactionId: Int) extends ChargePointReq
case class RemoteStopTransactionRes(accepted: Boolean) extends ChargePointRes


case class UnlockConnectorReq(connector: ConnectorScope) extends ChargePointReq
case class UnlockConnectorRes(accepted: Boolean) extends ChargePointRes


case class GetDiagnosticsReq(location: URI,
                             startTime: Option[ZonedDateTime],
                             stopTime: Option[ZonedDateTime],
                             retries: Retries) extends ChargePointReq
case class GetDiagnosticsRes(fileName: Option[String]) extends ChargePointRes


case class ChangeConfigurationReq(key: String, value: String) extends ChargePointReq
case class ChangeConfigurationRes(status: ConfigurationStatus.Value) extends ChargePointRes


case class GetConfigurationReq(keys: List[String]) extends ChargePointReq
case class GetConfigurationRes(values: List[KeyValue], unknownKeys: List[String]) extends ChargePointRes


case class ChangeAvailabilityReq(scope: Scope, availabilityType: AvailabilityType.Value) extends ChargePointReq
case class ChangeAvailabilityRes(status: AvailabilityStatus.Value) extends ChargePointRes


case object ClearCacheReq extends ChargePointReq
case class ClearCacheRes(accepted: Boolean) extends ChargePointRes


case class ResetReq(resetType: ResetType.Value) extends ChargePointReq
case class ResetRes(accepted: Boolean) extends ChargePointRes


case class UpdateFirmwareReq(retrieveDate: ZonedDateTime, location: URI, retries: Retries) extends ChargePointReq
case object UpdateFirmwareRes extends ChargePointRes


case class SendLocalListReq(updateType: UpdateType.Value,
                            listVersion: AuthListSupported,
                            localAuthorisationList: List[AuthorisationData],
                            hash: Option[String]) extends ChargePointReq

case class SendLocalListRes(status: UpdateStatus.Value) extends ChargePointRes


case object GetLocalListVersionReq extends ChargePointReq
case class GetLocalListVersionRes(version: AuthListVersion) extends ChargePointRes


case class ChargePointDataTransferReq(vendorId: String, messageId: Option[String], data: Option[String])
  extends ChargePointReq

case class ChargePointDataTransferRes(status: DataTransferStatus.Value, data: Option[String] = None)
  extends ChargePointRes


case class ReserveNowReq(connector: Scope,
                         expiryDate: ZonedDateTime,
                         idTag: IdTag,
                         parentIdTag: Option[String] = None,
                         reservationId: Int) extends ChargePointReq
case class ReserveNowRes(status: Reservation.Value) extends ChargePointRes


case class CancelReservationReq(reservationId: Int) extends ChargePointReq
case class CancelReservationRes(accepted: Boolean) extends ChargePointRes



object ConfigurationStatus extends Enumeration {
  val Accepted, Rejected, NotSupported = Value
}

object AvailabilityStatus extends Enumeration {
  val Accepted, Rejected, Scheduled = Value
}

object AvailabilityType extends Enumeration {
  val Operative, Inoperative = Value
}

object ResetType extends Enumeration {
  val Hard, Soft = Value
}

case class KeyValue(key: String, readonly: Boolean, value: Option[String])

object UpdateType extends Enumeration {
  val Differential, Full = Value
}

object UpdateStatus {
  sealed trait Value
  case class UpdateAccepted(hash: Option[String]) extends Value
  case object UpdateFailed extends Value
  case object HashError extends Value
  case object NotSupportedValue extends Value
  case object VersionMismatch extends Value
}

sealed trait AuthorisationData {
  def idTag: String
}

object AuthorisationData {
  def apply(idTag: String, idTagInfo: Option[IdTagInfo]): AuthorisationData = idTagInfo match {
    case Some(x) => AuthorisationAdd(idTag, x)
    case None => AuthorisationRemove(idTag)
  }
}

case class AuthorisationAdd(idTag: String, idTagInfo: IdTagInfo) extends AuthorisationData
case class AuthorisationRemove(idTag: String) extends AuthorisationData


object Reservation extends Enumeration {
  val Accepted, Faulted, Occupied, Rejected, Unavailable = Value
}

object AuthListVersion {
  def apply(version: Int): AuthListVersion =
    if (version < 0) AuthListNotSupported else AuthListSupported(version)
}
sealed trait AuthListVersion
case object AuthListNotSupported extends AuthListVersion
case class AuthListSupported(version: Int) extends AuthListVersion {
  require(version >= 0, s"version which is $version must be greater than or equal to 0")
}

case class Retries(numberOfRetries: Option[Int], interval: Option[FiniteDuration]) {
  def intervalInSeconds: Option[Int] = interval.map(_.toSeconds.toInt)
}

object Retries {
  val none = Retries(None, None)

  def fromInts(numberOfRetries: Option[Int], intervalInSeconds: Option[Int]): Retries =
    Retries(numberOfRetries, intervalInSeconds.map(_.seconds))
}
