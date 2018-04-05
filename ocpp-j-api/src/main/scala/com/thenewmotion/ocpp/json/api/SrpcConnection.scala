package com.thenewmotion.ocpp
package json
package api

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.collection.mutable
import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}
import org.json4s.JValue
import org.slf4j.LoggerFactory

/**
 * The middle layer in the three-layer protocol stack of OCPP-J: Simple Remote
 * Procedure Call.
 *
 * The SRPC layer relates WebSocket messages to each other as requests,
 * responses and error reports.
 *
 * Although the OCPP 1.6 specification no longer uses the term "SRPC", it is
 * present in the original specification on
 * http://www.gir.fr/ocppjs/ocpp_srpc_spec.shtml, which is referenced by the
 * IANA WebSocket Subprotocol Name Registry at
 * https://www.iana.org/assignments/websocket/websocket.xml. So bureaucracy is
 * on our side in naming this.
 */
trait SrpcComponent {
  trait SrpcConnection {
    /**
      * Send an outgoing request to the remote endpoint
      * @param msg The outgoing request
      * @return The incoming response, asynchronously
      */
    def sendRequest(msg: RequestMessage): Future[ResultMessage]
  }

  def srpcConnection: SrpcConnection

  /**
    * To be overridden to handle incoming requests
    *
    * @param msg The incoming request
    * @return The outgoing message, asynchronously
    */
  def onSrpcRequest(msg: RequestMessage): Future[ResultMessage]
}

trait DefaultSrpcComponent extends SrpcComponent {
  this: WebSocketComponent =>

  private[this] val logger = LoggerFactory.getLogger(DefaultSrpcComponent.this.getClass)

  private[this] val callIdGenerator = CallIdGenerator()

  implicit val executionContext: ExecutionContext

  class DefaultSrpcConnection extends SrpcConnection {

    private[this] val callIdCache: mutable.Map[String, Promise[ResultMessage]] = mutable.Map()

    def sendRequest(msg: RequestMessage): Future[ResultMessage] = {
      val callId = callIdGenerator.next()
      val responsePromise = Promise[ResultMessage]()

      callIdCache.synchronized {
        callIdCache.put(callId, responsePromise)
      }

      Try {
        webSocketConnection.send(TransportMessageParser.writeJValue(SrpcEnvelope(callId, msg)))
      } match {
        case Success(()) => responsePromise.future
        case Failure(e)  => Future.failed(e)
      }
    }

    private[DefaultSrpcComponent] def handleIncomingResult(callId: String, res: ResultMessage): Unit = {
      val cachedResponsePromise = callIdCache.synchronized {
        callIdCache.remove(callId)
      }

      cachedResponsePromise match {
        case None =>
          logger.info("Received response to no request: {}", res)
        case Some(resPromise) =>
          resPromise.success(res)
          ()
      }
    }
  }

  def srpcConnection: DefaultSrpcConnection

  def onMessage(jval: JValue): Unit = {
    val reqEnvelope = TransportMessageParser.parse(jval)
    logger.debug(s"onMessage called on $reqEnvelope")
    reqEnvelope.payload match {
      case req: RequestMessage =>
        logger.debug("Passing request to onSrpcRequest")
        onSrpcRequest(req) recover {
          case NonFatal(e) => ErrorResponseMessage(
            PayloadErrorCode.InternalError,
            "error getting response in SRPC layer"
          )
        } foreach { msg: ResultMessage =>
          val resEnvelope = SrpcEnvelope(reqEnvelope.callId, msg)
          webSocketConnection.send(TransportMessageParser.writeJValue(resEnvelope))
        }
      case result: ResultMessage =>
        srpcConnection.handleIncomingResult(reqEnvelope.callId, result)
    }
  }

  // TODO don't throw these away!
  def onError(ex: Throwable): Unit =
    logger.error("WebSocket error", ex)
}


