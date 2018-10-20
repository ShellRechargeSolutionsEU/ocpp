package com.thenewmotion.ocpp
package json
package api

import scala.language.higherKinds
import scala.concurrent.Future
import messages.{Request, Response}

/**
 * The highest layer in the three-layer protocol stack of OCPP-J: OCPP message
 * exchange.
 *
 * When mixed into something that is also a SimpleRPC component, can do OCPP
 * request-response exchanges using the SimpleRPC connection.
 *
 * @tparam OUTREQBOUND Supertype of outgoing requests (either ChargePointReq or CentralSystemReq)
 * @tparam INRESBOUND Supertype of incoming responses (either ChargePointRes or CentralSystemRes)
 * @tparam OUTREQRES Typeclass relating the types of incoming requests and outgoing responses
 * @tparam INREQBOUND Supertype of incoming requests (either ChargePointReq or CentralSystemReq)
 * @tparam OUTRESBOUND Supertype of outgoing responses (either ChargePointRes or CentralSystemRes)
 * @tparam INREQRES Typeclass relating the types of outgoing requests and incoming responses
 */
trait OcppConnectionComponent[
  OUTREQBOUND <: Request,
  INRESBOUND <: Response,
  OUTREQRES[_ <: OUTREQBOUND, _ <: INRESBOUND],
  INREQBOUND <: Request,
  OUTRESBOUND <: Response,
  INREQRES[_ <: INREQBOUND, _  <: OUTRESBOUND]
] {

  this: SrpcComponent =>

  trait OcppConnection {
    /** Send an outgoing OCPP request */
    def sendRequest[REQ <: OUTREQBOUND, RES <: INRESBOUND](req: REQ)(implicit reqRes: OUTREQRES[REQ, RES]): Future[RES]

    /**
     * Alternative to sendRequest that allows you to write code that processes
     * requests without knowing what type they are exactly.
     *
     * Downside is that it will not know which response you're getting back.
     * You'll have to pattern match on it somewhere to do something meaningful
     * with it.
     */
    def sendRequestUntyped(req: OUTREQBOUND): Future[INRESBOUND]
  }

  def ocppVersion: Version

  def ocppConnection: OcppConnection

  def onRequest[REQ <: INREQBOUND, RES <: OUTRESBOUND](req: REQ)(implicit reqRes: INREQRES[REQ, RES]): Future[RES]
}

// for now, we don't support the 'details' field of OCPP-J error messages
case class OcppError(error: PayloadErrorCode, description: String)
case class OcppException(ocppError: OcppError) extends Exception(s"${ocppError.error}: ${ocppError.description}")
object OcppException {
  def apply(error: PayloadErrorCode, description: String): OcppException =
    OcppException(OcppError(error, description))
}

