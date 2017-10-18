package com.thenewmotion.ocpp
package json.api

import java.net.URI
import javax.net.ssl.SSLContext

import org.java_websocket.client.DefaultSSLWebSocketClientFactory
import org.java_websocket.client.WebSocketClient
import org.java_websocket.drafts.Draft_17
import org.java_websocket.handshake.ServerHandshake
import org.json4s._
import org.json4s.native.Serialization
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

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
  def onDisconnect(): Unit

  /**
   * Called when connecting, to get a list of supported subprotocols
   */
  def requestedSubProtocols: List[String]
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

  def onMessage(jval: JValue) = logger.info("DummyWebSocketComponent received message {}", jval)

  def onDisconnect(): Unit = {}

  def requestedSubProtocols = List()
}

trait SimpleClientWebSocketComponent extends WebSocketComponent {

  import scala.concurrent.Await
  import scala.concurrent.Promise
  import scala.concurrent.duration.DurationInt
  import scala.concurrent.duration.FiniteDuration

  class SimpleClientWebSocketConnection(
    chargerId: String,
    uri: URI,
    authPassword: Option[String],
    openTimeout: FiniteDuration = 10.seconds
  )(implicit sslContext: SSLContext = SSLContext.getDefault) extends WebSocketConnection {

    private[this] val logger = LoggerFactory.getLogger(SimpleClientWebSocketConnection.this.getClass)

    import SimpleClientWebSocketConnection._

    private val actualUri = uriWithChargerId(uri, chargerId)

    private val headers: java.util.Map[String, String] = List(
      authPassword.map(password => AuthHeader -> s"Basic: ${toBase64String(chargerId, password)}"),
      noneIfEmpty(requestedSubProtocols).map(protocols => SubProtoHeader -> protocols.mkString(","))
    ).flatten.toMap.asJava

    private val subProtocolPromise = Promise[Option[String]]()

    private val client = new WebSocketClient(actualUri, new Draft_17(), headers, 0) {
      override def onOpen(serverHandshake: ServerHandshake): Unit = {
        val subProtocol = Option(serverHandshake.getFieldValue(SubProtoHeader))
        subProtocolPromise.success(subProtocol)
        logger.debug(s"WebSocket connection opened to $actualUri, sub protocol: $subProtocol")
      }

      override def onMessage(msg: String): Unit = {
        native.parseJsonOpt(msg) match {
          case None =>
            logger.debug("Received non-JSON message: {}", msg)
          case Some(jval) =>
            logger.debug("Received JSON message {}", jval)
            SimpleClientWebSocketComponent.this.onMessage(jval)
        }
      }

      override def onError(e: Exception): Unit = {
        logger.debug("Received error {}", e)
        SimpleClientWebSocketComponent.this.onError(e)
      }

      override def onClose(code: Int, reason: String, remote: Boolean): Unit =
        SimpleClientWebSocketComponent.this.onDisconnect()
    }

    def send(jval: JValue) = {
      logger.debug("Sending with Java-WebSocket: {}", jval)
      client.send(native.compactJson(native.renderJValue(jval)))
    }

    def close() = client.closeBlocking()

    private def connect() = {
      if (uri.getScheme == "wss") {
        logger.info(s"Using SSLContext protocol: ${sslContext.getProtocol}")
        client.setWebSocketFactory(new DefaultSSLWebSocketClientFactory(sslContext))
      }
      client.connectBlocking()
    }

    val connected = connect()
    logger.info(s"Created SimpleClientWebSocketConnection, connected = $connected")

    val subProtocol = Await.result(subProtocolPromise.future, openTimeout)
  }

  object SimpleClientWebSocketConnection {
    final val AuthHeader = "Authorization"
    final val SubProtoHeader = "Sec-WebSocket-Protocol"

    import org.apache.commons.codec.binary.Base64.encodeBase64String

    private def noneIfEmpty[T](list: List[T]): Option[List[T]] =
      if (list.isEmpty) None else Some(list)

    private def toBase64String(chargerId: String, password: String) = {
      def toBytes = s"$chargerId:".toCharArray.map(_.toByte) ++
        password.sliding(2, 2).map { byteAsHex =>
          Integer.parseInt(byteAsHex, 16).toByte
        }
      encodeBase64String(toBytes)
    }

    private def uriWithChargerId(base: URI, chargerId: String): URI =
      new URI(
        base.getScheme,
        base.getUserInfo,
        base.getHost,
        base.getPort,
        base.getPath + s"/$chargerId",
        base.getQuery,
        base.getFragment
      )
  }
}
