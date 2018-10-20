package com.thenewmotion.ocpp
package json.api
package client

import java.net.URI
import javax.net.ssl.SSLContext
import scala.language.higherKinds
import scala.concurrent.ExecutionContext
import VersionFamily.{CsMessageTypesForVersionFamily, CsmsMessageTypesForVersionFamily}
import messages.{ReqRes, Request, Response}
import messages.v1x.{CentralSystemReq, CentralSystemReqRes, CentralSystemRes, ChargePointReq, ChargePointReqRes, ChargePointRes}
import messages.v20._
import OcppJsonClient.VersionNotSupported

/**
 * An OCPP-J client implemented using Java-WebSocket.
 *
 * In OCPP-J, it is the Charge Point that is the client. So this client is
 * intended for use in charge point simulators (or charge point firmwares, if
 * there is someone writing those in Scala :-)).
 *
 * For a complete description of the interface of this object, see
 * [[com.thenewmotion.ocpp.json.api.OutgoingOcppEndpoint]].
 *
 * @param chargerId The charge point identity of the charge point for which you
 *                  want to set up a connection
 * @param centralSystemUri The endpoint URI of the Central System to connect to.
 * @param authPassword The Basic Auth password to use, hex-encoded
 */
abstract class OcppJsonClient[
  VFam <: VersionFamily,
  OUTREQBOUND <: Request,
  INRESBOUND <: Response,
  OUTREQRES[_ <: OUTREQBOUND, _ <: INRESBOUND] <: ReqRes[_, _],
  INREQBOUND <: Request,
  OUTRESBOUND <: Response,
  INREQRES[_ <: INREQBOUND, _ <: OUTRESBOUND] <: ReqRes[_, _]
] private[client] (
  chargerId: String,
  centralSystemUri: URI,
  authPassword: Option[String] = None
)(implicit val ec: ExecutionContext,
  val csmsMessages: CsmsMessageTypesForVersionFamily[VFam, OUTREQBOUND, INRESBOUND, OUTREQRES],
  val csMessages: CsMessageTypesForVersionFamily[VFam, INREQBOUND, OUTRESBOUND, INREQRES],
  sslContext: SSLContext = SSLContext.getDefault
) extends CakeBasedOcppClientEndpoint[
  VFam,
  OUTREQBOUND,
  INRESBOUND,
  OUTREQRES,
  INREQBOUND,
  OUTRESBOUND,
  INREQRES
] {

  protected abstract class BaseConnectionCake(versionsToRequest: Seq[Version])
    extends ConnectionCake
    with DefaultSrpcComponent
    with SimpleClientWebSocketComponent {

    self: OcppConnectionComponent[OUTREQBOUND, INRESBOUND, OUTREQRES, INREQBOUND, OUTRESBOUND, INREQRES] =>

    val webSocketConnection = new SimpleClientWebSocketConnection(
      chargerId,
      centralSystemUri,
      authPassword,
      requestedSubProtocols = versionsToRequest.map { version =>
        SimpleClientWebSocketComponent.wsSubProtocolForOcppVersion.getOrElse(
          version,
          throw VersionNotSupported(
            "JSON client only supports versions: ",
            supportedVersions = SimpleClientWebSocketComponent.ocppVersionForWsSubProtocol.values
          )
        )
      }
    )

    val negotiatedOcppVersion: Version =
      SimpleClientWebSocketComponent.ocppVersionForWsSubProtocol.getOrElse(
        webSocketConnection.subProtocol,
        throw new RuntimeException(s"Unknown protocol ${webSocketConnection.subProtocol} in use for connection")
      )

    val srpcConnection = new DefaultSrpcConnection
  }

  protected val connection: BaseConnectionCake

  /**
    * @return the OCPP version that this client object is using
    */
  def ocppVersion: Version = connection.ocppVersion
}

/** An OCPP-J client class for versions 1.5 and 1.6.
  *
  * @param chargerId The charge point identity of the charge point for which you
  *                  want to set up a connection
  * @param centralSystemUri The endpoint URI of the Central System to connect to.
  * @param versions A list of requested OCPP versions in order of preference e.g:
  *                 Seq(Version.V16, Version.V15)
  * @param authPassword The Basic Auth password to use, hex-encoded
  */
