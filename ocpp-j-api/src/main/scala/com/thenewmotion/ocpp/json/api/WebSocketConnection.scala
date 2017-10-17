package com.thenewmotion.ocpp
package json.api

import org.java_websocket.drafts.Draft_17
import org.java_websocket.handshake.ServerHandshake
import org.json4s._
import org.json4s.native.Serialization
import org.slf4j.LoggerFactory
import org.java_websocket.client.WebSocketClient
import java.net.URI
import javax.net.ssl.SSLContext

import org.java_websocket.client.DefaultSSLWebSocketClientFactory

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

  import scala.concurrent.duration.FiniteDuration
  import scala.concurrent.duration.DurationInt

  class SimpleClientWebSocketConnection(
    chargerId: String,
    uri: URI,
    authPassword: Option[String],
    openTimeout: FiniteDuration = 10.seconds
  )(implicit sslContext: SSLContext = SSLContext.getDefault) extends WebSocketConnection {

    private[this] val logger = LoggerFactory.getLogger(SimpleClientWebSocketConnection.this.getClass)

    private val actualUri = uriWithChargerId(uri, chargerId)

    private val authHeader: Option[(String, String)] =
      authPassword.map { password =>
        def toBytes = s"$chargerId:".toCharArray.map(_.toByte) ++
          password.sliding(2, 2).map { byteAsHex =>
            Integer.parseInt(byteAsHex, 16).toByte
          }

        import org.apache.commons.codec.binary.Base64.encodeBase64String
        "Authorization" -> s"Basic: ${encodeBase64String(toBytes)}"
      }

    private val headers: java.util.Map[String, String] =
      List(
        Some("Sec-WebSocket-Protocol" -> requestedSubProtocols.mkString(",")),
        authHeader
      ).flatten.toMap.asJava

    import scala.concurrent.Promise
    private val versionPromise = Promise[Option[Version]]()

    private val client = new WebSocketClient(actualUri, new Draft_17(), headers, 0) {
      override def onOpen(handShakeData: ServerHandshake): Unit = {
        import scala.collection.JavaConverters._
        val rfc2616Separators = "[()<>@,;:\"/\\[\\]?={} \t]+"
        val subProtoHeaders = Option(handShakeData.iterateHttpFields())
          .fold[List[String]](List.empty) {
          _.asScala.filter(_ == "Sec-WebSocket-Protocol").toList
        }

        val version = subProtoHeaders.flatMap(_.split(rfc2616Separators))
            .headOption match {
            case Some("ocpp1.5") => Some(Version.V15)
            case Some("ocpp1.6") => Some(Version.V16)
            case _ => None
          }

        versionPromise.success(version)
        logger.debug(s"WebSocket connection opened to $actualUri, version $version")
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

    private def uriWithChargerId(base: URI, chargerId: String): URI = {
      val pathWithChargerId = base.getPath + s"/$chargerId"
      new URI(base.getScheme, base.getUserInfo, base.getHost, base.getPort, pathWithChargerId, base.getQuery,
        base.getFragment)
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

    val negotiatedVersion = scala.concurrent.Await.result(
      versionPromise.future,
      openTimeout
    )

    logger.info(s"Created SimpleClientWebSocketConnection, connected = $connected")
  }
}
