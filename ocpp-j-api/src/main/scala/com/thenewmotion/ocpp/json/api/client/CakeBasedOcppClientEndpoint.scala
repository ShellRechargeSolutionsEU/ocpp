package com.thenewmotion.ocpp
package json
package api
package client

import scala.language.higherKinds
import VersionFamily.{CsMessageTypesForVersionFamily, CsmsMessageTypesForVersionFamily}
import messages.{ReqRes, Request, Response}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Library interface for the client (Charge Point) side.
  *
  * The library user has to provide:
  *   * A [[RequestHandler]], probably an instance of
  *     [[com.thenewmotion.ocpp.messages.v1x.ChargePoint]], which will handle
  *     incoming requests
  *
  * The trait provides the library user with:
  *   * A send method to send requests to the Central System
  *   * A close method to close the connection
  *   * An onClose method to get a future to be notified when the connection
  *     closes
  */
abstract class CakeBasedOcppClientEndpoint[
  VFam <: VersionFamily,
  OUTREQBOUND <: Request,
  INRESBOUND <: Response,
  OUTREQRES[_ <: OUTREQBOUND, _ <: INRESBOUND] <: ReqRes[_, _],
  INREQBOUND <: Request,
  OUTRESBOUND <: Response,
  INREQRES[_ <: INREQBOUND, _ <: OUTRESBOUND] <: ReqRes[_, _]
](
  implicit val csmsMessageTypeForVersionFamily: CsmsMessageTypesForVersionFamily[VFam, OUTREQBOUND, INRESBOUND, OUTREQRES],
  val csMessageTypeForVersionFamily: CsMessageTypesForVersionFamily[VFam, INREQBOUND, OUTRESBOUND, INREQRES]
) extends OutgoingOcppEndpoint[OUTREQBOUND, INRESBOUND, OUTREQRES] {

  def send[
    REQ <: OUTREQBOUND,
    RES <: INRESBOUND](req: REQ)(
    implicit reqRes: OUTREQRES[REQ, RES]
  ): Future[RES] =
    connection.sendRequest(req)

  def close(): Future[Unit] = connection.close()

  def requestHandler: RequestHandler[INREQBOUND, OUTRESBOUND, INREQRES]

  protected val connection: ConnectionCake

  def onClose: Future[Unit] = connection.onClose

  protected trait ConnectionCake {
    self: OcppConnectionComponent[
      OUTREQBOUND,
      INRESBOUND,
      OUTREQRES,
      INREQBOUND,
      OUTRESBOUND,
      INREQRES
    ] with SrpcComponent with WebSocketComponent =>

    def ocppVersion: Version

    final def sendRequest[REQ <: OUTREQBOUND, RES <: INRESBOUND](req: REQ)(
      implicit reqRes: OUTREQRES[REQ, RES]
    ): Future[RES] =
      ocppConnection.sendRequest(req)

    final def close(): Future[Unit] = srpcConnection.close()

    final def onClose: Future[Unit] = srpcConnection.onClose

    final def onRequest[REQ <: INREQBOUND, RES <: OUTRESBOUND](req: REQ)(
      implicit reqRes: INREQRES[REQ, RES]
    ): Future[RES] =
      requestHandler.apply(req)

    final implicit val executionContext: ExecutionContext =
      CakeBasedOcppClientEndpoint.this.ec
  }

  protected val ec: ExecutionContext
}
