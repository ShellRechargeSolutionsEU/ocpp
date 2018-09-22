package com.thenewmotion.ocpp
package json.api

import scala.concurrent.Future
import scala.language.higherKinds
import messages.ReqRes
import messages2.{Request, Response}

import scala.concurrent.ExecutionContext

/** One roles of the OCPP 2.0 communicatino protocol: Charging Station (CS) or
  * Charging Station Management System (CSMS)
  */
// TODO use at a higher level
sealed trait Side
case object Cs extends Side
case object Csms extends Side

trait Ocpp2ConnectionComponent[
  OUTREQBOUND <: Request,
  INRESBOUND <: Response,
  OUTREQRES[_ <: OUTREQBOUND,_ <: INRESBOUND] <: ReqRes[_, _],
  INREQBOUND <: Request,
  OUTRESBOUND <: Response,
  INREQRES[_ <: INREQBOUND, _ <: OUTRESBOUND] <: ReqRes[_, _]
] extends OcppConnectionComponent[OUTREQBOUND, INRESBOUND, OUTREQRES, INREQBOUND, OUTRESBOUND, INREQRES] {

  this: SrpcComponent =>

  implicit val executionContext: ExecutionContext

  trait Ocpp2Connection extends OcppConnection {

    def sendRequest[REQ <: OUTREQBOUND, RES <: INRESBOUND](req: REQ)(implicit reqRes: OUTREQRES[REQ, RES]): Future[RES]
   // def onSrpcCall(req: SrpcCall): Future[SrpcResponse] = ???


    def sendRequestUntyped(req: OUTREQBOUND): Future[INRESBOUND] = ???
  }

  def ocppVersion: Version = Version.V20

  def ocppConnection: Ocpp2Connection

  def onRequest[REQ <: INREQBOUND, RES <: OUTRESBOUND](req: REQ)
    (implicit reqRes: INREQRES[REQ, RES]): Future[RES] = ???
}
