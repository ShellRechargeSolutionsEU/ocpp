package com.thenewmotion.ocpp

import scala.language.higherKinds
import scala.concurrent.{Future, ExecutionContext}

package object messages {
  type IdTag = String

  trait RequestHandler[INREQ <: Req, OUTRES <: Res, RR[_ <: INREQ, _ <: OUTRES] <: ReqRes[_, _]] {
    def apply[REQ <: INREQ, RES <: OUTRES](req: REQ)(implicit reqRes: RR[REQ, RES], ec: ExecutionContext): Future[RES]
  }
}
