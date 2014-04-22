package com.thenewmotion.ocpp
package soap

import scala.xml.NodeSeq
import soapenvelope12.Body
import scalaxb.XMLFormat
import scala.concurrent.{ExecutionContext, Future}
import messages._

object ChargePointDispatcher {
  def apply(version: Version.Value): Dispatcher[ChargePointReq, ChargePointRes] = version match {
    case Version.V12 => sys.error("Requests to the charge point are not yet supported with OCPP 1.2")
    case Version.V15 => ChargePointDispatcherV15
  }
}

/**
 * Can call the corresponding methods on a ChargePointService object when given a message containing a request sent to
 * a charge point.
 */
object ChargePointDispatcherV15 extends AbstractDispatcher[ChargePointReq, ChargePointRes] {
  import v15.{ChargePointService => _, Value => _, _}
  import ConvertersV15._

  def version: Version.Value = Version.V15
  val actions = ChargePointAction
  import actions._

  def dispatch(action: Value, xml: NodeSeq, service: ChargePointReq => Future[ChargePointRes])
              (implicit ec: ExecutionContext): Future[Body] = {

    def remoteStartStopStatus(accepted: Boolean) = if (accepted) AcceptedValue2 else RejectedValue2

    def ?[XMLREQ: XMLFormat, XMLRES: XMLFormat](reqTrans: XMLREQ => ChargePointReq)(resTrans: ChargePointRes => XMLRES): Future[Body] =
      reqRes(action, xml, service)(reqTrans)(resTrans)

    action match {
      case CancelReservation => ?[CancelReservationRequest, CancelReservationResponse] { req =>
          CancelReservationReq(req.reservationId)
      } {
        case CancelReservationRes(accepted)  =>
          CancelReservationResponse(if (accepted) AcceptedValue9 else RejectedValue8)
      }

      case ChangeAvailability => ?[ChangeAvailabilityRequest, ChangeAvailabilityResponse] { req =>
          val availabilityType = req.typeValue match {
            case Inoperative => messages.AvailabilityType.Inoperative
            case Operative => messages.AvailabilityType.Operative
          }

          ChangeAvailabilityReq(Scope.fromOcpp(req.connectorId), availabilityType)
      } {
        case ChangeAvailabilityRes(result) =>

          ChangeAvailabilityResponse(result match {
            case messages.AvailabilityStatus.Accepted => AcceptedValue7
            case messages.AvailabilityStatus.Rejected => RejectedValue6
            case messages.AvailabilityStatus.Scheduled => Scheduled
          })
      }

      case ChangeConfiguration => ?[ChangeConfigurationRequest, ChangeConfigurationResponse] { req =>
        ChangeConfigurationReq(req.key, req.value)
      } {
        case ChangeConfigurationRes(result) =>
          ChangeConfigurationResponse(result match {
            case messages.ConfigurationStatus.Accepted => AcceptedValue8
            case messages.ConfigurationStatus.Rejected => RejectedValue7
            case messages.ConfigurationStatus.NotSupported => NotSupported
          })
      }

      case ClearCache => ?[ClearCacheRequest, ClearCacheResponse] {
        _ => ClearCacheReq
      } {
        case ClearCacheRes(accepted) =>
          ClearCacheResponse(if (accepted) AcceptedValue3 else RejectedValue3)
      }

      case GetConfiguration => ?[GetConfigurationRequest, GetConfigurationResponse] { req =>
          GetConfigurationReq(req.key.toList)
      } {
        case GetConfigurationRes(values, unknownKeys) =>
          def keyValue(kv: messages.KeyValue) = v15.KeyValue(kv.key, kv.readonly, kv.value)

          GetConfigurationResponse(values.map(keyValue), unknownKeys)
      }

      case GetDiagnostics => ?[GetDiagnosticsRequest, GetDiagnosticsResponse] { req =>
        val retrySettings = Retries.fromInts(req.retries, req.retryInterval)
        GetDiagnosticsReq(
          req.location,
          req.startTime.map(_.toDateTime),
          req.stopTime.map(_.toDateTime),
          retrySettings)
      } { case GetDiagnosticsRes(fileName) => GetDiagnosticsResponse(fileName) }

      case GetLocalListVersion => ?[GetLocalListVersionRequest, GetLocalListVersionResponse] { _ =>
        GetLocalListVersionReq
      } {
        case GetLocalListVersionRes(version) =>
          def versionToInt(v: AuthListVersion): Int = v match {
            case AuthListNotSupported => -1
            case AuthListSupported(i) => i
          }

          GetLocalListVersionResponse(versionToInt(version))
      }

      case RemoteStartTransaction => ?[RemoteStartTransactionRequest, RemoteStartTransactionResponse] { req =>
        val connectorScope = req.connectorId.map(ConnectorScope.fromOcpp)
        RemoteStartTransactionReq(req.idTag, connectorScope)
      } {
        case RemoteStartTransactionRes(accepted) =>
          RemoteStartTransactionResponse(remoteStartStopStatus(accepted))
      }

      case RemoteStopTransaction => ?[RemoteStopTransactionRequest, RemoteStopTransactionResponse] { req =>
           RemoteStopTransactionReq(req.transactionId)
      } {
        case RemoteStopTransactionRes(accepted) =>
          RemoteStopTransactionResponse(remoteStartStopStatus(accepted))
      }

      case ReserveNow => ?[ReserveNowRequest, ReserveNowResponse] { req =>
        ReserveNowReq(ConnectorScope.fromOcpp(req.connectorId),
          req.expiryDate.toDateTime,
          req.idTag, req.parentIdTag, req.reservationId)
      } {
        case ReserveNowRes(status) =>
          def genericStatusToV15Status(x: Reservation.Value) = x match {
            case Reservation.Accepted => Accepted
            case Reservation.Faulted => Faulted
            case Reservation.Occupied => Occupied
            case Reservation.Rejected => Rejected
            case Reservation.Unavailable => Unavailable
          }

          ReserveNowResponse(genericStatusToV15Status(status))
      }

      case Reset => ?[ResetRequest, ResetResponse] { req =>
        val resetType = req.typeValue match {
          case Hard => messages.ResetType.Hard
          case Soft => messages.ResetType.Soft
        }
        ResetReq(resetType)
      } {
        case ResetRes(accepted) =>
          ResetResponse(if (accepted) AcceptedValue6 else RejectedValue5)
      }

      case SendLocalList => ?[SendLocalListRequest, SendLocalListResponse] { req =>
        val updateType = req.updateType match {
          case Differential => messages.UpdateType.Differential
          case Full => messages.UpdateType.Full
        }
        val listVersion = AuthListSupported(req.listVersion)
        val localAuthList = req.localAuthorisationList.map(_.toOcpp).toList

        SendLocalListReq(updateType, listVersion, localAuthList, req.hash)
      } {
        case SendLocalListRes(result) =>
          val v15StatusAndHash = result.toV15
          SendLocalListResponse(v15StatusAndHash._1, v15StatusAndHash._2)
      }

      case UnlockConnector => ?[UnlockConnectorRequest, UnlockConnectorResponse] { req =>
        val connectorScope = ConnectorScope.fromOcpp(req.connectorId)
        UnlockConnectorReq(connectorScope)
      } {
        case UnlockConnectorRes(accepted) =>
          UnlockConnectorResponse(if (accepted) AcceptedValue4 else RejectedValue4)
      }

      case UpdateFirmware => ?[UpdateFirmwareRequest, UpdateFirmwareResponse] { req =>
          val retrieveDate = req.retrieveDate.toDateTime
          val retrySettings = Retries.fromInts(req.retries, req.retryInterval)
          UpdateFirmwareReq(retrieveDate, req.location, retrySettings)
      } (_ => UpdateFirmwareResponse() )
    }
  }
}
