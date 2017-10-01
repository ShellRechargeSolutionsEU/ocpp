package com.thenewmotion.ocpp.json.api

import com.thenewmotion.ocpp.messages._

import scala.language.{higherKinds, implicitConversions}
import scala.concurrent.{Future, ExecutionContext}

/**
 * The "magnet type" to allow people to specify the handler of incoming request
 * in different ways. See http://spray.io/blog/2012-12-13-the-magnet-pattern/
 * for a description of the magnet pattern.
 *
 * @tparam REQBOUND The supertype of all handleable requests (either ChargePointReq or CentralSystemReq)
 * @tparam RESBOUND The supertype of all responses that the handler can produce (either ChargePointRes or CentralSystemRes)
 * @tparam REQRES The typeclass linking request and response types for the same operation (either ChargePointReqRes or CentralSystemReqRes)
 */
trait RequestHandler[REQBOUND <: Req, RESBOUND <: Res, REQRES[_ <: REQBOUND, _ <: RESBOUND] <: ReqRes[_, _]] {

  def apply[REQ <: REQBOUND, RES <: RESBOUND](req: REQ)(
    implicit reqRes: REQRES[REQ, RES],
    ec: ExecutionContext
  ): Future[RES]
}

object RequestHandler {

  implicit def asyncFunctionAsChargePointRequestHandler(
    f: ChargePointReq => Future[ChargePointRes]
  ): RequestHandler[ChargePointReq, ChargePointRes, ChargePointReqRes] =
    new RequestHandler[ChargePointReq, ChargePointRes, ChargePointReqRes] {
      def apply[REQ <: ChargePointReq, RES <: ChargePointRes](req: REQ)(
        implicit reqRes: ChargePointReqRes[REQ, RES],
        ec: ExecutionContext
      ): Future[RES] = f(req).map(_.asInstanceOf[RES])
    }

  implicit def asyncFunctionAsCentralSystemRequestHandler(
    f: CentralSystemReq => Future[CentralSystemRes]
  ): RequestHandler[CentralSystemReq, CentralSystemRes, CentralSystemReqRes] =
    new RequestHandler[CentralSystemReq, CentralSystemRes, CentralSystemReqRes] {
      def apply[REQ <: CentralSystemReq, RES <: CentralSystemRes](req: REQ)(
        implicit reqRes: CentralSystemReqRes[REQ, RES],
        ec: ExecutionContext
      ): Future[RES] = f(req).map(_.asInstanceOf[RES])
    }

  implicit def syncFunctionAsChargePointRequestHandler(
    f: ChargePointReq => ChargePointRes
  ): RequestHandler[ChargePointReq, ChargePointRes, ChargePointReqRes] =
    new RequestHandler[ChargePointReq, ChargePointRes, ChargePointReqRes] {
      def apply[REQ <: ChargePointReq, RES <: ChargePointRes](req: REQ)(
        implicit reqRes: ChargePointReqRes[REQ, RES],
        ec: ExecutionContext
      ): Future[RES] = Future.successful(f(req).asInstanceOf[RES])
    }

  implicit def syncFunctionAsCentralSystemRequestHandler(
    f: CentralSystemReq => CentralSystemRes
  ): RequestHandler[CentralSystemReq, CentralSystemRes, CentralSystemReqRes] =
    new RequestHandler[CentralSystemReq, CentralSystemRes, CentralSystemReqRes] {
      def apply[REQ <: CentralSystemReq, RES <: CentralSystemRes](req: REQ)(
        implicit reqRes: CentralSystemReqRes[REQ, RES],
        ec: ExecutionContext
      ): Future[RES] = Future.successful(f(req).asInstanceOf[RES])
    }

  implicit def chargePointAsChargePointRequestHandler(
    cp: ChargePoint
  ): RequestHandler[ChargePointReq, ChargePointRes, ChargePointReqRes] =
    new RequestHandler[ChargePointReq, ChargePointRes, ChargePointReqRes] {
      def apply[REQ <: ChargePointReq, RES <: ChargePointRes](req: REQ)(
        implicit reqRes: ChargePointReqRes[REQ, RES],
        ec: ExecutionContext
      ): Future[RES] = cp.apply(req)
    }

  implicit def centralSystemAsCentralSystemRequestHandler(
    cp: CentralSystem
  ): RequestHandler[CentralSystemReq, CentralSystemRes, CentralSystemReqRes] =
    new RequestHandler[CentralSystemReq, CentralSystemRes, CentralSystemReqRes] {
      def apply[REQ <: CentralSystemReq, RES <: CentralSystemRes](req: REQ)(
        implicit reqRes: CentralSystemReqRes[REQ, RES],
        ec: ExecutionContext
      ): Future[RES] = cp.apply(req)
    }

  implicit def syncChargePointAsChargePointRequestHandler(
    cp: SyncChargePoint
  ): RequestHandler[ChargePointReq, ChargePointRes, ChargePointReqRes] =
    new RequestHandler[ChargePointReq, ChargePointRes, ChargePointReqRes] {
      def apply[REQ <: ChargePointReq, RES <: ChargePointRes](req: REQ)(
        implicit reqRes: ChargePointReqRes[REQ, RES],
        ec: ExecutionContext
      ): Future[RES] = Future.successful(cp.apply(req))
    }

  implicit def syncCentralSystemAsCentralSystemRequestHandler(
    cs: SyncCentralSystem
  ): RequestHandler[CentralSystemReq, CentralSystemRes, CentralSystemReqRes] =
    new RequestHandler[CentralSystemReq, CentralSystemRes, CentralSystemReqRes] {
      def apply[REQ <: CentralSystemReq, RES <: CentralSystemRes](req: REQ)(
        implicit reqRes: CentralSystemReqRes[REQ, RES],
        ec: ExecutionContext
      ): Future[RES] = Future.successful(cs.apply(req))
    }
}
