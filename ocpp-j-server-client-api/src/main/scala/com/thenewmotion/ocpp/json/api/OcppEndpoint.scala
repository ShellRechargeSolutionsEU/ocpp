package com.thenewmotion.ocpp
package json.api

import messages._

import scala.concurrent.{Future, ExecutionContext}

/**
 * Generic interface of an OCPP connection endpoint as it appears to the
 * library user:
 *
 *  * The send method sends outgoing requests over the OCPP connection
 *  * onRequest should be implemented to handle incoming requests
 *  * onDisconnect should be implemented to handle a disconnection
 *  * onError should be implemented to handle errors
 *
 * The signature of the onRequest method is a bit awkward to implement. See the
 * [[ChargePointEndpoint]] and [[CentralSystemEndpoint]] traits for extensions
 * of this trait that make it possible to handle incoming requests in a nicer
 * way.
 *
 * @tparam OUTREQ The type of outgoing requests (either ChargePointReq or CentralSystemReq)
 * @tparam INRES The type of incoming responses (either ChargePointRes or CentralSystemRes)
 * @tparam INREQ The type of incoming requests (either CentralSystemReq or ChargePointReq)
 * @tparam OUTRES The type of outgoing responses (either CentralSystemRes or ChargePointRes)
 */
trait GenericOcppEndpoint[OUTREQ <: Req, INRES <: Res, INREQ <: Req, OUTRES <: Res] {
  def send[REQ <: OUTREQ, RES <: INRES](req: REQ)(implicit reqRes: ReqRes[REQ, RES]): Future[RES]

  def onRequest[REQ <: INREQ, RES <: OUTRES](req: REQ)(implicit reqRes: ReqRes[REQ, RES]): Future[RES]

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
trait ChargePointEndpoint extends GenericOcppEndpoint[CentralSystemReq, CentralSystemRes, ChargePointReq, ChargePointRes] {
  def send[REQ <: CentralSystemReq, RES <: CentralSystemRes](req: REQ)(implicit reqRes: ReqRes[REQ, RES]): Future[RES]

  protected def requestHandler: RequestHandler[ChargePointReq, ChargePointRes, ChargePointReqRes]

  def onDisconnect(): Unit

  def onError(error: OcppError): Unit

  final def onRequest[REQ <: ChargePointReq, RES <: ChargePointRes](req: REQ)(implicit reqRes: ReqRes[REQ, RES]): Future[RES] =
    reqRes match {
      case r: ChargePointReqRes[REQ, RES] =>
        requestHandler.apply(req)(r, ec)
      case _ =>
        sys.error("Impossible: received Central System request in Charge Point")
    }

  protected implicit val ec: ExecutionContext
}

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
trait CentralSystemEndpoint extends GenericOcppEndpoint[ChargePointReq, ChargePointRes, CentralSystemReq, CentralSystemRes] {
  def send[REQ <: ChargePointReq, RES <: ChargePointRes](req: REQ)(implicit reqRes: ReqRes[REQ, RES]): Future[RES]

  protected def requestHandler: RequestHandler[CentralSystemReq, CentralSystemRes, CentralSystemReqRes]

  def onDisconnect(): Unit

  def onError(error: OcppError): Unit

  final def onRequest[REQ <: CentralSystemReq, RES <: CentralSystemRes](req: REQ)(implicit reqRes: ReqRes[REQ, RES]): Future[RES] =
    reqRes match {
      case r: CentralSystemReqRes[REQ, RES] =>
        requestHandler.apply(req)(r, ec)
      case _ =>
        sys.error("Impossible: received Central System request in Charge Point")
    }

  protected implicit val ec: ExecutionContext
}
