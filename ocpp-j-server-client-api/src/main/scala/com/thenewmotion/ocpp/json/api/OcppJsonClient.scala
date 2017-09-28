package com.thenewmotion.ocpp.json.api

import java.net.URI

import com.thenewmotion.ocpp.messages._

import scala.concurrent.{Future, ExecutionContext}
import javax.net.ssl.SSLContext

import com.thenewmotion.ocpp.Version

abstract class OcppJsonClient(
    chargerId: String,
    centralSystemUri: URI,
    authPassword: Option[String],
    version: Version)(implicit sslContext: SSLContext = SSLContext.getDefault)
  extends ChargePointEndpoint {

  // TODO OCPP-layer-specific, should go to OcppConnectionComponent
  val subProtocol = version match {
    case Version.V15 => "ocpp1.5"
    case Version.V16 => "ocpp1.6"
  }
  private[this] val ocppStack = new ChargePointOcppConnectionComponent with DefaultSrpcComponent with SimpleClientWebSocketComponent {
    // TODO this should give us back a negotiated WebSocket subprotocol later on,
    // which we then use to determine the negotiated OCPP version to initialize the ChargePointOcppConnection
    val webSocketConnection = new SimpleClientWebSocketConnection(
      chargerId,
      centralSystemUri,
      authPassword,
      List(subProtocol)
    )

    val srpcConnection = new DefaultSrpcConnection
    val ocppConnection = defaultChargePointOcppConnection(version)

    override def onRequest[REQ <: ChargePointReq, RES <: ChargePointRes](req: REQ)(implicit reqRes: ReqRes[REQ, RES]): Future[RES] =
      OcppJsonClient.this.onRequest(req)

    override def onOcppError(error: OcppError): Unit = OcppJsonClient.this.onError(error)

    override def onDisconnect(): Unit = OcppJsonClient.this.onDisconnect()
  }

  def send[REQ <: CentralSystemReq, RES <: CentralSystemRes](req: REQ)(implicit reqRes: ReqRes[REQ, RES]): Future[RES] =
    ocppStack.ocppConnection.sendRequest(req)

  def close() = ocppStack.webSocketConnection.close()

  protected implicit val ec: ExecutionContext =
    ExecutionContext.Implicits.global
}
