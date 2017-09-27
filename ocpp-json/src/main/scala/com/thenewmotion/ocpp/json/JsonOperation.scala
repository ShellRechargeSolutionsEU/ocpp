package com.thenewmotion.ocpp
package json

import scala.language.existentials
import scala.util.{Success, Failure, Try}
import org.json4s._
import messages._

import scala.concurrent.{Future, ExecutionContext}

/**
 * A grouping of all the things you need to process JSON-encoded OCPP messages for a certain operation.
 *
 * So there are separate JsonOperation instances for operations like BootNotification, StartTransaction etc.
 *
 * This is done to have a convenient object to store for the responses you are awaiting, where you have to remember the
 * message type that you expect to get in return.
 *
 * @param action
 * @param reqRes
 * @param reqSerializer
 * @param resSerializer
 * @tparam REQ
 * @tparam RES
 * @tparam V
 */
class JsonOperation[REQ <: Req, RES <: Res, V <: Version](
  val action: Enumeration#Value
)
(
  implicit val reqRes: ReqRes[REQ, RES],
  reqSerializer: OcppMessageSerializer[REQ, V],
  resSerializer: OcppMessageSerializer[RES, V]
) {
  def serializeReq(req: REQ): JValue = reqSerializer.serialize(req)
  def deserializeReq(jval: JValue): REQ = reqSerializer.deserialize(jval)

  def serializeRes(res: RES): JValue = resSerializer.serialize(res)
  def deserializeRes(jval: JValue): RES = resSerializer.deserialize(jval)

  def reqRes(reqJson: JValue)(f: (REQ, ReqRes[REQ, RES]) => Future[RES])(implicit ec: ExecutionContext): Future[JValue] =
    f(deserializeReq(reqJson), reqRes).map(serializeRes)
}

trait JsonOperations[REQ <: Req, RES <: Res, V <: Version] {
  sealed trait LookupResult
  case object NotImplemented extends LookupResult
  case object Unsupported extends LookupResult
  case class Supported(op: JsonOperation[_ <: REQ, _ <: RES, V]) extends LookupResult

  def enum: Enumeration

  def operations: Traversable[JsonOperation[_ <: REQ, _ <: RES, V]]

  def jsonOpForAction(action: Enumeration#Value): Option[JsonOperation[_ <: REQ, _ <: RES, V]] =
    operations.find(_.action == action)

  def jsonOpForActionName(operationName: String): LookupResult = {
    Try(enum.withName(operationName)) match {
      case Success(action) => jsonOpForAction(action) match {
        case None => Unsupported
        case Some(jsonAction) => Supported(jsonAction)
      }
      case Failure(_)         => NotImplemented
    }
  }

  /**
   * @param reqRes A ReqRes object for a certain OCPP operation
   *
   * @tparam Q The request type for this OCPP operation
   * @tparam S The response type for this OCPP operation
   *
   * @return The JsonOperation instance for the OCPP operation of the given ReqRes object
   * @throws NoSuchElementException If the OCPP operation for the given ReqRes is not supported with OCPP-JSON
   */
  def jsonOpForReqRes[Q <: REQ, S <: RES](reqRes: ReqRes[Q, S]): JsonOperation[Q, S, V]
}

object JsonOperations {

  // TODO try to  make these <Party><Version>-specific definitions shorter
  implicit object CentralSystemV15 extends JsonOperations[CentralSystemReq, CentralSystemRes, Version.V15.type] {

    import CentralSystemAction._
    import v15.SerializationV15._

    val enum = CentralSystemAction

    val authorizeJsonOp = new JsonOperation[AuthorizeReq, AuthorizeRes, Version.V15.type](Authorize)
    val bootNotificationJsonOp = new JsonOperation[BootNotificationReq, BootNotificationRes, Version.V15.type](BootNotification)
    val diagnosticsStatusNotificationJsonOp = new JsonOperation[DiagnosticsStatusNotificationReq, DiagnosticsStatusNotificationRes.type, Version.V15.type](DiagnosticsStatusNotification)
    val firmwareStatusNotificationJsonOp = new JsonOperation[FirmwareStatusNotificationReq, FirmwareStatusNotificationRes.type, Version.V15.type](FirmwareStatusNotification)
    val heartbeatJsonOp = new JsonOperation[HeartbeatReq.type, HeartbeatRes, Version.V15.type](Heartbeat)
    val meterValuesJsonOp = new JsonOperation[MeterValuesReq, MeterValuesRes.type, Version.V15.type](MeterValues)
    val startTransactionJsonOp = new JsonOperation[StartTransactionReq, StartTransactionRes, Version.V15.type](StartTransaction)
    val statusNotificationJsonOp = new JsonOperation[StatusNotificationReq, StatusNotificationRes.type, Version.V15.type](StatusNotification)
    val stopTransactionJsonOp = new JsonOperation[StopTransactionReq, StopTransactionRes, Version.V15.type](StopTransaction)

    val operations: Traversable[JsonOperation[_ <: CentralSystemReq, _ <: CentralSystemRes, Version.V15.type]] = List(
      authorizeJsonOp,
      bootNotificationJsonOp,
      diagnosticsStatusNotificationJsonOp,
      firmwareStatusNotificationJsonOp,
      heartbeatJsonOp,
      meterValuesJsonOp,
      startTransactionJsonOp,
      statusNotificationJsonOp,
      stopTransactionJsonOp
    )

    def jsonOpForReqRes[Q <: CentralSystemReq, S <: CentralSystemRes](reqRes: ReqRes[Q, S]): JsonOperation[Q, S, Version.V15.type] = {
      import ReqRes._
      reqRes match {
        case AuthorizeReqRes => authorizeJsonOp
        case BootNotificationReqRes => bootNotificationJsonOp
        case FirmwareStatusNotificationReqRes => firmwareStatusNotificationJsonOp
        case HeartbeatReqRes => heartbeatJsonOp
        case MeterValuesReqRes => meterValuesJsonOp
        case StartTransactionReqRes => startTransactionJsonOp
        case StatusNotificationReqRes => statusNotificationJsonOp
        case StopTransactionReqRes => stopTransactionJsonOp
        case _ => throw new NoSuchElementException(s"Not a central system ReqRes: $reqRes")
      }
    }
  }

