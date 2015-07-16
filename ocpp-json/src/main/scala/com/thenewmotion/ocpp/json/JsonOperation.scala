package com.thenewmotion.ocpp.json

import scala.language.existentials

import scala.util.{Failure, Success, Try}
import scala.reflect._
import org.json4s._
import com.thenewmotion.ocpp.messages._
import com.thenewmotion.ocpp.json.JsonDeserializable._

class JsonOperation[REQ <: Req, RES <: Res](val action: Enumeration#Value)
                                           (implicit reqRes: ReqRes[REQ, RES],
                                            reqDeserializable: JsonDeserializable[REQ],
                                            reqTag: ClassTag[REQ],
                                            resDeserializable: JsonDeserializable[RES]) {
  def deserializeReq(jval: JValue): REQ = jsonDeserializable[REQ].deserializeV15(jval)

  def deserializeRes(jval: JValue): RES = jsonDeserializable[RES].deserializeV15(jval)

  private val reqClass = classTag[REQ].runtimeClass

  def matchesRequest(req: Req): Boolean = reqClass.isInstance(req)
}

trait JsonOperations[REQ <: Req, RES <: Res] {
  sealed trait LookupResult
  case object NotImplemented extends LookupResult
  case object Unsupported extends LookupResult
  case class Supported(op: JsonOperation[_ <: REQ, _ <: RES]) extends LookupResult

  def enum: Enumeration

  def operations: Traversable[JsonOperation[_ <: REQ, _ <: RES]]

  def jsonOpForAction(action: Enumeration#Value): Option[JsonOperation[_ <: REQ, _ <: RES]] =
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
   * @tparam Req The request type for this OCPP operation
   * @tparam Res The response type for this OCPP operation
   *
   * @return The JsonOperation instance for the OCPP operation of the given ReqRes object
   * @throws NoSuchElementException If the OCPP operation for the given ReqRes is not supported with OCPP-JSON
   */
  def jsonOpForReqRes[Req <: REQ, Res <: RES](reqRes: ReqRes[Req, Res]): JsonOperation[Req, Res]
}

object CentralSystemOperations extends JsonOperations[CentralSystemReq, CentralSystemRes] {

  import CentralSystemAction._

  val enum = CentralSystemAction

  val authorizeJsonOp = new JsonOperation[AuthorizeReq, AuthorizeRes](Authorize)
  val bootNotificationJsonOp = new JsonOperation[BootNotificationReq, BootNotificationRes](BootNotification)
  val diagnosticsStatusNotificationJsonOp = new JsonOperation[DiagnosticsStatusNotificationReq, DiagnosticsStatusNotificationRes.type](DiagnosticsStatusNotification)
  val firmwareStatusNotificationJsonOp = new JsonOperation[FirmwareStatusNotificationReq, FirmwareStatusNotificationRes.type](FirmwareStatusNotification)
  val heartbeatJsonOp = new JsonOperation[HeartbeatReq.type, HeartbeatRes](Heartbeat)
  val meterValuesJsonOp = new JsonOperation[MeterValuesReq, MeterValuesRes.type](MeterValues)
  val startTransactionJsonOp = new JsonOperation[StartTransactionReq, StartTransactionRes](StartTransaction)
  val statusNotificationJsonOp = new JsonOperation[StatusNotificationReq, StatusNotificationRes.type](StatusNotification)
  val stopTransactionJsonOp = new JsonOperation[StopTransactionReq, StopTransactionRes](StopTransaction)

  val operations: Traversable[JsonOperation[_ <: CentralSystemReq, _ <: CentralSystemRes]] = List(
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

  def jsonOpForReqRes[Req <: CentralSystemReq, Res <: CentralSystemRes](reqRes: ReqRes[Req, Res]): JsonOperation[Req, Res] = {
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

object ChargePointOperations extends JsonOperations[ChargePointReq, ChargePointRes] {
  import ChargePointAction._

  val enum = ChargePointAction

  val cancelReservationJsonOp = new JsonOperation[CancelReservationReq, CancelReservationRes](CancelReservation)
  val changeAvailabilityJsonOp = new JsonOperation[ChangeAvailabilityReq, ChangeAvailabilityRes](ChangeAvailability)
  val changeConfigurationJsonOp = new JsonOperation[ChangeConfigurationReq, ChangeConfigurationRes](ChangeConfiguration)
  val clearCacheJsonOp = new JsonOperation[ClearCacheReq.type, ClearCacheRes](ClearCache)
  val getConfigurationJsonOp = new JsonOperation[GetConfigurationReq, GetConfigurationRes](GetConfiguration)
  val getDiagnosticsJsonOp = new JsonOperation[GetDiagnosticsReq, GetDiagnosticsRes](GetDiagnostics)
  val getLocalListVersionJsonOp = new JsonOperation[GetLocalListVersionReq.type, GetLocalListVersionRes](GetLocalListVersion)
  val remoteStartTransactionJsonOp = new JsonOperation[RemoteStartTransactionReq, RemoteStartTransactionRes](RemoteStartTransaction)
  val remoteStopTransactionJsonOp = new JsonOperation[RemoteStopTransactionReq, RemoteStopTransactionRes](RemoteStopTransaction)
  val reserveNowJsonOp = new JsonOperation[ReserveNowReq, ReserveNowRes](ReserveNow)
  val resetJsonOp = new JsonOperation[ResetReq, ResetRes](Reset)
  val sendLocalListJsonOp = new JsonOperation[SendLocalListReq, SendLocalListRes](SendLocalList)
  val unlockConnectorJsonOp = new JsonOperation[UnlockConnectorReq, UnlockConnectorRes](UnlockConnector)
  val updateFirmwareJsonOp = new JsonOperation[UpdateFirmwareReq, UpdateFirmwareRes.type](UpdateFirmware)

  val operations: Traversable[JsonOperation[_ <: ChargePointReq, _ <: ChargePointRes]] = List(
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
    updateFirmwareJsonOp)

  def jsonOpForReqRes[Req <: ChargePointReq, Res <: ChargePointRes](reqRes: ReqRes[Req, Res]): JsonOperation[Req, Res] = {
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




