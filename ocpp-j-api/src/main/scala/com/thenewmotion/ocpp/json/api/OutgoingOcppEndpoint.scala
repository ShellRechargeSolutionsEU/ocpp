package com.thenewmotion.ocpp
package json.api

import scala.language.higherKinds
import scala.concurrent.Future
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

