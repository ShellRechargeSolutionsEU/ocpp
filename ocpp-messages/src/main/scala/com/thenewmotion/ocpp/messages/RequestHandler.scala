package com.thenewmotion.ocpp
package messages

import scala.language.higherKinds
import scala.concurrent.{ExecutionContext, Future}

trait RequestHandler[INREQ <: Req, OUTRES <: Res, REQRES[_ <: INREQ, _ <: OUTRES] <: ReqRes[_, _]] {

  def apply[REQ <: INREQ, RES <: OUTRES](req: REQ)(
    implicit reqRes: REQRES[REQ, RES],
    ec: ExecutionContext
  ): Future[RES]
}
