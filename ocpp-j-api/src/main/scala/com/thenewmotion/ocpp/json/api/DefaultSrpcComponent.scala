package com.thenewmotion.ocpp
package json
package api

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}
import org.json4s.JValue
import org.slf4j.LoggerFactory
import DefaultSrpcComponent._

trait DefaultSrpcComponent extends SrpcComponent {
  this: WebSocketComponent =>

  private[this] val logger = LoggerFactory.getLogger(DefaultSrpcComponent.this.getClass)

  private[this] val callIdGenerator = CallIdGenerator()

  protected implicit val executionContext: ExecutionContext

  class DefaultSrpcConnection extends SrpcConnection {

    private var state: ConnectionState = Open

    private val callIdCache: mutable.Map[String, Promise[SrpcResponse]] =
      mutable.Map.empty[String, Promise[SrpcResponse]]

    /** The number of incoming calls received that we have not yet responded to */
    private var numIncomingCalls: Int = 0

    private val closePromise: Promise[Unit] = Promise[Unit]()

    def sendCall(msg: SrpcCall): Future[SrpcResponse] = synchronized {
      val callId = callIdGenerator.next()
      val responsePromise = Promise[SrpcResponse]()

      state match {
        case Open =>
          callIdCache.put(callId, responsePromise)
          Try {
                webSocketConnection.send(TransportMessageParser.writeJValue(SrpcEnvelope(callId, msg)))
              } match {
            case Success(()) => responsePromise.future
            case Failure(e)  => Future.failed(e)
          }
        case Closing | Closed =>
          Future.failed(new IllegalStateException("Connection already closed"))
      }
    }

    def close(): Future[Unit] = {
      val shouldClose = synchronized {
        state match {
          case Open =>
            val shouldClose = if (numIncomingCalls == 0) {
              state = Closed
              true
            } else {
              state = Closing
              false
            }
            shouldClose
          case Closing | Closed =>
            throw new IllegalStateException("Connection already closed")
        }
      }

      // Let's call close() without the monitor lock held, because
      // implementations may be synchronous and trigger a call to
      // onWebSocketDisconnect from another thread before this call returns
      if (shouldClose) webSocketConnection.close()

      closePromise.future
    }

    def forceClose(): Unit = {
      synchronized {
        state = Closed
      }
      webSocketConnection.close()
    }

    def onClose: Future[Unit] = closePromise.future

    private[DefaultSrpcComponent] def handleIncomingCall(req: SrpcCall): Future[SrpcResponse] = synchronized {
      state match {
        case Open =>
          numIncomingCalls += 1
          onSrpcCall(req)
        case Closing | Closed =>
          Future.successful(SrpcCallError(PayloadErrorCode.GenericError, "Connection is closing"))
      }
    }

    private[DefaultSrpcComponent] def handleIncomingResponse(callId: String, res: SrpcResponse): Unit = synchronized {
      state match {
        case Closed =>
          logger.warn("Received response with call ID {} while already closed. Dropping.", callId)
        case Open | Closing =>
          val cachedResponsePromise = callIdCache.remove(callId)

          cachedResponsePromise match {
            case None =>
              logger.warn("Received response to no call, the unknown call ID is {}", callId)
            case Some(resPromise) =>
              resPromise.success(res)
              ()
          }
      }
    }

    private[DefaultSrpcComponent] def handleOutgoingResponse(callId: String, msg: SrpcResponse): Unit = {
      val shouldClose = synchronized {
        Try {
          state match {
            case Closed =>
              logger.warn(s"WebSocket connection closed before we could respond to call; call ID $callId")
            case Open | Closing =>
              val resEnvelope = SrpcEnvelope(callId, msg)
              webSocketConnection.send(TransportMessageParser.writeJValue(resEnvelope))
          }
        } match {
          case Failure(e) => logger.warn("Could not write response to WebSocket", e)
          case _          =>
        }

        incomingCallEnded()
      }

      if (shouldClose) webSocketConnection.close()
    }

    private[DefaultSrpcComponent] def handleWebSocketDisconnect(): Unit = synchronized {
      state = Closed
      closePromise.trySuccess(())
      ()
    }

    /**
      * Should be called whenever SrpcConnection has received the response to an
      * incoming call from the call handler, or definitively failed to do
      * so. In those cases we may have to finish a graceful close in progress.
      *
      * @return Whether the WebSocket connection should now be closed
      */
    private def incomingCallEnded(): Boolean = {
      numIncomingCalls -= 1

      if (numIncomingCalls == 0) {
        val oldState = state

        state = state match {
          case Closing | Closed =>
            Closed
          case Open =>
            Open
        }

        oldState == Closing
      } else false
    }
  }

  def srpcConnection: DefaultSrpcConnection

  def onMessage(jval: JValue): Unit = {
    val reqEnvelope = TransportMessageParser.parse(jval)
    logger.debug(s"onMessage called on $reqEnvelope")
    reqEnvelope.payload match {
      case req: SrpcCall =>
        logger.debug("Passing call to onSrpcCall")
        srpcConnection.handleIncomingCall(req) recover {
          case NonFatal(e) => SrpcCallError(
            PayloadErrorCode.InternalError,
            "error getting result in SRPC layer"
          )
        } foreach { msg: SrpcResponse =>
          srpcConnection.handleOutgoingResponse(reqEnvelope.callId, msg)
        }
      case response: SrpcResponse =>
        srpcConnection.handleIncomingResponse(reqEnvelope.callId, response)
    }
  }

  def onWebSocketDisconnect(): Unit = {
    srpcConnection.handleWebSocketDisconnect()
  }

  // TODO don't throw these away!
  def onError(ex: Throwable): Unit =
    logger.error("WebSocket error", ex)
}

object DefaultSrpcComponent {

  /**
    * Our SRPC connections can be in three states:
    *
    * * OPEN (calls and responses can be sent)
    * * CLOSING (only responses can be sent, trying to send a call results in
    * an error)
    * * CLOSED (nothing can be sent and the underlying WebSocket is closed too)
    *
    * This is done so that when an application calls closes an OCPP connection, we
    * give asynchronous call processors a chance to respond before shutting
    * down the WebSocket connection which would lead to unexpected errors.
    *
    * Unfortunately OCPP has no mechanism to tell the remote side we're about to
    * close, so they might still send new calls while we're CLOSING. In that
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
}
