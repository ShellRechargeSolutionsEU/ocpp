package com.thenewmotion.ocpp
package json
package api

import com.thenewmotion.ocpp.messages._
import org.slf4j.LoggerFactory

import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success, Try}
import scala.collection.mutable

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * A component that, when mixed into something that is also an SRPC component, can send and receive OCPP-JSON messages
 * over that SRPC connection.
 *
 * @tparam OUTREQ Type of outgoing requests
 * @tparam INRES Type of incoming responses
 * @tparam INREQ Type of incoming requests
 * @tparam OUTRES Type of outgoing responses
 */
trait OcppConnectionComponent[OUTREQ <: Req, INRES <: Res, INREQ <: Req, OUTRES <: Res] {
  this: SrpcComponent =>

  trait OcppConnection {
    /** Send an outgoing OCPP request */
    def sendRequest[REQ <: OUTREQ, RES <: INRES](req: REQ)(implicit reqRes: ReqRes[REQ, RES]): Future[RES]

    /** Handle an incoming SRPC message */
    def onSrpcMessage(msg: TransportMessage)
  }

  def ocppConnection: OcppConnection

  def onRequest[REQ <: INREQ, RES <: OUTRES](req: REQ)(implicit reqRes: ReqRes[REQ, RES]): Future[RES]
  def onOcppError(error: OcppError)
}

// TODO support the 'details' field of OCPP error messages
case class OcppError(error: PayloadErrorCode.Value, description: String)
case class OcppException(ocppError: OcppError) extends Exception(s"${ocppError.error}: ${ocppError.description}")
object OcppException {
  def apply(error: PayloadErrorCode.Value, description: String): OcppException =
    OcppException(OcppError(error, description))
}

trait DefaultOcppConnectionComponent[OUTREQ <: Req, INRES <: Res, INREQ <: Req, OUTRES <: Res]
  extends OcppConnectionComponent[OUTREQ, INRES, INREQ, OUTRES] {

  this: SrpcComponent =>

  trait DefaultOcppConnection extends OcppConnection {
    /** The version of the OCPP protocol used on this connection */
    type V <: Version

    /** The operations that the other side can request from us */
    val ourOperations: JsonOperations[INREQ, OUTRES, V]
    val theirOperations: JsonOperations[OUTREQ, INRES, V]

    private val logger = LoggerFactory.getLogger(DefaultOcppConnection.this.getClass)

    private[this] val callIdGenerator = CallIdGenerator()

    sealed case class OutstandingRequest[REQ <: OUTREQ, RES <: INRES](operation: JsonOperation[REQ, RES, V],
                                                                      responsePromise: Promise[RES])

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

      def respondWithError(errCode: PayloadErrorCode.Value, description: String) =
        srpcConnection.send(ErrorResponseMessage(req.callId, errCode, description))

      val opName = req.procedureName
      jsonOpForActionName(opName) match {
        case NotImplemented => respondWithError(PayloadErrorCode.NotImplemented, s"Unknown operation $opName")
        case Unsupported => respondWithError(PayloadErrorCode.NotSupported, s"We do not support $opName")
        case Supported(operation) =>
          val responseSrpc = operation.reqRes(req.payload)((req, rr) => onRequest(req)(rr)) map { res =>
            ResponseMessage(req.callId, res)
          } recover {
            case e: Exception =>
              logger.warn(s"Exception processing OCPP request {}: {} {}",
                req.procedureName, e.getClass.getSimpleName, e.getMessage)

              val ocppError = e match {
                case OcppException(err) => err
                case _ => OcppError(PayloadErrorCode.InternalError, "Unexpected error processing request")
              }
              ErrorResponseMessage(req.callId, ocppError.error, ocppError.description)
          }

          responseSrpc onComplete {
            case Success(json) => srpcConnection.send(json)
            case Failure(e) =>
              logger.error(
                s"OCPP response future failed for $opName with call ID ${req.callId}. This ought to be impossible.")
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

    def sendRequest[REQ <: OUTREQ, RES <: INRES](req: REQ)(implicit reqRes: ReqRes[REQ, RES]): Future[RES] = {
      Try(theirOperations.jsonOpForReqRes(reqRes)) match {
        case Success(operation) => sendRequestWithJsonOperation[REQ, RES](req, operation)
        case Failure(e: NoSuchElementException) =>
          val operationName = getProcedureName(req)
          throw new Exception(s"Tried to send unsupported OCPP request $operationName")
        case Failure(e) => throw e
      }
    }

    private def sendRequestWithJsonOperation[REQ <: OUTREQ, RES <: INRES](
      req: REQ,
      jsonOperation: JsonOperation[REQ, RES, V]
    ) = {
      val callId = callIdGenerator.next()
      val responsePromise = Promise[RES]()

      callIdCache.put(callId, OutstandingRequest[REQ, RES](jsonOperation, responsePromise))

      srpcConnection.send(RequestMessage(callId, getProcedureName(req), jsonOperation.serializeReq(req)))
      responsePromise.future
    }

    private def getProcedureName(c: Message) = {
      c.getClass.getSimpleName.replaceFirst("Re[qs]\\$?$", "")
    }
  }

  def onRequest[REQ <: INREQ, RES <: OUTRES](req: REQ)(implicit reqRes: ReqRes[REQ, RES]): Future[RES]
  def onOcppError(error: OcppError): Unit

  def onSrpcMessage(msg: TransportMessage) = ocppConnection.onSrpcMessage(msg)
}

trait ChargePointOcppConnectionComponent
  extends DefaultOcppConnectionComponent[CentralSystemReq, CentralSystemRes, ChargePointReq, ChargePointRes] {
  this: SrpcComponent =>

  class ChargePointOcppConnection[Ver <: Version](
    implicit val ourOperations: JsonOperations[ChargePointReq, ChargePointRes, Ver],
    val theirOperations: JsonOperations[CentralSystemReq, CentralSystemRes, Ver]
  ) extends DefaultOcppConnection {

    type V = Ver
  }

  def defaultChargePointOcppConnection(version: Version) = version match {
    case Version.V15 => new ChargePointOcppConnection[Version.V15.type]
    case Version.V16 => new ChargePointOcppConnection[Version.V16.type]
  }
}

trait CentralSystemOcppConnectionComponent
  extends DefaultOcppConnectionComponent[ChargePointReq, ChargePointRes, CentralSystemReq, CentralSystemRes] {
  this: SrpcComponent =>

  class CentralSystemOcppConnection[Ver <: Version](
    implicit val ourOperations: JsonOperations[CentralSystemReq, CentralSystemRes, Ver],
    val theirOperations: JsonOperations[ChargePointReq, ChargePointRes, Ver]
  ) extends DefaultOcppConnection {

    type V = Ver
  }

  def defaultCentralSystemOcppConnection(version: Version) = version match {
    case Version.V15 => new CentralSystemOcppConnection[Version.V15.type]
    case Version.V16 => new CentralSystemOcppConnection[Version.V16.type]
  }
}
