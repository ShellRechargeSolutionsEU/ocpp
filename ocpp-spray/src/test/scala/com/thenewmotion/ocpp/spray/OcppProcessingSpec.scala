package com.thenewmotion.ocpp.spray

import _root_.spray.http._
import _root_.spray.http.HttpRequest
import org.specs2.mutable.SpecificationWithJUnit
import com.thenewmotion.ocpp._
import Version._
import org.specs2.mock.Mockito
import org.joda.time.DateTime
import HttpHeaders.`Content-Type`
import scala.io.Source


class OcppProcessingSpec extends SpecificationWithJUnit with Mockito with SoapUtils {

  "OcppProcessing" should {

    "call the user-supplied CentralSystemService according to the request" in new TestScope {
      val result = OcppProcessing(httpRequest, _ => mockCentralService)
      result.right.get._2() // need to force lazy value

      there was one(mockCentralService).heartbeat
    }

    "report an identity mismatch fault if the service function throws a ChargeBoxIdentityException" in new TestScope {
      def serviceFunction(ci: ChargerInfo): CentralSystemService = throw new ChargeBoxIdentityException("bestaat niet")
      val result = OcppProcessing[CentralSystemService](httpRequest, serviceFunction _)

      result must beRight
      result.right.get._2().entity.asString must beMatching(".*Fault.*bestaat niet.*")
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

      val result = OcppProcessing(httpRequest, _ => mockCPService)
      result.right.get._2() // need to force lazy value

      there was one(mockCPService).getLocalListVersion
    }

    "dispatch ocpp 1.2" in new TestScope {
      import com.thenewmotion.ocpp.v12._

      val version = V12
      val req = bodyFrom("v12/heartbeatRequest.xml")
      val res = OcppProcessing.dispatch(Some(V12), req, mockCentralService).any.head

      res.value mustEqual HeartbeatResponse(dateTime.toXMLCalendar)
      res.namespace mustEqual Some(version.namespace)
      there was one(mockCentralService).heartbeat
    }

    "dispatch ocpp 1.5" in new TestScope {
      import com.thenewmotion.ocpp.v15._

      val version = V15
      val req = bodyFrom("v15/heartbeatRequest.xml")
      val res = OcppProcessing.dispatch(Some(V15), req, mockCentralService).any.head

      res.value mustEqual HeartbeatResponse(dateTime.toXMLCalendar)
      res.namespace mustEqual Some(version.namespace)
      there was one(mockCentralService).heartbeat
    }
  }

  private trait TestScope extends org.specs2.specification.Scope {
    val dateTime = DateTime.now
    val mockCentralService = mock[CentralSystemService]
    mockCentralService.heartbeat returns dateTime
    val httpRequest = mock[HttpRequest]
    httpRequest.method returns HttpMethods.POST
    httpRequest.headers returns List(`Content-Type`(MediaTypes.`application/soap+xml`))
    val mockEntity = mock[HttpEntity]
    mockEntity.buffer returns bytesOfResourceFile("v15/heartbeatRequest.xml")
    httpRequest.entity returns mockEntity
  }

  private def bytesOfResourceFile(filename: String) =
    Source.fromInputStream(getClass.getResourceAsStream(filename)).mkString.getBytes
}