  implicit object CentralSystemV16 extends JsonOperations[CentralSystemReq, CentralSystemRes, Version.V16.type] {

    import CentralSystemAction._
    import v16.SerializationV16._

    val enum = CentralSystemAction

    val authorizeJsonOp = new JsonOperation[AuthorizeReq, AuthorizeRes, Version.V16.type](Authorize)
    val bootNotificationJsonOp = new JsonOperation[BootNotificationReq, BootNotificationRes, Version.V16.type](BootNotification)
    val diagnosticsStatusNotificationJsonOp = new JsonOperation[DiagnosticsStatusNotificationReq, DiagnosticsStatusNotificationRes.type, Version.V16.type](DiagnosticsStatusNotification)
    val firmwareStatusNotificationJsonOp = new JsonOperation[FirmwareStatusNotificationReq, FirmwareStatusNotificationRes.type, Version.V16.type](FirmwareStatusNotification)
    val heartbeatJsonOp = new JsonOperation[HeartbeatReq.type, HeartbeatRes, Version.V16.type](Heartbeat)
    val meterValuesJsonOp = new JsonOperation[MeterValuesReq, MeterValuesRes.type, Version.V16.type](MeterValues)
    val startTransactionJsonOp = new JsonOperation[StartTransactionReq, StartTransactionRes, Version.V16.type](StartTransaction)
    val statusNotificationJsonOp = new JsonOperation[StatusNotificationReq, StatusNotificationRes.type, Version.V16.type](StatusNotification)
    val stopTransactionJsonOp = new JsonOperation[StopTransactionReq, StopTransactionRes, Version.V16.type](StopTransaction)

    val operations: Traversable[JsonOperation[_ <: CentralSystemReq, _ <: CentralSystemRes, Version.V16.type]] = List(
      authorizeJsonOp,
      bootNotificationJsonOp,
      diagnosticsStatusNotificationJsonOp,
      firmwareStatusNotificationJsonOp,
      heartbeatJsonOp,
      meterValuesJsonOp,
      startTransactionJsonOp,
      statusNotificationJsonOp,
      stopTransactionJsonOp
    )

    def jsonOpForReqRes[Q <: CentralSystemReq, S <: CentralSystemRes](reqRes: ReqRes[Q, S]): JsonOperation[Q, S, Version.V16.type] = {
      import ReqRes._
      reqRes match {
        case AuthorizeReqRes => authorizeJsonOp
        case BootNotificationReqRes => bootNotificationJsonOp
        case FirmwareStatusNotificationReqRes => firmwareStatusNotificationJsonOp
        case HeartbeatReqRes => heartbeatJsonOp
        case MeterValuesReqRes => meterValuesJsonOp
        case StartTransactionReqRes => startTransactionJsonOp
        case StatusNotificationReqRes => statusNotificationJsonOp
        case StopTransactionReqRes => stopTransactionJsonOp
        case _ => throw new NoSuchElementException(s"Not a central system ReqRes: $reqRes")
      }
    }
  }

