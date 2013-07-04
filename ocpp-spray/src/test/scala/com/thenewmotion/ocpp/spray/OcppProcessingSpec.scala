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

    /* -- some tests commented out because they can't run with spray 1.1-M7 because the HttpRequest class is final.
          They work with 1.1-M8.

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
      val httpRequest = mock[HttpRequest]
      httpRequest.method returns HttpMethods.POST
      httpRequest.headers returns List(`Content-Type`(MediaTypes.`application/soap+xml`))
      val mockEntity = mock[HttpEntity]
      mockEntity.buffer returns bytesOfResourceFile("v15/getLocalListVersionRequest.xml")
      httpRequest.entity returns mockEntity

      val result = OcppProcessing.applyDecoded(httpRequest, _ => Some(mockCPService))
      result.right.get._2() // need to force lazy value

      there was one(mockCPService).getLocalListVersion
    }*/

    "dispatch ocpp 1.2" in new TestScope {
      import com.thenewmotion.ocpp.v12._

      val version = V12
      val req = bodyFrom("v12/heartbeatRequest.xml")
      val res = OcppProcessing.dispatch(V12, req, mockCentralService).any.head

      res.value mustEqual HeartbeatResponse(dateTime.toXMLCalendar)
      res.namespace mustEqual Some(version.namespace)
      there was one(mockCentralService).heartbeat
    }

    "dispatch ocpp 1.5" in new TestScope {
      import com.thenewmotion.ocpp.v15._

      val version = V15
      val req = bodyFrom("v15/heartbeatRequest.xml")
      val res = OcppProcessing.dispatch(V15, req, mockCentralService).any.head

      res.value mustEqual HeartbeatResponse(dateTime.toXMLCalendar)
      res.namespace mustEqual Some(version.namespace)
      there was one(mockCentralService).heartbeat
    }

    "support GZIP encoding" in new EncodingSpec {
      verify(Gzip)
    }

    "support DEFLATE encoding" in new EncodingSpec {
      verify(Deflate)
    }
  }

  private trait TestScope extends Scope {
    val dateTime = DateTime.now
    val mockCentralService = mock[CentralSystemService]
    mockCentralService.heartbeat returns dateTime
    /* Disabled because HttpRequest is unmockable, see above

    val httpRequest = mock[HttpRequest]
    httpRequest.method returns HttpMethods.POST
    httpRequest.headers returns List(`Content-Type`(MediaTypes.`application/soap+xml`))
    val mockEntity = mock[HttpEntity]
    mockEntity.buffer returns bytesOfResourceFile("v15/heartbeatRequest.xml")
    httpRequest.entity returns mockEntity
    */
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
