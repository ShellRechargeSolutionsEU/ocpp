package com.thenewmotion.ocpp
package json.api
package client

import java.net.URI
import scala.collection.JavaConverters._
import scala.concurrent.{Await, Promise}
import scala.concurrent.duration.{Duration, DurationInt, FiniteDuration}
import org.java_websocket.WebSocket
import org.java_websocket.exceptions.InvalidDataException
import org.java_websocket.extensions.IExtension
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.protocols.{IProtocol, Protocol}
import org.json4s._
import org.slf4j.LoggerFactory
import SimpleClientWebSocketComponent._

trait SimpleClientWebSocketComponent extends WebSocketComponent {

  class SimpleClientWebSocketConnection(
    chargerId: String,
    uri: URI,
    authPassword: Option[String],
    requestedSubProtocols: Seq[String],
    wsOpenTimeout: FiniteDuration = 10.seconds
  )(implicit sslContext: javax.net.ssl.SSLContext = javax.net.ssl.SSLContext.getDefault)
    extends WebSocketConnection {
    private val logger = LoggerFactory.getLogger(SimpleClientWebSocketConnection.this.getClass)

    private val actualUri = uriWithChargerId(uri, chargerId)

    private val headers: java.util.Map[String, String] =
      authPassword.map(password => authHeader -> s"Basic ${toBase64String(chargerId, password)}")
       .toMap.asJava

    import org.java_websocket.client.WebSocketClient
    import org.java_websocket.drafts.Draft_6455
    import org.java_websocket.handshake.ServerHandshake

    private val subProtocolPromise = Promise[String]()
    private val connectedPromise = Promise[Either[Exception, Unit]]()

    val draftWithRightSubprotocols = new Draft_6455(
      List.empty[IExtension].asJava,
      requestedSubProtocols.map(new Protocol(_): IProtocol).asJava
    )

    protected val client = new WebSocketClient(actualUri, draftWithRightSubprotocols, headers, 0) {

      override def onWebsocketHandshakeReceivedAsClient(
        conn: WebSocket,
        request: ClientHandshake,
        response: ServerHandshake
      ): Unit = {
        val subProtocol = noneIfEmpty(response.getFieldValue(subProtoHeader)).map(_.mkString)
        logger.debug("Received subprotocol offer from server: {}", subProtocol)
        subProtocol match {
          case Some(proto) if requestedSubProtocols.contains(proto) =>
            subProtocolPromise.success(proto)
            ()
          case _ =>
            throw new InvalidDataException(
              1007,
              s"Server using unrequested subprotocol ${subProtocol.getOrElse("<none>")}"
            )
        }
      }

      def onOpen(handshakeData: ServerHandshake): Unit = {
        logger.debug(s"WebSocket connection opened to $actualUri, sub protocol: $subProtocol")
        connectedPromise.success(Right(()))
        ()
      }

      def onMessage(message: String): Unit = {
        native.parseJsonOpt(message) match {
          case None =>
            logger.debug("Received non-JSON message: {}", message)
          case Some(jVal) =>
            logger.debug("Received JSON message {}", jVal)
            SimpleClientWebSocketComponent.this.onMessage(jVal)
        }
      }

      def onError(ex: Exception): Unit = {
        logger.debug("Received WebSocket error {}", ex)

        if (!failToConnect(ex) && connectedSuccessfully)
          SimpleClientWebSocketComponent.this.onError(ex)
      }

      def onClose(code: Int, reason: String, remote: Boolean): Unit = {
        logger.debug(
          "WebSocket connection closed: code {}, remote {}, reason \"{}\"",
          code.asInstanceOf[Object],
          remote.asInstanceOf[Object],
          reason
        )
        lazy val openingException = WebSocketErrorWhenOpeningException(code, reason, remote)

        if (!failToConnect(openingException) && connectedSuccessfully)
          SimpleClientWebSocketComponent.this.onWebSocketDisconnect()
      }

      private def failToConnect(ex: Exception): Boolean =
        connectedPromise.trySuccess(Left(ex))

      private def connectedSuccessfully: Boolean =
        Await.result(connectedPromise.future, Duration.Inf).isRight
    }

    def send(jVal: JValue): Unit = {
      logger.debug("Sending with Java-WebSocket: {}", jVal)
      client.send(native.compactJson(native.renderJValue(jVal)))
    }

    def close(): Unit = client.close()

    private def connect(): Unit = {
      logger.debug(s"Connecting using uri: $actualUri")
      if (uri.getScheme == "wss") {
        logger.debug(s"Using SSLContext protocol: ${sslContext.getProtocol}")
        client.setSocketFactory(sslContext.getSocketFactory)
      }
      client.connect()
    }

    connect() // connect only after setting up the socket event handlers
    Await.result(connectedPromise.future, wsOpenTimeout) match {
      case Left(ex)  => throw ex
      case Right(()) =>
    }

    logger.debug("Connected to {}", actualUri)

    val subProtocol: String = Await.result(subProtocolPromise.future, wsOpenTimeout)
  }
}

object SimpleClientWebSocketComponent {
  final val authHeader = "Authorization"

  final val subProtoHeader = "Sec-WebSocket-Protocol"

  final val wsSubProtocolForOcppVersion: Map[Version, String] = Map(
    Version.V15 -> "ocpp1.5",
    Version.V16 -> "ocpp1.6",
    Version.V20 -> "ocpp2.0"
  )

  final val ocppVersionForWsSubProtocol = wsSubProtocolForOcppVersion.map(_.swap)

  private def noneIfEmpty[T](seq: Seq[T]): Option[Seq[T]] =
    if (seq.isEmpty) None else Some(seq)

  private[api] def toBase64String(chargerId: String, password: String) = {
    def toBytes = s"$chargerId:".toCharArray.map(_.toByte) ++
      password.grouped(2).map { byteAsHex =>
        Integer.parseInt(byteAsHex, 16).toByte
      }

    java.util.Base64.getEncoder.encodeToString(toBytes)
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

  case class WebSocketErrorWhenOpeningException(code: Int, reason: String, remote: Boolean) extends
    RuntimeException(
      "WebSocket disconnected unexpectedly while handshaking: code = " + code +
      ", remote = " + remote + ", reason = \"" + reason + "\""
    )
}
