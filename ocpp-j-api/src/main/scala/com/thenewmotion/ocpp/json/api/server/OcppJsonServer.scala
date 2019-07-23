package com.thenewmotion.ocpp
package json.api
package server

import java.net.InetSocketAddress
import java.util.Collections

import scala.language.higherKinds
import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}
import org.java_websocket.WebSocket
import org.java_websocket.drafts.{Draft, Draft_6455}
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import org.java_websocket.protocols.{IProtocol, Protocol}
import messages._
import OcppJsonServer._
import VersionFamily.{CsMessageTypesForVersionFamily, CsmsMessageTypesForVersionFamily}
import org.java_websocket.extensions.IExtension

/**
  * A simple server implementation to show how this library can be used in servers.
  *
  * @param listenPort The port to listen on
  * @param requestedOcppVersion The OCPP version to serve (either 1.5, 1.6 or 2.0; negotiation is not supported)
  */
abstract class OcppJsonServer[
  VFam <: VersionFamily,
  OUTREQBOUND <: Request,
  INRESBOUND <: Response,
  OUTREQRES[_ <: OUTREQBOUND, _ <: INRESBOUND] <: ReqRes[_, _],
  INREQBOUND <: Request,
  OUTRESBOUND <: Response,
  INREQRES[_ <: INREQBOUND, _ <: OUTRESBOUND] <: ReqRes[_, _]
](listenPort: Int, requestedOcppVersion: Version)
  (implicit ec: ExecutionContext,
    val csMessages: CsMessageTypesForVersionFamily[VFam, OUTREQBOUND, INRESBOUND, OUTREQRES],
    val csmsMessages: CsmsMessageTypesForVersionFamily[VFam, INREQBOUND, OUTRESBOUND, INREQRES]
  )
  extends WebSocketServer(
    new InetSocketAddress(listenPort),
    Collections.singletonList[Draft](new Draft_6455(
      Collections.emptyList[IExtension](),
      Collections.singletonList[IProtocol](new Protocol(protosForVersions(requestedOcppVersion)))
    ))
  ){

  protected trait ConnectionCake extends OcppConnectionComponent[
    OUTREQBOUND,
    INRESBOUND,
    OUTREQRES,
    INREQBOUND,
    OUTRESBOUND,
    INREQRES
  ] {
    self: SrpcComponent with WebSocketComponent =>
  }

  type OutgoingEndpoint = OutgoingOcppEndpoint[OUTREQBOUND, INRESBOUND, OUTREQRES]

  protected abstract class BaseConnectionCake(connection: WebSocket, chargePointIdentity: String)
    extends ConnectionCake with DefaultSrpcComponent with SimpleServerWebSocketComponent {

    final protected val executionContext: ExecutionContext = ec

    final override val srpcConnection: DefaultSrpcConnection = new DefaultSrpcConnection()

    final override val webSocketConnection: SimpleServerWebSocketConnection = new SimpleServerWebSocketConnection {
      val webSocket: WebSocket = connection
    }

    final private val outgoingEndpoint: OutgoingEndpoint = new OutgoingEndpoint {
      def send[REQ <: OUTREQBOUND, RES <: INRESBOUND](req: REQ)(implicit reqRes: OUTREQRES[REQ, RES]): Future[RES] =
        ocppConnection.sendRequest(req)

      def close(): Future[Unit] = srpcConnection.close()

      val onClose: Future[Unit] = srpcConnection.onClose
    }

    final private val requestHandler = handleConnection(chargePointIdentity, outgoingEndpoint)

    final def onRequest[REQ <: INREQBOUND, RES <: OUTRESBOUND](req: REQ)(implicit reqRes: INREQRES[REQ, RES]) =
      requestHandler.apply(req)
  }

  object connectionMap {
    private val ocppConnections: mutable.Map[WebSocket, BaseConnectionCake] =
      mutable.HashMap[WebSocket, BaseConnectionCake]()

    def put(conn: WebSocket, cake: BaseConnectionCake) : Unit = connectionMap.synchronized {
      ocppConnections.put(conn, cake)
      ()
    }

    def remove(conn: WebSocket): Option[BaseConnectionCake] = connectionMap.synchronized {
      ocppConnections.remove(conn)
    }

    def get(conn: WebSocket): Option[BaseConnectionCake] = connectionMap.synchronized {
      ocppConnections.get(conn)
    }
  }

  /**
    * This method should be overridden by the user of this class to define the behavior of the Central System. It will
    * be called once for each connection to this server that is established.
    *
    * @param clientChargePointIdentity The charge point identity of the client
    * @param remote An OutgoingEndpoint to send messages to the Charge Point or close the connection
    *
    * @return The handler for incoming requests from the Charge Point
    */
  def handleConnection(
    clientChargePointIdentity: String,
    remote: OutgoingEndpoint
  ): RequestHandler[INREQBOUND, OUTRESBOUND, INREQRES]

  override def onStart(): Unit = {}

  override def onOpen(conn: WebSocket, hndshk: ClientHandshake): Unit = {

    val uri = hndshk.getResourceDescriptor
    uri.split("/").lastOption match {

      case None =>
        conn.close(1003, "No ChargePointIdentity in path")

      case Some(chargePointIdentity) =>
        onOpenWithCPIdentity(conn, chargePointIdentity)
    }
  }

  private def onOpenWithCPIdentity(conn : WebSocket, chargePointIdentity: String): Unit = {
    val ocppConnection: BaseConnectionCake = newConnectionCake(conn, chargePointIdentity)

    connectionMap.put(conn, ocppConnection)
    ()
  }

  protected def newConnectionCake(connection: WebSocket, chargePointIdentity: String): BaseConnectionCake

  override def onClose(
    conn: WebSocket,
    code: Int,
    reason: String,
    remote: Boolean
  ): Unit = {
    connectionMap.remove(conn) foreach { c =>
      c.feedIncomingDisconnect()
    }
  }

  override def onMessage(conn: WebSocket, message: String): Unit =
    connectionMap.get(conn) foreach { c =>
      c.feedIncomingMessage(message)
    }

  override def onError(conn: WebSocket, ex: Exception): Unit =
    connectionMap.get(conn) foreach { c =>
      c.feedIncomingError(ex)
    }
}

object OcppJsonServer {

  private val protosForVersions = Map[Version, String](
    Version.V15 -> "ocpp1.5",
    Version.V16 -> "ocpp1.6",
    Version.V20 -> "ocpp2.0"
  )
}

