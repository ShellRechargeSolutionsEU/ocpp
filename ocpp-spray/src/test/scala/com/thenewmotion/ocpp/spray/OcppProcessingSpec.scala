package com.thenewmotion.ocpp
package spray

import _root_.spray.http._
import _root_.spray.http.HttpRequest
import _root_.spray.httpx.encoding.{Deflate, Encoder, Gzip}
import _root_.spray.http.HttpHeaders.{`Content-Type`,`X-Forwarded-For`}
import _root_.spray.http.StatusCodes.{UnsupportedMediaType, MethodNotAllowed}
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import org.specs2.mock.Mockito
import java.time.ZonedDateTime
import scala.io.Source
import com.thenewmotion.ocpp.Version._
import com.thenewmotion.ocpp.soap.SoapUtils
import com.thenewmotion.ocpp.messages.{CentralSystemReq => CsReq, CentralSystemRes => CsRes, ChargePointReq => CpReq,
                                       ChargePointRes => CpRes, _}
import scala.concurrent.{Await, Future, ExecutionContext}
import scala.concurrent.duration.Duration
import ExecutionContext.Implicits.global
import java.net.{InetAddress, URI}
import soap.RichZonedDateTime

class OcppProcessingSpec extends Specification with Mockito with SoapUtils {

  "OcppProcessing" should {

    "call the user-supplied function with the parsed request" in new TestScope {
      val mockProcessingFunction = mock[(ChargerInfo, CsReq) => Future[CsRes]]
      mockProcessingFunction(anyObject, anyObject) returns Future.successful(HeartbeatRes(ZonedDateTime.now()))

      Await.result(OcppProcessing.applyDecoded[CsReq, CsRes](httpRequest)(mockProcessingFunction), Duration(2, "seconds"))

      val expectedChargerInfo = ChargerInfo(V15, Some(new URI("http://address.com")), "chargeBoxIdentity", Some(InetAddress.getByName("192.168.1.100")))
      there was one(mockProcessingFunction).apply(expectedChargerInfo, HeartbeatReq)
    }

    "produce a Fault response if the processing function returns a failed future" in new TestScope {
      val failingProcessingFunction = mock[(ChargerInfo, CsReq) => Future[CsRes]]
      failingProcessingFunction(anyObject, anyObject) returns Future.failed(new RuntimeException("b0rk! b0rk!"))

      val response = Await.result(OcppProcessing.applyDecoded[CsReq, CsRes](httpRequest)(failingProcessingFunction),
        Duration(2, "seconds"))

      response.entity.asString must beMatching(".*Fault.*") and beMatching(".*b0rk! b0rk!.*")
    }

    "produce a Fault response if the processing function throws" in new TestScope {
      val throwingProcessingFunction = mock[(ChargerInfo, CsReq) => Future[CsRes]]
      throwingProcessingFunction(anyObject, anyObject) throws new RuntimeException("b0rk! b0rk!")

      val response = Await.result(OcppProcessing.applyDecoded[CsReq, CsRes](httpRequest)(throwingProcessingFunction),
        Duration(2, "seconds"))

      response.entity.asString must beMatching(".*Fault.*") and beMatching(".*b0rk! b0rk!.*")
    }

    "call the user-supplied ChargePointService according to the request" in {
      val mockProcessingFunction = mock[(ChargerInfo, CpReq) => Future[CpRes]]
      mockProcessingFunction.apply(anyObject, anyObject) returns Future.successful(GetLocalListVersionRes(AuthListSupported(0)))
      val httpRequest = HttpRequest(HttpMethods.POST,
                                    Uri("/"),
                                    List(`Content-Type`(MediaTypes.`application/soap+xml`), `X-Forwarded-For`("192.168.1.100")),
                                    HttpEntity(bytesOfResourceFile("v15/getLocalListVersionRequest.xml")))

      Await.result(OcppProcessing.applyDecoded[CpReq, CpRes](httpRequest)(mockProcessingFunction), Duration(2, "seconds"))

      val expectedChargerInfo = ChargerInfo(V15, Some(new URI("http://localhost:8080/ocpp/")), "TestTwin1", Some(InetAddress.getByName("192.168.1.100")))
      there was one(mockProcessingFunction).apply(expectedChargerInfo, GetLocalListVersionReq)
    }

    "dispatch ocpp 1.2" in new TestScope {
      import com.thenewmotion.ocpp.v12._

      val version = V12
      val req = bodyFrom("v12/heartbeatRequest.xml")
      val res = Await.result(OcppProcessing.dispatch(V12, req, mockFunction), Duration(2, "seconds")).any.head

      there was one(mockFunction).apply(HeartbeatReq)
      res.value mustEqual HeartbeatResponse(referenceTime.toXMLCalendar)
    }

    "dispatch ocpp 1.5" in new TestScope {
      import com.thenewmotion.ocpp.v15._

      val version = V15
      val req = bodyFrom("v15/heartbeatRequest.xml")
      val res = Await.result(OcppProcessing.dispatch(V15, req, mockFunction), Duration(2, "seconds")).any.head

      there was one(mockFunction).apply(HeartbeatReq)
      res.value mustEqual HeartbeatResponse(referenceTime.toXMLCalendar)
    }

    "support GZIP encoding" in new EncodingSpec {
      verify(Gzip)
    }

    "support DEFLATE encoding" in new EncodingSpec {
      verify(Deflate)
    }

    "return messages with charge point namespace when processing messages for charger" in new TestScope {
      val req = bodyFrom("v15/getLocalListVersionRequest.xml")
      val mockCPFunction = mock[CpReq => Future[CpRes]]
      mockCPFunction.apply(GetLocalListVersionReq) returns Future.successful(GetLocalListVersionRes(AuthListNotSupported))

      val res = Await.result(OcppProcessing.dispatch(V15, req, mockCPFunction), Duration(2, "seconds")).any.head

      res.namespace mustEqual Some("urn://Ocpp/Cp/2012/06/")
    }

    "return messages with central system namespace when processing messages for central service" in new TestScope {
      val req = bodyFrom("v15/heartbeatRequest.xml")

      val res = Await.result(OcppProcessing.dispatch(V15, req, mockFunction), Duration(2, "seconds")).any.head

      res.namespace mustEqual Some("urn://Ocpp/Cs/2012/06/")
    }

    "produce an UnsupportedMediaType error if the media type is missing or wrong" in new TestScope {
      val processingFunction = mock[(ChargerInfo, CsReq) => Future[CsRes]]

      val response = Await.result(OcppProcessing.applyDecoded[CsReq, CsRes](httpRequestWithNoContentType)(processingFunction),
        Duration(2, "seconds"))

      response.status mustEqual UnsupportedMediaType
    }

    "produce a MethodNotAllowed error if the wrong http method is used" in new TestScope {
      val processingFunction = mock[(ChargerInfo, CsReq) => Future[CsRes]]

      val response = Await.result(OcppProcessing.applyDecoded[CsReq, CsRes](httpRequestWithWrongMethod)(processingFunction),
        Duration(2, "seconds"))

      response.status mustEqual MethodNotAllowed
    }
  }

