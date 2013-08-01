package com.thenewmotion
package ocpp

import scala.xml.NodeSeq
import soapenvelope12.Body
import scalaxb.XMLFormat
import chargepoint._

object ChargePointDispatcher {
  def apply(version: Version.Value): Dispatcher[ChargePoint] = version match {
    case Version.V12 => sys.error("Requests to the charge point are not yet supported with OCPP 1.2")
    case Version.V15 => ChargePointDispatcherV15
  }
}

/**
 * Can call the corresponding methods on a ChargePointService object when given a message containing a request sent to
 * a charge point.
 */
object ChargePointDispatcherV15 extends AbstractDispatcher[ChargePoint] {
  def version: Version.Value = Version.V15

  val actions = ChargePointAction
  import actions._

  def dispatch(action: Value, xml: NodeSeq, service: => ChargePoint): Body = {
    import ConvertersV15._
    import v15._

    def remoteStartStopStatus(accepted: Boolean) = if (accepted) AcceptedValue2 else RejectedValue2

    def ?[REQ: XMLFormat, RES: XMLFormat](f: REQ => RES): Body = reqRes(action, xml)(f)

    action match {
      case CancelReservation => ?[CancelReservationRequest, CancelReservationResponse] {
        req =>
          val CancelReservationRes(accepted) = service(CancelReservationReq(req.reservationId))
          CancelReservationResponse(if (accepted) AcceptedValue9 else RejectedValue8)
      }

      case ChangeAvailability => ?[ChangeAvailabilityRequest, ChangeAvailabilityResponse] {
        req =>
          val availabilityType = req.typeValue match {
            case Inoperative => chargepoint.AvailabilityType.Inoperative
            case Operative => chargepoint.AvailabilityType.Operative
          }

          val ChangeAvailabilityRes(result) = service(ChangeAvailabilityReq(
            Scope.fromOcpp(req.connectorId),
            availabilityType))

          ChangeAvailabilityResponse(result match {
            case chargepoint.AvailabilityStatus.Accepted => AcceptedValue7
            case chargepoint.AvailabilityStatus.Rejected => RejectedValue6
            case chargepoint.AvailabilityStatus.Scheduled => Scheduled
          })
      }

      case ChangeConfiguration => ?[ChangeConfigurationRequest, ChangeConfigurationResponse] {
        req =>
          val ChangeConfigurationRes(result) = service(ChangeConfigurationReq(req.key, req.value))
          ChangeConfigurationResponse(ConfigurationStatus.fromString(result.toString))
      }

      case ClearCache => ?[ClearCacheRequest, ClearCacheResponse] {
        req =>
          val ClearCacheRes(accepted) = service(ClearCacheReq)
          ClearCacheResponse(if (accepted) AcceptedValue3 else RejectedValue3)
      }

      case GetConfiguration => ?[GetConfigurationRequest, GetConfigurationResponse] {
        req =>
          def keyValue(kv: chargepoint.KeyValue) = v15.KeyValue(kv.key, kv.readonly, kv.value)
          val GetConfigurationRes(values, unknownKeys) = service(GetConfigurationReq(req.key.toList))
          GetConfigurationResponse(values.map(keyValue), unknownKeys)
      }

      case GetDiagnostics => ?[GetDiagnosticsRequest, GetDiagnosticsResponse] {
        req =>
          val retrySettings = Retries.fromInts(req.retries, req.retryInterval)
          val GetDiagnosticsRes(fileName) = service(GetDiagnosticsReq(
            req.location,
            req.startTime.map(_.toDateTime),
            req.stopTime.map(_.toDateTime),
            retrySettings))
          GetDiagnosticsResponse(fileName)
      }

      case GetLocalListVersion => ?[GetLocalListVersionRequest, GetLocalListVersionResponse] {
        req =>
          def versionToInt(v: AuthListVersion): Int = v match {
            case AuthListNotSupported => -1
            case AuthListSupported(i) => i
          }
          val GetLocalListVersionRes(version) = service(GetLocalListVersionReq)
          GetLocalListVersionResponse(versionToInt(version))
      }

      case RemoteStartTransaction => ?[RemoteStartTransactionRequest, RemoteStartTransactionResponse] {
        req =>
          val connectorScope = req.connectorId.map(ConnectorScope.fromOcpp)
          val RemoteStartTransactionRes(accepted) = service(RemoteStartTransactionReq(req.idTag, connectorScope))
          RemoteStartTransactionResponse(remoteStartStopStatus(accepted))
      }

      case RemoteStopTransaction => ?[RemoteStopTransactionRequest, RemoteStopTransactionResponse] {
        req =>
          val RemoteStopTransactionRes(accepted) = service(RemoteStopTransactionReq(req.transactionId))
          RemoteStopTransactionResponse(remoteStartStopStatus(accepted))
      }

      case ReserveNow => ?[ReserveNowRequest, ReserveNowResponse] {
        req =>
          def genericStatusToV15Status(s: Reservation.Value) = ReservationStatus.fromString(s.toString)

          val ReserveNowRes(status) = service(ReserveNowReq(ConnectorScope.fromOcpp(req.connectorId),
            req.expiryDate.toDateTime,
            req.idTag, req.parentIdTag, req.reservationId))
          ReserveNowResponse(genericStatusToV15Status(status))
      }

      case Reset => ?[ResetRequest, ResetResponse] {
        req =>
          val resetType = req.typeValue match {
            case Hard => chargepoint.ResetType.Hard
            case Soft => chargepoint.ResetType.Soft
          }
          val ResetRes(accepted) = service(ResetReq(resetType))
          ResetResponse(if (accepted) AcceptedValue6 else RejectedValue5)
      }

      case SendLocalList => ?[SendLocalListRequest, SendLocalListResponse] {
        req =>

          val updateType = req.updateType match {
            case Differential => chargepoint.UpdateType.Differential
            case Full => chargepoint.UpdateType.Full
          }
          val listVersion = AuthListSupported(req.listVersion)
          val localAuthList = req.localAuthorisationList.map(_.toOcpp).toList

          val SendLocalListRes(result) = service(SendLocalListReq(updateType, listVersion, localAuthList, req.hash))
          val v15StatusAndHash = result.toV15
          SendLocalListResponse(v15StatusAndHash._1, v15StatusAndHash._2)
      }

      case UnlockConnector => ?[UnlockConnectorRequest, UnlockConnectorResponse] {
        req =>
          val connectorScope = ConnectorScope.fromOcpp(req.connectorId)
          val UnlockConnectorRes(accepted) = service(UnlockConnectorReq(connectorScope))
          UnlockConnectorResponse(if (accepted) AcceptedValue4 else RejectedValue4)
      }

      case UpdateFirmware => ?[UpdateFirmwareRequest, UpdateFirmwareResponse] {
        req =>
          val retrieveDate = req.retrieveDate.toDateTime
          val retrySettings = Retries.fromInts(req.retries, req.retryInterval)
          service(UpdateFirmwareReq(retrieveDate, req.location, retrySettings))
          UpdateFirmwareResponse()
      }
    }
  }
}
