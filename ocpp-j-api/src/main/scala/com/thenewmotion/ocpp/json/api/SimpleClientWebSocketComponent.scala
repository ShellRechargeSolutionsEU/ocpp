package com.thenewmotion.ocpp
package json.api

import java.net.URI

import org.json4s._
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

trait SimpleClientWebSocketComponent extends WebSocketComponent {

  import scala.concurrent.Await
  import scala.concurrent.Promise
  import scala.concurrent.duration.DurationInt
  import scala.concurrent.duration.FiniteDuration

  class SimpleClientWebSocketConnection(
    chargerId: String,
    uri: URI,
    authPassword: Option[String],
    requestedSubProtocols: Seq[String],
    wsOpenTimeout: FiniteDuration = 10.seconds
  )(implicit sslContext: javax.net.ssl.SSLContext = javax.net.ssl.SSLContext.getDefault)
    extends WebSocketConnection {
    private val logger = LoggerFactory.getLogger(SimpleClientWebSocketConnection.this.getClass)

    import SimpleClientWebSocketComponent._

    private val actualUri = uriWithChargerId(uri, chargerId)

    private val headers: java.util.Map[String, String] = List(
      authPassword.map(password => AuthHeader -> s"Basic: ${toBase64String(chargerId, password)}"),
      noneIfEmpty(requestedSubProtocols).map(protocols => SubProtoHeader -> protocols.mkString(","))
    ).flatten.toMap.asJava

    import org.java_websocket.client.DefaultSSLWebSocketClientFactory
    import org.java_websocket.client.WebSocketClient
    import org.java_websocket.drafts.Draft_17
    import org.java_websocket.handshake.ServerHandshake

    private val subProtocolPromise = Promise[Option[String]]()

    protected val client = new WebSocketClient(actualUri, new Draft_17(), headers, 0) {
      def onOpen(handshakeData: ServerHandshake) = {
        val subProtocol = noneIfEmpty(handshakeData.getFieldValue(SubProtoHeader)).map(_.mkString)
        subProtocolPromise.success(subProtocol)
        logger.debug(s"WebSocket connection opened to $actualUri, sub protocol: $subProtocol")

      }

      def onMessage(message: String) = {
        native.parseJsonOpt(message) match {
          case None =>
            logger.debug("Received non-JSON message: {}", message)
          case Some(jVal) =>
            logger.debug("Received JSON message {}", jVal)
            SimpleClientWebSocketComponent.this.onMessage(jVal)
        }
      }

      def onError(ex: Exception) = {
        logger.debug("Received error {}", ex)
        SimpleClientWebSocketComponent.this.onError(ex)
      }

      def onClose(code: Int, reason: String, remote: Boolean) = {
        SimpleClientWebSocketComponent.this.onDisconnect()
      }
    }

    def send(jVal: JValue) = {
      logger.debug("Sending with Java-WebSocket: {}", jVal)
      client.send(native.compactJson(native.renderJValue(jVal)))
    }

    def close() = client.closeBlocking()

    def connect(): Boolean = {
      logger.debug(s"Connecting using uri: $actualUri")
      if (uri.getScheme == "wss") {
        logger.debug(s"Using SSLContext protocol: ${sslContext.getProtocol}")
        client.setWebSocketFactory(new DefaultSSLWebSocketClientFactory(sslContext))
      }
      client.connectBlocking()
    }

    private val connected = connect() // connect only after setting up the socket event handlers
    logger.debug(s"Created SimpleClientWebSocketConnection, connected = $connected")

    val subProtocol = if (connected) Await.result(subProtocolPromise.future, wsOpenTimeout) else None
  }
}

object SimpleClientWebSocketComponent {
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
