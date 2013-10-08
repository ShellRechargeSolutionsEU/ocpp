package com.thenewmotion.ocpp.spray

import _root_.spray.http._
import _root_.spray.http.HttpRequest
import _root_.spray.httpx.encoding.{Deflate, Encoder, Gzip}
import _root_.spray.http.HttpHeaders.`Content-Type`
import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.Scope
import org.specs2.mock.Mockito
import org.joda.time.DateTime
import scala.io.Source
import com.thenewmotion.ocpp.Version._
import com.thenewmotion.ocpp.centralsystem._
import com.thenewmotion.ocpp.chargepoint._
import com.thenewmotion.ocpp._


class OcppProcessingSpec extends SpecificationWithJUnit with Mockito with SoapUtils {

  "OcppProcessing" should {

    "call the user-supplied CentralSystemService according to the request" in new TestScope {
      val Right((_, f)) = OcppProcessing.applyDecoded[CentralSystem](httpRequest)
      f(Some(mockCentralService))
      there was one(mockCentralService).apply(HeartbeatReq)
    }

    "report an identity mismatch fault if the service function returns None" in new TestScope {
      val Right((_, f)) = OcppProcessing.applyDecoded[CentralSystem](httpRequest)
      val result = OcppProcessing.applyDecoded[CentralSystem](httpRequest)
      val response = f(None)
      response.entity.asString must beMatching(".*IdentityMismatch.*")
    }

    "call the user-supplied ChargePointService according to the request" in {
      val mockCPService = mock[ChargePoint]
      mockCPService.apply(GetLocalListVersionReq) returns GetLocalListVersionRes(AuthListSupported(0))
      val httpRequest = HttpRequest(HttpMethods.POST,
                                    Uri("/"),
                                    List(`Content-Type`(MediaTypes.`application/soap+xml`)),
                                    HttpEntity(bytesOfResourceFile("v15/getLocalListVersionRequest.xml")))
      val Right((_, f)) = OcppProcessing.applyDecoded[ChargePoint](httpRequest)

      f(Some(mockCPService))

      there was one(mockCPService).apply(GetLocalListVersionReq)
    }

    "dispatch ocpp 1.2" in new TestScope {
      import com.thenewmotion.ocpp.v12._

      val version = V12
      val req = bodyFrom("v12/heartbeatRequest.xml")
      val res = OcppProcessing.dispatch(V12, req, mockCentralService).any.head

      there was one(mockCentralService).apply(HeartbeatReq)
      res.value mustEqual HeartbeatResponse(dateTime.toXMLCalendar)
    }

    "dispatch ocpp 1.5" in new TestScope {
      import com.thenewmotion.ocpp.v15._

      val version = V15
      val req = bodyFrom("v15/heartbeatRequest.xml")
      val res = OcppProcessing.dispatch(V15, req, mockCentralService).any.head

      there was one(mockCentralService).apply(HeartbeatReq)
      res.value mustEqual HeartbeatResponse(dateTime.toXMLCalendar)
    }

    "support GZIP encoding" in new EncodingSpec {
      verify(Gzip)
    }

    "support DEFLATE encoding" in new EncodingSpec {
      verify(Deflate)
    }

    "return messages with charge point namespace when processing messages for charger" in new TestScope {
      val req = bodyFrom("v15/getLocalListVersionRequest.xml")
      mockChargePointService.apply(GetLocalListVersionReq) returns GetLocalListVersionRes(AuthListNotSupported)

      val res = OcppProcessing.dispatch(V15, req, mockChargePointService).any.head

      res.namespace mustEqual Some("urn://Ocpp/Cp/2012/06/")
    }

    "return messages with central system namespace when processing messages for central service" in new TestScope {
      val req = bodyFrom("v15/heartbeatRequest.xml")

      val res = OcppProcessing.dispatch(V15, req, mockCentralService).any.head

      res.namespace mustEqual Some("urn://Ocpp/Cs/2012/06/")
    }
  }

  private trait TestScope extends Scope {
    val dateTime = DateTime.now
    val mockChargePointService = mock[ChargePoint]
    val mockCentralService = mock[CentralSystem]
    mockCentralService.apply(HeartbeatReq) returns HeartbeatRes(dateTime)
    val httpRequest = HttpRequest(
      HttpMethods.POST,
      Uri("/"),
      List(`Content-Type`(MediaTypes.`application/soap+xml`)),
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
