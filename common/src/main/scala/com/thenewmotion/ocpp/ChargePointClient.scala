package com.thenewmotion.ocpp

import com.thenewmotion.ocpp
import dispatch.Http
import ChargePoint._


/**
 * @author Yaroslav Klymko
 */
trait ChargePointClient extends ChargePoint with Client

object ChargePointClient {
  def apply(chargeBoxIdentity: String,
            version: Version.Value,
            uri: Uri,
            http: Http,
            endpoint: Option[Uri] = None): ChargePoint = version match {
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


  def apply(req: Req) = req match {
    case x: RemoteStartTransactionReq =>
      val req = RemoteStartTransactionRequest(x.idTag, x.connector.map(_.toOcpp))
      RemoteStartTransactionRes(?(_.remoteStartTransaction, req).toOcpp)

    case x: RemoteStopTransactionReq => RemoteStopTransactionRes(
      ?(_.remoteStopTransaction, RemoteStopTransactionRequest(x.transactionId)).toOcpp)

    case x: UnlockConnectorReq => UnlockConnectorRes(
      ?(_.unlockConnector, UnlockConnectorRequest(x.connector.toOcpp)) match {
        case AcceptedValue3 => true
        case RejectedValue3 => false
      })

    case x: GetDiagnosticsReq =>
      val req = GetDiagnosticsRequest(
        location = x.location,
        startTime = x.startTime.map(_.toXMLCalendar),
        stopTime = x.stopTime.map(_.toXMLCalendar),
        retries = x.retries.numberOfRetries,
        retryInterval = x.retries.intervalInSeconds)

      GetDiagnosticsRes(?(_.getDiagnostics, req).fileName)

    case x: ChangeConfigurationReq => ChangeConfigurationRes(
      ?(_.changeConfiguration, ChangeConfigurationRequest(x.key, x.value)) match {
        case Accepted => ocpp.ConfigurationStatus.Accepted
        case Rejected => ocpp.ConfigurationStatus.Rejected
        case NotSupported => ocpp.ConfigurationStatus.NotSupported
      })

    case x: GetConfigurationReq => notSupported("getConfiguration")

    case x: ChangeAvailabilityReq =>
      val availability = x.availabilityType match {
        case ocpp.AvailabilityType.Operative => Operative
        case ocpp.AvailabilityType.Inoperative => Inoperative
      }
      ChangeAvailabilityRes(?(_.changeAvailability, ChangeAvailabilityRequest(x.scope.toOcpp, availability)) match {
        case AcceptedValue => ocpp.AvailabilityStatus.Accepted
        case RejectedValue => ocpp.AvailabilityStatus.Rejected
        case Scheduled => ocpp.AvailabilityStatus.Scheduled
      })

    case ClearCacheReq => ClearCacheRes(?(_.clearCache, ClearCacheRequest()) match {
      case AcceptedValue4 => true
      case RejectedValue4 => false
    })

    case x: ResetReq =>
      val value = x.resetType match {
        case ocpp.ResetType.Hard => Hard
        case ocpp.ResetType.Soft => Soft
      }
      ResetRes(?(_.reset, ResetRequest(value)) match {
        case AcceptedValue2 => true
        case RejectedValue2 => false
      })

    case x: UpdateFirmwareReq =>
      ?(_.updateFirmware, UpdateFirmwareRequest(
        retrieveDate = x.retrieveDate.toXMLCalendar,
        location = x.location,
        retries = x.retries.numberOfRetries,
        retryInterval = x.retries.intervalInSeconds))
      UpdateFirmwareRes

    case _: SendLocalListReq => notSupported("sendLocalList")
    case GetLocalListVersionReq => notSupported("getLocalListVersion")
    case _: DataTransferReq => notSupported("dataTransfer")
    case _: ReserveNowReq => notSupported("reserveNow")
    case _: CancelReservationReq => notSupported("cancelReservation")
  }
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


  def apply(req: Req) = req match {
    case x: RemoteStartTransactionReq =>
      val req = RemoteStartTransactionRequest(x.idTag, x.connector.map(_.toOcpp))
      RemoteStartTransactionRes(?(_.remoteStartTransaction, req).toOcpp)

    case x: RemoteStopTransactionReq => RemoteStopTransactionRes(
      ?(_.remoteStopTransaction, RemoteStopTransactionRequest(x.transactionId)).toOcpp)

    case x: UnlockConnectorReq => UnlockConnectorRes(
      ?(_.unlockConnector, UnlockConnectorRequest(x.connector.toOcpp)) match {
        case AcceptedValue4 => true
        case RejectedValue4 => false
      })

    case x: GetDiagnosticsReq =>
      val req = GetDiagnosticsRequest(
        location = x.location,
        startTime = x.startTime.map(_.toXMLCalendar),
        stopTime = x.stopTime.map(_.toXMLCalendar),
        retries = x.retries.numberOfRetries,
        retryInterval = x.retries.intervalInSeconds)
      GetDiagnosticsRes(?(_.getDiagnostics, req).fileName)

    case x: ChangeConfigurationReq => ChangeConfigurationRes(
      ?(_.changeConfiguration, ChangeConfigurationRequest(x.key, x.value)) match {
        case AcceptedValue8 => ocpp.ConfigurationStatus.Accepted
        case RejectedValue7 => ocpp.ConfigurationStatus.Rejected
        case NotSupported => ocpp.ConfigurationStatus.NotSupported
      })

    case x: GetConfigurationReq =>
      val res = ?(_.getConfiguration, GetConfigurationRequest(x.keys: _*))
      val values = res.configurationKey.map {
        case KeyValue(key, readonly, value) => ocpp.KeyValue(key, readonly, value)
      }.toList
      GetConfigurationRes(values, res.unknownKey.toList)

    case x: ChangeAvailabilityReq =>
      val availability = x.availabilityType match {
        case ocpp.AvailabilityType.Operative => Operative
        case ocpp.AvailabilityType.Inoperative => Inoperative
      }
      ChangeAvailabilityRes(?(_.changeAvailability, ChangeAvailabilityRequest(x.scope.toOcpp, availability)) match {
        case AcceptedValue7 => ocpp.AvailabilityStatus.Accepted
        case RejectedValue6 => ocpp.AvailabilityStatus.Rejected
        case Scheduled => ocpp.AvailabilityStatus.Scheduled
      })

    case ClearCacheReq => ClearCacheRes(?(_.clearCache, ClearCacheRequest()) match {
      case AcceptedValue3 => true
      case RejectedValue3 => false
    })

    case x: ResetReq =>
      val value = x.resetType match {
        case ocpp.ResetType.Hard => Hard
        case ocpp.ResetType.Soft => Soft
      }
      ResetRes(?(_.reset, ResetRequest(value)) match {
        case AcceptedValue6 => true
        case RejectedValue5 => false
      })

    case x: UpdateFirmwareReq =>
      ?(_.updateFirmware, UpdateFirmwareRequest(
        retrieveDate = x.retrieveDate.toXMLCalendar,
        location = x.location,
        retries = x.retries.numberOfRetries,
        retryInterval = x.retries.intervalInSeconds))
      UpdateFirmwareRes

    case x: SendLocalListReq =>
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
        case AcceptedValue10 => ocpp.UpdateAccepted(res.hash)
        case Failed => ocpp.UpdateFailed
        case HashError => ocpp.HashError
        case NotSupportedValue => ocpp.NotSupportedValue
        case VersionMismatch => ocpp.VersionMismatch
      })

    case GetLocalListVersionReq => GetLocalListVersionRes(
      AuthListVersion(?(_.getLocalListVersion, GetLocalListVersionRequest())))

    case x: DataTransferReq =>
      val res = ?(_.dataTransfer, DataTransferRequest(x.vendorId, x.messageId, x.data))
      val status: ocpp.DataTransferStatus.Value = {
        import ocpp.{DataTransferStatus => ocpp}
        res.status match {
          case AcceptedValue => ocpp.Accepted
          case RejectedValue => ocpp.Rejected
          case UnknownMessageId => ocpp.UnknownMessageId
          case UnknownVendorId => ocpp.UnknownVendorId
        }
      }
      DataTransferRes(status, res.data)

    case x: ReserveNowReq =>
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

    case x: CancelReservationReq => CancelReservationRes(
      ?(_.cancelReservation, CancelReservationRequest(x.reservationId)) match {
        case AcceptedValue9 => true
        case RejectedValue8 => false
      })
  }
}