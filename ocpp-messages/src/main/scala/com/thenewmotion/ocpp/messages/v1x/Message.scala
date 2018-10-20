package com.thenewmotion.ocpp
package messages
package v1x

import java.net.URI
import java.time.ZonedDateTime
import scala.concurrent.duration._
import enums.reflection.EnumUtils.{Enumerable, Nameable}

sealed trait StopReason extends Nameable
object StopReason extends EnumerableWithDefault[StopReason] {
  case object EmergencyStop extends StopReason
  case object EVDisconnected extends StopReason
  case object HardReset extends StopReason
  case object PowerLoss extends StopReason
  case object Reboot extends StopReason
  case object Remote extends StopReason
  case object SoftReset extends StopReason
  case object UnlockCommand extends StopReason
  case object DeAuthorized extends StopReason
  case object Local extends StopReason
  case object Other extends StopReason

  val values = Set(EmergencyStop, EVDisconnected, HardReset, PowerLoss,
    Reboot, Remote, SoftReset, UnlockCommand, DeAuthorized, Local, Other)

  val default = Local
}

sealed trait RegistrationStatus extends Nameable
object RegistrationStatus extends Enumerable[RegistrationStatus] {
  case object Accepted extends RegistrationStatus
  case object Rejected extends RegistrationStatus
  case object Pending extends RegistrationStatus
  val values = Set(Accepted, Rejected, Pending)
}

sealed trait DiagnosticsStatus extends Nameable
object DiagnosticsStatus extends Enumerable[DiagnosticsStatus] {
  case object Uploaded extends DiagnosticsStatus
  case object UploadFailed extends DiagnosticsStatus
  case object Uploading extends DiagnosticsStatus // ocpp 1.6
  case object Idle extends DiagnosticsStatus // ocpp 1.6
  val values = Set(Uploaded, UploadFailed, Uploading, Idle)
}

sealed trait TriggerMessageStatus extends Nameable
object TriggerMessageStatus extends Enumerable[TriggerMessageStatus] {
  case object Accepted extends TriggerMessageStatus
  case object Rejected extends TriggerMessageStatus
  case object NotImplemented extends TriggerMessageStatus
  val values = Set(Accepted, Rejected, NotImplemented)
}

sealed trait UnlockStatus extends Nameable
object UnlockStatus extends Enumerable[UnlockStatus] {
  case object Unlocked extends UnlockStatus
  case object UnlockFailed extends UnlockStatus
  case object NotSupported extends UnlockStatus
  val values = Set(Unlocked, UnlockFailed, NotSupported)
}

sealed trait Message
sealed trait Req extends Message with Request
sealed trait Res extends Message with Response

@SerialVersionUID(0)
sealed trait CentralSystemMessage extends Message
sealed trait CentralSystemReq extends CentralSystemMessage with Req
sealed trait CentralSystemRes extends CentralSystemMessage with Res


case class AuthorizeReq(idTag: IdTag) extends CentralSystemReq
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
                              reason: StopReason, // ocpp 1.6
                              meters: List[meter.Meter]) extends CentralSystemReq
case class StopTransactionRes(idTag: Option[IdTagInfo]) extends CentralSystemRes


case object HeartbeatReq extends CentralSystemReq
case class HeartbeatRes(currentTime: ZonedDateTime) extends CentralSystemRes


case class MeterValuesReq(scope: Scope, transactionId: Option[Int], meters: List[meter.Meter]) extends CentralSystemReq
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
case class BootNotificationRes(status: RegistrationStatus,
                               currentTime: ZonedDateTime /*optional in OCPP 1.2*/ ,
                               interval: FiniteDuration /*optional in OCPP 1.2*/) extends CentralSystemRes

case class CentralSystemDataTransferReq(vendorId: String, messageId: Option[String], data: Option[String])
  extends CentralSystemReq

case class CentralSystemDataTransferRes(status: DataTransferStatus, data: Option[String] = None)
  extends CentralSystemRes

case class StatusNotificationReq(scope: Scope,
                                 status: ChargePointStatus,
                                 timestamp: Option[ZonedDateTime],
                                 vendorId: Option[String]) extends CentralSystemReq
case object StatusNotificationRes extends CentralSystemRes


case class FirmwareStatusNotificationReq(status: FirmwareStatus) extends CentralSystemReq
case object FirmwareStatusNotificationRes extends CentralSystemRes


case class DiagnosticsStatusNotificationReq(status: DiagnosticsStatus) extends CentralSystemReq
case object DiagnosticsStatusNotificationRes extends CentralSystemRes

sealed trait FirmwareStatus extends Nameable
object FirmwareStatus extends Enumerable[FirmwareStatus] {
  case object Downloaded extends FirmwareStatus
  case object DownloadFailed extends FirmwareStatus
  case object Downloading extends FirmwareStatus // ocpp 1.6
  case object InstallationFailed extends FirmwareStatus
  case object Installed extends FirmwareStatus
  case object Installing extends FirmwareStatus // ocpp 1.6
  case object Idle extends FirmwareStatus // ocpp 1.6

