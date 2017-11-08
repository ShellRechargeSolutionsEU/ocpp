package com.thenewmotion.ocpp
package json.api

import java.net.URI
import java.time.{ZoneId, ZonedDateTime}
import scala.concurrent.{Await, Promise}
import scala.concurrent.duration._
import scala.util.Success
import org.specs2.mutable.Specification
import org.specs2.concurrent.ExecutionEnv
import messages._

class ClientServerIntegrationSpec extends Specification {
  sequential

  import OcppJsonServer._

  "Client and server" should {

    "exchange client-initiated request-response" in { implicit ee: ExecutionEnv =>

      val testPort = 34782
      val testSerial = "testserial"
      val testResponse = HeartbeatRes(ZonedDateTime.of(2017, 7, 7, 12, 30, 6, 0, ZoneId.of("UTC")))
      val serverStarted = Promise[Unit]()

      val server = new OcppJsonServer(testPort, Version.V16) {
        def handleConnection(cpSerial: String, remote: OutgoingEndpoint): IncomingEndpoint =
          new IncomingEndpoint {
            val requestHandler: CentralSystemRequestHandler = { (req: CentralSystemReq) =>
              req match {
                case HeartbeatReq =>
                  testResponse
                case _ =>
                  sys.error(s"Unexpected request in test server: $req")
              }
            }

            def onDisconnect(): Unit = {}

            def onError(err: OcppError): Unit = {}
          }

        override def onStart(): Unit = {
          serverStarted.complete(Success(()))
          ()
        }
      }

      server.start()

      try {
        Await.result(serverStarted.future, 2.seconds)

        val client = new OcppJsonClient(
          testSerial,
          new URI(s"http://127.0.0.1:$testPort/"),
          versions = List(Version.V16)
        ) {
          val requestHandler: ChargePointRequestHandler = { (req: ChargePointReq) =>
            sys.error("No incoming charge point request expected in this test"): ChargePointRes
          }

          def onDisconnect(): Unit = {}

          def onError(err: OcppError): Unit = {}
        }

        client.send(HeartbeatReq) must beEqualTo(testResponse).await
      } finally server.stop()
    }

    "exchange server-initiated request-response" in { implicit ee: ExecutionEnv =>
      val testPort = 34783
      val testSerial = "testserial"
      val serverTestResponse = HeartbeatRes(ZonedDateTime.of(2017, 7, 7, 12, 30, 6, 0, ZoneId.of("UTC")))
      val clientTestResponse = GetLocalListVersionRes(AuthListSupported(42))
      val serverStarted = Promise[Unit]()

      val clientResponsePromise = Promise[GetLocalListVersionRes]()

      val server = new OcppJsonServer(testPort, Version.V16) {
        def handleConnection(cpSerial: String, remote: OutgoingEndpoint): IncomingEndpoint = {
          new IncomingEndpoint {
            val requestHandler: CentralSystemRequestHandler = (req: CentralSystemReq) =>
              req match {
                case HeartbeatReq =>
                  clientResponsePromise.completeWith {
                                                       remote.send(GetLocalListVersionReq)
                                                     }

                  serverTestResponse
                case _ =>
                  sys.error(s"Unexpected request to server in test: $req")
              }

            def onDisconnect(): Unit = {}

            def onError(err: OcppError): Unit = {}
          }
        }

        override def onStart(): Unit = {
          serverStarted.complete(Success(()))
          ()
        }
      }

      server.start()

      try {
        Await.result(serverStarted.future, 2.seconds)

        val client = new OcppJsonClient(
          testSerial,
          new URI(s"http://127.0.0.1:$testPort/"),
          versions = List(Version.V16)
        ) {
          val requestHandler: ChargePointRequestHandler = { (req: ChargePointReq) =>
            req match {
              case GetLocalListVersionReq => GetLocalListVersionRes(AuthListSupported(42))
              case _ => sys.error(s"Unexpected request to client in test: $req")
            }
          }

          def onDisconnect(): Unit = {}

          def onError(err: OcppError): Unit = {}
        }

        client.send(HeartbeatReq)

        clientResponsePromise.future must beEqualTo(clientTestResponse).await

      } finally server.stop()
    }
  }
}
