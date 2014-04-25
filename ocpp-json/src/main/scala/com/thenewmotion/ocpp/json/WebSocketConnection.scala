package com.thenewmotion.ocpp.json

import org.json4s._
import org.json4s.native.Serialization
import com.typesafe.scalalogging.slf4j.Logging
import java.net.URI
import io.backchat.hookup._
import io.backchat.hookup.HookupClient.Receive
import io.backchat.hookup.{HookupClientConfig, JsonMessage, TextMessage, Connected}

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

class DummyWebSocketComponent extends WebSocketComponent with Logging {

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

trait HookupClientWebSocketComponent extends WebSocketComponent {

  private val ocppProtocol = "ocpp1.5"

  class HookupClientWebSocketConnection(chargerId: String, config: HookupClientConfig) extends WebSocketConnection with Logging {

    private val hookupClientConfig = HookupClientConfig(uri = uriWithChargerId(config.uri, chargerId))

    private val client = new DefaultHookupClient(hookupClientConfig) {
      def receive: Receive = {
        case Connected => logger.debug("WebSocket connection connected to {}", hookupClientConfig.uri)
        case Disconnected(_) => onDisconnect()
        case JsonMessage(jval) =>
          logger.debug("Received JSON message {}", jval)
          onMessage(jval)
        case TextMessage(txt) =>
          logger.debug("Received non-JSON message \"{}\"", txt)
          onError(new Exception(s"Invalid JSON received: $txt"))
        case e@Error(maybeEx) =>
          logger.debug("Received error {}", e)
          val exception = maybeEx getOrElse new Exception("WebSocket error received without more information")
          onError(exception)
      }
    }

    private def uriWithChargerId(base: URI, chargerId: String): URI = {
      val pathWithChargerId = base.getPath + s"/$chargerId"
      new URI(base.getScheme, base.getUserInfo, base.getHost, base.getPort, pathWithChargerId, base.getQuery,
        base.getFragment)
    }

    def send(jval: JValue) = {
      logger.debug("Sending with Hookup: {}", jval)
      client.send(jval)
    }

    def close() = client.close()

    client.connect(ocppProtocol)
  }
}
