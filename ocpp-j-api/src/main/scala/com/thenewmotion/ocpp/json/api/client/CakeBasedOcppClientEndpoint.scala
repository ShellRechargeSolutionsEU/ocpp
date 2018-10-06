package com.thenewmotion.ocpp
package json
package api
package client

import messages.v1x._


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
trait CakeBasedOcppClientEndpoint
  extends OutgoingOcppEndpoint[CentralSystemReq, CentralSystemRes, CentralSystemReqRes] {

  def send[REQ <: CentralSystemReq, RES <: CentralSystemRes](req: REQ)(
    implicit reqRes: CentralSystemReqRes[REQ, RES]
  ): Future[RES] =
    connection.sendRequest(req)

  def close(): Future[Unit] = connection.close()

  def requestHandler: RequestHandler[ChargePointReq, ChargePointRes, ChargePointReqRes]

  protected val connection: ConnectionCake

  def onClose: Future[Unit] = connection.onClose

  protected trait ConnectionCake {
    self: OcppConnectionComponent[
      CentralSystemReq,
      CentralSystemRes,
      CentralSystemReqRes,
      ChargePointReq,
      ChargePointRes,
      ChargePointReqRes
    ] with SrpcComponent with WebSocketComponent =>

    def ocppVersion: Version

    final def sendRequest[REQ <: CentralSystemReq, RES <: CentralSystemRes](req: REQ)(
      implicit reqRes: CentralSystemReqRes[REQ, RES]
    ): Future[RES] =
      ocppConnection.sendRequest(req)

    final def close(): Future[Unit] = srpcConnection.close()

    final def onClose: Future[Unit] = srpcConnection.onClose

    final def onRequest[REQ <: ChargePointReq, RES <: ChargePointRes](req: REQ)(
      implicit reqRes: ChargePointReqRes[REQ, RES]
    ): Future[RES] =
      requestHandler.apply(req)

    final implicit val executionContext: ExecutionContext =
      CakeBasedOcppClientEndpoint.this.ec
  }

  protected val ec: ExecutionContext
}
