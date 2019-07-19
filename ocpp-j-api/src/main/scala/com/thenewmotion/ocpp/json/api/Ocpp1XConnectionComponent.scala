package com.thenewmotion.ocpp
package json
package api

import scala.language.higherKinds
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Try, Success, Failure}
import messages.ReqRes
import messages.v1x._
import json.v1x.{JsonOperation, JsonOperations}

/**
  * The OCPP layer connection component for OCPP-J  1.X, i.e. OCPP 1.5 and 1.6
  *
  * The type parameters are different depending on whether the trait is being
  * used to handle messages on the Central System or Charge Point side.
  *
  * @tparam OUTREQBOUND Supertype of outgoing requests (either ChargePointReq or CentralSystemReq)
  * @tparam INRESBOUND Supertype of incoming responses (either ChargePointRes or CentralSystemRes)
  * @tparam OUTREQRES Typeclass relating the types of incoming requests and outgoing responses
  * @tparam INREQBOUND Supertype of incoming requests (either ChargePointReq or CentralSystemReq)
  * @tparam OUTRESBOUND Supertype of outgoing responses (either ChargePointRes or CentralSystemRes)
  * @tparam INREQRES Typeclass relating the types of outgoing requests and incoming responses
  */
trait Ocpp1XConnectionComponent[
  OUTREQBOUND <: Req,
  INRESBOUND <: Res,
  OUTREQRES[_ <: OUTREQBOUND, _ <: INRESBOUND ] <: ReqRes[_, _],
  INREQBOUND <: Req,
  OUTRESBOUND <: Res,
  INREQRES [_ <: INREQBOUND,  _ <: OUTRESBOUND] <: ReqRes[_, _]
] extends BaseOcppConnectionComponent[OUTREQBOUND, INRESBOUND, OUTREQRES, INREQBOUND, OUTRESBOUND, INREQRES] {

  this: SrpcComponent =>

  protected implicit val executionContext: ExecutionContext

  trait Ocpp1XConnection extends BaseOcppConnection {
    /** The version of the OCPP protocol used on this connection */
    type V <: Version1X

    /** The operations that the other side can request from us */
    protected[this] val ourOperations:   JsonOperations[INREQBOUND,  OUTRESBOUND, INREQRES, V]
    protected[this] val theirOperations: JsonOperations[OUTREQBOUND, INRESBOUND,  OUTREQRES, V]

    def onSrpcCall(req: SrpcCall): Future[SrpcResponse] = {
      import ourOperations._

      def respondWithError(
        errCode: PayloadErrorCode,
        description: String
      ): Future[SrpcCallError] =
        Future.successful(SrpcCallError(errCode, description))

      val opName = req.procedureName
      jsonOpForActionName(opName) match {
        case NotImplemented =>
          respondWithError(PayloadErrorCode.NotImplemented, s"Unknown operation $opName")
        case Unsupported =>
          respondWithError(PayloadErrorCode.NotSupported, s"We do not support $opName")
        case Supported(operation) =>
          val responseSrpc = operation.reqRes(req.payload)((req, rr) => onRequest(req)(rr)) map SrpcCallResult

          responseSrpc recover (logIncomingRequestHandlingError(req) andThen requestHandlerErrorToSrpcCallResult)
      }
    }

    private def handleIncomingResponse[REQ <: OUTREQBOUND, RES <: INRESBOUND](
      op: JsonOperation[OUTREQBOUND, INRESBOUND, REQ, RES, OUTREQRES, V],
      res: SrpcCallResult
    ): RES = {
      Try(op.deserializeRes(res.payload)) match {
        case Success(response) =>
          response
        case Failure(e) =>
          throw OcppException(
            PayloadErrorCode.FormationViolation,
            s"Failed to parse OCPP response ${res.payload} for operation $op: $e"
          )
      }
    }

    def sendRequest[REQ <: OUTREQBOUND, RES <: INRESBOUND](req: REQ)(
      implicit reqRes: OUTREQRES[REQ, RES]
    ): Future[RES] = {

      theirOperations.jsonOpForReqRes(reqRes) match {
        case Some(operation) => sendRequestWithJsonOperation[REQ, RES](req, operation)
        case None =>
          Future.failed(OcppException(
            PayloadErrorCode.NotSupported,
            s"Operation is not supported for this version of OCPP"
          ))
      }
    }

    private def sendRequestWithJsonOperation[REQ <: OUTREQBOUND, RES <: INRESBOUND](
      req: REQ,
      jsonOperation: JsonOperation[OUTREQBOUND, INRESBOUND, REQ, RES, OUTREQRES, V]
    ): Future[RES] = {

      val actionName = jsonOperation.action.name

      (Try {
             srpcConnection.sendCall(SrpcCall(actionName, jsonOperation.serializeReq(req)))
           } match {
        case Success(future) => future
        case Failure(e) => Future.failed(e)
      }) map {
        case res: SrpcCallResult =>
          handleIncomingResponse(jsonOperation, res)
        case SrpcCallError(code, description, _) =>
          throw new OcppException(OcppError(code, description))
      }
    }
  }

  def ocppConnection: Ocpp1XConnection

  def onRequest[REQ <: INREQBOUND, RES <: OUTRESBOUND](req: REQ)(implicit reqRes: INREQRES[REQ, RES]): Future[RES]

  def onSrpcCall(msg: SrpcCall): Future[SrpcResponse] = ocppConnection.onSrpcCall(msg)
}

