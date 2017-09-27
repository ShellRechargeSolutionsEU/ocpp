package com.thenewmotion.ocpp.json.api

import com.thenewmotion.ocpp.Version
import com.thenewmotion.ocpp.messages.{Req, ReqRes, Res}

import scala.concurrent.Future

trait OcppEndpoint[OUTREQ <: Req, INRES <: Res, INREQ <: Req, OUTRES <: Res] {
  def send[REQ <: OUTREQ, RES <: INRES](req: REQ)(implicit reqRes: ReqRes[REQ, RES], version:Version): Future[RES]

  def onRequest[REQ <: INREQ, RES <: OUTRES](req: REQ)(implicit reqRes: ReqRes[REQ, RES], version:Version): Future[RES]

  def onError(error: OcppError): Unit

  def onDisconnect(): Unit
}