  val values = Set(
    Downloaded,
    DownloadFailed,
    Downloading,
    InstallationFailed,
    Installed,
    Installing,
    Idle
  )
}

@SerialVersionUID(0)
sealed trait ChargePointMessage extends Message
sealed trait ChargePointReq extends ChargePointMessage with Req
sealed trait ChargePointRes extends ChargePointMessage with Res

// ocpp 1.6: charging profiles
case class ChargingSchedulePeriod(
  startOffset: FiniteDuration,
  amperesLimit: Double,
  numberPhases: Option[Int]
)

case class ChargingSchedule(
  chargingRateUnit: UnitOfChargingRate,
  chargingSchedulePeriods: List[ChargingSchedulePeriod],
  minChargingRate: Option[Double],
  startsAt: Option[ZonedDateTime],
  duration: Option[FiniteDuration]
)

case class ChargingProfile(
  id: Int,
  stackLevel: Int,
  chargingProfilePurpose: ChargingProfilePurpose,
  chargingProfileKind: ChargingProfileKind,
  chargingSchedule: ChargingSchedule,
  transactionId: Option[Int],
  validFrom: Option[ZonedDateTime],
  validTo: Option[ZonedDateTime]
)

case class SetChargingProfileReq(
  connector: Scope,
  chargingProfile: ChargingProfile
) extends ChargePointReq
case class SetChargingProfileRes(
  status: ChargingProfileStatus
) extends ChargePointRes

case class ClearChargingProfileReq(
  id: Option[Int],
  connector: Option[Scope],
  chargingProfilePurpose: Option[ChargingProfilePurpose],
  stackLevel: Option[Int]
) extends ChargePointReq
case class ClearChargingProfileRes(
  status: ClearChargingProfileStatus
) extends ChargePointRes

case class GetCompositeScheduleReq(
  connector: Scope,
  duration: FiniteDuration,
  chargingRateUnit: Option[UnitOfChargingRate]
) extends ChargePointReq
case class GetCompositeScheduleRes(
  status: CompositeScheduleStatus
) extends ChargePointRes

// ocpp 1.6: trigger message
sealed trait MessageTrigger
sealed trait MessageTriggerWithoutScope extends MessageTrigger with Nameable
sealed trait MessageTriggerWithScope extends MessageTrigger {
  def scope: Option[Scope]
}

object MessageTriggerWithoutScope extends Enumerable[MessageTriggerWithoutScope] {
  case object BootNotification extends MessageTriggerWithoutScope
  case object DiagnosticsStatusNotification extends MessageTriggerWithoutScope
  case object FirmwareStatusNotification extends MessageTriggerWithoutScope
  case object Heartbeat extends MessageTriggerWithoutScope
  val values = Set(
    BootNotification,
    DiagnosticsStatusNotification,
    FirmwareStatusNotification,
    Heartbeat
  )
}

object MessageTriggerWithScope {
  final case class MeterValues(
    scope: Option[ConnectorScope]
  ) extends MessageTriggerWithScope
  final case class StatusNotification(
    scope: Option[ConnectorScope]
  ) extends MessageTriggerWithScope
}

case class TriggerMessageReq(
  requestedMessage: MessageTrigger
) extends ChargePointReq
case class TriggerMessageRes(
  status: TriggerMessageStatus
) extends ChargePointRes

case class RemoteStartTransactionReq(
  idTag: IdTag,
  connector: Option[ConnectorScope],
  chargingProfile: Option[ChargingProfile] // ocpp 1.6
) extends ChargePointReq
case class RemoteStartTransactionRes(accepted: Boolean) extends ChargePointRes


case class RemoteStopTransactionReq(transactionId: Int) extends ChargePointReq
case class RemoteStopTransactionRes(accepted: Boolean) extends ChargePointRes


case class UnlockConnectorReq(connector: ConnectorScope) extends ChargePointReq
case class UnlockConnectorRes(status: UnlockStatus) extends ChargePointRes


case class GetDiagnosticsReq(location: URI,
                             startTime: Option[ZonedDateTime],
                             stopTime: Option[ZonedDateTime],
                             retries: Retries) extends ChargePointReq
case class GetDiagnosticsRes(fileName: Option[String]) extends ChargePointRes


case class ChangeConfigurationReq(key: String, value: String) extends ChargePointReq
case class ChangeConfigurationRes(status: ConfigurationStatus) extends ChargePointRes


case class GetConfigurationReq(keys: List[String]) extends ChargePointReq
case class GetConfigurationRes(values: List[KeyValue], unknownKeys: List[String]) extends ChargePointRes


case class ChangeAvailabilityReq(scope: Scope, availabilityType: AvailabilityType) extends ChargePointReq
case class ChangeAvailabilityRes(status: AvailabilityStatus) extends ChargePointRes


