package com.thenewmotion.chargenetwork.ocpp

import java.net.URI
import scalaxb.{Fault, SoapClients}
import com.thenewmotion.chargenetwork.ocpp
import com.thenewmotion.time.Imports._
import javax.xml.datatype.XMLGregorianCalendar


/**
 * @author Yaroslav Klymko
 */
trait ChargePointClient extends ChargePointService {
  // todo move to scalax
  protected implicit def fromOptionToOption[A, B](from: Option[A])(implicit conversion: A => B): Option[B] = from.map(conversion(_))

  // todo
  protected implicit def xmlGregorianCalendarOption(x: Option[DateTime]): Option[XMLGregorianCalendar] = fromOptionToOption(x)

  protected def rightOrException[T](x: Either[Fault[Any], T]) = x match {
    case Left(fault) => sys.error(fault.toString) // TODO
    case Right(t) => t
  }
}

class ChargePointClientV12(uri: java.net.URI, chargeBoxIdentity: String) extends ChargePointClient {
  import v12._

  private def id = chargeBoxIdentity

  val bindings = new ChargePointServiceSoapBindings with SoapClients with FixedDispatchHttpClients {
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

class ChargePointClientV15(uri: java.net.URI, chargeBoxIdentity: String) extends ChargePointClient {
  import v15._

  private def id = chargeBoxIdentity

  val bindings = new ChargePointServiceSoapBindings with SoapClients with FixedDispatchHttpClients {
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