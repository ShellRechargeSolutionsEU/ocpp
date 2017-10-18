package com.thenewmotion.ocpp
package json.api

import java.net.URI

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

  def onDisconnect(): Unit = {}
}

class SimpleWebSocketClient(
  chargerId: String,
  uri: URI,
  authPassword: Option[String],
  requestedSubProtocols: Seq[String]
)(implicit sslContext: javax.net.ssl.SSLContext = javax.net.ssl.SSLContext.getDefault) {
  private val logger = LoggerFactory.getLogger(SimpleWebSocketClient.this.getClass)

  import SimpleWebSocketClient._
  import org.java_websocket.client.DefaultSSLWebSocketClientFactory
  import org.java_websocket.client.WebSocketClient
  import org.java_websocket.drafts.Draft_17
  import org.java_websocket.handshake.ServerHandshake

  private val actualUri = uriWithChargerId(uri, chargerId)

  private val headers: java.util.Map[String, String] = List(
    authPassword.map(password => AuthHeader -> s"Basic: ${toBase64String(chargerId, password)}"),
    noneIfEmpty(requestedSubProtocols).map(protocols => SubProtoHeader -> protocols.mkString(","))
  ).flatten.toMap.asJava

  var onWebSocketClose = (_: Int, _: String, _: Boolean) => {}
  var onWebSocketMessage = (_: String) => {}
  var onWebSocketOpen = (_: URI, _: Option[String]) => {}
  var onWebSocketError = (_: Exception) => {}

  private val instance = new WebSocketClient(actualUri, new Draft_17(), headers, 0) {
    def onClose(code: Int, reason: String, remote: Boolean) = onWebSocketClose(code, reason, remote)
    def onMessage(message: String) = onWebSocketMessage(message)
    def onOpen(handshakeData: ServerHandshake) = {
      val subProtocol = handshakeData.getFieldValue(SubProtoHeader)
      onWebSocketOpen(actualUri, noneIfEmpty(subProtocol).map(_.mkString))
    }
    def onError(ex: Exception) = onWebSocketError(ex)
  }

  def send(text: String) = instance.send(text)

  def closeBlocking() = instance.closeBlocking()

  def connect(): Boolean = {
    logger.debug(s"Connecting using uri: $actualUri")
    if (uri.getScheme == "wss") {
      logger.debug(s"Using SSLContext protocol: ${sslContext.getProtocol}")
      instance.setWebSocketFactory(new DefaultSSLWebSocketClientFactory(sslContext))
    }
    instance.connectBlocking()
  }
}

object SimpleWebSocketClient {
  final val AuthHeader = "Authorization"
  final val SubProtoHeader = "Sec-WebSocket-Protocol"
  final val wsSubProtocolForOcppVersion: Map[Version, String] =
    Map(Version.V15 -> "ocpp1.5", Version.V16 -> "ocpp1.6")
  final val ocppVersionForWsSubProtocol = wsSubProtocolForOcppVersion.map(_.swap)

  import org.apache.commons.codec.binary.Base64.encodeBase64String

  private def noneIfEmpty[T](seq: Seq[T]): Option[Seq[T]] =
    if (seq.isEmpty) None else Some(seq)

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

trait SimpleClientWebSocketComponent extends WebSocketComponent {

  import scala.concurrent.Await
  import scala.concurrent.Promise
  import scala.concurrent.duration.DurationInt
  import scala.concurrent.duration.FiniteDuration

  class SimpleClientWebSocketConnection(
    client: SimpleWebSocketClient,
    wsOpenTimeout: FiniteDuration = 10.seconds
  ) extends WebSocketConnection {
    private val logger = LoggerFactory.getLogger(SimpleClientWebSocketConnection.this.getClass)

    private val subProtocolPromise = Promise[Option[String]]()

    client.onWebSocketOpen = (uri: URI, subProtocol: Option[String]) => {
      subProtocolPromise.success(subProtocol)
      logger.debug(s"WebSocket connection opened to $uri, sub protocol: $subProtocol")
    }

    client.onWebSocketMessage = (msg: String) => {
      native.parseJsonOpt(msg) match {
        case None =>
          logger.debug("Received non-JSON message: {}", msg)
        case Some(jVal) =>
          logger.debug("Received JSON message {}", jVal)
          SimpleClientWebSocketComponent.this.onMessage(jVal)
      }
    }

    client.onWebSocketError = (e: Exception) => {
      logger.debug("Received error {}", e)
      SimpleClientWebSocketComponent.this.onError(e)
    }

    client.onWebSocketClose = (_: Int, _: String, _: Boolean) => {
      connected = false
      SimpleClientWebSocketComponent.this.onDisconnect()
    }

    def send(jVal: JValue) = {
      logger.debug("Sending with Java-WebSocket: {}", jVal)
      client.send(native.compactJson(native.renderJValue(jVal)))
    }

    def close() = client.closeBlocking()

    var connected = client.connect() // connect only after setting up the socket event handlers
    logger.debug(s"Created SimpleClientWebSocketConnection, connected = $connected")

    val subProtocol = if (connected) Await.result(subProtocolPromise.future, wsOpenTimeout) else None
  }
}
