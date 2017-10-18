package com.thenewmotion.ocpp
package json.api

import java.net.URI

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

import scala.concurrent.ExecutionContext.Implicits.global

class OcppJsonClientSpec extends Specification with Mockito {
  "OcppJsonClient" should {
    "negotiate the correct ocpp version" in {
      "when requesting ocpp1.5" in new TestScope {
        val versions = List(Version.V15)
        val ocppJsonClient = new OcppJsonClient(chargerId, new URI(centralSystemUri), versions) {
          def requestHandler = ???
          def onError(err: OcppError) = println(s"OCPP error: ${err.error} ${err.description}")
          def onDisconnect() = println("WebSocket disconnect")
        }
        ocppJsonClient.connection.ocppVersion must beEqualTo(Version.V15)
      }
/*
      "when requesting ocpp1.6" in new TestScope {

      }
      "when requesting ocpp1.5, ocpp1.6" in new TestScope {

      }
      "when requesting ocpp1.6, ocpp1.5" in new TestScope {

      }
      "when requesting ocpp2.0" in new TestScope {

      }
*/
    }
  }

  private trait TestScope extends Scope {
    val chargerId = "test-charger"
    val centralSystemUri = "ws://localhost:8080/ocppws"
  }
}
