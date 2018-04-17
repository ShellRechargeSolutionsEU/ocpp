package com.thenewmotion.ocpp
package json.api

import org.json4s._
import org.json4s.native.Serialization
import org.slf4j.LoggerFactory

/**
 * The lowest layer in the three-layer protocol stack of OCPP-J: WebSocket
 */
trait WebSocketComponent {
  trait WebSocketConnection {
    /**
     * Send a JSON message to the other party
     */
    def send(msg: JValue): Unit

    /**
     * Hang up the connection to the other party
     *
     * DefaultSrpcComponent#DefaultSrpcConnection calls this method with the
     * monitor lock held, so this method should not block until the connection
     * is fully closed. If it does, that will deadlock the SRPC layer because
     * the onWebSocketDisconnect handler can't complete.
     */
    def close(): Unit
  }

  def webSocketConnection: WebSocketConnection

  /**
   * Called when a new JSON message arrives.
   *
   * To be implemented by children using the WebSocket connectivity. To be called by children implementing the
   * WebSocket connectivity.
   */
  def onMessage(msg: JValue)

  /**
   * Called when a WebSocket error occurs
   * @param e
   */
  def onError(e: Throwable)

  /**
   * Called when the WebSocket connection is disconnected
   */
  def onWebSocketDisconnect(): Unit
}

class DummyWebSocketComponent extends WebSocketComponent {
  private[this] val logger = LoggerFactory.getLogger(DummyWebSocketComponent.this.getClass)

  class DummyWebSocketConnection extends WebSocketConnection {
    def send(msg: JValue) = {
      val string = Serialization.write(msg)(DefaultFormats)
      logger.info(s"Sending $string")
    }

    def close() = {}
  }

  def webSocketConnection = new DummyWebSocketConnection

  def onError(e: Throwable) = logger.info(s"DummyWebSocketComponent received error {}", e)

  def onMessage(jVal: JValue) = logger.info("DummyWebSocketComponent received message {}", jVal)

  def onWebSocketDisconnect(): Unit = {}
}