  private trait TestScope extends Scope {
    val referenceTime = ZonedDateTime.now()
    val mockFunction = mock[CsReq => Future[CsRes]]
    mockFunction.apply(anyObject) returns Future.successful(HeartbeatRes(referenceTime))

    val httpRequest = HttpRequest(
      HttpMethods.POST,
      Uri("/"),
      List(`Content-Type`(MediaTypes.`application/soap+xml`), `X-Forwarded-For`("192.168.1.100")),
      HttpEntity(bytesOfResourceFile("v15/heartbeatRequest.xml")))

    val httpRequestWithWrongMethod = HttpRequest(
      HttpMethods.PUT,
      Uri("/"),
      List(`Content-Type`(MediaTypes.`application/soap+xml`)),
      HttpEntity(bytesOfResourceFile("v15/heartbeatRequest.xml")))

    val httpRequestWithNoContentType = HttpRequest(
      HttpMethods.POST,
      Uri("/"),
      List(),
      HttpEntity(bytesOfResourceFile("v15/heartbeatRequest.xml")))
  }

  private def bytesOfResourceFile(filename: String) =
    Source.fromInputStream(getClass.getResourceAsStream(filename)).mkString.getBytes

  trait EncodingSpec extends Scope {
    def verify(encoder: Encoder) = {
      val request = HttpRequest(entity = HttpEntity("request"))
      val response = HttpResponse(entity = HttpEntity("response"))

      val (actualRequest, encode) = OcppProcessing.decodeEncode(encoder.encode(request))
      request.entity mustEqual actualRequest.entity
      val actualResponse = encode(response)
      actualResponse.entity mustEqual encoder.encode(response).entity
      actualResponse.headers.collectFirst {
        case h if h is "content-encoding" => h.value
      } must beSome(encoder.encoding.value)
    }
  }
}