  implicit object ChargePointV15 extends JsonOperations[ChargePointReq, ChargePointRes, Version.V15.type] {

    import ChargePointAction._
    import v15.SerializationV15._

    val enum = ChargePointAction

    val cancelReservationJsonOp = new JsonOperation[CancelReservationReq, CancelReservationRes, Version.V15.type](CancelReservation)
    val changeAvailabilityJsonOp = new JsonOperation[ChangeAvailabilityReq, ChangeAvailabilityRes, Version.V15.type](ChangeAvailability)
    val changeConfigurationJsonOp = new JsonOperation[ChangeConfigurationReq, ChangeConfigurationRes, Version.V15.type](ChangeConfiguration)
    val clearCacheJsonOp = new JsonOperation[ClearCacheReq.type, ClearCacheRes, Version.V15.type](ClearCache)
    val getConfigurationJsonOp = new JsonOperation[GetConfigurationReq, GetConfigurationRes, Version.V15.type](GetConfiguration)
    val getDiagnosticsJsonOp = new JsonOperation[GetDiagnosticsReq, GetDiagnosticsRes, Version.V15.type](GetDiagnostics)
    val getLocalListVersionJsonOp = new JsonOperation[GetLocalListVersionReq.type, GetLocalListVersionRes, Version.V15.type](GetLocalListVersion)
    val remoteStartTransactionJsonOp = new JsonOperation[RemoteStartTransactionReq, RemoteStartTransactionRes, Version.V15.type](RemoteStartTransaction)
    val remoteStopTransactionJsonOp = new JsonOperation[RemoteStopTransactionReq, RemoteStopTransactionRes, Version.V15.type](RemoteStopTransaction)
    val reserveNowJsonOp = new JsonOperation[ReserveNowReq, ReserveNowRes, Version.V15.type](ReserveNow)
    val resetJsonOp = new JsonOperation[ResetReq, ResetRes, Version.V15.type](Reset)
    val sendLocalListJsonOp = new JsonOperation[SendLocalListReq, SendLocalListRes, Version.V15.type](SendLocalList)
    val unlockConnectorJsonOp = new JsonOperation[UnlockConnectorReq, UnlockConnectorRes, Version.V15.type](UnlockConnector)
    val updateFirmwareJsonOp = new JsonOperation[UpdateFirmwareReq, UpdateFirmwareRes.type, Version.V15.type](UpdateFirmware)

    val operations: Traversable[JsonOperation[_ <: ChargePointReq, _ <: ChargePointRes, Version.V15.type]] = List(
      cancelReservationJsonOp,
      changeAvailabilityJsonOp,
      changeConfigurationJsonOp,
      clearCacheJsonOp,
      getConfigurationJsonOp,
      getDiagnosticsJsonOp,
      getLocalListVersionJsonOp,
      remoteStartTransactionJsonOp,
      remoteStopTransactionJsonOp,
      reserveNowJsonOp,
      resetJsonOp,
      sendLocalListJsonOp,
      unlockConnectorJsonOp,
      updateFirmwareJsonOp
    )

    def jsonOpForReqRes[Q <: ChargePointReq, S <: ChargePointRes](reqRes: ReqRes[Q, S]): JsonOperation[Q, S, Version.V15.type] = {
      import ReqRes._

      reqRes match {
        case CancelReservationReqRes => cancelReservationJsonOp
        case ChangeAvailabilityReqRes => changeAvailabilityJsonOp
        case ChangeConfigurationReqRes => changeConfigurationJsonOp
        case ClearCacheReqRes => clearCacheJsonOp
        case GetConfigurationReqRes => getConfigurationJsonOp
        case GetDiagnosticsReqRes => getDiagnosticsJsonOp
        case GetLocalListVersionReqRes => getLocalListVersionJsonOp
        case RemoteStartTransactionReqRes => remoteStartTransactionJsonOp
        case RemoteStopTransactionReqRes => remoteStopTransactionJsonOp
        case ReserveNowReqRes => reserveNowJsonOp
        case ResetReqRes => resetJsonOp
        case SendLocalListReqRes => sendLocalListJsonOp
        case UnlockConnectorReqRes => unlockConnectorJsonOp
        case UpdateFirmwareReqRes => updateFirmwareJsonOp
        case _ => throw new NoSuchElementException(s"Not a charge point ReqRes: $reqRes")
      }
    }
  }

