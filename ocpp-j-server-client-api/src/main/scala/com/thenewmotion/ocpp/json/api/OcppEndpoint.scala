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
  def send[REQ <: OUTREQ, RES <: INRES](req: REQ)(implicit reqRes: OUTREQRES[REQ, RES]): Future[RES]

  def close(): Unit

  def requestHandler: RequestHandler[INREQ, OUTRES, INREQRES]

  def onDisconnect(): Unit

  def onError(error: OcppError): Unit
}

trait CakeBasedOcppEndpoint[
  OUTREQ <: Req,
  INRES <: Res,
  OUTREQRES[_ <: OUTREQ, _ <: INRES] <: ReqRes[_, _],
  INREQ <: Req,
  OUTRES <: Res,
  INREQRES[_ <: INREQ, _ <: OUTRES] <: ReqRes[_, _]
] {
  def send[REQ <: OUTREQ, RES <: INRES](req: REQ)(implicit reqRes: OUTREQRES[REQ, RES]): Future[RES] =
    connectionCake.sendRequest(req)

  def close(): Unit = connectionCake.close()

  def requestHandler: RequestHandler[INREQ, OUTRES, INREQRES]

  protected val connectionCake: ConnectionCake

  protected trait ConnectionCake {
    self: OcppConnectionComponent[OUTREQ, INRES, OUTREQRES, INREQ, OUTRES, INREQRES]
      with SrpcComponent
      with WebSocketComponent =>

    def sendRequest[REQ <: OUTREQ, RES <: INRES](req: REQ)(implicit reqRes: OUTREQRES[REQ, RES]): Future[RES] =
      ocppConnection.sendRequest(req)

    def close(): Unit = webSocketConnection.close()

    final def onRequest[REQ <: INREQ, RES <: OUTRES](req: REQ)(
      implicit reqRes: INREQRES[REQ, RES]
    ): Future[RES] =
      requestHandler.apply(req)

    def onOcppError(error: OcppError): Unit =
      CakeBasedOcppEndpoint.this.onError(error)

    def onDisconnect(): Unit =
      CakeBasedOcppEndpoint.this.onDisconnect()
  }

  protected implicit val ec: ExecutionContext

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
 *
 * The "ec" member is to be overridden by library classes offering more concrete
 * implementations of an endpoint and not by library users themselves.
 */
trait ChargePointEndpoint extends OcppEndpoint[
  CentralSystemReq,
  CentralSystemRes,
  CentralSystemReqRes,
  ChargePointReq,
  ChargePointRes,
  ChargePointReqRes
]

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
 * There will be one instance of CentralSystemEndpoint for each charge point
 * connected to the Central System.
 *
 * The library user has to provide:
 *   * A [[RequestHandler]], probably an instance of
 *     [[com.thenewmotion.ocpp.messages.CentralSystem]], which will
 *     handle incoming requests
 *   * Implementations of onError and onDisconnect to handle these events
 *
 * The trait provides the library user with:
 *   * A send method to send requests to the Charge Point
 *
 * The "ec" member is to be overridden by library classes offering more concrete
 * implementations of an endpoint and not by library users themselves.
 */
trait CentralSystemEndpoint extends OcppEndpoint[
  ChargePointReq,
  ChargePointRes,
  ChargePointReqRes,
  CentralSystemReq,
  CentralSystemRes,
  CentralSystemReqRes
]

trait CakeBasedCentralSystemEndpoint extends CakeBasedOcppEndpoint[
  ChargePointReq,
  ChargePointRes,
  ChargePointReqRes,
  CentralSystemReq,
  CentralSystemRes,
  CentralSystemReqRes
]
