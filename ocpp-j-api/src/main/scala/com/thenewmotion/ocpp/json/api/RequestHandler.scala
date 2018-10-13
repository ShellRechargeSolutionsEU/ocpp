package com.thenewmotion.ocpp
package json
package api

import messages.{ReqRes, Request, Response}
import messages.v1x._
import messages.v20._

import scala.language.{higherKinds, implicitConversions}
import scala.concurrent.{ExecutionContext, Future}

/**
 * The "magnet type" to allow people to specify the handler of incoming request
 * in different ways. See http://spray.io/blog/2012-12-13-the-magnet-pattern/
 * for a description of the magnet pattern.
 *
 * @tparam REQBOUND The supertype of all handleable requests (either ChargePointReq or CentralSystemReq)
 * @tparam RESBOUND The supertype of all responses that the handler can produce (either ChargePointRes or CentralSystemRes)
 * @tparam REQRES The typeclass linking request and response types for the same operation (either ChargePointReqRes or CentralSystemReqRes)
 */
trait RequestHandler[REQBOUND <: Request, RESBOUND <: Response, REQRES[_ <: REQBOUND, _ <: RESBOUND] <: ReqRes[_, _]] {

  def apply[REQ <: REQBOUND, RES <: RESBOUND](req: REQ)(
    implicit reqRes: REQRES[REQ, RES],
    ec: ExecutionContext
  ): Future[RES]
}

object RequestHandler {

  implicit def fromChargePointAsyncFunction(
    f: ChargePointReq => Future[ChargePointRes]
  ): RequestHandler[ChargePointReq, ChargePointRes, ChargePointReqRes] =
    new RequestHandler[ChargePointReq, ChargePointRes, ChargePointReqRes] {
      def apply[REQ <: ChargePointReq, RES <: ChargePointRes](req: REQ)(
        implicit reqRes: ChargePointReqRes[REQ, RES],
        ec: ExecutionContext
      ): Future[RES] = f(req).map(_.asInstanceOf[RES])
    }

  implicit def fromCentralSystemAsyncFunction(
    f: CentralSystemReq => Future[CentralSystemRes]
  ): RequestHandler[CentralSystemReq, CentralSystemRes, CentralSystemReqRes] =
    new RequestHandler[CentralSystemReq, CentralSystemRes, CentralSystemReqRes] {
      def apply[REQ <: CentralSystemReq, RES <: CentralSystemRes](req: REQ)(
        implicit reqRes: CentralSystemReqRes[REQ, RES],
        ec: ExecutionContext
      ): Future[RES] = f(req).map(_.asInstanceOf[RES])
    }

  implicit def fromChargePointSyncFunction(
    f: ChargePointReq => ChargePointRes
  ): RequestHandler[ChargePointReq, ChargePointRes, ChargePointReqRes] =
    new RequestHandler[ChargePointReq, ChargePointRes, ChargePointReqRes] {
      def apply[REQ <: ChargePointReq, RES <: ChargePointRes](req: REQ)(
        implicit reqRes: ChargePointReqRes[REQ, RES],
        ec: ExecutionContext
      ): Future[RES] = Future(f(req).asInstanceOf[RES])
    }

  implicit def fromCentralSystemSyncFunction(
    f: CentralSystemReq => CentralSystemRes
  ): RequestHandler[CentralSystemReq, CentralSystemRes, CentralSystemReqRes] =
    new RequestHandler[CentralSystemReq, CentralSystemRes, CentralSystemReqRes] {
      def apply[REQ <: CentralSystemReq, RES <: CentralSystemRes](req: REQ)(
        implicit reqRes: CentralSystemReqRes[REQ, RES],
        ec: ExecutionContext
      ): Future[RES] = Future(f(req).asInstanceOf[RES])
    }

  implicit def fromChargePoint(
    cp: ChargePoint
  ): RequestHandler[ChargePointReq, ChargePointRes, ChargePointReqRes] =
    new RequestHandler[ChargePointReq, ChargePointRes, ChargePointReqRes] {
      def apply[REQ <: ChargePointReq, RES <: ChargePointRes](req: REQ)(
        implicit reqRes: ChargePointReqRes[REQ, RES],
        ec: ExecutionContext
      ): Future[RES] = cp.apply(req)
    }

  implicit def fromCentralSystem(
    cp: CentralSystem
  ): RequestHandler[CentralSystemReq, CentralSystemRes, CentralSystemReqRes] =
    new RequestHandler[CentralSystemReq, CentralSystemRes, CentralSystemReqRes] {
      def apply[REQ <: CentralSystemReq, RES <: CentralSystemRes](req: REQ)(
        implicit reqRes: CentralSystemReqRes[REQ, RES],
        ec: ExecutionContext
      ): Future[RES] = cp.apply(req)
    }

  implicit def fromSyncChargePoint(
    cp: SyncChargePoint
  ): RequestHandler[ChargePointReq, ChargePointRes, ChargePointReqRes] =
    new RequestHandler[ChargePointReq, ChargePointRes, ChargePointReqRes] {
      def apply[REQ <: ChargePointReq, RES <: ChargePointRes](req: REQ)(
        implicit reqRes: ChargePointReqRes[REQ, RES],
        ec: ExecutionContext
      ): Future[RES] = Future(cp.apply(req))
    }

  implicit def fromSyncCentralSystem(
    cs: SyncCentralSystem
  ): RequestHandler[CentralSystemReq, CentralSystemRes, CentralSystemReqRes] =
    new RequestHandler[CentralSystemReq, CentralSystemRes, CentralSystemReqRes] {
      def apply[REQ <: CentralSystemReq, RES <: CentralSystemRes](req: REQ)(
        implicit reqRes: CentralSystemReqRes[REQ, RES],
        ec: ExecutionContext
      ): Future[RES] = Future(cs.apply(req))
    }

  implicit def fromCsSyncFunction(f: CsRequest => CsResponse): RequestHandler[CsRequest, CsResponse, CsReqRes] =
    new RequestHandler[CsRequest, CsResponse, CsReqRes] {
      def apply[REQ <: CsRequest, RES <: CsResponse](req: REQ)(
        implicit reqRes: CsReqRes[REQ, RES],
        ec: ExecutionContext
      ): Future[RES] = Future(f(req).asInstanceOf[RES])
    }

  implicit def fromCsmsSyncFunction(
    f: CsmsRequest => CsmsResponse
  ): RequestHandler[CsmsRequest, CsmsResponse, CsmsReqRes] =
    new RequestHandler[CsmsRequest, CsmsResponse, CsmsReqRes] {
      def apply[REQ <: CsmsRequest, RES <: CsmsResponse](req: REQ)(
        implicit reqRes: CsmsReqRes[REQ, RES],
        ec: ExecutionContext
      ): Future[RES] = Future(f(req).asInstanceOf[RES])
    }
}
