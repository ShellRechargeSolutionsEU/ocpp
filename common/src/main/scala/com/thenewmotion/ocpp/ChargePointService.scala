package com.thenewmotion.ocpp

import org.joda.time.DateTime
import java.net.URI

/**
 * @author Yaroslav Klymko
 */
trait ChargePointService {
  type Accepted = Boolean
  type FileName = String
  type ListVersion = Int
  type IdTag = String

  def remoteStartTransaction(idTag: IdTag, connector: Option[ConnectorScope]): Accepted

  def remoteStopTransaction(transactionId: Int): Accepted

  def unlockConnector(connector: ConnectorScope): Accepted

  def getDiagnostics(location: URI,
                     startTime: Option[DateTime],
                     stopTime: Option[DateTime],
                     retries: Option[Int],
                     retryInterval: Option[Int]): Option[FileName]

  def changeConfiguration(key: String, value: String): ConfigurationStatus.Value

  @throws[ActionNotSupportedException]
  def getConfiguration(keys: List[String]): Configuration

  def changeAvailability(scope: Scope, availabilityType: AvailabilityType.Value): AvailabilityStatus.Value

  def clearCache: Boolean

  def reset(resetType: ResetType.Value): Accepted

  def updateFirmware(retrieveDate: DateTime,
                     location: URI,
                     retries: Option[Int],
                     retryInterval: Option[Int])

  @throws[ActionNotSupportedException]
  def sendLocalList(updateType: UpdateType.Value,
                    listVersion: ListVersion,
                    localAuthorisationList: List[AuthorisationData],
                    hash: Option[String]): UpdateStatus.Value

  @throws[ActionNotSupportedException]
  def getLocalListVersion: ListVersion

//  @throws[ActionNotSupportedException]
//  def dataTransfer

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

case class AuthorisationData(idTag: String, idTagInfo: Option[IdTagInfo])


object Reservation extends Enumeration {
  val Accepted, Faulted, Occupied, Rejected, Unavailable = Value
}