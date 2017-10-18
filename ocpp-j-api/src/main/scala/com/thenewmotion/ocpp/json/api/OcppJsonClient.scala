package com.thenewmotion.ocpp
package json
package api

import java.net.URI

import scala.concurrent.ExecutionContext
import javax.net.ssl.SSLContext

/**
 * An OCPP-J client implemented using Java-WebSocket.
 *
 * In OCPP-J, it is the Charge Point that is the client. So this client is
 * intended for use in charge point simulators (or charge point firmwares, if
 * there is someone writing those in Scala :-)).
 *
 * This class is to be used by instantiating your own subclass of it, which
 * should provide a handler for incoming requests, a handler for errors, and a
 * disconnection handler as explained at [[OcppEndpoint]].
 *
 * @param chargerId The charge point identity of the charge point for which you
 *                  want to set up a connection
 * @param centralSystemUri The endpoint URI of the Central System to connect to.
 * @param versions A list of requested OCPP versions in order of preference e.g:
 * List(Version.V16, Version.V15)
 * @param authPassword The Basic Auth password to use, hex-encoded
 */
abstract class OcppJsonClient(
  chargerId: String,
  centralSystemUri: URI,
  versions: List[Version],
  authPassword: Option[String] = None
)(implicit val ec: ExecutionContext,
  sslContext: SSLContext = SSLContext.getDefault
) extends CakeBasedChargePointEndpoint {

  import SimpleWebSocketClient._

  val simpleWebSocketClient = new SimpleWebSocketClient(
    chargerId,
    centralSystemUri,
    authPassword,
    versions.map(wsSubProtocolForOcppVersion)
  )

  val connection: ConnectionCake = new ConnectionCake
    with ChargePointOcppConnectionComponent
    with DefaultSrpcComponent
    with SimpleClientWebSocketComponent {

    lazy val webSocketConnection = new SimpleClientWebSocketConnection(simpleWebSocketClient)

    val subProtocol = webSocketConnection.subProtocol.getOrElse(
      throw new RuntimeException(s"Server does not support requested versions: ${versions.mkString(",")}")
    )

    lazy val ocppVersion = ocppVersionForWsSubProtocol.getOrElse(
      subProtocol,
      throw new RuntimeException(s"Client does not support requested versions: ${versions.mkString(",")}")
    )

    lazy val ocppConnection = defaultChargePointOcppConnection
    lazy val srpcConnection = new DefaultSrpcConnection
  }
}
