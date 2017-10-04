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
 * @param version The OCPP version to use (either Version.V15 or Version.V16)
 * @param authPassword The Basic Auth password to use, hex-encoded
 */
abstract class OcppJsonClient(
  chargerId: String,
  centralSystemUri: URI,
  version: Version,
  authPassword: Option[String] = None
)(implicit sslContext: SSLContext = SSLContext.getDefault)
  extends CakeBasedChargePointEndpoint {

  protected implicit val ec: ExecutionContext =
    ExecutionContext.Implicits.global

  val connectionCake: ConnectionCake =
    new ConnectionCake
      with ChargePointOcppConnectionComponent
      with DefaultSrpcComponent
      with SimpleClientWebSocketComponent {

    // TODO this should give us back a negotiated WebSocket subprotocol later on,
    // which we then use to determine the negotiated OCPP version to initialize the ChargePointOcppConnection
    val webSocketConnection = new SimpleClientWebSocketConnection(
      chargerId,
      centralSystemUri,
      authPassword
    )

    val srpcConnection = new DefaultSrpcConnection
    val ocppConnection = defaultChargePointOcppConnection(version)

    def requestedVersions: List[Version] = List(version)
  }
}
