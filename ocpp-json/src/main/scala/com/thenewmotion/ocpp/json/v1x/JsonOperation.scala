package com.thenewmotion.ocpp
package json
package v1x

import messages.ReqRes
import messages.v1x._
import CentralSystemAction.{DataTransfer => CsDataTransfer, _}
import ChargePointAction._
import enums.reflection.EnumUtils.{Enumerable, Nameable}
import org.json4s._

import scala.concurrent.{ExecutionContext, Future}
import scala.language.{existentials, higherKinds}

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
class JsonOperation[
  REQBOUND <: Req,
  RESBOUND <: Res,
  REQ <: REQBOUND,
  RES <: RESBOUND,
  REQRES[_ <: REQBOUND, _ <: RESBOUND] <: ReqRes[_, _],
  V <: Version
](
  val action: Nameable
)
(
  implicit val reqRes: REQRES[REQ, RES],
  reqSerializer: OcppMessageSerializer[REQ, V],
  resSerializer: OcppMessageSerializer[RES, V]
) {
  def serializeReq(req: REQ): JValue = reqSerializer.serialize(req)
  def deserializeReq(jval: JValue): REQ = reqSerializer.deserialize(jval)

  def serializeRes(res: RES): JValue = resSerializer.serialize(res)
  def deserializeRes(jval: JValue): RES = resSerializer.deserialize(jval)

  def reqRes(reqJson: JValue)(f: (REQ, REQRES[REQ, RES]) => Future[RES])(implicit ec: ExecutionContext): Future[JValue] = {
    Future.fromTry(scala.util.Try(deserializeReq(reqJson))).flatMap { request =>
      f(request, reqRes).map(serializeRes)
    }
  }
}

/**
 * The set of possible operations available for a given side of communication
 * (either Charge Point or Central System) and version.
 *
 * There are instances with different types for each of the four possible
 * combinations of side and version.
 *
 * The side of communication is given by REQBOUND and RESBOUND: they can be
 * either ChargePointReq and ChargePointRes or CentralSystemReq and
 * CentralSystemRes. The version is given by V, which can be either
 * Version.V15.type or Version.V16.type.
 */
trait JsonOperations[
  REQBOUND <: Req,
  RESBOUND <: Res,
  REQRES[_ <: REQBOUND, _ <: RESBOUND] <: ReqRes[_, _],
  V <: Version
] {

  type MyJsonOperation[REQ <: REQBOUND, RES <: RESBOUND] =
    JsonOperation[REQBOUND, RESBOUND, REQ, RES, REQRES, V]

  sealed trait LookupResult
  case object NotImplemented extends LookupResult
  case object Unsupported extends LookupResult
  case class Supported(op: MyJsonOperation[_ <: REQBOUND, _ <: RESBOUND]) extends LookupResult

  type ActionType <: Nameable
  def enum: Enumerable[ActionType]

  def operations: Traversable[MyJsonOperation[_ <: REQBOUND, _ <: RESBOUND]]

  def jsonOpForAction(action: ActionType): Option[MyJsonOperation[_ <: REQBOUND, _ <: RESBOUND]] =
    operations.find(_.action == action)

  def jsonOpForActionName(operationName: String): LookupResult = {
    enum.withName(operationName) match {
      case Some(action) => jsonOpForAction(action) match {
        case None             => Unsupported
        case Some(jsonAction) => Supported(jsonAction)
      }
      case None         => NotImplemented
    }
  }

  /**
   * @tparam REQ The request type for the OCPP operation
   * @tparam RES The response type for the OCPP operation
   *
   * @param reqRes A ReqRes object for a certain OCPP operation
   *
   * @return The JsonOperation instance for the OCPP operation of the given ReqRes object
   * @throws NoSuchElementException If the OCPP operation for the given ReqRes is not supported with OCPP-JSON
   */
  def jsonOpForReqRes[REQ <: REQBOUND, RES <: RESBOUND](reqRes: REQRES[REQ, RES]): Option[MyJsonOperation[REQ, RES]] =
    jsonOpForReqResPF.lift(reqRes)

  protected def jsonOpForReqResPF[REQ <: REQBOUND, RES <: RESBOUND]: PartialFunction[REQRES[REQ, RES], MyJsonOperation[REQ, RES]]

  protected def jsonOp[REQ <: REQBOUND, RES <: RESBOUND](action: ActionType)(
    implicit reqRes: REQRES[REQ, RES],
    reqSerializer: OcppMessageSerializer[REQ, V],
    resSerializer: OcppMessageSerializer[RES, V]
  ): MyJsonOperation[REQ, RES] =
    new JsonOperation[REQBOUND, RESBOUND, REQ, RES, REQRES, V](action)
}

object JsonOperations {

