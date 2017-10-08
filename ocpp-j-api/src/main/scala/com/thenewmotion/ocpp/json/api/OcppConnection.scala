package com.thenewmotion.ocpp
package json
package api

import scala.language.higherKinds
import scala.concurrent.{Future, Promise, ExecutionContext}
import scala.util.{Success, Failure, Try}
import scala.collection.mutable
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
  OUTREQBOUND <: Req,
  INRESBOUND <: Res,
  OUTREQRES[_ <: OUTREQBOUND, _ <: INRESBOUND],
  INREQBOUND <: Req,
  OUTRESBOUND <: Res,
  INREQRES[_ <: INREQBOUND, _  <: OUTRESBOUND]
] {

  this: SrpcComponent =>

  trait OcppConnection {
    /** Send an outgoing OCPP request */
    def sendRequest[REQ <: OUTREQBOUND, RES <: INRESBOUND](req: REQ)(implicit reqRes: OUTREQRES[REQ, RES]): Future[RES]

    /** Handle an incoming SRPC message */
    def onSrpcMessage(msg: TransportMessage)
  }

  def ocppConnection: OcppConnection

  def onRequest[REQ <: INREQBOUND, RES <: OUTRESBOUND](req: REQ)(implicit reqRes: INREQRES[REQ, RES]): Future[RES]
  def onOcppError(error: OcppError)

  /** OCPP versions that we support, used during connection handshake */
  def requestedVersions: List[Version]

  protected final val wsSubProtocolForOcppVersion = Map[Version, String](
    Version.V15 -> "ocpp1.5",
    Version.V16 -> "ocpp1.6"
  )
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
    val ourOperations:   JsonOperations[INREQBOUND,  OUTRESBOUND, INREQRES, V]
    val theirOperations: JsonOperations[OUTREQBOUND, INRESBOUND,  OUTREQRES, V]

    private val logger = LoggerFactory.getLogger(DefaultOcppConnection.this.getClass)

    private[this] val callIdGenerator = CallIdGenerator()

    private[this] sealed case class OutstandingRequest[REQ <: OUTREQBOUND, RES <: INRESBOUND](
      operation: JsonOperation[OUTREQBOUND, INRESBOUND, REQ, RES, OUTREQRES, V],
      responsePromise: Promise[RES]
    )

    private[this] val callIdCache: mutable.Map[String, OutstandingRequest[_, _]] = mutable.Map()

    def onSrpcMessage(msg: TransportMessage) {
      logger.debug("Incoming SRPC message: {}", msg)

      msg match {
        case req: RequestMessage => handleIncomingRequest(req)
        case res: ResponseMessage => handleIncomingResponse(res)
        case err: ErrorResponseMessage => handleIncomingError(err)
      }
    }

    private def handleIncomingRequest(req: RequestMessage) {
      import ourOperations._

      def respondWithError(
        errCode: PayloadErrorCode,
        description: String
      ): Unit =
        srpcConnection.send(ErrorResponseMessage(req.callId, errCode, description))

      val opName = req.procedureName
      jsonOpForActionName(opName)  match {
        case NotImplemented => respondWithError(PayloadErrorCode.NotImplemented, s"Unknown operation $opName")
        case Unsupported => respondWithError(PayloadErrorCode.NotSupported, s"We do not support $opName")
        case Supported(operation) =>
          val responseSrpc = operation.reqRes(req.payload)((req, rr) => onRequest(req)(rr)) map { res =>
            ResponseMessage(req.callId, res)
          }

          responseSrpc onComplete {
            case Success(json) => srpcConnection.send(json)

            case Failure(e) =>
              logger.warn("Exception processing OCPP request {}: {} {}",
                req.procedureName, e.getClass.getSimpleName, e.getMessage)

              val ocppError = e match {
                case OcppException(err) => err
                case _ => OcppError(PayloadErrorCode.InternalError, "Unexpected error processing request")
              }

              srpcConnection.send(ErrorResponseMessage(
                req.callId,
                ocppError.error,
                ocppError.description
              ))
          }
      }
    }

    private def handleIncomingResponse(res: ResponseMessage): Unit = {
      callIdCache.remove(res.callId) match {
        case None =>
          logger.info("Received response for no request: {}", res)
        case Some(OutstandingRequest(op, resPromise)) =>
          Try(op.deserializeRes(res.payload)) match {
            case Success(response) =>
              resPromise.success(response)
              ()
            case Failure(e) =>
              logger.info("Failed to parse OCPP response {} to call {} (operation {})", res.payload, res.callId, op, e)
              resPromise.failure(e)
              ()
          }
      }
    }

    private def handleIncomingError(err: ErrorResponseMessage): Unit = err match {
      case ErrorResponseMessage(callId, errCode, description, details) =>
        callIdCache.remove(callId) match {
          case None => onOcppError(OcppError(errCode, description))
          case Some(OutstandingRequest(operation, futureResponse)) =>
            futureResponse failure new OcppException(OcppError(errCode, description))
            ()
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
      val callId = callIdGenerator.next()
      val responsePromise = Promise[RES]()

      callIdCache.put(callId, OutstandingRequest[REQ, RES](jsonOperation, responsePromise))

      val actionName = jsonOperation.action.name

      // TODO make sure a failure to serialize (e.g. version incompatibility)
      // will lead to failed future for request sender, not in an exception
      // thrown directly at him
      srpcConnection.send(RequestMessage(callId, actionName, jsonOperation.serializeReq(req)))
      responsePromise.future
    }
  }

  def onRequest[REQ <: INREQBOUND, RES <: OUTRESBOUND](req: REQ)(implicit reqRes: INREQRES[REQ, RES]): Future[RES]
  def onOcppError(error: OcppError): Unit

  def onSrpcMessage(msg: TransportMessage) = ocppConnection.onSrpcMessage(msg)

  def requestedSubProtocols: List[String] =
    requestedVersions.map(wsSubProtocolForOcppVersion)
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

    type V = Ver
  }

  def defaultChargePointOcppConnection(version: Version) = version match {
    case Version.V15 => new ChargePointOcppConnection[Version.V15.type]
    case Version.V16 => new ChargePointOcppConnection[Version.V16.type]
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

    type V = Ver
  }

  def defaultCentralSystemOcppConnection(version: Version) = version match {
    case Version.V15 => new CentralSystemOcppConnection[Version.V15.type]
    case Version.V16 => new CentralSystemOcppConnection[Version.V16.type]
  }
}
