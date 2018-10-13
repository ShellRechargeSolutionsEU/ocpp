package com.thenewmotion.ocpp
package json
package api
package server

import scala.concurrent.ExecutionContext
import org.java_websocket.WebSocket
import messages.v20._
import VersionFamily._

/**
  * A simple server implementation to show how this library can be used in servers.
  *
  * @param listenPort The port to listen on
  * @param requestedOcppVersion The OCPP version to serve (either 1.5 or 1.6; negotiation is not supported)
  */
abstract class Ocpp20JsonServer(listenPort: Int)(implicit ec: ExecutionContext)
  extends OcppJsonServer[
    VersionFamily.V20.type,
    CsRequest,
    CsResponse,
    CsReqRes,
    CsmsRequest,
    CsmsResponse,
    CsmsReqRes
  ](listenPort, Version.V20) {

  protected def newConnectionCake(
    connection: WebSocket,
    chargePointIdentity: String
  ): BaseConnectionCake = new BaseConnectionCake(connection, chargePointIdentity) with CsmsOcpp20ConnectionComponent
}

object Ocpp20JsonServer {
  type OutgoingEndpoint = OutgoingOcppEndpoint[CsRequest, CsResponse, CsReqRes]
}

