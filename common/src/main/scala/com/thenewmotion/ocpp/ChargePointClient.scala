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

object ChargePointClient {
  def apply(version: Version.Value)
           (chargeBoxIdentity: String, uri: URI, http: Http): ChargePointService = version match {
    case Version.V12 => new ChargePointClientV12(chargeBoxIdentity, uri, http)
    case Version.V15 => new ChargePointClientV15(chargeBoxIdentity, uri, http)
  }
}

class ChargePointClientV12(val chargeBoxIdentity: String, uri: URI, http: Http) extends ChargePointClient {
  import v12._

  val bindings = new CustomDispatchHttpClients(http) with ChargePointServiceSoapBindings with SoapClients {
    override def baseAddress = uri
  }

  private def ?[T](f: ChargePointService => Either[scalaxb.Fault[Any], T]): T = rightOrException(f(bindings.service))

  private implicit def remoteStartStopStatusToAccepted(x: RemoteStartStopStatus): Accepted = x match {
    case AcceptedValue5 => true
    case RejectedValue5 => false
  }

  def remoteStartTransaction(idTag: String, connector: Option[ConnectorScope]) = {
    val req = RemoteStartTransactionRequest(idTag, connector.map(_.toOcpp))
    ?[RemoteStartStopStatus](_.remoteStartTransaction(req, id))
  }

  def remoteStopTransaction(transactionId: Int) =
    ?[RemoteStartStopStatus](_.remoteStopTransaction(RemoteStopTransactionRequest(transactionId), id))


  def unlockConnector(connector: ConnectorScope) =
    ?(_.unlockConnector(UnlockConnectorRequest(connector.toOcpp), id)) match {
      case AcceptedValue3 => true
      case RejectedValue3 => false
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
      case Accepted => ocpp.ConfigurationStatus.Accepted
      case Rejected => ocpp.ConfigurationStatus.Rejected
      case NotSupported => ocpp.ConfigurationStatus.NotSupported
    }

  def getConfiguration(keys: List[String]) = throw new ActionNotSupportedException(Version.V12, "getConfiguration")

  def changeAvailability(scope: Scope, availabilityType: ocpp.AvailabilityType.Value) = {
    val availability = availabilityType match {
      case ocpp.AvailabilityType.Operative => Operative
      case ocpp.AvailabilityType.Inoperative => Inoperative
    }
    ?(_.changeAvailability(ChangeAvailabilityRequest(scope.toOcpp, availability), id)) match {
      case AcceptedValue => ocpp.AvailabilityStatus.Accepted
      case RejectedValue => ocpp.AvailabilityStatus.Rejected
      case Scheduled => ocpp.AvailabilityStatus.Scheduled
    }
  }

  def clearCache = ?(_.clearCache(ClearCacheRequest(), id)) match {
    case AcceptedValue4 => true
    case RejectedValue4 => false
  }

  def reset(resetType: ocpp.ResetType.Value) = {
    val x = resetType match {
      case ocpp.ResetType.Hard => Hard
      case ocpp.ResetType.Soft => Soft
    }
    ?(_.reset(ResetRequest(x), id)) match {
      case AcceptedValue2 => true
      case RejectedValue2 => false
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
    case AcceptedValue2 => true
    case RejectedValue2 => false
  }

  def remoteStartTransaction(idTag: String, connector: Option[ConnectorScope]) = {
    val req = RemoteStartTransactionRequest(idTag, connector.map(_.toOcpp))
    ?[RemoteStartStopStatus](_.remoteStartTransaction(req, id))
  }

  def remoteStopTransaction(transactionId: Int) =
    ?[RemoteStartStopStatus](_.remoteStopTransaction(RemoteStopTransactionRequest(transactionId), id))

  def unlockConnector(connector: ConnectorScope) =
    ?(_.unlockConnector(UnlockConnectorRequest(connector.toOcpp), id)) match {
      case AcceptedValue4 => true
      case RejectedValue4 => false
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
      case AcceptedValue8 => ocpp.ConfigurationStatus.Accepted
      case RejectedValue7 => ocpp.ConfigurationStatus.Rejected
      case NotSupported => ocpp.ConfigurationStatus.NotSupported
    }

  def getConfiguration(keys: List[String]) = {
    val res = ?(_.getConfiguration(GetConfigurationRequest(keys: _*), id))
    val values = res.configurationKey.map {
      case KeyValue(key, readonly, value) => ocpp.KeyValue(key, readonly, value)
    }.toList
    Configuration(values, res.unknownKey.toList)
  }

  def changeAvailability(scope: Scope, availabilityType: ocpp.AvailabilityType.Value) = {
    val availability = availabilityType match {
      case ocpp.AvailabilityType.Operative => Operative
      case ocpp.AvailabilityType.Inoperative => Inoperative
    }
    ?(_.changeAvailability(ChangeAvailabilityRequest(scope.toOcpp, availability), id)) match {
      case AcceptedValue7 => ocpp.AvailabilityStatus.Accepted
      case RejectedValue6 => ocpp.AvailabilityStatus.Rejected
      case Scheduled => ocpp.AvailabilityStatus.Scheduled
    }
  }

  def clearCache = ?(_.clearCache(ClearCacheRequest(), id)) match {
    case AcceptedValue3 => true
    case RejectedValue3 => false
  }

  def reset(resetType: ocpp.ResetType.Value) = {
    val x = resetType match {
      case ocpp.ResetType.Hard => Hard
      case ocpp.ResetType.Soft => Soft
    }
    ?(_.reset(ResetRequest(x), id)) match {
      case AcceptedValue6 => true
      case RejectedValue5 => false
    }
  }

  def updateFirmware(retrieveDate: DateTime, location: URI, retries: Option[Int], retryInterval: Option[Int]) {
    ?(_.updateFirmware(UpdateFirmwareRequest(retrieveDate, location, retries, retryInterval), id))
  }
}