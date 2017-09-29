package com.thenewmotion.ocpp.json.api

import org.json4s.JValue
import org.slf4j.LoggerFactory
import com.thenewmotion.ocpp.json.{TransportMessage, TransportMessageParser}

/**
 * The middle layer in the three-layer protocol stack of OCPP-J: Simple Remote
 * Procedure Call.
 *
 * The SRPC layer relates WebSocket messages to each other as requests,
 * responses and error reports.
 *
 * Although the OCPP 1.6 specification no longer uses the term "SRPC", it is
 * present in the original specification on
 * http://www.gir.fr/ocppjs/ocpp_srpc_spec.shtml, which is referenced by the
 * IANA WebSocket Subprotocol Name Registry at
 * https://www.iana.org/assignments/websocket/websocket.xml. So bureaucracy is
 * on our side in naming this.
 */
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

  def onMessage(jval: JValue): Unit =
    onSrpcMessage(TransportMessageParser.parse(jval))

  def onError(ex: Throwable): Unit =
    logger.error("WebSocket error", ex)
}


