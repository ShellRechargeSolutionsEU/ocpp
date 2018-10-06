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

/**
 * An OCPP-J client implemented using Java-WebSocket.
 *
 * In OCPP-J, it is the Charge Point that is the client. So this client is
 * intended for use in charge point simulators (or charge point firmwares, if
 * there is someone writing those in Scala :-)).
 *
 * @param chargerId The charge point identity of the charge point for which you
 *                  want to set up a connection
 * @param centralSystemUri The endpoint URI of the Central System to connect to.
 * @param versions A list of requested OCPP versions in order of preference e.g:
 * Seq(Version.V16, Version.V15)
 * @param authPassword The Basic Auth password to use, hex-encoded
 */
// TODO actually use this class and shre code, e.g. WebSocket init
abstract class OcppJsonClient[
  VFam <: VersionFamily,
  INREQBOUND <: Request,
  OUTRESBOUND <: Response,
  INREQRES[_ <: INREQBOUND, _ <: OUTRESBOUND] <: ReqRes[_, _],
  OUTREQBOUND <: Request,
  INRESBOUND <: Response,
  OUTREQRES[_ <: OUTREQBOUND, _ <: INRESBOUND] <: ReqRes[_, _]
] private (
  chargerId: String,
  centralSystemUri: URI,
  versions: Seq[Version],
  authPassword: Option[String] = None
)(implicit val ec: ExecutionContext,
  val csMessages: CsMessageTypesForVersionFamily[VFam, INREQBOUND, OUTRESBOUND, INREQRES],
  val csmsMessages: CsmsMessageTypesForVersionFamily[VFam, OUTREQBOUND, INRESBOUND, OUTREQRES],
  sslContext: SSLContext = SSLContext.getDefault
) extends CakeBasedOcppClientEndpoint[
  VFam,
  INREQBOUND,
  OUTRESBOUND,
  INREQRES,
  OUTREQBOUND,
  INRESBOUND,
  OUTREQRES
]

abstract class Ocpp1XJsonClient private[client](
  chargerId: String,
  centralSystemUri: URI,
  versions: Seq[Version],
  authPassword: Option[String] = None
)(implicit val ec: ExecutionContext,
  sslContext: SSLContext = SSLContext.getDefault
) extends CakeBasedOcppClientEndpoint[
  VersionFamily.V1X.type,
  ChargePointReq,
  ChargePointRes,
  ChargePointReqRes,
  CentralSystemReq,
  CentralSystemRes,
  CentralSystemReqRes
  ]
{

  val connection: ConnectionCake = new ConnectionCake
    with ChargePointOcpp1XConnectionComponent
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

abstract class Ocpp20JsonClient private[client] (
  chargerId: String,
  centralSystemUri: URI,
  authPassword: Option[String] = None
)(implicit val ec: ExecutionContext,
  sslContext: SSLContext = SSLContext.getDefault
) extends CakeBasedOcppClientEndpoint[
  VersionFamily.V20.type,
  CsRequest,
  CsResponse,
  CsReqRes,
  CsmsRequest,
  CsmsResponse,
  CsmsReqRes
  ]
{

  val connection: ConnectionCake = new ConnectionCake
    with CsOcpp20ConnectionComponent
    with DefaultSrpcComponent
    with SimpleClientWebSocketComponent {

    import SimpleClientWebSocketComponent._

    val subprotocolToRequest = wsSubProtocolForOcppVersion(Version.V20)

    val webSocketConnection = new SimpleClientWebSocketConnection(
      chargerId,
      centralSystemUri,
      authPassword,
      requestedSubProtocols = Seq(subprotocolToRequest)
    )

    if (webSocketConnection.subProtocol != subprotocolToRequest) {
      throw new RuntimeException(
        s"Server using protocol ${webSocketConnection.subProtocol} instead of $subprotocolToRequest"
      )
    }

    val srpcConnection = new DefaultSrpcConnection
  }
}

object OcppJsonClient {
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
