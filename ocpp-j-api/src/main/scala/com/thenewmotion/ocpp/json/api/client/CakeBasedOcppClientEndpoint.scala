package com.thenewmotion.ocpp.json.api.client

import com.thenewmotion.ocpp.Version
import com.thenewmotion.ocpp.json.api._
import com.thenewmotion.ocpp.messages._

import scala.concurrent.{ExecutionContext, Future}

/**
  * Library interface for the client (Charge Point) side.
  *
  * The library user has to provide:
  *   * A [[RequestHandler]], probably an instance of
  *     [[com.thenewmotion.ocpp.messages.ChargePoint]], which will handle
  *     incoming requests
  *   * Implementations of onError and onDisconnect to handle these events
  *
  * The trait provides the library user with:
  *   * A send method to send requests to the Central System
  *   * A close method to close the connection
  */
trait CakeBasedOcppClientEndpoint
  extends OutgoingOcppEndpoint[CentralSystemReq, CentralSystemRes, CentralSystemReqRes]
  with IncomingOcppEndpoint[ChargePointReq, ChargePointRes, ChargePointReqRes] {

  def send[REQ <: CentralSystemReq, RES <: CentralSystemRes](req: REQ)(
    implicit reqRes: CentralSystemReqRes[REQ, RES]
  ): Future[RES] =
    connection.sendRequest(req)

  def close(): Unit = connection.close()

  def requestHandler: RequestHandler[ChargePointReq, ChargePointRes, ChargePointReqRes]

  protected val connection: ConnectionCake

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

    final def close(): Unit = webSocketConnection.close()

    final def onRequest[REQ <: ChargePointReq, RES <: ChargePointRes](req: REQ)(
      implicit reqRes: ChargePointReqRes[REQ, RES]
    ): Future[RES] =
      requestHandler.apply(req)

    final def onOcppError(error: OcppError): Unit =
      CakeBasedOcppClientEndpoint.this.onError(error)

    final def onDisconnect(): Unit =
      CakeBasedOcppClientEndpoint.this.onDisconnect()

    final implicit val executionContext: ExecutionContext =
      CakeBasedOcppClientEndpoint.this.ec
  }

  protected val ec: ExecutionContext

  def onDisconnect(): Unit

  def onError(error: OcppError): Unit
}
