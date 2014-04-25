package com.thenewmotion.ocpp.json

import org.json4s.JValue
import com.typesafe.scalalogging.slf4j.Logging

trait SrpcComponent {
  trait SrpcConnection {
    def send(msg: TransportMessage)
  }

  def srpcConnection: SrpcConnection

  def onSrpcMessage(msg: TransportMessage)
}

trait DefaultSrpcComponent extends SrpcComponent with Logging {
  this: WebSocketComponent =>

  class DefaultSrpcConnection extends SrpcConnection {
    def send(msg: TransportMessage) {
      webSocketConnection.send(TransportMessageParser.writeJValue(msg))
    }
  }

  def onMessage(jval: JValue) = onSrpcMessage(TransportMessageParser.parse(jval))

  def onError(ex: Throwable) = logger.error("WebSocket error", ex)
}


