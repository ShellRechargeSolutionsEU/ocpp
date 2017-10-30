package com.thenewmotion.ocpp
package json.api

import scala.language.higherKinds
import scala.concurrent.{Future, ExecutionContext}
import messages._

/**
 * Generic interface of an outgoing OCPP connection endpoint as it appears to the
 * library user:
 *
 *  * The send method should be called to send outgoing requests
 *  * The close method should be called to close the connection
 *
 * @tparam OUTREQ The type of outgoing requests (either ChargePointReq or CentralSystemReq)
 * @tparam INRES The type of incoming responses (either ChargePointRes or CentralSystemRes)
 * @tparam OUTREQRES Typeclass relating outgoing request types to incoming response types
 */
trait OutgoingOcppEndpoint[
  OUTREQ <: Req,
  INRES <: Res,
  OUTREQRES[_ <: OUTREQ, _ <: INRES] <: ReqRes[_, _]
] {

  /**
    * Send a request to the party at the other side of this connection
    *
    * @param req    The request to send
    * @param reqRes Evidence of the request-response relationship of the REQ and RES types
    * @tparam REQ The type of request (e.g. BootNotificationReq, ResetReq, ...)
    * @tparam RES The type of response (e.g. BootNotificationRes, ResetRes, ...)
    * @return A future that will be completed with the response from the other
    *         side. If the other side fails to respond, the future will be failed.
    */
  def send[REQ <: OUTREQ, RES <: INRES](req: REQ)(implicit reqRes: OUTREQRES[REQ, RES]): Future[RES]

  /**
    * Close the connection
    *
    * This method is synchronous: when it returns, the connection has been closed.
    */
  def close(): Unit
}

/**
 * Generic interface of an incoming OCPP connection endpoint that can be
 * implemented by the library user to handle incoming OCPP requests.
 *
 *  * onRequest should be overridden to handle incoming requests
 *  * onDisconnect should be overridden to handle a disconnection
 *  * onError should be overridden to handle OCPP errors sent by the other
 *    party that do not reference a request we sent before
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

  /**
   * A callback that is called when an OCPP error is received which does not
   * relate to a request that was sent to the send method. If an error is
   * received about a specific request, it will be reported by failing the
   * result Future for that request instead.
   *
   * @param error
   */
  def onError(error: OcppError): Unit
}

/**
  * Library interface for the client (Charge Point) side.
  *
  * The library user has to provide:
  *   * A [[RequestHandler]], probably an instance of
  *     [[com.thenewmotion.ocpp.messages.ChargePoint]], which will handle
  *     incoming requests
  *   * Implementations of onError and onDisconnect to handle these events
  *
  * The trait provides the library user with:
  *   * A send method to send requests to the Central System
  *   * A close method to close the connection
  */
trait CakeBasedOcppClientEndpoint
  extends OutgoingOcppEndpoint[CentralSystemReq, CentralSystemRes, CentralSystemReqRes]
  with IncomingOcppEndpoint[ChargePointReq, ChargePointRes, ChargePointReqRes] {

  def send[REQ <: CentralSystemReq, RES <: CentralSystemRes](req: REQ)(
    implicit reqRes: CentralSystemReqRes[REQ, RES]
  ): Future[RES] =
    connection.sendRequest(req)

  def close(): Unit = connection.close()

  def requestHandler: RequestHandler[ChargePointReq, ChargePointRes, ChargePointReqRes]

  protected val connection: ConnectionCake

  protected trait ConnectionCake {
    self: OcppConnectionComponent[
      CentralSystemReq,
      CentralSystemRes,
      CentralSystemReqRes,
      ChargePointReq,
      ChargePointRes,
      ChargePointReqRes
    ] with SrpcComponent with WebSocketComponent =>

    def ocppVersion: Version

    final def sendRequest[REQ <: CentralSystemReq, RES <: CentralSystemRes](req: REQ)(
      implicit reqRes: CentralSystemReqRes[REQ, RES]
    ): Future[RES] =
      ocppConnection.sendRequest(req)

    final def close(): Unit = webSocketConnection.close()

    final def onRequest[REQ <: ChargePointReq, RES <: ChargePointRes](req: REQ)(
      implicit reqRes: ChargePointReqRes[REQ, RES]
    ): Future[RES] =
      requestHandler.apply(req)

    final def onOcppError(error: OcppError): Unit =
      CakeBasedOcppClientEndpoint.this.onError(error)

    final def onDisconnect(): Unit =
      CakeBasedOcppClientEndpoint.this.onDisconnect()

    final implicit val executionContext: ExecutionContext =
      CakeBasedOcppClientEndpoint.this.ec
  }

  protected val ec: ExecutionContext

  def onDisconnect(): Unit

  def onError(error: OcppError): Unit
}

