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

  protected def jsonOp[Q <: REQ, S <: RES](action: Enumeration#Value)(
    implicit reqRes: ReqRes[Q, S],
    reqSerializer: OcppMessageSerializer[Q, V],
    resSerializer: OcppMessageSerializer[S, V]
  ): JsonOperation[Q, S, V] =
    new JsonOperation[Q, S, V](action)
}

object JsonOperations {

  implicit object CentralSystemV15 extends JsonOperations[CentralSystemReq, CentralSystemRes, Version.V15.type] {

    import CentralSystemAction._
    import v15.SerializationV15._

    val enum = CentralSystemAction

    val authorizeJsonOp = jsonOp[AuthorizeReq, AuthorizeRes](Authorize)
    val bootNotificationJsonOp = jsonOp[BootNotificationReq, BootNotificationRes](BootNotification)
    val diagnosticsStatusNotificationJsonOp = jsonOp[DiagnosticsStatusNotificationReq, DiagnosticsStatusNotificationRes.type](DiagnosticsStatusNotification)
    val firmwareStatusNotificationJsonOp = jsonOp[FirmwareStatusNotificationReq, FirmwareStatusNotificationRes.type](FirmwareStatusNotification)
    val heartbeatJsonOp = jsonOp[HeartbeatReq.type, HeartbeatRes](Heartbeat)
    val meterValuesJsonOp = jsonOp[MeterValuesReq, MeterValuesRes.type](MeterValues)
    val startTransactionJsonOp = jsonOp[StartTransactionReq, StartTransactionRes](StartTransaction)
    val statusNotificationJsonOp = jsonOp[StatusNotificationReq, StatusNotificationRes.type](StatusNotification)
    val stopTransactionJsonOp = jsonOp[StopTransactionReq, StopTransactionRes](StopTransaction)

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

    val authorizeJsonOp = jsonOp[AuthorizeReq, AuthorizeRes](Authorize)
    val bootNotificationJsonOp = jsonOp[BootNotificationReq, BootNotificationRes](BootNotification)
    val diagnosticsStatusNotificationJsonOp = jsonOp[DiagnosticsStatusNotificationReq, DiagnosticsStatusNotificationRes.type](DiagnosticsStatusNotification)
    val firmwareStatusNotificationJsonOp = jsonOp[FirmwareStatusNotificationReq, FirmwareStatusNotificationRes.type](FirmwareStatusNotification)
    val heartbeatJsonOp = jsonOp[HeartbeatReq.type, HeartbeatRes](Heartbeat)
    val meterValuesJsonOp = jsonOp[MeterValuesReq, MeterValuesRes.type](MeterValues)
    val startTransactionJsonOp = jsonOp[StartTransactionReq, StartTransactionRes](StartTransaction)
    val statusNotificationJsonOp = jsonOp[StatusNotificationReq, StatusNotificationRes.type](StatusNotification)
    val stopTransactionJsonOp = jsonOp[StopTransactionReq, StopTransactionRes](StopTransaction)

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

    val cancelReservationJsonOp = jsonOp[CancelReservationReq, CancelReservationRes](CancelReservation)
    val changeAvailabilityJsonOp = jsonOp[ChangeAvailabilityReq, ChangeAvailabilityRes](ChangeAvailability)
    val changeConfigurationJsonOp = jsonOp[ChangeConfigurationReq, ChangeConfigurationRes](ChangeConfiguration)
    val clearCacheJsonOp = jsonOp[ClearCacheReq.type, ClearCacheRes](ClearCache)
    val getConfigurationJsonOp = jsonOp[GetConfigurationReq, GetConfigurationRes](GetConfiguration)
    val getDiagnosticsJsonOp = jsonOp[GetDiagnosticsReq, GetDiagnosticsRes](GetDiagnostics)
    val getLocalListVersionJsonOp = jsonOp[GetLocalListVersionReq.type, GetLocalListVersionRes](GetLocalListVersion)
    val remoteStartTransactionJsonOp = jsonOp[RemoteStartTransactionReq, RemoteStartTransactionRes](RemoteStartTransaction)
    val remoteStopTransactionJsonOp = jsonOp[RemoteStopTransactionReq, RemoteStopTransactionRes](RemoteStopTransaction)
    val reserveNowJsonOp = jsonOp[ReserveNowReq, ReserveNowRes](ReserveNow)
    val resetJsonOp = jsonOp[ResetReq, ResetRes](Reset)
    val sendLocalListJsonOp = jsonOp[SendLocalListReq, SendLocalListRes](SendLocalList)
    val unlockConnectorJsonOp = jsonOp[UnlockConnectorReq, UnlockConnectorRes](UnlockConnector)
    val updateFirmwareJsonOp = jsonOp[UpdateFirmwareReq, UpdateFirmwareRes.type](UpdateFirmware)

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

    val cancelReservationJsonOp = jsonOp[CancelReservationReq, CancelReservationRes](CancelReservation)
    val changeAvailabilityJsonOp = jsonOp[ChangeAvailabilityReq, ChangeAvailabilityRes](ChangeAvailability)
    val changeConfigurationJsonOp = jsonOp[ChangeConfigurationReq, ChangeConfigurationRes](ChangeConfiguration)
    val clearCacheJsonOp = jsonOp[ClearCacheReq.type, ClearCacheRes](ClearCache)
    val getConfigurationJsonOp = jsonOp[GetConfigurationReq, GetConfigurationRes](GetConfiguration)
    val getDiagnosticsJsonOp = jsonOp[GetDiagnosticsReq, GetDiagnosticsRes](GetDiagnostics)
    val getLocalListVersionJsonOp = jsonOp[GetLocalListVersionReq.type, GetLocalListVersionRes](GetLocalListVersion)
    val remoteStartTransactionJsonOp = jsonOp[RemoteStartTransactionReq, RemoteStartTransactionRes](RemoteStartTransaction)
    val remoteStopTransactionJsonOp = jsonOp[RemoteStopTransactionReq, RemoteStopTransactionRes](RemoteStopTransaction)
    val reserveNowJsonOp = jsonOp[ReserveNowReq, ReserveNowRes](ReserveNow)
    val resetJsonOp = jsonOp[ResetReq, ResetRes](Reset)
    val sendLocalListJsonOp = jsonOp[SendLocalListReq, SendLocalListRes](SendLocalList)
    val unlockConnectorJsonOp = jsonOp[UnlockConnectorReq, UnlockConnectorRes](UnlockConnector)
    val updateFirmwareJsonOp = jsonOp[UpdateFirmwareReq, UpdateFirmwareRes.type](UpdateFirmware)
    val setChargingProfileJsonOp = jsonOp[SetChargingProfileReq, SetChargingProfileRes](SetChargingProfile)
    val clearChargingProfileJsonOp = jsonOp[ClearChargingProfileReq, ClearChargingProfileRes](ClearChargingProfile)
    val getCompositeScheduleJsonOp = jsonOp[GetCompositeScheduleReq, GetCompositeScheduleRes](GetCompositeSchedule)
    val triggerMessageJsonOp = jsonOp[TriggerMessageReq, TriggerMessageRes](TriggerMessage)

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