case object ClearCacheReq extends ChargePointReq
case class ClearCacheRes(accepted: Boolean) extends ChargePointRes


case class ResetReq(resetType: ResetType) extends ChargePointReq
case class ResetRes(accepted: Boolean) extends ChargePointRes


case class UpdateFirmwareReq(retrieveDate: ZonedDateTime, location: URI, retries: Retries) extends ChargePointReq
case object UpdateFirmwareRes extends ChargePointRes


case class SendLocalListReq(updateType: UpdateType,
                            listVersion: AuthListSupported,
                            localAuthorisationList: List[AuthorisationData],
                            hash: Option[String] // dropped in ocpp 1.6
                            ) extends ChargePointReq

case class SendLocalListRes(status: UpdateStatus) extends ChargePointRes


case object GetLocalListVersionReq extends ChargePointReq
case class GetLocalListVersionRes(version: AuthListVersion) extends ChargePointRes


case class ChargePointDataTransferReq(vendorId: String, messageId: Option[String], data: Option[String])
  extends ChargePointReq

case class ChargePointDataTransferRes(status: DataTransferStatus, data: Option[String] = None)
  extends ChargePointRes


case class ReserveNowReq(connector: Scope,
                         expiryDate: ZonedDateTime,
                         idTag: IdTag,
                         parentIdTag: Option[String] = None,
                         reservationId: Int) extends ChargePointReq
case class ReserveNowRes(status: Reservation) extends ChargePointRes


case class CancelReservationReq(reservationId: Int) extends ChargePointReq
case class CancelReservationRes(accepted: Boolean) extends ChargePointRes



sealed trait ConfigurationStatus extends Nameable
object ConfigurationStatus extends Enumerable[ConfigurationStatus] {
  case object Accepted extends ConfigurationStatus
  case object Rejected extends ConfigurationStatus
  case object NotSupported extends ConfigurationStatus
  case object RebootRequired extends ConfigurationStatus

  val values = Set(Accepted, Rejected, NotSupported, RebootRequired)
}

sealed trait AvailabilityStatus extends Nameable
object AvailabilityStatus extends Enumerable[AvailabilityStatus] {
  case object Accepted extends AvailabilityStatus
  case object Rejected extends AvailabilityStatus
  case object Scheduled extends AvailabilityStatus

  val values = Set(Accepted, Rejected, Scheduled)
}

sealed trait AvailabilityType extends Nameable
object AvailabilityType extends Enumerable[AvailabilityType] {
  case object Operative extends AvailabilityType
  case object Inoperative extends AvailabilityType

  val values = Set(Operative, Inoperative)
}

sealed trait ResetType extends Nameable
object ResetType extends Enumerable[ResetType] {
  case object Hard extends ResetType
  case object Soft extends ResetType

  val values = Set(Hard, Soft)
}

case class KeyValue(key: String, readonly: Boolean, value: Option[String])

sealed trait UpdateType extends Nameable
object UpdateType extends Enumerable[UpdateType] {
  case object Differential extends UpdateType
  case object Full extends UpdateType

  val values = Set(Differential, Full)
}

sealed trait UpdateStatus
sealed trait UpdateStatusWithoutHash extends UpdateStatus with Nameable
sealed trait UpdateStatusWithHash extends UpdateStatus {
  def hash: Option[String]
}

object UpdateStatusWithoutHash extends Enumerable[UpdateStatusWithoutHash] {
  case object Failed extends UpdateStatusWithoutHash
  case object HashError extends UpdateStatusWithoutHash
  case object NotSupported extends UpdateStatusWithoutHash
  case object VersionMismatch extends UpdateStatusWithoutHash
  val values = Set(Failed, HashError, VersionMismatch, NotSupported)
}

object UpdateStatusWithHash {
  final case class Accepted(hash: Option[String]) extends UpdateStatusWithHash
}

sealed trait AuthorisationData {
  def idTag: IdTag
}

object AuthorisationData {
  def apply(idTag: IdTag, idTagInfo: Option[IdTagInfo]): AuthorisationData = idTagInfo match {
    case Some(x) => AuthorisationAdd(idTag, x)
    case None => AuthorisationRemove(idTag)
  }
}

case class AuthorisationAdd(idTag: IdTag, idTagInfo: IdTagInfo) extends AuthorisationData
case class AuthorisationRemove(idTag: IdTag) extends AuthorisationData


sealed trait Reservation extends Nameable
object Reservation extends Enumerable[Reservation] {
  case object Accepted extends Reservation
  case object Faulted extends Reservation
  case object Occupied extends Reservation
  case object Rejected extends Reservation
  case object Unavailable extends Reservation

  val values = Set(Accepted, Faulted, Occupied, Rejected, Unavailable)
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

trait EnumerableWithDefault[T <: Nameable] extends Enumerable[T] {
  val default: T
}
