package com.thenewmotion.ocpp.json

import org.json4s.JValue
import org.slf4j.LoggerFactory

trait SrpcComponent {
  trait SrpcConnection {
    def send(msg: TransportMessage)
  }

  def srpcConnection: SrpcConnection

  def onSrpcMessage(msg: TransportMessage)
}

trait DefaultSrpcComponent extends SrpcComponent {
  this: WebSocketComponent =>

  private[this] val logger = LoggerFactory.getLogger(DefaultSrpcComponent.this.getClass)

  class DefaultSrpcConnection extends SrpcConnection {
    def send(msg: TransportMessage) {
      webSocketConnection.send(TransportMessageParser.writeJValue(msg))
    }
  }

  def onMessage(jval: JValue) = onSrpcMessage(TransportMessageParser.parse(jval))

  def onError(ex: Throwable) = logger.error("WebSocket error", ex)
}


