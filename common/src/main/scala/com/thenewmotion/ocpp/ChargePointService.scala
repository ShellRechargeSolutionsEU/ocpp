package com.thenewmotion.ocpp

import org.joda.time.DateTime
import java.net.URI

/**
 * @author Yaroslav Klymko
 */
trait ChargePointService {
  type Accepted = Boolean
  type FileName = String

  def remoteStartTransaction(idTag: String, connector: Option[ConnectorScope]): Accepted

  def remoteStopTransaction(transactionId: Int): Accepted

  def unlockConnector(connector: ConnectorScope): Accepted

  def getDiagnostics(location: URI,
                     startTime: Option[DateTime],
                     stopTime: Option[DateTime],
                     retries: Option[Int],
                     retryInterval: Option[Int]): Option[FileName]

  def changeConfiguration(key: String, value: String): ConfigurationStatus.Value

//  @throws[ActionNotSupportedException]
//  def getConfiguration

  def changeAvailability(scope: Scope, availabilityType: AvailabilityType.Value): AvailabilityStatus.Value

  def clearCache: Boolean

  def reset(resetType: ResetType.Value): Accepted

  def updateFirmware(retrieveDate: DateTime,
                     location: URI,
                     retries: Option[Int],
                     retryInterval: Option[Int])

//  @throws[ActionNotSupportedException]
//  def sendLocalList

//  @throws[ActionNotSupportedException]
//  def getLocalListVersion

//  @throws[ActionNotSupportedException]
//  def dataTransfer

//  @throws[ActionNotSupportedException]
//  def reserveNow

//  @throws[ActionNotSupportedException]
//  def cancelReservation
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