  implicit object CentralSystemV15 extends JsonOperations[
    CentralSystemReq,
    CentralSystemRes,
    CentralSystemReqRes,
    Version.V15.type
  ] {

    type ActionType = CentralSystemAction
    val enum = CentralSystemAction

    import v15.SerializationV15._

    val authorizeJsonOp = jsonOp[AuthorizeReq, AuthorizeRes](Authorize)
    val bootNotificationJsonOp = jsonOp[BootNotificationReq, BootNotificationRes](BootNotification)
    val dataTransferJsonOp = jsonOp[CentralSystemDataTransferReq, CentralSystemDataTransferRes](CsDataTransfer)
    val diagnosticsStatusNotificationJsonOp = jsonOp[DiagnosticsStatusNotificationReq, DiagnosticsStatusNotificationRes.type](DiagnosticsStatusNotification)
    val firmwareStatusNotificationJsonOp = jsonOp[FirmwareStatusNotificationReq, FirmwareStatusNotificationRes.type](FirmwareStatusNotification)
    val heartbeatJsonOp = jsonOp[HeartbeatReq.type, HeartbeatRes](Heartbeat)
    val meterValuesJsonOp = jsonOp[MeterValuesReq, MeterValuesRes.type](MeterValues)
    val startTransactionJsonOp = jsonOp[StartTransactionReq, StartTransactionRes](StartTransaction)
    val statusNotificationJsonOp = jsonOp[StatusNotificationReq, StatusNotificationRes.type](StatusNotification)
    val stopTransactionJsonOp = jsonOp[StopTransactionReq, StopTransactionRes](StopTransaction)

    val operations: Traversable[MyJsonOperation[_ <: CentralSystemReq, _ <: CentralSystemRes]] = List(
      authorizeJsonOp,
      bootNotificationJsonOp,
      dataTransferJsonOp,
      diagnosticsStatusNotificationJsonOp,
      firmwareStatusNotificationJsonOp,
      heartbeatJsonOp,
      meterValuesJsonOp,
      startTransactionJsonOp,
      statusNotificationJsonOp,
      stopTransactionJsonOp
    )

    def jsonOpForReqResPF[REQ <: CentralSystemReq, RES <: CentralSystemRes]: PartialFunction[CentralSystemReqRes[REQ, RES], MyJsonOperation[REQ, RES]] = {
      case AuthorizeReqRes => authorizeJsonOp
      case BootNotificationReqRes => bootNotificationJsonOp
      case CentralSystemDataTransferReqRes => dataTransferJsonOp
      case DiagnosticsStatusNotificationReqRes => diagnosticsStatusNotificationJsonOp
      case FirmwareStatusNotificationReqRes => firmwareStatusNotificationJsonOp
      case HeartbeatReqRes => heartbeatJsonOp
      case MeterValuesReqRes => meterValuesJsonOp
      case StartTransactionReqRes => startTransactionJsonOp
      case StatusNotificationReqRes => statusNotificationJsonOp
      case StopTransactionReqRes => stopTransactionJsonOp
    }
  }

  implicit object CentralSystemV16 extends JsonOperations[
    CentralSystemReq,
    CentralSystemRes,
    CentralSystemReqRes,
    Version.V16.type
  ] {

    type ActionType = CentralSystemAction
    val enum = CentralSystemAction

    import v16.SerializationV16._

    val authorizeJsonOp = jsonOp[AuthorizeReq, AuthorizeRes](Authorize)
    val bootNotificationJsonOp = jsonOp[BootNotificationReq, BootNotificationRes](BootNotification)
    val diagnosticsStatusNotificationJsonOp = jsonOp[DiagnosticsStatusNotificationReq, DiagnosticsStatusNotificationRes.type](DiagnosticsStatusNotification)
    val dataTransferJsonOp = jsonOp[CentralSystemDataTransferReq, CentralSystemDataTransferRes](CsDataTransfer)
    val firmwareStatusNotificationJsonOp = jsonOp[FirmwareStatusNotificationReq, FirmwareStatusNotificationRes.type](FirmwareStatusNotification)
    val heartbeatJsonOp = jsonOp[HeartbeatReq.type, HeartbeatRes](Heartbeat)
    val meterValuesJsonOp = jsonOp[MeterValuesReq, MeterValuesRes.type](MeterValues)
    val startTransactionJsonOp = jsonOp[StartTransactionReq, StartTransactionRes](StartTransaction)
    val statusNotificationJsonOp = jsonOp[StatusNotificationReq, StatusNotificationRes.type](StatusNotification)
    val stopTransactionJsonOp = jsonOp[StopTransactionReq, StopTransactionRes](StopTransaction)

    val operations: Traversable[MyJsonOperation[_ <: CentralSystemReq, _ <: CentralSystemRes]] = List(
      authorizeJsonOp,
      bootNotificationJsonOp,
      diagnosticsStatusNotificationJsonOp,
      dataTransferJsonOp,
      firmwareStatusNotificationJsonOp,
      heartbeatJsonOp,
      meterValuesJsonOp,
      startTransactionJsonOp,
      statusNotificationJsonOp,
      stopTransactionJsonOp
    )

    def jsonOpForReqResPF[REQ <: CentralSystemReq, RES <: CentralSystemRes]:
    PartialFunction[CentralSystemReqRes[REQ, RES], MyJsonOperation[REQ, RES]] = {
      case AuthorizeReqRes => authorizeJsonOp
      case BootNotificationReqRes => bootNotificationJsonOp
      case DiagnosticsStatusNotificationReqRes => diagnosticsStatusNotificationJsonOp
      case CentralSystemDataTransferReqRes => dataTransferJsonOp
      case FirmwareStatusNotificationReqRes => firmwareStatusNotificationJsonOp
      case HeartbeatReqRes => heartbeatJsonOp
      case MeterValuesReqRes => meterValuesJsonOp
      case StartTransactionReqRes => startTransactionJsonOp
      case StatusNotificationReqRes => statusNotificationJsonOp
      case StopTransactionReqRes => stopTransactionJsonOp
    }
  }

