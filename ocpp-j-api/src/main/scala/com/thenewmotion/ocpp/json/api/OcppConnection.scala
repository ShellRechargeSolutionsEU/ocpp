package com.thenewmotion.ocpp
package json
package api

import scala.language.higherKinds
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}
import com.thenewmotion.ocpp.messages._
import org.slf4j.LoggerFactory

/**
 * The highest layer in the three-layer protocol stack of OCPP-J: OCPP message
 * exchange.
 *
 * When mixed into something that is also a SimpleRPC component, can do OCPP
 * request-response exchanges using the SimpleRPC connection.
 *
 * @tparam OUTREQBOUND Supertype of outgoing requests (either ChargePointReq or CentralSystemReq)
 * @tparam INRESBOUND Supertype of incoming responses (either ChargePointRes or CentralSystemRes)
 * @tparam OUTREQRES Typeclass relating the types of incoming requests and outgoing responses
 * @tparam INREQBOUND Supertype of incoming requests (either ChargePointReq or CentralSystemReq)
 * @tparam OUTRESBOUND Supertype of outgoing responses (either ChargePointRes or CentralSystemRes)
 * @tparam INREQRES Typeclass relating the types of outgoing requests and incoming responses
 */
trait OcppConnectionComponent[
  OUTREQBOUND <: RequestV1orV2,
  INRESBOUND <: ResponseV1orV2,
  OUTREQRES[_ <: OUTREQBOUND, _ <: INRESBOUND],
  INREQBOUND <: RequestV1orV2,
  OUTRESBOUND <: ResponseV1orV2,
  INREQRES[_ <: INREQBOUND, _  <: OUTRESBOUND]
] {

  this: SrpcComponent =>

  trait OcppConnection {
    /** Send an outgoing OCPP request */
    def sendRequest[REQ <: OUTREQBOUND, RES <: INRESBOUND](req: REQ)(implicit reqRes: OUTREQRES[REQ, RES]): Future[RES]

    /**
     * Alternative to sendRequest that allows you to write code that processes
     * requests without knowing what type they are exactly.
     *
     * Downside is that it will not know which response you're getting back.
     * You'll have to pattern match on it somewhere to do something meaningful
     * with it.
     */
    def sendRequestUntyped(req: OUTREQBOUND): Future[INRESBOUND]
  }

  def ocppVersion: Version

  def ocppConnection: OcppConnection

  def onRequest[REQ <: INREQBOUND, RES <: OUTRESBOUND](req: REQ)(implicit reqRes: INREQRES[REQ, RES]): Future[RES]
}

// for now, we don't support the 'details' field of OCPP-J error messages
case class OcppError(error: PayloadErrorCode, description: String)
case class OcppException(ocppError: OcppError) extends Exception(s"${ocppError.error}: ${ocppError.description}")
object OcppException {
  def apply(error: PayloadErrorCode, description: String): OcppException =
    OcppException(OcppError(error, description))
}

trait DefaultOcppConnectionComponent[
  OUTREQBOUND <: Req,
  INRESBOUND <: Res,
  OUTREQRES[_ <: OUTREQBOUND, _ <: INRESBOUND ] <: ReqRes[_, _],
  INREQBOUND <: Req,
  OUTRESBOUND <: Res,
  INREQRES [_ <: INREQBOUND,  _ <: OUTRESBOUND] <: ReqRes[_, _]
] extends OcppConnectionComponent[OUTREQBOUND, INRESBOUND, OUTREQRES, INREQBOUND, OUTRESBOUND, INREQRES] {

  this: SrpcComponent =>

  implicit val executionContext: ExecutionContext

  trait DefaultOcppConnection extends OcppConnection {
    /** The version of the OCPP protocol used on this connection */
    type V <: Version

    /** The operations that the other side can request from us */
    protected[this] val ourOperations:   JsonOperations[INREQBOUND,  OUTRESBOUND, INREQRES, V]
    protected[this] val theirOperations: JsonOperations[OUTREQBOUND, INRESBOUND,  OUTREQRES, V]

    private val logger = LoggerFactory.getLogger(DefaultOcppConnection.this.getClass)

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

          responseSrpc recover { case NonFatal(e) =>
            logger.warn("Exception processing OCPP request {}: {} {}",
              req.procedureName, e.getClass.getSimpleName, e.getMessage)

            val ocppError = e match {
              case OcppException(err) => err
              case _ => OcppError(PayloadErrorCode.InternalError, "Unexpected error processing request")
            }

            SrpcCallError(
              ocppError.error,
              ocppError.description
            )
          }
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
            s"Operation not supported at version this version of OCPP"
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

  def ocppConnection: DefaultOcppConnection

  def onRequest[REQ <: INREQBOUND, RES <: OUTRESBOUND](req: REQ)(implicit reqRes: INREQRES[REQ, RES]): Future[RES]

  def onSrpcCall(msg: SrpcCall): Future[SrpcResponse] = ocppConnection.onSrpcCall(msg)
}

trait ChargePointOcppConnectionComponent
  extends DefaultOcppConnectionComponent[
    CentralSystemReq,
    CentralSystemRes,
    CentralSystemReqRes,
    ChargePointReq,
    ChargePointRes,
    ChargePointReqRes
  ] {

  this: SrpcComponent =>

  class ChargePointOcppConnection[Ver <: Version](
    implicit val ourOperations: JsonOperations[ChargePointReq, ChargePointRes, ChargePointReqRes, Ver],
    val theirOperations: JsonOperations[CentralSystemReq, CentralSystemRes, CentralSystemReqRes, Ver]
  ) extends DefaultOcppConnection {

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

  def defaultChargePointOcppConnection: ChargePointOcppConnection[_ <: Version] = ocppVersion match {
    case Version.V15 => new ChargePointOcppConnection[Version.V15.type]
    case Version.V16 => new ChargePointOcppConnection[Version.V16.type]
    case _ => throw new RuntimeException(s"OCPP JSON not supported for $ocppVersion")
  }
}

trait CentralSystemOcppConnectionComponent
  extends DefaultOcppConnectionComponent[
    ChargePointReq,
    ChargePointRes,
    ChargePointReqRes,
    CentralSystemReq,
    CentralSystemRes,
    CentralSystemReqRes
  ] {

  this: SrpcComponent =>

  class CentralSystemOcppConnection[Ver <: Version](
    implicit val ourOperations: JsonOperations[CentralSystemReq, CentralSystemRes, CentralSystemReqRes, Ver],
    val theirOperations: JsonOperations[ChargePointReq, ChargePointRes, ChargePointReqRes, Ver]
  ) extends DefaultOcppConnection {

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

  def defaultCentralSystemOcppConnection: CentralSystemOcppConnection[_ <: Version] = ocppVersion match {
    case Version.V15 => new CentralSystemOcppConnection[Version.V15.type]
    case Version.V16 => new CentralSystemOcppConnection[Version.V16.type]
    case _ => throw new RuntimeException(s"OCPP JSON not supported for $ocppVersion")
  }
}
