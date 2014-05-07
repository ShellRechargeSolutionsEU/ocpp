package com.thenewmotion.ocpp.json

import java.net.URI
import com.thenewmotion.ocpp.messages._
import scala.concurrent.Future
import io.backchat.hookup.HookupClientConfig

abstract class OcppJsonClient(chargerId: String, centralSystemUri: URI)
  extends OcppEndpoint[CentralSystemReq, CentralSystemRes, ChargePointReq, ChargePointRes] {

  private[this] val ocppStack = new ChargePointOcppConnectionComponent with DefaultSrpcComponent with HookupClientWebSocketComponent {
    val webSocketConnection = new HookupClientWebSocketConnection(chargerId, hookupClientConfig)
    val srpcConnection = new DefaultSrpcConnection
    val ocppConnection = new ChargePointOcppConnection

    override def onRequest(req: ChargePointReq): Future[ChargePointRes] =
      OcppJsonClient.this.onRequest(req)

    override def onOcppError(error: OcppError): Unit = OcppJsonClient.this.onError(error)

    override def onDisconnect(): Unit = OcppJsonClient.this.onDisconnect
  }

  def send[REQ <: CentralSystemReq, RES <: CentralSystemRes](req: REQ)(implicit reqRes: ReqRes[REQ, RES]): Future[RES] =
    ocppStack.ocppConnection.sendRequest(req)

  def close() = ocppStack.webSocketConnection.close()

  /**
   * Override this to supply your own configuration for the Hookup WebSocket client
   *
   * The server URL is part of the config, and put in here in the default implementation. So usually when overriding
   * this method you will want to call super.hookupClientConfig.copy(...) to add in your own settings but leave the
   * right server URL in place.
   *
   * @return
   */
  protected def hookupClientConfig: HookupClientConfig = new HookupClientConfig(uri = centralSystemUri)
}
