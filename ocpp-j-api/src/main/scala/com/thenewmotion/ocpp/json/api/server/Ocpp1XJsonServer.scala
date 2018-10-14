package com.thenewmotion.ocpp
package json.api
package server

import scala.concurrent.ExecutionContext
import org.java_websocket.WebSocket
import messages.v1x._

/**
 * A simple server implementation to show how this library can be used in servers.
 *
 * @param listenPort The port to listen on
 * @param requestedOcppVersion The OCPP version to serve (either 1.5 or 1.6; negotiation is not supported)
 */
abstract class Ocpp1XJsonServer(listenPort: Int, requestedOcppVersion: Version1X)(implicit ec: ExecutionContext)
  extends OcppJsonServer[
    VersionFamily.V1X.type,
    ChargePointReq,
    ChargePointRes,
    ChargePointReqRes,
    CentralSystemReq,
    CentralSystemRes,
    CentralSystemReqRes
  ](listenPort, requestedOcppVersion) {
  protected def newConnectionCake(
    connection: WebSocket,
    chargePointIdentity: String
  ): BaseConnectionCake =
    new BaseConnectionCake(connection, chargePointIdentity)
      with CentralSystemOcpp1XConnectionComponent {

      lazy val ocppConnection: Ocpp1XConnection = defaultCentralSystemOcppConnection

      lazy val ocppVersion: Version = requestedOcppVersion
  }
}

object Ocpp1XJsonServer {
  type OutgoingEndpoint = OutgoingOcppEndpoint[ChargePointReq, ChargePointRes, ChargePointReqRes]
}

