package com.thenewmotion.ocpp
package json.api
package client

import java.net.URI
import javax.net.ssl.SSLContext
import scala.concurrent.ExecutionContext

/**
 * An OCPP-J client implemented using Java-WebSocket.
 *
 * In OCPP-J, it is the Charge Point that is the client. So this client is
 * intended for use in charge point simulators (or charge point firmwares, if
 * there is someone writing those in Scala :-)).
 *
 * This class is to be used by instantiating your own subclass of it, which
 * should provide a handler for incoming requests, a handler for errors, and a
 * disconnection handler as explained at [[IncomingOcppEndpoint]].
 *
 * @param chargerId The charge point identity of the charge point for which you
 *                  want to set up a connection
 * @param centralSystemUri The endpoint URI of the Central System to connect to.
 * @param versions A list of requested OCPP versions in order of preference e.g:
 * Seq(Version.V16, Version.V15)
 * @param authPassword The Basic Auth password to use, hex-encoded
 */
abstract class OcppJsonClient(
  chargerId: String,
  centralSystemUri: URI,
  versions: Seq[Version],
  authPassword: Option[String] = None
)(implicit val ec: ExecutionContext,
  sslContext: SSLContext = SSLContext.getDefault
) extends CakeBasedOcppClientEndpoint {

  val connection: ConnectionCake = new ConnectionCake
    with ChargePointOcppConnectionComponent
    with DefaultSrpcComponent
    with SimpleClientWebSocketComponent {

    import OcppJsonClient._
    import SimpleClientWebSocketComponent._

    val webSocketConnection = new SimpleClientWebSocketConnection(
      chargerId,
      centralSystemUri,
      authPassword,
      requestedSubProtocols = versions.map { version =>
        wsSubProtocolForOcppVersion.getOrElse(
          version,
          throw VersionNotSupported(
            "JSON client only supports versions: ",
            supportedVersions = ocppVersionForWsSubProtocol.values
          )
        )
      }
    )

    val ocppVersion = ocppVersionForWsSubProtocol.getOrElse(
      webSocketConnection.subProtocol,
      throw new RuntimeException(s"Unknown protocol ${webSocketConnection.subProtocol} in use for connection")
    )

    val srpcConnection = new DefaultSrpcConnection
    val ocppConnection = defaultChargePointOcppConnection
  }
}

object OcppJsonClient {
  case class VersionNotSupported(msg: String, supportedVersions: Iterable[Version])
    extends RuntimeException(msg + supportedVersions.mkString(", "))
}
