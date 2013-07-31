package com.thenewmotion.ocpp

import com.thenewmotion.ocpp
import dispatch.Http
import org.joda.time.DateTime


/**
 * @author Yaroslav Klymko
 */
trait ChargePointClient extends ChargePointService with Client

object ChargePointClient {
  def apply(chargeBoxIdentity: String,
            version: Version.Value,
            uri: Uri,
            http: Http,
            endpoint: Option[Uri] = None): ChargePointService = version match {
    case Version.V12 => new ChargePointClientV12(chargeBoxIdentity, uri, http, endpoint)
    case Version.V15 => new ChargePointClientV15(chargeBoxIdentity, uri, http, endpoint)
  }
}

private[ocpp] class ChargePointClientV12(val chargeBoxIdentity: String, uri: Uri, http: Http, endpoint: Option[Uri])
  extends ChargePointClient
  with ScalaxbClient {

  import v12._
  import ConvertersV12._

  def version = Version.V12

  type Service = ChargePointService

  val service = new CustomDispatchHttpClients(http) with ChargePointServiceSoapBindings with WsaAddressingSoapClients {
    override def baseAddress = uri
    def endpoint = ChargePointClientV12.this.endpoint
  }.service

  def remoteStartTransaction(idTag: IdTag, connector: Option[ConnectorScope]) = {
    val req = RemoteStartTransactionRequest(idTag, connector.map(_.toOcpp))
    ?(_.remoteStartTransaction, req).toOcpp
  }

  def remoteStopTransaction(transactionId: Int) =
    ?(_.remoteStopTransaction, RemoteStopTransactionRequest(transactionId)).toOcpp


  def unlockConnector(connector: ConnectorScope) =
    ?(_.unlockConnector, UnlockConnectorRequest(connector.toOcpp)) match {
      case AcceptedValue3 => true
      case RejectedValue3 => false
    }

  def getDiagnostics(location: Uri,
                     startTime: Option[DateTime],
                     stopTime: Option[DateTime],
                     retries: Retries) = {
    val req = GetDiagnosticsRequest(
      location,
      startTime.map(_.toXMLCalendar),
      stopTime.map(_.toXMLCalendar),
      retries.numberOfRetries,
      retries.intervalInSeconds)
    val res = ?(_.getDiagnostics, req)
    res.fileName
  }

  def changeConfiguration(key: String, value: String) =
    ?(_.changeConfiguration, ChangeConfigurationRequest(key, value)) match {
      case Accepted => ocpp.ConfigurationStatus.Accepted
      case Rejected => ocpp.ConfigurationStatus.Rejected
      case NotSupported => ocpp.ConfigurationStatus.NotSupported
    }

  def getConfiguration(keys: List[String]) = notSupported("getConfiguration")

  def changeAvailability(scope: Scope, availabilityType: ocpp.AvailabilityType.Value) = {
    val availability = availabilityType match {
      case ocpp.AvailabilityType.Operative => Operative
      case ocpp.AvailabilityType.Inoperative => Inoperative
    }
    ?(_.changeAvailability, ChangeAvailabilityRequest(scope.toOcpp, availability)) match {
      case AcceptedValue => ocpp.AvailabilityStatus.Accepted
      case RejectedValue => ocpp.AvailabilityStatus.Rejected
      case Scheduled => ocpp.AvailabilityStatus.Scheduled
    }
  }

  def clearCache = ?(_.clearCache, ClearCacheRequest()) match {
    case AcceptedValue4 => true
    case RejectedValue4 => false
  }

  def reset(resetType: ocpp.ResetType.Value) = {
    val x = resetType match {
      case ocpp.ResetType.Hard => Hard
      case ocpp.ResetType.Soft => Soft
    }
    ?(_.reset, ResetRequest(x)) match {
      case AcceptedValue2 => true
      case RejectedValue2 => false
    }
  }

  def updateFirmware(retrieveDate: DateTime, location: Uri, retries: Retries) {
    ?(_.updateFirmware, UpdateFirmwareRequest(retrieveDate.toXMLCalendar, location,
                                              retries.numberOfRetries, retries.intervalInSeconds))
  }

  def sendLocalList(updateType: UpdateType.Value,
                    listVersion: AuthListSupported,
                    localAuthorisationList: List[AuthorisationData],
                    hash: Option[String]) = notSupported("sendLocalList")

  def getLocalListVersion = notSupported("getLocalListVersion")

  def dataTransfer(vendorId: String, messageId: Option[String], data: Option[String]) =
    notSupported("dataTransfer")

  def reserveNow(connector: Scope, expiryDate: DateTime, idTag: IdTag, parentIdTag: Option[String], reservationId: Int) =
    notSupported("reserveNow")

  def cancelReservation(reservationId: Int) = notSupported("cancelReservation")
}