  implicit object ChargePointV15 extends JsonOperations[
    ChargePointReq,
    ChargePointRes,
    ChargePointReqRes,
    Version.V15.type
  ] {

    type ActionType = ChargePointAction
    val enum = ChargePointAction

    import v15.SerializationV15._

    val cancelReservationJsonOp = jsonOp[CancelReservationReq, CancelReservationRes](CancelReservation)
    val changeAvailabilityJsonOp = jsonOp[ChangeAvailabilityReq, ChangeAvailabilityRes](ChangeAvailability)
    val changeConfigurationJsonOp = jsonOp[ChangeConfigurationReq, ChangeConfigurationRes](ChangeConfiguration)
    val chargePointDataTransferJsonOp = jsonOp[ChargePointDataTransferReq, ChargePointDataTransferRes](DataTransfer)
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

    val operations: Traversable[MyJsonOperation[_ <: ChargePointReq, _ <: ChargePointRes]] = List(
      cancelReservationJsonOp,
      changeAvailabilityJsonOp,
      changeConfigurationJsonOp,
      chargePointDataTransferJsonOp,
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

    def jsonOpForReqResPF[REQ <: ChargePointReq, RES <: ChargePointRes] : PartialFunction[ChargePointReqRes[REQ, RES], MyJsonOperation[REQ, RES]] = {
      case CancelReservationReqRes => cancelReservationJsonOp
      case ChangeAvailabilityReqRes => changeAvailabilityJsonOp
      case ChangeConfigurationReqRes => changeConfigurationJsonOp
      case ChargePointDataTransferReqRes => chargePointDataTransferJsonOp
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
    }
  }

  implicit object ChargePointV16 extends JsonOperations[
    ChargePointReq,
    ChargePointRes,
    ChargePointReqRes,
    Version.V16.type
  ] {

    type ActionType = ChargePointAction
    val enum = ChargePointAction

    import v16.SerializationV16._

    val cancelReservationJsonOp = jsonOp[CancelReservationReq, CancelReservationRes](CancelReservation)
    val changeAvailabilityJsonOp = jsonOp[ChangeAvailabilityReq, ChangeAvailabilityRes](ChangeAvailability)
    val changeConfigurationJsonOp = jsonOp[ChangeConfigurationReq, ChangeConfigurationRes](ChangeConfiguration)
    val clearCacheJsonOp = jsonOp[ClearCacheReq.type, ClearCacheRes](ClearCache)
    val chargePointDataTransferJsonOp = jsonOp[ChargePointDataTransferReq, ChargePointDataTransferRes](DataTransfer)
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

    val operations: Traversable[MyJsonOperation[_ <: ChargePointReq, _ <: ChargePointRes]] = List(
      cancelReservationJsonOp,
      changeAvailabilityJsonOp,
      changeConfigurationJsonOp,
      chargePointDataTransferJsonOp,
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

    def jsonOpForReqResPF[REQ <: ChargePointReq, RES <: ChargePointRes]: PartialFunction[ChargePointReqRes[REQ, RES], MyJsonOperation[REQ, RES]] = {
      case CancelReservationReqRes => cancelReservationJsonOp
      case ChangeAvailabilityReqRes => changeAvailabilityJsonOp
      case ChangeConfigurationReqRes => changeConfigurationJsonOp
      case ChargePointDataTransferReqRes => chargePointDataTransferJsonOp
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
    }
  }
}



