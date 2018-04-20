package com.thenewmotion.ocpp
package json
package api

import scala.language.higherKinds

import messages._

/**
  * Generic interface of an incoming OCPP connection endpoint that can be
  * implemented by the library user to handle incoming OCPP requests.
  *
  *  * onRequest should be overridden to handle incoming requests
  *  * onDisconnect should be overridden to handle a disconnection
  *
  * @tparam INREQ The type of incoming requests (either ChargePointReq or CentralSystemReq)
  * @tparam OUTRES The type of outgoing responses (either ChargePointReq or CentralSystemReq)
  * @tparam INREQRES Typeclass relating incoming request types to incoming response types
  */
trait IncomingOcppEndpoint[
INREQ <: Req,
OUTRES <: Res,
INREQRES[_ <: INREQ, _ <: OUTRES] <: ReqRes[_, _]
] {


  /**
    * A handler for incoming requests.
    *
    * RequestHandler is a magnet type. You can actually specify:
    *   - a function from INREQ to OUTRES
    *   - a function from INREQ to Future[OUTRES]
    *   - an instance of [[com.thenewmotion.ocpp.messages.ChargePoint]] or [[com.thenewmotion.ocpp.messages.CentralSystem]]
    *   - an instance of [[com.thenewmotion.ocpp.messages.SyncChargePoint]] or [[com.thenewmotion.ocpp.messages.SyncCentralSystem]]
    */
  def requestHandler: RequestHandler[INREQ, OUTRES, INREQRES]

  /**
    * A callback that is called when the connection has been closed
    */
  def onDisconnect(): Unit
}