private[ocpp] class ChargePointClientV15(val chargeBoxIdentity: String, uri: Uri, http: Http, endpoint: Option[Uri])
  extends ChargePointClient
  with ScalaxbClient {

  import v15._
  import ConvertersV15._

  def version = Version.V15

  type Service = ChargePointService

  val service = new CustomDispatchHttpClients(http) with ChargePointServiceSoapBindings with WsaAddressingSoapClients {
    override def baseAddress = uri
    def endpoint = ChargePointClientV15.this.endpoint
  }.service

  def remoteStartTransaction(idTag: IdTag, connector: Option[ConnectorScope]) = {
    val req = RemoteStartTransactionRequest(idTag, connector.map(_.toOcpp))
    ?(_.remoteStartTransaction, req).toOcpp
  }

  def remoteStopTransaction(transactionId: Int) =
    ?(_.remoteStopTransaction, RemoteStopTransactionRequest(transactionId)).toOcpp

  def unlockConnector(connector: ConnectorScope) =
    ?(_.unlockConnector, UnlockConnectorRequest(connector.toOcpp)) match {
      case AcceptedValue4 => true
      case RejectedValue4 => false
    }

  def getDiagnostics(location: Uri,
                     startTime: Option[DateTime],
                     stopTime: Option[DateTime],
                     retries: Retries) = {
    val req = GetDiagnosticsRequest(
      location,
      startTime.map(_.toXMLCalendar),
      stopTime.map(_.toXMLCalendar),
      retries.numberOfRetries,
      retries.intervalInSeconds)
    val res = ?(_.getDiagnostics, req)
    res.fileName
  }

  def changeConfiguration(key: String, value: String) =
    ?(_.changeConfiguration, ChangeConfigurationRequest(key, value)) match {
      case AcceptedValue8 => ocpp.ConfigurationStatus.Accepted
      case RejectedValue7 => ocpp.ConfigurationStatus.Rejected
      case NotSupported => ocpp.ConfigurationStatus.NotSupported
    }

  def getConfiguration(keys: List[String]) = {
    val res = ?(_.getConfiguration, GetConfigurationRequest(keys: _*))
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
    ?(_.changeAvailability, ChangeAvailabilityRequest(scope.toOcpp, availability)) match {
      case AcceptedValue7 => ocpp.AvailabilityStatus.Accepted
      case RejectedValue6 => ocpp.AvailabilityStatus.Rejected
      case Scheduled => ocpp.AvailabilityStatus.Scheduled
    }
  }

  def clearCache = ?(_.clearCache, ClearCacheRequest()) match {
    case AcceptedValue3 => true
    case RejectedValue3 => false
  }

  def reset(resetType: ocpp.ResetType.Value) = {
    val x = resetType match {
      case ocpp.ResetType.Hard => Hard
      case ocpp.ResetType.Soft => Soft
    }
    ?(_.reset, ResetRequest(x)) match {
      case AcceptedValue6 => true
      case RejectedValue5 => false
    }
  }

  def updateFirmware(retrieveDate: DateTime, location: Uri, retries: Retries) {
    ?(_.updateFirmware, UpdateFirmwareRequest(retrieveDate.toXMLCalendar, location,
                                              retries.numberOfRetries, retries.intervalInSeconds))
  }

  def sendLocalList(updateType: ocpp.UpdateType.Value,
                    listVersion: AuthListSupported,
                    localAuthorisationList: List[ocpp.AuthorisationData],
                    hash: Option[String]) = {
    val update = {
      import ocpp.{UpdateType => ocpp}
      updateType match {
        case ocpp.Differential => Differential
        case ocpp.Full => Full
      }
    }

    def authorisationData(x: ocpp.AuthorisationData): AuthorisationData = x match {
      case AuthorisationAdd(idTag, idTagInfo) => AuthorisationData(idTag, Some(idTagInfo.toIdTagInfo))
      case AuthorisationRemove(idTag) => AuthorisationData(idTag, None)
    }

    val req = SendLocalListRequest(update, listVersion.version, localAuthorisationList.map(authorisationData(_)), hash)
    val res = ?(_.sendLocalList, req)

    import ocpp.{UpdateStatus => ocpp}
    res.status match {
      case AcceptedValue10 => ocpp.UpdateAccepted(res.hash)
      case Failed => ocpp.UpdateFailed
      case HashError => ocpp.HashError
      case NotSupportedValue => ocpp.NotSupportedValue
      case VersionMismatch => ocpp.VersionMismatch
    }
  }

  def getLocalListVersion = AuthListVersion(?(_.getLocalListVersion, GetLocalListVersionRequest()))

  def dataTransfer(vendorId: String, messageId: Option[String], data: Option[String]) = {
    val res = ?(_.dataTransfer, DataTransferRequest(vendorId, messageId, data))
    val status: ocpp.DataTransferStatus.Value = {
      import ocpp.{DataTransferStatus => ocpp}
      res.status match {
        case AcceptedValue => ocpp.Accepted
        case RejectedValue => ocpp.Rejected
        case UnknownMessageId => ocpp.UnknownMessageId
        case UnknownVendorId => ocpp.UnknownVendorId
      }
    }
    ocpp.ChargePointService.DataTransferRes(status, res.data)
  }

  def reserveNow(connector: Scope,
                 expiryDate: DateTime,
                 idTag: IdTag,
                 parentIdTag: Option[String],
                 reservationId: Int) = {
    val req = ReserveNowRequest(connector.toOcpp, expiryDate.toXMLCalendar, idTag, parentIdTag, reservationId)

    import ocpp.{Reservation => ocpp}
    ?(_.reserveNow, req) match {
      case Accepted => ocpp.Accepted
      case Faulted => ocpp.Faulted
      case Occupied => ocpp.Occupied
      case Rejected => ocpp.Rejected
      case Unavailable => ocpp.Unavailable
    }
  }

  def cancelReservation(reservationId: Int) =
    ?(_.cancelReservation, CancelReservationRequest(reservationId)) match {
      case AcceptedValue9 => true
      case RejectedValue8 => false
    }
}