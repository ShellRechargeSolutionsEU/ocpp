package com.thenewmotion
package ocpp

import scala.concurrent.duration._
import scala.xml.NodeSeq
import soapenvelope12.Body
import scalaxb.XMLFormat

object ChargePointDispatcher {
  def apply(version: Version.Value, log: LogFunc): Dispatcher[ChargePointService] = version match {
    case Version.V12 => sys.error("Requests to the charge point are not yet supported with OCPP 1.2")
    case Version.V15 => new ChargePointDispatcherV15(log)
  }
}

/**
 * Can call the corresponding methods on a ChargePointService object when given a message containing a request sent to
 * a charge point.
 */
class ChargePointDispatcherV15(log: LogFunc = (_ => ())) extends AbstractDispatcher[ChargePointService](log) {
  def version: Version.Value = Version.V15

  val actions = ChargePointAction
  import actions._

  def dispatch(action: Value, xml: NodeSeq, service: => ChargePointService): Body = {
    import ConvertersV15._
    import v15._

    def booleanToAcceptedString(b: Boolean) = if (b) "Accepted" else "Rejected"
    def booleanToRemoteStartStopStatus(b: Boolean) = RemoteStartStopStatus.fromString(booleanToAcceptedString(b))

    def ?[REQ: XMLFormat, RES: XMLFormat](f: REQ => RES): Body = reqRes(action, xml)(f)

    action match {

      case CancelReservation => ?[CancelReservationRequest, CancelReservationResponse] {
        req =>
          def booleanToCancelReservationStatus(s: Boolean) =
            CancelReservationStatus.fromString(booleanToAcceptedString(s))

          CancelReservationResponse(booleanToCancelReservationStatus(service.cancelReservation(req.reservationId)))
      }

      case ChangeAvailability => ?[ChangeAvailabilityRequest, ChangeAvailabilityResponse] {
        req =>
          val result = service.changeAvailability(Scope.fromOcpp(req.connectorId),
                                                  ocpp.AvailabilityType.withName(req.typeValue.toString))
          ChangeAvailabilityResponse(AvailabilityStatus.fromString(result.toString))
      }

      case ChangeConfiguration => ?[ChangeConfigurationRequest, ChangeConfigurationResponse] {
        req =>
          val result = service.changeConfiguration(req.key, req.value)
          ChangeConfigurationResponse(ConfigurationStatus.fromString(result.toString))
      }

      case ClearCache => ?[ClearCacheRequest, ClearCacheResponse] {
        req =>
          def booleanToClearCacheStatus(b: Boolean) = ClearCacheStatus.fromString(booleanToAcceptedString(b))

          ClearCacheResponse(booleanToClearCacheStatus(service.clearCache))
      }

      case GetConfiguration => ?[GetConfigurationRequest, GetConfigurationResponse] {
        req =>
          def genericKVToV15KV(kv: ocpp.KeyValue) = v15.KeyValue(kv.key, kv.readonly, kv.value)

          val result = service.getConfiguration(req.key.toList)
          GetConfigurationResponse(result.values.map(genericKVToV15KV), result.unknownKeys)
      }

      case GetDiagnostics => ?[GetDiagnosticsRequest, GetDiagnosticsResponse] {
        req =>
          val retrySettings = Retries.fromInts(req.retries, req.retryInterval)
          GetDiagnosticsResponse(
            service.getDiagnostics(req.location,
                                   req.startTime.map(_.toDateTime),
                                   req.stopTime.map(_.toDateTime),
                                   retrySettings)
          )
      }

      case GetLocalListVersion => ?[GetLocalListVersionRequest, GetLocalListVersionResponse] {
        req =>
          def versionToInt(v: AuthListVersion): Int = v match {
            case AuthListNotSupported => -1
            case AuthListSupported(i) => i
          }
          GetLocalListVersionResponse(versionToInt(service.getLocalListVersion))
      }

      case RemoteStartTransaction => ?[RemoteStartTransactionRequest, RemoteStartTransactionResponse] {
        req =>
          val connectorScope = req.connectorId.map(ConnectorScope.fromOcpp(_))

          RemoteStartTransactionResponse(
            booleanToRemoteStartStopStatus(service.remoteStartTransaction(req.idTag, connectorScope)))
      }

      case RemoteStopTransaction => ?[RemoteStopTransactionRequest, RemoteStopTransactionResponse] {
        req =>
          RemoteStopTransactionResponse(
            booleanToRemoteStartStopStatus(service.remoteStopTransaction(req.transactionId)))
      }

      case ReserveNow => ?[ReserveNowRequest, ReserveNowResponse] {
        req =>
          def genericStatusToV15Status(s: Reservation.Value) = ReservationStatus.fromString(s.toString)

          val status = service.reserveNow(ConnectorScope.fromOcpp(req.connectorId),
                                          req.expiryDate.toDateTime,
                                          req.idTag, req.parentIdTag, req.reservationId)

          ReserveNowResponse(genericStatusToV15Status(status))
      }

      case Reset => ?[ResetRequest, ResetResponse] {
        req =>
          def v15ResetTypeToGenericResetType(resetType: ResetType) =
            com.thenewmotion.ocpp.ResetType.withName(resetType.toString)

          def booleanToResetStatus(b: Boolean) = ResetStatus.fromString(booleanToAcceptedString(b))

          val result = service.reset(v15ResetTypeToGenericResetType(req.typeValue))
          ResetResponse(booleanToResetStatus(result))
      }

      case SendLocalList => ?[SendLocalListRequest, SendLocalListResponse] {
        req =>
          val updateType = ocpp.UpdateType.withName(req.updateType.toString)
          val listVersion = AuthListSupported(req.listVersion)
          val localAuthList = req.localAuthorisationList.map(_.toOcpp).toList

          val result = service.sendLocalList(updateType,
                                             listVersion,
                                             localAuthList,
                                             req.hash)
          val v15StatusAndHash = result.toV15
          SendLocalListResponse(v15StatusAndHash._1, v15StatusAndHash._2)
      }

      case UnlockConnector => ?[UnlockConnectorRequest, UnlockConnectorResponse] {
        req =>
          val connectorScope = ConnectorScope.fromOcpp(req.connectorId)

          val result = service.unlockConnector(connectorScope)
          UnlockConnectorResponse(UnlockStatus.fromString(booleanToAcceptedString(result)))
      }

      case UpdateFirmware => ?[UpdateFirmwareRequest, UpdateFirmwareResponse] {
        req =>
          val retrieveDate = req.retrieveDate.toDateTime
          val retrySettings = Retries.fromInts(req.retries, req.retryInterval)

          service.updateFirmware(retrieveDate, req.location, retrySettings)
          UpdateFirmwareResponse()
      }
    }
  }
}
