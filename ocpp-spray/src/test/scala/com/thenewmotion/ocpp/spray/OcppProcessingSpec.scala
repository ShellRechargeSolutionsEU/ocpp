package com.thenewmotion.ocpp.spray

import _root_.spray.http._
import _root_.spray.http.HttpRequest
import _root_.spray.httpx.encoding.{Deflate, Encoder, Gzip}
import _root_.spray.http.HttpHeaders.`Content-Type`
import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.Scope
import org.specs2.mock.Mockito
import com.thenewmotion.ocpp.{Scope => _, _}
import Version._
import org.joda.time.DateTime
import scala.io.Source


class OcppProcessingSpec extends SpecificationWithJUnit with Mockito with SoapUtils {

  "OcppProcessing" should {

    "call the user-supplied CentralSystemService according to the request" in new TestScope {
      val result = OcppProcessing.applyDecoded(httpRequest, _ => Some(mockCentralService))
      result.right.get._2() // need to force lazy value

      there was one(mockCentralService).heartbeat
    }

    "report an identity mismatch fault if the service function returns None" in new TestScope {
      val result = OcppProcessing.applyDecoded[CentralSystemService](httpRequest, _ => None)

      result must beLeft
      result.left.get.entity.asString must beMatching(".*IdentityMismatch.*")
    }

    "call the user-supplied ChargePointService according to the request" in {
      val mockCPService = mock[ChargePointService]
      mockCPService.getLocalListVersion returns AuthListSupported(0)
      val httpRequest = HttpRequest(HttpMethods.POST,
                                    Uri("/"),
                                    List(`Content-Type`(MediaTypes.`application/soap+xml`)),
                                    HttpEntity(bytesOfResourceFile("v15/getLocalListVersionRequest.xml")))

      val result = OcppProcessing.applyDecoded(httpRequest, _ => Some(mockCPService))
      result.right.get._2() // need to force lazy value

      there was one(mockCPService).getLocalListVersion
    }

    "dispatch ocpp 1.2" in new TestScope {
      import com.thenewmotion.ocpp.v12._

      val version = V12
      val req = bodyFrom("v12/heartbeatRequest.xml")
      val res = OcppProcessing.dispatch(V12, req, mockCentralService).any.head

      there was one(mockCentralService).heartbeat
      res.value mustEqual HeartbeatResponse(dateTime.toXMLCalendar)
    }

    "dispatch ocpp 1.5" in new TestScope {
      import com.thenewmotion.ocpp.v15._

      val version = V15
      val req = bodyFrom("v15/heartbeatRequest.xml")
      val res = OcppProcessing.dispatch(V15, req, mockCentralService).any.head

      there was one(mockCentralService).heartbeat
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
      mockChargePointService.getLocalListVersion returns AuthListNotSupported

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
    val mockChargePointService = mock[ChargePointService]
    val mockCentralService = mock[CentralSystemService]
    mockCentralService.heartbeat returns dateTime
    val httpRequest = HttpRequest(HttpMethods.POST,
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
