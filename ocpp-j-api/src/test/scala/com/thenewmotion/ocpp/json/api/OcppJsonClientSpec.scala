package com.thenewmotion.ocpp
package json.api

import java.net.URI

import org.specs2.mutable.BeforeAfter
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

import scala.concurrent.ExecutionContext.Implicits.global

class OcppJsonClientSpec extends Specification {
  sequential

  import org.mockserver.model.HttpRequest.request

  "OcppJsonClient" should {
    "negotiate the correct ocpp version" in {
      "when requesting ocpp1.6" in new TestScope {
        val requesting = List(Version.V16)
        server.when(request().withMethod("GET").withPath(s"$path/$chargerId")).callback(V15V16)
        createOcppJsonClient(requesting).connection.ocppVersion must beEqualTo(Version.V16)
      }
      "when requesting ocpp1.5, ocpp1.6" in new TestScope {
        val requesting = List(Version.V15, Version.V16)
        server.when(request().withMethod("GET").withPath(s"$path/$chargerId")).callback(V15V16)
        createOcppJsonClient(requesting).connection.ocppVersion must beEqualTo(Version.V15)
      }
      "when requesting ocpp1.6, ocpp1.5" in new TestScope {
        val requesting = List(Version.V16, Version.V15)
        server.when(request().withMethod("GET").withPath(s"$path/$chargerId")).callback(V15V16)
        createOcppJsonClient(requesting).connection.ocppVersion must beEqualTo(Version.V16)
      }
      "when requesting ocpp1.6, ocpp1.5 and server supports 1.5 only" in new TestScope {
        val requesting = List(Version.V16, Version.V15)
        server.when(request().withMethod("GET").withPath(s"$path/$chargerId")).callback(V15)
        createOcppJsonClient(requesting).connection.ocppVersion must beEqualTo(Version.V15)
      }
      "when requesting ocpp1.6 and server supports 1.5 only" in new TestScope {
        val requesting = List(Version.V16)
        server.when(request().withMethod("GET").withPath(s"$path/$chargerId")).callback(V15)
        createOcppJsonClient(requesting) must throwA[OcppJsonClient.VersionMismatch]
      }
      "when requesting a version the JSON client doesn't support" in new TestScope {
        val requesting = List(Version.V12)
        createOcppJsonClient(requesting) must throwA[OcppJsonClient.VersionNotSupported]
      }
    }
  }

  private class TestScope extends Scope with BeforeAfter {

    import org.mockserver.integration.ClientAndServer

    var server: ClientAndServer = _

    val port = 1080
    override def before =
      server = ClientAndServer.startClientAndServer(port)
    override def after = server.stop

    val path = "/ocppws"
    val chargerId = "test-charger"
    val centralSystemUri = s"ws://localhost:$port$path"

    def createOcppJsonClient(versions: Seq[Version]) =
      new OcppJsonClient(chargerId, new URI(centralSystemUri), versions) {

        import json.PayloadErrorCode
        import messages.ChargePointReq

        import scala.concurrent.Future

        val requestHandler: ChargePointRequestHandler = (_: ChargePointReq) =>
          Future.failed(OcppException(PayloadErrorCode.NotSupported, "OcppJsonClientSpec"))
        def onError(err: OcppError) = {}
        def onDisconnect() = {}
      }

    import org.mockserver.model.HttpCallback

    val V15 = new HttpCallback().withCallbackClass(
      "com.thenewmotion.ocpp.json.api.UpgradeRequestCallBackV15"
    )

    val V15V16 = new HttpCallback().withCallbackClass(
      "com.thenewmotion.ocpp.json.api.UpgradeRequestCallBackV15V16"
    )
  }
}

import java.security.MessageDigest
import java.util.Base64

import org.mockserver.mock.action.ExpectationCallback
import org.mockserver.model.Header
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse.response
import org.mockserver.model.HttpStatusCode.SWITCHING_PROTOCOLS_101

class UpgradeRequestCallBackV15 extends UpgradeRequestCallBack {
  val ocppVersions = Seq(Version.V15)
}
class UpgradeRequestCallBackV15V16 extends UpgradeRequestCallBack {
  val ocppVersions = Seq(Version.V15, Version.V16)
}
trait UpgradeRequestCallBack extends ExpectationCallback {
  def ocppVersions: Seq[Version]
  lazy val subProtocols = ocppVersions.map(SimpleWebSocketClient.wsSubProtocolForOcppVersion)
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
      val requestedProtocols = httpRequest.getFirstHeader(SimpleWebSocketClient.SubProtoHeader)
      val subProtocol = requestedProtocols.split(rfc2616Separators).intersect(subProtocols).headOption
      subProtocol.fold(Seq.empty[Header]) { protocol =>
        Seq(new Header(SimpleWebSocketClient.SubProtoHeader, protocol))
      }
    }
    response().withStatusCode(SWITCHING_PROTOCOLS_101.code()).withHeaders(headers: _*)
  }
}
