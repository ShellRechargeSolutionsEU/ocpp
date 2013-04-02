package com.thenewmotion.ocpp

import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.mock.Mockito
import Version._
import com.thenewmotion.time.Imports._

/**
 * @author Yaroslav Klymko
 */
class CentralSystemDispatchSpec extends SpecificationWithJUnit with Mockito with SoapUtils {
  "CentralSystemDispatch" should {
    "support ocpp 1.2" in new TestScope {

      import v12._

      val version = V12
      val req = bodyFrom("v12/heartbeatRequest.xml")
      val res = CentralSystemDispatcher(req, _ => service, _ => ()).any.head

      res.value mustEqual HeartbeatResponse(dateTime)
      res.namespace mustEqual Some(version.namespace)
      there was one(service).heartbeat
    }

    "support ocpp 1.5" in new TestScope {
      import v15._
      val version = V15
      val req = bodyFrom("v15/heartbeatRequest.xml")
      val res = CentralSystemDispatcher(req, _ => service, _ => ()).any.head

      res.value mustEqual HeartbeatResponse(dateTime)
      res.namespace mustEqual Some(version.namespace)
      there was one(service).heartbeat
    }
  }

  trait TestScope extends org.specs2.specification.Scope {
    val dateTime = DateTime.now
    val service = mock[CentralSystemService]
    service.heartbeat returns dateTime
  }
}
