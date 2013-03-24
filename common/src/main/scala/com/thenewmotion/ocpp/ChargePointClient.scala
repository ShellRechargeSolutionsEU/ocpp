package com.thenewmotion.ocpp

import java.net.URI
import scalaxb.SoapClients
import com.thenewmotion.ocpp
import com.thenewmotion.time.Imports._
import dispatch.Http


/**
 * @author Yaroslav Klymko
 */
trait ChargePointClient extends ChargePointService with Client

class ChargePointClientV12(val chargeBoxIdentity: String, uri: URI, http: Http) extends ChargePointClient {
  import v12._

  val bindings = new CustomDispatchHttpClients(http) with ChargePointServiceSoapBindings with SoapClients {
    override def baseAddress = uri
  }

  private def ?[T](f: ChargePointService => Either[scalaxb.Fault[Any], T]): T = rightOrException(f(bindings.service))

  private implicit def remoteStartStopStatusToAccepted(x: RemoteStartStopStatus): Accepted = x match {
    case Accepted => true
    case Rejected => false
  }

  def remoteStartTransaction(idTag: String, connectorId: Option[Int]) =
    ?[RemoteStartStopStatus](_.remoteStartTransaction(RemoteStartTransactionRequest(idTag, connectorId), id))

  def remoteStopTransaction(transactionId: Int) =
    ?[RemoteStartStopStatus](_.remoteStopTransaction(RemoteStopTransactionRequest(transactionId), id))

  def unlockConnector(connectorId: Int) =
    ?(_.unlockConnector(UnlockConnectorRequest(connectorId), id)) match {
      case AcceptedValue5 => true
      case RejectedValue5 => false
    }

  def getDiagnostics(location: URI,
                     startTime: Option[DateTime],
                     stopTime: Option[DateTime],
                     retries: Option[Int],
                     retryInterval: Option[Int]) = {
    val req = GetDiagnosticsRequest(location, startTime, stopTime, retries, retryInterval)
    val res = ?(_.getDiagnostics(req, id))
    res.fileName
  }

  def changeConfiguration(key: String, value: String) =
    ?(_.changeConfiguration(ChangeConfigurationRequest(key, value), id)) match {
      case AcceptedValue => ConfigurationAccepted
      case RejectedValue => ConfigurationRejected
      case NotSupported => ConfigurationNotSupported
    }

  def changeAvailability(connectorId: Int, availabilityType: ocpp.AvailabilityType) = {
    val availability = availabilityType match {
      case ocpp.Operative => Operative
      case ocpp.Inoperative => Inoperative
    }
    ?(_.changeAvailability(ChangeAvailabilityRequest(connectorId, availability), id)) match {
      case AcceptedValue3 => AvailabilityAccepted
      case RejectedValue3 => AvailabilityRejected
      case Scheduled => AvailabilityScheduled
    }
  }

  def clearCache = ?(_.clearCache(ClearCacheRequest(), id)) match {
    case AcceptedValue2 => true
    case RejectedValue2 => false
  }

  def reset(resetType: ocpp.ResetType) = {
    val x = resetType match {
      case ocpp.Hard => Hard
      case ocpp.Soft => Soft
    }
    ?(_.reset(ResetRequest(x), id)) match {
      case AcceptedValue4 => true
      case RejectedValue4 => false
    }
  }

  def updateFirmware(retrieveDate: DateTime, location: URI, retries: Option[Int], retryInterval: Option[Int]) {
    ?(_.updateFirmware(UpdateFirmwareRequest(retrieveDate, location, retries, retryInterval), id))
  }
}

class ChargePointClientV15(val chargeBoxIdentity: String, uri: URI, http: Http) extends ChargePointClient {
  import v15._

  val bindings = new CustomDispatchHttpClients(http) with ChargePointServiceSoapBindings with SoapClients {
    override def baseAddress = uri
  }

  private def ?[T](f: ChargePointService => Either[scalaxb.Fault[Any], T]): T = rightOrException(f(bindings.service))

  private implicit def remoteStartStopStatusToAccepted(x: RemoteStartStopStatus): Accepted = x match {
    case AcceptedValue4 => true
    case RejectedValue3 => false
  }

  def remoteStartTransaction(idTag: String, connectorId: Option[Int]) =
    ?[RemoteStartStopStatus](_.remoteStartTransaction(RemoteStartTransactionRequest(idTag, connectorId), id))

  def remoteStopTransaction(transactionId: Int) =
    ?[RemoteStartStopStatus](_.remoteStopTransaction(RemoteStopTransactionRequest(transactionId), id))

  def unlockConnector(connectorId: Int) =
    ?(_.unlockConnector(UnlockConnectorRequest(connectorId), id)) match {
      case AcceptedValue9 => true
      case RejectedValue8 => false
    }

  def getDiagnostics(location: URI,
                     startTime: Option[DateTime],
                     stopTime: Option[DateTime],
                     retries: Option[Int],
                     retryInterval: Option[Int]) = {
    val req = GetDiagnosticsRequest(location, startTime, stopTime, retries, retryInterval)
    val res = ?(_.getDiagnostics(req, id))
    res.fileName
  }

  def changeConfiguration(key: String, value: String) =
    ?(_.changeConfiguration(ChangeConfigurationRequest(key, value), id)) match {
      case AcceptedValue5 => ConfigurationAccepted
      case RejectedValue4 => ConfigurationRejected
      case NotSupportedValue => ConfigurationNotSupported
    }

  def changeAvailability(connectorId: Int, availabilityType: ocpp.AvailabilityType) = {
    val availability = availabilityType match {
      case ocpp.Operative => Operative
      case ocpp.Inoperative => Inoperative
    }
    ?(_.changeAvailability(ChangeAvailabilityRequest(connectorId, availability), id)) match {
      case AcceptedValue7 => AvailabilityAccepted
      case RejectedValue6 => AvailabilityRejected
      case Scheduled => AvailabilityScheduled
    }
  }

  def clearCache = ?(_.clearCache(ClearCacheRequest(), id)) match {
    case AcceptedValue6 => true
    case RejectedValue5 => false
  }

  def reset(resetType: ocpp.ResetType) = {
    val x = resetType match {
      case ocpp.Hard => Hard
      case ocpp.Soft => Soft
    }
    ?(_.reset(ResetRequest(x), id)) match {
      case AcceptedValue8 => true
      case RejectedValue7 => false
    }
  }

  def updateFirmware(retrieveDate: DateTime, location: URI, retries: Option[Int], retryInterval: Option[Int]) {
    ?(_.updateFirmware(UpdateFirmwareRequest(retrieveDate, location, retries, retryInterval), id))
  }
}