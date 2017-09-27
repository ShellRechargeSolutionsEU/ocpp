package com.thenewmotion.ocpp.json.api

import java.net.URI

import com.thenewmotion.ocpp.messages._

import scala.concurrent.Future
import javax.net.ssl.SSLContext

import com.thenewmotion.ocpp.Version

abstract class OcppJsonClient(
    chargerId: String,
    centralSystemUri: URI,
    authPassword: Option[String],
  // TODO give Version argument instead of String
    ocppProtocols: List[String])(implicit sslContext: SSLContext = SSLContext.getDefault)
  extends OcppEndpoint[CentralSystemReq, CentralSystemRes, ChargePointReq, ChargePointRes] {

  implicit val version = ocppProtocols.head match {
    case "ocpp1.2" => Version.V12
    case "ocpp1.5" => Version.V15
    case "ocpp1.6" => Version.V16
  }
  private[this] val ocppStack = new ChargePointOcppConnectionComponent with DefaultSrpcComponent with SimpleClientWebSocketComponent {
    val webSocketConnection = new SimpleClientWebSocketConnection(
      chargerId,
      centralSystemUri,
      authPassword,
      ocppProtocols
    )
    val srpcConnection = new DefaultSrpcConnection
    val ocppConnection = new ChargePointOcppConnection(version)

    override def onRequest[REQ <: ChargePointReq, RES <: ChargePointRes](req: REQ)(implicit reqRes: ReqRes[REQ, RES], version:Version): Future[RES] =
      OcppJsonClient.this.onRequest(req)

    override def onOcppError(error: OcppError): Unit = OcppJsonClient.this.onError(error)

    override def onDisconnect(): Unit = OcppJsonClient.this.onDisconnect()
  }

  def send[REQ <: CentralSystemReq, RES <: CentralSystemRes](req: REQ)(implicit reqRes: ReqRes[REQ, RES], version:Version): Future[RES] =
    ocppStack.ocppConnection.sendRequest(req)

  def close() = ocppStack.webSocketConnection.close()
}
