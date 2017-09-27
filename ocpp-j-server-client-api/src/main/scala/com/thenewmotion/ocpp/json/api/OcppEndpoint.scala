package com.thenewmotion.ocpp
package json.api

import messages.{Req, ReqRes, Res}

import scala.concurrent.Future

trait OcppEndpoint[OUTREQ <: Req, INRES <: Res, INREQ <: Req, OUTRES <: Res] {
  def send[REQ <: OUTREQ, RES <: INRES](req: REQ)(implicit reqRes: ReqRes[REQ, RES]): Future[RES]

  // TODO have to offer a user-friendlier alternative that does not have the implicit ReqRes argument
  // perhaps make the library user supply a ChargePoint/CentralSystem implementation instead? Or actually an asynchronous version of those traits...
  def onRequest[REQ <: INREQ, RES <: OUTRES](req: REQ)(implicit reqRes: ReqRes[REQ, RES]): Future[RES]

  def onError(error: OcppError): Unit

  def onDisconnect(): Unit
}
