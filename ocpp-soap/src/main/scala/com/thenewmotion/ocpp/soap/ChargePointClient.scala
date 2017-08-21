package com.thenewmotion.ocpp
package soap

import com.thenewmotion.ocpp.{messages => ocpp}
import dispatch.Http
import messages._


/**
 * @author Yaroslav Klymko
 */
trait ChargePointClient extends ChargePoint with Client

object ChargePointClient {
  def apply(chargeBoxIdentity: String,
            version: Version.Value,
            uri: Uri,
            http: Http,
            endpoint: Option[Uri] = None): ChargePointClient = version match {
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


  def remoteStartTransaction(x: RemoteStartTransactionReq) = {
    val req = RemoteStartTransactionRequest(x.idTag, x.connector.map(_.toOcpp))
    RemoteStartTransactionRes(?(_.remoteStartTransaction, req).toOcpp)
  }

  def remoteStopTransaction(req: RemoteStopTransactionReq) = RemoteStopTransactionRes(
    ?(_.remoteStopTransaction, RemoteStopTransactionRequest(req.transactionId)).toOcpp)

  def unlockConnector(req: UnlockConnectorReq) = UnlockConnectorRes(
    ?(_.unlockConnector, UnlockConnectorRequest(req.connector.toOcpp)) match {
      case AcceptedValue3 => true
      case RejectedValue3 => false
    })

  def getDiagnostics(x: GetDiagnosticsReq) = {
    val req = GetDiagnosticsRequest(
      location = x.location,
      startTime = x.startTime.map(_.toXMLCalendar),
      stopTime = x.stopTime.map(_.toXMLCalendar),
      retries = x.retries.numberOfRetries,
      retryInterval = x.retries.intervalInSeconds)
    GetDiagnosticsRes(stringOption(?(_.getDiagnostics, req).fileName))
  }

  def changeConfiguration(req: ChangeConfigurationReq) = ChangeConfigurationRes(
    ?(_.changeConfiguration, ChangeConfigurationRequest(req.key, req.value)) match {
      case Accepted => ocpp.ConfigurationStatus.Accepted
      case Rejected => ocpp.ConfigurationStatus.Rejected
      case NotSupported => ocpp.ConfigurationStatus.NotSupported
    })

  def getConfiguration(req: GetConfigurationReq) = notSupported("getConfiguration")

  def changeAvailability(x: ChangeAvailabilityReq) = {
    val availability = x.availabilityType match {
      case ocpp.AvailabilityType.Operative => Operative
      case ocpp.AvailabilityType.Inoperative => Inoperative
    }
    ChangeAvailabilityRes(?(_.changeAvailability, ChangeAvailabilityRequest(x.scope.toOcpp, availability)) match {
      case AcceptedValue => ocpp.AvailabilityStatus.Accepted
      case RejectedValue => ocpp.AvailabilityStatus.Rejected
      case Scheduled => ocpp.AvailabilityStatus.Scheduled
    })
  }

  def clearCache = ClearCacheRes(?(_.clearCache, ClearCacheRequest()) match {
    case AcceptedValue4 => true
    case RejectedValue4 => false
  })

  def reset(x: ResetReq) = {
    val value = x.resetType match {
      case ocpp.ResetType.Hard => Hard
      case ocpp.ResetType.Soft => Soft
    }
    ResetRes(?(_.reset, ResetRequest(value)) match {
      case AcceptedValue2 => true
      case RejectedValue2 => false
    })
  }

  def updateFirmware(x: UpdateFirmwareReq) {
    ?(_.updateFirmware, UpdateFirmwareRequest(
      retrieveDate = x.retrieveDate.toXMLCalendar,
      location = x.location,
      retries = x.retries.numberOfRetries,
      retryInterval = x.retries.intervalInSeconds))
  }

  def sendLocalList(req: SendLocalListReq) = notSupported("sendLocalList")
  def getLocalListVersion = notSupported("getLocalListVersion")
  def dataTransfer(req: ChargePointDataTransferReq) = notSupported("dataTransfer")
  def reserveNow(req: ReserveNowReq) = notSupported("reserveNow")
  def cancelReservation(req: CancelReservationReq) = notSupported("cancelReservation")
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

  def remoteStartTransaction(x: RemoteStartTransactionReq) = {
    val req = RemoteStartTransactionRequest(x.idTag, x.connector.map(_.toOcpp))
    RemoteStartTransactionRes(?(_.remoteStartTransaction, req).toOcpp)
  }

  def remoteStopTransaction(req: RemoteStopTransactionReq) = RemoteStopTransactionRes(
    ?(_.remoteStopTransaction, RemoteStopTransactionRequest(req.transactionId)).toOcpp)

  def unlockConnector(req: UnlockConnectorReq) = UnlockConnectorRes(
    ?(_.unlockConnector, UnlockConnectorRequest(req.connector.toOcpp)) match {
      case AcceptedValue4 => true
      case RejectedValue4 => false
    })

  def getDiagnostics(x: GetDiagnosticsReq) = {
    val req = GetDiagnosticsRequest(
      location = x.location,
      startTime = x.startTime.map(_.toXMLCalendar),
      stopTime = x.stopTime.map(_.toXMLCalendar),
      retries = x.retries.numberOfRetries,
      retryInterval = x.retries.intervalInSeconds)
    GetDiagnosticsRes(?(_.getDiagnostics, req).fileName)
  }

  def changeConfiguration(x: ChangeConfigurationReq) = ChangeConfigurationRes(
    ?(_.changeConfiguration, ChangeConfigurationRequest(x.key, x.value)) match {
      case AcceptedValue8 => ocpp.ConfigurationStatus.Accepted
      case RejectedValue7 => ocpp.ConfigurationStatus.Rejected
      case NotSupported => ocpp.ConfigurationStatus.NotSupported
    })

  def getConfiguration(x: GetConfigurationReq) = {
    val res = ?(_.getConfiguration, GetConfigurationRequest(x.keys))
    val values = res.configurationKey.map {
      case KeyValue(key, readonly, value) => ocpp.KeyValue(key, readonly, stringOption(value))
    }.toList
    GetConfigurationRes(values, res.unknownKey.toList)
  }

  def changeAvailability(req: ChangeAvailabilityReq) = {
    val availability = req.availabilityType match {
      case ocpp.AvailabilityType.Operative => Operative
      case ocpp.AvailabilityType.Inoperative => Inoperative
    }
    ChangeAvailabilityRes(?(_.changeAvailability, ChangeAvailabilityRequest(req.scope.toOcpp, availability)) match {
      case AcceptedValue7 => ocpp.AvailabilityStatus.Accepted
      case RejectedValue6 => ocpp.AvailabilityStatus.Rejected
      case Scheduled => ocpp.AvailabilityStatus.Scheduled
    })
  }

  def clearCache = ClearCacheRes(?(_.clearCache, ClearCacheRequest()) match {
    case AcceptedValue3 => true
    case RejectedValue3 => false
  })

  def reset(req: ResetReq) = {
    val value = req.resetType match {
      case ocpp.ResetType.Hard => Hard
      case ocpp.ResetType.Soft => Soft
    }
    ResetRes(?(_.reset, ResetRequest(value)) match {
      case AcceptedValue6 => true
      case RejectedValue5 => false
    })
  }

  def updateFirmware(x: UpdateFirmwareReq) {
    ?(_.updateFirmware, UpdateFirmwareRequest(
      retrieveDate = x.retrieveDate.toXMLCalendar,
      location = x.location,
      retries = x.retries.numberOfRetries,
      retryInterval = x.retries.intervalInSeconds))
  }

  def sendLocalList(x: SendLocalListReq) = {
    val update = {
      import ocpp.{UpdateType => ocpp}
      x.updateType match {
        case ocpp.Differential => Differential
        case ocpp.Full => Full
      }
    }

    def authorisationData(x: ocpp.AuthorisationData): AuthorisationData = x match {
      case AuthorisationAdd(idTag, idTagInfo) => AuthorisationData(idTag, Some(idTagInfo.toIdTagInfo))
      case AuthorisationRemove(idTag) => AuthorisationData(idTag, None)
    }

    val req = SendLocalListRequest(update, x.listVersion.version, x.localAuthorisationList.map(authorisationData), x.hash)
    val res = ?(_.sendLocalList, req)

    import ocpp.{UpdateStatus => ocpp}
    SendLocalListRes(res.status match {
      case AcceptedValue10 => ocpp.UpdateAccepted(stringOption(res.hash))
      case Failed => ocpp.UpdateFailed
      case HashError => ocpp.HashError
      case NotSupportedValue => ocpp.NotSupportedValue
      case VersionMismatch => ocpp.VersionMismatch
    })
  }

  def getLocalListVersion = GetLocalListVersionRes(
    AuthListVersion(?(_.getLocalListVersion, GetLocalListVersionRequest())))

  def dataTransfer(req: ChargePointDataTransferReq) = {
    val res = ?(_.dataTransfer, DataTransferRequest(req.vendorId, req.messageId, req.data))
    val status: ocpp.DataTransferStatus.Value = {
      import ocpp.{DataTransferStatus => ocpp}
      res.status match {
        case AcceptedValue => ocpp.Accepted
        case RejectedValue => ocpp.Rejected
        case UnknownMessageId => ocpp.UnknownMessageId
        case UnknownVendorId => ocpp.UnknownVendorId
      }
    }
    ChargePointDataTransferRes(status, stringOption(res.data))
  }

  def reserveNow(x: ReserveNowReq) = {
    val req = ReserveNowRequest(
      connectorId = x.connector.toOcpp,
      expiryDate = x.expiryDate.toXMLCalendar,
      idTag = x.idTag,
      parentIdTag = x.parentIdTag,
      reservationId = x.reservationId)

    import ocpp.{Reservation => ocpp}
    ReserveNowRes(?(_.reserveNow, req) match {
      case Accepted => ocpp.Accepted
      case Faulted => ocpp.Faulted
      case Occupied => ocpp.Occupied
      case Rejected => ocpp.Rejected
      case Unavailable => ocpp.Unavailable
    })
  }

  def cancelReservation(req: CancelReservationReq) = CancelReservationRes(
    ?(_.cancelReservation, CancelReservationRequest(req.reservationId)) match {
      case AcceptedValue9 => true
      case RejectedValue8 => false
    })
}