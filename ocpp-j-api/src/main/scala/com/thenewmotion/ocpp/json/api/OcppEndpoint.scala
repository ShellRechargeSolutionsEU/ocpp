package com.thenewmotion.ocpp
package json.api

import scala.language.higherKinds
import scala.concurrent.{Future, ExecutionContext}
import messages._

/**
 * Generic interface of an OCPP connection endpoint as it appears to the
 * library user:
 *
 *  * The send method should be called to send outgoing requests
 *  * The close method should be called to close the connection
 *  * onRequest should be overridden to handle incoming requests
 *  * onDisconnect should be overridden to handle a disconnection
 *  * onError should be overridden to handle OCPP errors sent by the other
 *    party
 *
 * @tparam OUTREQ The type of outgoing requests (either ChargePointReq or CentralSystemReq)
 * @tparam INRES The type of incoming responses (either ChargePointRes or CentralSystemRes)
 * @tparam OUTREQRES Typeclass relating outgoing request types to incoming response types
 * @tparam INREQ The type of incoming requests (either CentralSystemReq or ChargePointReq)
 * @tparam OUTRES The type of outgoing responses (either CentralSystemRes or ChargePointRes)
 * @tparam INREQRES Typeclass relating incoming request types to outgoing response types
 */
trait OcppEndpoint[
  OUTREQ <: Req,
  INRES <: Res,
  OUTREQRES[_ <: OUTREQ, _ <: INRES] <: ReqRes[_, _],
  INREQ <: Req,
  OUTRES <: Res,
  INREQRES[_ <: INREQ, _ <: OUTRES] <: ReqRes[_, _]
] {

  /**
   * Send a request to the party at the other side of this connection
   *
   * @param req The request to send
   * @param reqRes Evidence of the request-response relationship of the REQ and RES types
   * @tparam REQ The type of request (e.g. BootNotificationReq, ResetReq, ...)
   * @tparam RES The type of response (e.g. BootNotificationRes, ResetRes, ...)
   * @return A future that will be completed with the response from the other
   *         side. If the other side fails to respond, the future will be failed.
   */
  def send[REQ <: OUTREQ, RES <: INRES](req: REQ)(implicit reqRes: OUTREQRES[REQ, RES]): Future[RES]

  /**
   * Close the connection
   */
  def close(): Unit

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

trait CakeBasedOcppEndpoint[
  OUTREQ <: Req,
  INRES <: Res,
  OUTREQRES[_ <: OUTREQ, _ <: INRES] <: ReqRes[_, _],
  INREQ <: Req,
  OUTRES <: Res,
  INREQRES[_ <: INREQ, _ <: OUTRES] <: ReqRes[_, _]
] extends OcppEndpoint[
  OUTREQ,
  INRES,
  OUTREQRES,
  INREQ,
  OUTRES,
  INREQRES
]  {
  def send[REQ <: OUTREQ, RES <: INRES](req: REQ)(implicit reqRes: OUTREQRES[REQ, RES]): Future[RES] =
    connectionCake.sendRequest(req)

  def close(): Unit = connectionCake.close()

  def requestHandler: RequestHandler[INREQ, OUTRES, INREQRES]

  protected val connectionCake: ConnectionCake

  protected trait ConnectionCake {
    self: OcppConnectionComponent[OUTREQ, INRES, OUTREQRES, INREQ, OUTRES, INREQRES]
      with SrpcComponent
      with WebSocketComponent =>

    final def sendRequest[REQ <: OUTREQ, RES <: INRES](req: REQ)(implicit reqRes: OUTREQRES[REQ, RES]): Future[RES] =
      ocppConnection.sendRequest(req)

    final def close(): Unit = webSocketConnection.close()

    final def onRequest[REQ <: INREQ, RES <: OUTRES](req: REQ)(
      implicit reqRes: INREQRES[REQ, RES]
    ): Future[RES] =
      requestHandler.apply(req)

    final def onOcppError(error: OcppError): Unit =
      CakeBasedOcppEndpoint.this.onError(error)

    final def onDisconnect(): Unit =
      CakeBasedOcppEndpoint.this.onDisconnect()

    final implicit val executionContext: ExecutionContext =
      CakeBasedOcppEndpoint.this.ec
  }

  protected val ec: ExecutionContext

  def onDisconnect(): Unit

  def onError(error: OcppError): Unit
}


/**
 * Library interface for the Charge Point side.
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
trait CakeBasedChargePointEndpoint extends CakeBasedOcppEndpoint[
  CentralSystemReq,
  CentralSystemRes,
  CentralSystemReqRes,
  ChargePointReq,
  ChargePointRes,
  ChargePointReqRes
]

/**
 * Library interface for the Central System side.
 *
 * There will be one instance of CakeBasedCentralSystemEndpoint for each charge
 * point connected to the Central System.
 *
 * The library user has to provide:
 *   * A [[RequestHandler]], probably an instance of
 *     [[com.thenewmotion.ocpp.messages.CentralSystem]], which will
 *     handle incoming requests
 *   * Implementations of onError and onDisconnect to handle these events
 *
 * The trait provides the library user with:
 *   * A send method to send requests to the Charge Point
 *   * A close method to close the connection
 */
trait CakeBasedCentralSystemEndpoint extends CakeBasedOcppEndpoint[
  ChargePointReq,
  ChargePointRes,
  ChargePointReqRes,
  CentralSystemReq,
  CentralSystemRes,
  CentralSystemReqRes
]
