package com.thenewmotion.ocpp.json

import org.java_websocket.drafts.Draft_17
import org.java_websocket.handshake.ServerHandshake
import org.json4s._
import org.json4s.native.Serialization
import org.slf4j.LoggerFactory
import org.java_websocket.client.WebSocketClient
import java.net.URI
import scala.collection.JavaConverters._

trait WebSocketComponent {
  trait WebSocketConnection {
    /**
     * Send a JSON message to the other party
     */
    def send(msg: JValue): Unit

    /**
     * Hang up the connection to the other party
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
  def onDisconnect(): Unit = {}
}

class DummyWebSocketComponent extends WebSocketComponent {

  private[this] val logger = LoggerFactory.getLogger(DummyWebSocketComponent.this.getClass)

  class MockWebSocketConnection extends WebSocketConnection {
    def send(msg: JValue) = {
      val string = Serialization.write(msg)(DefaultFormats)
      logger.info(s"Sending $string")
    }

    def close() = {}
  }

  def webSocketConnection = new MockWebSocketConnection

  def onError(e: Throwable) = logger.info(s"DummyWebSocketComponent received error {}", e)

  def onMessage(jval: JValue) = logger.info("DummyWebSocketComponent received message {}", jval)
}

trait SimpleClientWebSocketComponent extends WebSocketComponent {

  private val ocppProtocol = "ocpp1.5"

  class SimpleClientWebSocketConnection(chargerId: String, uri: URI) extends WebSocketConnection {

    private[this] val logger = LoggerFactory.getLogger(SimpleClientWebSocketConnection.this.getClass)

    private val actualUri = uriWithChargerId(uri, chargerId)

    private val headers = Map("Sec-WebSocket-Protocol" -> ocppProtocol).asJava

    private val client = new WebSocketClient(actualUri, new Draft_17(), headers, 0) {

      override def onOpen(h: ServerHandshake): Unit =
        logger.debug("WebSocket connection opened to {}", actualUri)

      override def onMessage(msg: String): Unit = {
        native.parseJsonOpt(msg) match {
          case None =>
            logger.debug("Received non-JSON message: {}", msg)
          case Some(jval) =>
            logger.debug("Received JSON message {}", jval)
            SimpleClientWebSocketComponent.this.onMessage(jval)
        }
      }

      override def onError(e: Exception) = {
        logger.debug("Received error {}", e)
        SimpleClientWebSocketComponent.this.onError(e)
      }

      override def onClose(code: Int, reason: String, remote: Boolean): Unit =
        SimpleClientWebSocketComponent.this.onDisconnect()
    }

    private def uriWithChargerId(base: URI, chargerId: String): URI = {
      val pathWithChargerId = base.getPath + s"/$chargerId"
      new URI(base.getScheme, base.getUserInfo, base.getHost, base.getPort, pathWithChargerId, base.getQuery,
        base.getFragment)
    }

    def send(jval: JValue) = {
      logger.debug("Sending with Hookup: {}", jval)
      client.send(native.compactJson(native.renderJValue(jval)))
    }

    def close() = client.closeBlocking()

    val connected = client.connectBlocking()
    logger.info(s"Created SimpleClientWebSocketConnection, connected = $connected")
  }
}
