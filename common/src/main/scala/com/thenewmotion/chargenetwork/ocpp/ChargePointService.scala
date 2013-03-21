package com.thenewmotion.chargenetwork.ocpp

import org.joda.time.DateTime

/**
 * @author Yaroslav Klymko
 */
trait ChargePointService {
  type Accepted = Boolean
  type FileName = String

  def remoteStartTransaction(idTag: String, connectorId: Option[Int] = None): Accepted

  def remoteStopTransaction(transactionId: Int): Accepted

  def unlockConnector(connectorId: Int): Accepted

  def getDiagnostics(location: java.net.URI,
                     startTime: Option[DateTime] = None,
                     stopTime: Option[DateTime] = None,
                     retries: Option[Int] = None,
                     retryInterval: Option[Int] = None): Option[FileName]

  def changeConfiguration(key: String, value: String): ConfigurationStatus

  def changeAvailability(connectorId: Int, typeValue: AvailabilityType): AvailabilityStatus

  def clearCache: Boolean

  def reset(value: ResetType): Accepted

  def updateFirmware(retrieveDate: DateTime,
                     location: java.net.URI,
                     retries: Option[Int] = None,
                     retryInterval: Option[Int] = None)

  //  def sendLocalList[V <: V15.type](value: SendLocalListRequest)
  //  def dataTransfer[V <: V15.type](value: DataTransferRequest)
  //  def reserveNow(value: ReserveNowRequest)
  //  def getLocalListVersion[V <: V15.type](value: GetLocalListVersionRequest)
  //  def cancelReservation[V <: V15.type](value: CancelReservationRequest)
  //  def getConfiguration[V <: V15.type](value: GetConfigurationRequest)
}

sealed trait ConfigurationStatus
case object ConfigurationAccepted extends ConfigurationStatus
case object ConfigurationRejected extends ConfigurationStatus
case object ConfigurationNotSupported extends ConfigurationStatus

sealed trait AvailabilityType
case object Operative extends AvailabilityType
case object Inoperative extends AvailabilityType

trait AvailabilityStatus
case object AvailabilityAccepted extends AvailabilityStatus
case object AvailabilityRejected extends AvailabilityStatus
case object AvailabilityScheduled extends AvailabilityStatus

sealed trait ResetType
case object Hard extends ResetType
case object Soft extends ResetType