  implicit object ChargePointV16 extends JsonOperations[ChargePointReq, ChargePointRes, Version.V16.type] {

    import ChargePointAction._
    import v16.SerializationV16._

    val enum = ChargePointAction

    val cancelReservationJsonOp = new JsonOperation[CancelReservationReq, CancelReservationRes, Version.V16.type](CancelReservation)
    val changeAvailabilityJsonOp = new JsonOperation[ChangeAvailabilityReq, ChangeAvailabilityRes, Version.V16.type](ChangeAvailability)
    val changeConfigurationJsonOp = new JsonOperation[ChangeConfigurationReq, ChangeConfigurationRes, Version.V16.type](ChangeConfiguration)
    val clearCacheJsonOp = new JsonOperation[ClearCacheReq.type, ClearCacheRes, Version.V16.type](ClearCache)
    val getConfigurationJsonOp = new JsonOperation[GetConfigurationReq, GetConfigurationRes, Version.V16.type](GetConfiguration)
    val getDiagnosticsJsonOp = new JsonOperation[GetDiagnosticsReq, GetDiagnosticsRes, Version.V16.type](GetDiagnostics)
    val getLocalListVersionJsonOp = new JsonOperation[GetLocalListVersionReq.type, GetLocalListVersionRes, Version.V16.type](GetLocalListVersion)
    val remoteStartTransactionJsonOp = new JsonOperation[RemoteStartTransactionReq, RemoteStartTransactionRes, Version.V16.type](RemoteStartTransaction)
    val remoteStopTransactionJsonOp = new JsonOperation[RemoteStopTransactionReq, RemoteStopTransactionRes, Version.V16.type](RemoteStopTransaction)
    val reserveNowJsonOp = new JsonOperation[ReserveNowReq, ReserveNowRes, Version.V16.type](ReserveNow)
    val resetJsonOp = new JsonOperation[ResetReq, ResetRes, Version.V16.type](Reset)
    val sendLocalListJsonOp = new JsonOperation[SendLocalListReq, SendLocalListRes, Version.V16.type](SendLocalList)
    val unlockConnectorJsonOp = new JsonOperation[UnlockConnectorReq, UnlockConnectorRes, Version.V16.type](UnlockConnector)
    val updateFirmwareJsonOp = new JsonOperation[UpdateFirmwareReq, UpdateFirmwareRes.type, Version.V16.type](UpdateFirmware)
    val setChargingProfileJsonOp = new JsonOperation[SetChargingProfileReq, SetChargingProfileRes, Version.V16.type](SetChargingProfile)
    val clearChargingProfileJsonOp = new JsonOperation[ClearChargingProfileReq, ClearChargingProfileRes, Version.V16.type](ClearChargingProfile)
    val getCompositeScheduleJsonOp = new JsonOperation[GetCompositeScheduleReq, GetCompositeScheduleRes, Version.V16.type](GetCompositeSchedule)
    val triggerMessageJsonOp = new JsonOperation[TriggerMessageReq, TriggerMessageRes, Version.V16.type](TriggerMessage)

    val operations: Traversable[JsonOperation[_ <: ChargePointReq, _ <: ChargePointRes, Version.V16.type]] = List(
      cancelReservationJsonOp,
      changeAvailabilityJsonOp,
      changeConfigurationJsonOp,
      clearCacheJsonOp,
      getConfigurationJsonOp,
      getDiagnosticsJsonOp,
      getLocalListVersionJsonOp,
      remoteStartTransactionJsonOp,
      remoteStopTransactionJsonOp,
      reserveNowJsonOp,
      resetJsonOp,
      sendLocalListJsonOp,
      unlockConnectorJsonOp,
      updateFirmwareJsonOp,
      setChargingProfileJsonOp,
      clearChargingProfileJsonOp,
      getCompositeScheduleJsonOp,
      triggerMessageJsonOp
    )

    def jsonOpForReqRes[Q <: ChargePointReq, S <: ChargePointRes](reqRes: ReqRes[Q, S]): JsonOperation[Q, S, Version.V16.type] = {
      import ReqRes._

      reqRes match {
        case CancelReservationReqRes => cancelReservationJsonOp
        case ChangeAvailabilityReqRes => changeAvailabilityJsonOp
        case ChangeConfigurationReqRes => changeConfigurationJsonOp
        case ClearCacheReqRes => clearCacheJsonOp
        case GetConfigurationReqRes => getConfigurationJsonOp
        case GetDiagnosticsReqRes => getDiagnosticsJsonOp
        case GetLocalListVersionReqRes => getLocalListVersionJsonOp
        case RemoteStartTransactionReqRes => remoteStartTransactionJsonOp
        case RemoteStopTransactionReqRes => remoteStopTransactionJsonOp
        case ReserveNowReqRes => reserveNowJsonOp
        case ResetReqRes => resetJsonOp
        case SendLocalListReqRes => sendLocalListJsonOp
        case UnlockConnectorReqRes => unlockConnectorJsonOp
        case UpdateFirmwareReqRes => updateFirmwareJsonOp
        case SetChargingProfileReqRes => setChargingProfileJsonOp
        case ClearChargingProfileReqRes => clearChargingProfileJsonOp
        case GetCompositeScheduleReqRes => getCompositeScheduleJsonOp
        case TriggerMessageReqRes => triggerMessageJsonOp
        case _ => throw new NoSuchElementException(s"Not a charge point ReqRes: $reqRes")
      }
    }
  }
}



