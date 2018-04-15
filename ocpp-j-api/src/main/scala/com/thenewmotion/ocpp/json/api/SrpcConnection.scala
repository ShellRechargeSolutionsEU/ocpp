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

    /**
      * Close the connection.
      *
      * This will allow all pending incoming requests to be responded to,
      * and only then will close the underlying WebSocket.
      *
      * @return A future that completes when connection has indeed closed
      */
    def close(): Future[Unit]

    /**
      * Close the connection, without waiting for the request handler to handle
      * requests that were already received.
      *
      * @return
      */
    def forceClose(): Unit
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

  import DefaultSrpcComponent._

  private[this] val logger = LoggerFactory.getLogger(DefaultSrpcComponent.this.getClass)

  private[this] val callIdGenerator = CallIdGenerator()

  implicit val executionContext: ExecutionContext

  class DefaultSrpcConnection extends SrpcConnection {

    private var state: ConnectionState = Open

    private val callIdCache: mutable.Map[String, Promise[ResultMessage]] =
        mutable.Map.empty[String, Promise[ResultMessage]]

    /** The number of incoming requests received that we have not yet responded to */
    private var numIncomingRequests: Int = 0

    private var closePromise: Option[Promise[Unit]] = None

    def close(): Future[Unit] = synchronized {
      state match {
        case Open =>
          val p = Promise[Unit]()
          closePromise = Some(p)
          if (numIncomingRequests == 0) {
            completeGracefulClose()
            state = Closed
          } else {
            state = Closing
          }
          p.future
        case _ =>
          throw new IllegalStateException("Connection already closed")
      }
    }

    def forceClose(): Unit = synchronized {
      webSocketConnection.close()
      state = Closed
    }

    def sendRequest(msg: RequestMessage): Future[ResultMessage] = synchronized {
      val callId = callIdGenerator.next()
      val responsePromise = Promise[ResultMessage]()

      state match {
        case Open =>
          callIdCache.put(callId, responsePromise)
          Try {
                webSocketConnection.send(TransportMessageParser.writeJValue(SrpcEnvelope(callId, msg)))
          } match {
            case Success(()) => responsePromise.future
            case Failure(e)  => Future.failed(e)
          }
        case _ =>
          // TODO genericerror with message
          throw new IllegalStateException("Connection already closed")
      }
    }

    private[DefaultSrpcComponent] def handleIncomingCall(req: RequestMessage): Future[ResultMessage] = synchronized {
      state match {
        case Open =>
          numIncomingRequests += 1
          onSrpcRequest(req)
        case _ =>
          Future.successful(ErrorResponseMessage(PayloadErrorCode.GenericError, "Connection is closing"))
      }
    }

    private[DefaultSrpcComponent] def handleIncomingResult(callId: String, res: ResultMessage): Unit = synchronized {
      val cachedResponsePromise = callIdCache.remove(callId)

      cachedResponsePromise match {
        case None =>
          logger.info("Received response to no request: {}", res)
        case Some(resPromise) =>
          resPromise.success(res)
          ()
      }
    }

    // TODO what if connection has closed by now due to remote close or RST or whatever?
    private[DefaultSrpcComponent] def handleOutgoingResult(callId: String, msg: ResultMessage): Unit = synchronized {
      val resEnvelope = SrpcEnvelope(callId, msg)
      try {
        webSocketConnection.send(TransportMessageParser.writeJValue(resEnvelope))
      } finally {
        srpcConnection.incomingCallEnded()
      }
    }

    /**
      * Should be called whenever SrpcConnection has received the response to an
      * incoming request from the request handler, or definitively failed to do
      * so.
      *
      * @return Whether the WebSocket connection should now be closed
      */
    private def incomingCallEnded(): Unit = {
      numIncomingRequests -= 1

      if (numIncomingRequests == 0 && state == Closing) {
        completeGracefulClose()
      }
    }

    private def completeGracefulClose(): Unit = {
      webSocketConnection.close()
      // TODO or set Closed state in onClose handler?
      state = Closed
      closePromise.foreach(_.success(()))
    }
  }

  def srpcConnection: DefaultSrpcConnection

  def onMessage(jval: JValue): Unit = {
    val reqEnvelope = TransportMessageParser.parse(jval)
    logger.debug(s"onMessage called on $reqEnvelope")
    reqEnvelope.payload match {
      case req: RequestMessage =>
        logger.debug("Passing request to onSrpcRequest")
        srpcConnection.handleIncomingCall(req) recover {
          case NonFatal(e) => ErrorResponseMessage(
            PayloadErrorCode.InternalError,
            "error getting response in SRPC layer"
          )
        } foreach { msg: ResultMessage =>
          srpcConnection.handleOutgoingResult(reqEnvelope.callId, msg)
        }
      case result: ResultMessage =>
        srpcConnection.handleIncomingResult(reqEnvelope.callId, result)
    }
  }

  // TODO don't throw these away!
  def onError(ex: Throwable): Unit =
    logger.error("WebSocket error", ex)
}

object DefaultSrpcComponent {

  /**
    * Our SRPC connections can be in three states:
    *
    * * OPEN (requests and responses can be sent)
    * * CLOSING (only responses can be sent, trying to send a request results in
    * an error)
    * * CLOSED (nothing can be sent and the underlying WebSocket is closed too)
    *
    * This is done so that when an application calls closes an OCPP connection, we
    * give asynchronous request processors a chance to respond before shutting
    * down the WebSocket connection which would lead to unexpected errors.
    *
    * Unfortunately OCPP has no mechanism to tell the remote side we're about to
    * close, so they might still send new requests while we're CLOSING. In that
    * case the SRPC layer will send them a CALLERROR with a GenericError error
    * code.
    *
    * Note that this state is about the SRPC level in the network stack. The
    * WebSocket connection should be fully open when the SRPC layer is in Open
    * or Closing state. When the WebSocket is closed remotely, we go immediately
    * from Open to Closed.
    */
  sealed trait ConnectionState
  case object Open    extends ConnectionState
  case object Closing extends ConnectionState
  case object Closed  extends ConnectionState


  type CallIdCache = Map[String, Promise[ResultMessage]]
}
