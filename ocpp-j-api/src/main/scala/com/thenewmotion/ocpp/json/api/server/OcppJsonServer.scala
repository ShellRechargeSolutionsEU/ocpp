package com.thenewmotion.ocpp
package json.api
package server

import java.net.InetSocketAddress
import scala.concurrent.ExecutionContext
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import messages._

abstract class OcppJsonServer(listenPort: Int, ocppVersion: Version)
  extends WebSocketServer(new InetSocketAddress(listenPort)) {

  type OutgoingEndpoint = OutgoingOcppEndpoint[ChargePointReq, ChargePointRes, ChargePointReqRes]

  type IncomingEndpoint = IncomingOcppEndpoint[CentralSystemReq, CentralSystemRes, CentralSystemReqRes]

  def connectionHandler: OutgoingEndpoint => IncomingEndpoint

  override def onOpen(conn: WebSocket, hndshk: ClientHandshake): Unit = {

    val ocppConnection = new CentralSystemOcppConnectionComponent  with DefaultSrpcComponent with SimpleServerWebSocketComponent {
      override def ocppConnection: DefaultOcppConnection = defaultCentralSystemOcppConnection

      override def srpcConnection: DefaultSrpcConnection = new DefaultSrpcConnection()

      override def webSocketConnection: SimpleServerWebSocketConnection = new SimpleServerWebSocketConnection {
        val webSocket: WebSocket = conn
      }

      def onRequest[REQ <: CentralSystemReq, RES <: CentralSystemRes](req: REQ)(implicit reqRes: CentralSystemReqRes[REQ, RES]) = ???

      def onOcppError(error: OcppError): Unit = ???

      def onDisconnect() = ???

      implicit val executionContext: ExecutionContext = ???

      def ocppVersion: Version = ???
    }
  }

  override def onClose(
                        conn: WebSocket,
                        code: Int,
                        reason: IdTag,
                        remote: Boolean
                      ): Unit = ???

  override def onMessage(conn: WebSocket, message: IdTag): Unit = ???

  override def onError(conn: WebSocket, ex: Exception): Unit = ???
}
