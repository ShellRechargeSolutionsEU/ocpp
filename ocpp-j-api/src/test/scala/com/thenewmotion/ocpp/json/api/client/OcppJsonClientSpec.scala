package com.thenewmotion.ocpp
package json
package api
package client

import java.net.URI
import java.security.MessageDigest
import java.util.Base64
import java.util.concurrent.atomic.AtomicInteger
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import org.specs2.mutable.{After, Specification}
import org.specs2.specification.Scope
import org.mockserver.integration.ClientAndServer
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response
import org.mockserver.model.HttpStatusCode.SWITCHING_PROTOCOLS_101
import org.mockserver.model.{ConnectionOptions, Header, HttpRequest}
import org.mockserver.mock.action.ExpectationResponseCallback
import messages.v1x.ChargePointReq

class OcppJsonClientSpec extends Specification {
  sequential

  "Ocpp1XJsonClient" should {

    "negotiate the correct ocpp version" in {
      "when requesting ocpp1.6" in new TestScope {
        val requesting = List(Version.V16)
        server.when(request().withMethod("GET").withPath(s"$path/$chargerId")).respond(version15And16Responder)
        createOcppJsonClient(requesting).ocppVersion must beEqualTo(Version.V16)
      }
      "when requesting ocpp1.5, ocpp1.6" in new TestScope {
        val requesting = List(Version.V15, Version.V16)
        server.when(request().withMethod("GET").withPath(s"$path/$chargerId")).respond(version15And16Responder)
        createOcppJsonClient(requesting).ocppVersion must beEqualTo(Version.V15)
      }
      "when requesting ocpp1.6, ocpp1.5" in new TestScope {
        val requesting = List(Version.V16, Version.V15)
        server.when(request().withMethod("GET").withPath(s"$path/$chargerId")).respond(version15And16Responder)
        createOcppJsonClient(requesting).ocppVersion must beEqualTo(Version.V16)
      }
      "when requesting ocpp1.6, ocpp1.5 and server supports 1.5 only" in new TestScope {
        val requesting = List(Version.V16, Version.V15)
        server.when(request().withMethod("GET").withPath(s"$path/$chargerId")).respond(version15Responder)
        createOcppJsonClient(requesting).ocppVersion must beEqualTo(Version.V15)
      }
      "when requesting ocpp1.6 and server supports 1.5 only" in new TestScope {
        val requesting = List(Version.V16)
        server.when(request().withMethod("GET").withPath(s"$path/$chargerId")).respond(version15Responder)
        createOcppJsonClient(requesting) must throwA[SimpleClientWebSocketComponent.WebSocketErrorWhenOpeningException]
      }
      "when requesting a version the JSON client doesn't support" in new TestScope {
        val requesting = List(Version.V12)
        createOcppJsonClient(requesting) must throwA[OcppJsonClient.VersionNotSupported]
      }
      "when requesting to a server that's not listening" in new TestScope {
        val requesting = List(Version.V15)
        server.stop()
        createOcppJsonClient(requesting) must throwA[java.net.ConnectException]
      }
    }
  }

  private trait TestScope extends Scope with After {

    lazy val port = TestScope.nextPortNumber.getAndIncrement()
    lazy val server: ClientAndServer = ClientAndServer.startClientAndServer(port)

    override def after = server.stop

    val path = "/ocppws"
    val chargerId = "test-charger"
    val centralSystemUri = s"ws://localhost:$port$path"

    def createOcppJsonClient(versions: Seq[Version1X]) =
      OcppJsonClient.forVersion1x(chargerId, new URI(centralSystemUri), versions) {
        (_: ChargePointReq) =>
          Future.failed(OcppException(PayloadErrorCode.NotSupported, "OcppJsonClientSpec"))
      }

    val version15Responder = new UpgradeRequestCallbackV15()

    val version15And16Responder = new UpgradeRequestCallbackV15V16()
  }

  object TestScope {
    val nextPortNumber = new AtomicInteger(1080)
  }
}

class UpgradeRequestCallbackV15 extends UpgradeRequestCallback {
  val ocppVersions = Seq(Version.V15)
}
class UpgradeRequestCallbackV15V16 extends UpgradeRequestCallback {
  val ocppVersions = Seq(Version.V15, Version.V16)
}
trait UpgradeRequestCallback extends ExpectationResponseCallback {
  def ocppVersions: Seq[Version]
  lazy val subProtocols = ocppVersions.map(SimpleClientWebSocketComponent.wsSubProtocolForOcppVersion)
  private val magicGuid = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11"
  private val rfc2616Separators = "[()<>@,;:\\\\\"/\\[\\]?={} \t]+"
  private val mDigest: MessageDigest = MessageDigest.getInstance("SHA1")
  def handle(httpRequest: HttpRequest) = {
    val headers: Seq[Header] = {
      val webSocketKey = httpRequest.getFirstHeader("Sec-WebSocket-Key")
      val bytes = mDigest.digest((webSocketKey + magicGuid).getBytes)
      val webSocketAccept = Base64.getEncoder.encodeToString(bytes)
      Seq(
        new Header("Upgrade", "websocket"),
        new Header("Connection", "Upgrade"),
        new Header("Sec-WebSocket-Accept", webSocketAccept)
      )
    } ++ {
      val requestedProtocols = httpRequest.getFirstHeader(SimpleClientWebSocketComponent.subProtoHeader)
      val subProtocol = requestedProtocols.split(rfc2616Separators).intersect(subProtocols).headOption
      subProtocol.fold(Seq.empty[Header]) { protocol =>
        Seq(new Header(SimpleClientWebSocketComponent.subProtoHeader, protocol))
      }
    }
    response()
      .withStatusCode(SWITCHING_PROTOCOLS_101.code())
      .withHeaders(headers: _*)
      .withConnectionOptions(
        new ConnectionOptions()
          .withSuppressConnectionHeader(true)
          .withSuppressContentLengthHeader(true)
      )
  }
}
