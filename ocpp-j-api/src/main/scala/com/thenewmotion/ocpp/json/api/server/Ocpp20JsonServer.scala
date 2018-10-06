package com.thenewmotion.ocpp
package json.api
package server

import java.net.InetSocketAddress
import java.util.Collections

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}
import org.java_websocket.WebSocket
import org.java_websocket.drafts.{Draft, Draft_6455}
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import messages.v20._
import Ocpp20JsonServer._
import org.java_websocket.protocols.{IProtocol, Protocol}

// TODO drive down code duplicated from OCPP 1.x, make nice OcppJsonServer.forV1x / .forV20 iface
/**
  * A simple server implementation to show how this library can be used in servers.
  *
  * @param listenPort The port to listen on
  * @param requestedOcppVersion The OCPP version to serve (either 1.5 or 1.6; negotiation is not supported)
  */
abstract class Ocpp20JsonServer(listenPort: Int)
  extends WebSocketServer(
    new InetSocketAddress(listenPort),
    Collections.singletonList[Draft](new Draft_6455(
      Collections.emptyList(),
      Collections.singletonList[IProtocol](new Protocol("ocpp2.0"))
    ))
  ){

  private type OcppCake = CsmsOcpp20ConnectionComponent with DefaultSrpcComponent with SimpleServerWebSocketComponent

  private val ocppConnections: mutable.Map[WebSocket, OcppCake] = mutable.HashMap[WebSocket, OcppCake]()

  /**
    * This method should be overridden by the user of this class to define the behavior of the Central System. It will
    * be called once for each connection to this server that is established.
    *
    * @param clientChargePointIdentity The charge point identity of the client
    * @param remote An OutgoingEndpoint to send messages to the Charge Point or close the connection
    *
    * @return The handler for incoming requests from the Charge Point
    */
  def handleConnection(clientChargePointIdentity: String, remote: OutgoingEndpoint): CsmsRequestHandler

  override def onStart(): Unit = {}

  override def onOpen(conn: WebSocket, hndshk: ClientHandshake): Unit = {

    val uri = hndshk.getResourceDescriptor
    uri.split("/").lastOption match {

      case None =>
        conn.close(1003, "No ChargePointIdentity in path")

      case Some(chargePointIdentity) =>
        onOpenWithCPIdentity(conn, chargePointIdentity)
    }
  }

  private def onOpenWithCPIdentity(conn : WebSocket, chargePointIdentity: String): Unit = {
    val ocppConnection = new CsmsOcpp20ConnectionComponent with DefaultSrpcComponent with SimpleServerWebSocketComponent {

      override val srpcConnection: DefaultSrpcConnection = new DefaultSrpcConnection()

      override val webSocketConnection: SimpleServerWebSocketConnection = new SimpleServerWebSocketConnection {
        val webSocket: WebSocket = conn
      }

      private val outgoingEndpoint = new OutgoingEndpoint {
        def send[REQ <: CsRequest, RES <: CsResponse](req: REQ)(implicit reqRes: CsReqRes[REQ, RES]): Future[RES] =
          ocppConnection.sendRequest(req)

        def close(): Future[Unit] = srpcConnection.close()

        val onClose: Future[Unit] = srpcConnection.onClose
      }

      private val requestHandler = handleConnection(chargePointIdentity, outgoingEndpoint)

      def onRequest[REQ <: CsmsRequest, RES <: CsmsResponse](req: REQ)(implicit reqRes: CsmsReqRes[REQ, RES]) =
        requestHandler.apply(req)

      implicit val executionContext: ExecutionContext = concurrent.ExecutionContext.Implicits.global

      override val ocppVersion: Version = Version.V20
    }

    ocppConnections.put(conn, ocppConnection)
    ()
  }

  override def onClose(
    conn: WebSocket,
    code: Int,
    reason: String,
    remote: Boolean
  ): Unit = {
    ocppConnections.remove(conn) foreach { c =>
      c.feedIncomingDisconnect()
    }
  }

  override def onMessage(conn: WebSocket, message: String): Unit =
    ocppConnections.get(conn) foreach { c =>
      c.feedIncomingMessage(message)
    }

  override def onError(conn: WebSocket, ex: Exception): Unit =
    ocppConnections.get(conn) foreach { c =>
      c.feedIncomingError(ex)
    }
}

object Ocpp20JsonServer {
  type OutgoingEndpoint = OutgoingOcppEndpoint[CsRequest, CsResponse, CsReqRes]
}