abstract class Ocpp1XJsonClient private[client] (
  chargerId: String,
  centralSystemUri: URI,
  versions: Seq[Version],
  authPassword: Option[String] = None
)(implicit ec: ExecutionContext,
  sslContext: SSLContext = SSLContext.getDefault
) extends OcppJsonClient[
  VersionFamily.V1X.type,
  CentralSystemReq,
  CentralSystemRes,
  CentralSystemReqRes,
  ChargePointReq,
  ChargePointRes,
  ChargePointReqRes
](
  chargerId,
  centralSystemUri,
  authPassword
)
{

  override protected val connection: BaseConnectionCake =
    new BaseConnectionCake(versions) with ChargePointOcpp1XConnectionComponent {

      val ocppVersion: Version = negotiatedOcppVersion

      val ocppConnection = defaultChargePointOcppConnection
  }
}

/** An OCPP-J client class for version 2.0.
  *
  * @param chargerId The charge point identity of the charge point for which you
  *                  want to set up a connection
  * @param centralSystemUri The endpoint URI of the Central System to connect to.
  * @param authPassword The Basic Auth password to use, hex-encoded
  */
abstract class Ocpp20JsonClient private[client] (
  chargerId: String,
  centralSystemUri: URI,
  authPassword: Option[String] = None
)(implicit ec: ExecutionContext,
  sslContext: SSLContext = SSLContext.getDefault
) extends OcppJsonClient[
  VersionFamily.V20.type,
  CsmsRequest,
  CsmsResponse,
  CsmsReqRes,
  CsRequest,
  CsResponse,
  CsReqRes
](
  chargerId,
  centralSystemUri,
  authPassword
) {

  override val connection: BaseConnectionCake =
    new BaseConnectionCake(Seq(Version.V20)) with CsOcpp20ConnectionComponent {

      if (negotiatedOcppVersion != Version.V20) {
        throw new RuntimeException(
          s"Server using protocol ${webSocketConnection.subProtocol} instead of ocpp2.0"
        )
      }
    }
}

object OcppJsonClient {
  // It would be interesting to have a factory method that uses the version
  // negotiation to give 2.0 if supported and otherwise 1.x. Library users could
  // then case match on it to use it type-safely further on.
  // That would however require a total refactoring of the way we set up client
  // connections, so that we *first* establish a WebSocket connection and *then*
  // proceed to build the whole cake on top of it instead of establishing the
  // WS connection while building the cake.

  /**
    * The factory method to create an OcppJsonClient for OCPP 1.2, 1.5 and/or
    * 1.6.
    *
    * @param chargerId
    * @param centralSystemUri
    * @param versions
    * @param authPassword
    * @param incomingRequestHandler
    * @param ec
    * @param sslContext
    * @return
    */
  def forVersion1x(
    chargerId: String,
    centralSystemUri: URI,
    versions: Seq[Version1X],
    authPassword: Option[String] = None
  )(
    incomingRequestHandler: ChargePointRequestHandler
  )(implicit ec: ExecutionContext,
    sslContext: SSLContext = SSLContext.getDefault
  ): Ocpp1XJsonClient = new Ocpp1XJsonClient(chargerId, centralSystemUri, versions, authPassword)(ec, sslContext) {
    override val requestHandler: ChargePointRequestHandler = incomingRequestHandler
  }

  /**
    * The factory method to create an OcppJsonClient for OCPP 2.0.
    *
    * @param chargerId
    * @param centralSystemUri
    * @param versions
    * @param authPassword
    * @param incomingRequestHandler
    * @param ec
    * @param sslContext
    * @return
    */
  def forVersion20(
    chargerId: String,
    centralSystemUri: URI,
    authPassword: Option[String] = None
  )(
    incomingRequestHandler: CsRequestHandler
  )(implicit ec: ExecutionContext,
    sslContext: SSLContext = SSLContext.getDefault
  ): Ocpp20JsonClient = new Ocpp20JsonClient(chargerId, centralSystemUri, authPassword)(ec, sslContext) {
    override val requestHandler: CsRequestHandler = incomingRequestHandler
  }

  case class VersionNotSupported(msg: String, supportedVersions: Iterable[Version])
    extends RuntimeException(msg + supportedVersions.mkString(", "))
}