trait ChargePointOcpp1XConnectionComponent
  extends Ocpp1XConnectionComponent[
    CentralSystemReq,
    CentralSystemRes,
    CentralSystemReqRes,
    ChargePointReq,
    ChargePointRes,
    ChargePointReqRes
    ] {

  this: SrpcComponent =>

  class ChargePointOcpp1XConnection[Ver <: Version1X](
    implicit val ourOperations: JsonOperations[ChargePointReq, ChargePointRes, ChargePointReqRes, Ver],
    val theirOperations: JsonOperations[CentralSystemReq, CentralSystemRes, CentralSystemReqRes, Ver]
  ) extends Ocpp1XConnection {

    def sendRequestUntyped(req: CentralSystemReq): Future[CentralSystemRes] =
      req match {
        case r: AuthorizeReq                     => sendRequest(r)
        case r: BootNotificationReq              => sendRequest(r)
        case r: CentralSystemDataTransferReq     => sendRequest(r)
        case r: DiagnosticsStatusNotificationReq => sendRequest(r)
        case r: FirmwareStatusNotificationReq    => sendRequest(r)
        case r: HeartbeatReq.type                => sendRequest(r)
        case r: MeterValuesReq                   => sendRequest(r)
        case r: StartTransactionReq              => sendRequest(r)
        case r: StatusNotificationReq            => sendRequest(r)
        case r: StopTransactionReq               => sendRequest(r)
      }

    type V = Ver
  }

  def defaultChargePointOcppConnection: ChargePointOcpp1XConnection[_ <: Version] = ocppVersion match {
    case Version.V15 => new ChargePointOcpp1XConnection[Version.V15.type]
    case Version.V16 => new ChargePointOcpp1XConnection[Version.V16.type]
    case _ => throw new RuntimeException(s"OCPP JSON not supported for $ocppVersion")
  }
}

trait CentralSystemOcpp1XConnectionComponent
  extends Ocpp1XConnectionComponent[
    ChargePointReq,
    ChargePointRes,
    ChargePointReqRes,
    CentralSystemReq,
    CentralSystemRes,
    CentralSystemReqRes
    ] {

  this: SrpcComponent =>

  class CentralSystemOcpp1XConnection[Ver <: Version1X](
    implicit val ourOperations: JsonOperations[CentralSystemReq, CentralSystemRes, CentralSystemReqRes, Ver],
    val theirOperations: JsonOperations[ChargePointReq, ChargePointRes, ChargePointReqRes, Ver]
  ) extends Ocpp1XConnection {

    def sendRequestUntyped(req: ChargePointReq): Future[ChargePointRes] =
      req match {
        case r: CancelReservationReq        => sendRequest(r)
        case r: ChangeAvailabilityReq       => sendRequest(r)
        case r: ChangeConfigurationReq      => sendRequest(r)
        case r: ChargePointDataTransferReq  => sendRequest(r)
        case r: ClearCacheReq.type          => sendRequest(r)
        case r: ClearChargingProfileReq     => sendRequest(r)
        case r: GetConfigurationReq         => sendRequest(r)
        case r: GetCompositeScheduleReq     => sendRequest(r)
        case r: GetDiagnosticsReq           => sendRequest(r)
        case r: GetLocalListVersionReq.type => sendRequest(r)
        case r: RemoteStartTransactionReq   => sendRequest(r)
        case r: RemoteStopTransactionReq    => sendRequest(r)
        case r: ReserveNowReq               => sendRequest(r)
        case r: ResetReq                    => sendRequest(r)
        case r: SendLocalListReq            => sendRequest(r)
        case r: SetChargingProfileReq       => sendRequest(r)
        case r: TriggerMessageReq           => sendRequest(r)
        case r: UnlockConnectorReq          => sendRequest(r)
        case r: UpdateFirmwareReq           => sendRequest(r)
      }

    type V = Ver
  }

  def defaultCentralSystemOcppConnection: CentralSystemOcpp1XConnection[_ <: Version] = ocppVersion match {
    case Version.V15 => new CentralSystemOcpp1XConnection[Version.V15.type]
    case Version.V16 => new CentralSystemOcpp1XConnection[Version.V16.type]
    case _ => throw new RuntimeException(s"OCPP JSON not supported for $ocppVersion")
  }
}
