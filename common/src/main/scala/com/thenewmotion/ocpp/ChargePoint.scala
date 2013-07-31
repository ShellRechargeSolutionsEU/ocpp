package com.thenewmotion.ocpp

import org.joda.time.DateTime
import scala.concurrent.duration._

/**
 * @author Yaroslav Klymko
 */
trait ChargePoint extends (ChargePoint.Req => ChargePoint.Res)

object ChargePoint {

  @SerialVersionUID(0)
  sealed trait Message
  sealed trait Req extends Message
  sealed trait Res extends Message


  case class RemoteStartTransactionReq(idTag: IdTag, connector: Option[ConnectorScope]) extends Req
  case class RemoteStartTransactionRes(accepted: Boolean) extends Res


  case class RemoteStopTransactionReq(transactionId: Int) extends Req
  case class RemoteStopTransactionRes(accepted: Boolean) extends Res


  case class UnlockConnectorReq(connector: ConnectorScope) extends Req
  case class UnlockConnectorRes(accepted: Boolean) extends Res


  case class GetDiagnosticsReq(location: Uri,
                               startTime: Option[DateTime],
                               stopTime: Option[DateTime],
                               retries: Retries) extends Req
  case class GetDiagnosticsRes(fileName: Option[String]) extends Res


  case class ChangeConfigurationReq(key: String, value: String) extends Req
  case class ChangeConfigurationRes(status: ConfigurationStatus.Value) extends Res


  case class GetConfigurationReq(keys: List[String]) extends Req
  case class GetConfigurationRes(values: List[KeyValue], unknownKeys: List[String]) extends Res


  case class ChangeAvailabilityReq(scope: Scope, availabilityType: AvailabilityType.Value) extends Req
  case class ChangeAvailabilityRes(status: AvailabilityStatus.Value) extends Res


  case object ClearCacheReq extends Req
  case class ClearCacheRes(accepted: Boolean) extends Res


  case class ResetReq(resetType: ResetType.Value) extends Req
  case class ResetRes(accepted: Boolean) extends Res


  case class UpdateFirmwareReq(retrieveDate: DateTime, location: Uri, retries: Retries) extends Req
  case object UpdateFirmwareRes extends Res


  case class SendLocalListReq(updateType: UpdateType.Value,
                              listVersion: AuthListSupported,
                              localAuthorisationList: List[AuthorisationData],
                              hash: Option[String]) extends Req

  case class SendLocalListRes(status: UpdateStatus.Value) extends Res


  case object GetLocalListVersionReq extends Req
  case class GetLocalListVersionRes(version: AuthListVersion) extends Res


  case class DataTransferReq(vendorId: String, messageId: Option[String], data: Option[String]) extends Req
  case class DataTransferRes(status: DataTransferStatus.Value, data: Option[String] = None) extends Res


  case class ReserveNowReq(connector: Scope,
                           expiryDate: DateTime,
                           idTag: IdTag,
                           parentIdTag: Option[String] = None,
                           reservationId: Int) extends Req
  case class ReserveNowRes(status: Reservation.Value) extends Res


  case class CancelReservationReq(reservationId: Int) extends Req
  case class CancelReservationRes(accepted: Boolean) extends Res
}

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
  def fromInts(numberOfRetries: Option[Int], intervalInSeconds: Option[Int]): Retries =
    Retries(numberOfRetries, intervalInSeconds.map(_.seconds))
}