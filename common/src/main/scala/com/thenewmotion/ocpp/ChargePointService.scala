package com.thenewmotion.ocpp

import org.joda.time.DateTime
import java.net.URI

/**
 * @author Yaroslav Klymko
 */
trait ChargePointService {
  type Accepted = Boolean
  type FileName = String

  def remoteStartTransaction(idTag: String, connectorId: Option[Int] = None): Accepted

  def remoteStopTransaction(transactionId: Int): Accepted

  def unlockConnector(connectorId: Int): Accepted

  def getDiagnostics(location: URI,
                     startTime: Option[DateTime] = None,
                     stopTime: Option[DateTime] = None,
                     retries: Option[Int] = None,
                     retryInterval: Option[Int] = None): Option[FileName]

  def changeConfiguration(key: String, value: String): ConfigurationStatus.Value

  def changeAvailability(connectorId: Int, availabilityType: AvailabilityType.Value): AvailabilityStatus.Value

  def clearCache: Boolean

  def reset(resetType: ResetType.Value): Accepted

  def updateFirmware(retrieveDate: DateTime,
                     location: URI,
                     retries: Option[Int] = None,
                     retryInterval: Option[Int] = None)

  // since OCPP 1.5
  //  def sendLocalList
  //  def dataTransfer
  //  def reserveNow
  //  def getLocalListVersion
  //  def cancelReservation
  //  def getConfiguration
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