package com.thenewmotion.ocpp

import org.joda.time.DateTime
import scala.concurrent.duration.FiniteDuration

/**
 * @author Yaroslav Klymko
 */
trait ChargePointService {
  type Accepted = Boolean
  type FileName = String

  def remoteStartTransaction(idTag: IdTag, connector: Option[ConnectorScope]): Accepted

  def remoteStopTransaction(transactionId: Int): Accepted

  def unlockConnector(connector: ConnectorScope): Accepted

  // FIXME: change method signature so it can handle the case that only one of retries and retryInterval is specified
  def getDiagnostics(location: Uri,
                     startTime: Option[DateTime],
                     stopTime: Option[DateTime],
                     retries: Option[GetDiagnosticsRetries]): Option[FileName]

  def changeConfiguration(key: String, value: String): ConfigurationStatus.Value

  @throws[ActionNotSupportedException]
  def getConfiguration(keys: List[String]): Configuration

  def changeAvailability(scope: Scope, availabilityType: AvailabilityType.Value): AvailabilityStatus.Value

  def clearCache: Boolean

  def reset(resetType: ResetType.Value): Accepted

  def updateFirmware(retrieveDate: DateTime,
                     location: Uri,
                     retries: Option[Int],
                     retryInterval: Option[Int])

  @throws[ActionNotSupportedException]
  def sendLocalList(updateType: UpdateType.Value,
                    listVersion: AuthListSupported,
                    localAuthorisationList: List[AuthorisationData],
                    hash: Option[String]): UpdateStatus.Value

  @throws[ActionNotSupportedException]
  def getLocalListVersion: AuthListVersion

  @throws[ActionNotSupportedException]
  def dataTransfer(vendorId: String, messageId: Option[String], data: Option[String]): DataTransferResponse

  @throws[ActionNotSupportedException]
  def reserveNow(connector: Scope,
                 expiryDate: DateTime,
                 idTag: IdTag,
                 parentIdTag: Option[String] = None,
                 reservationId: Int): Reservation.Value

  @throws[ActionNotSupportedException]
  def cancelReservation(reservationId: Int): Boolean
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
case class Configuration(values: List[KeyValue] = Nil, unknownKeys: List[String])


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

case class GetDiagnosticsRetries(numberOfRetries: Int, interval: FiniteDuration)