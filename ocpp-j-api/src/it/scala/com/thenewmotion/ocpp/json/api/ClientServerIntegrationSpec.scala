package com.thenewmotion.ocpp
package json.api

import java.net.URI
import java.time.{ZoneId, ZonedDateTime}
import scala.concurrent.{Await, Promise}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.Success
import org.specs2.mutable.Specification

import messages.v1x._
import OcppJsonServer.OutgoingEndpoint

class ClientServerIntegrationSpec extends Specification {
  sequential

  "Client and server" should {

    "exchange client-initiated request-response" in {

      val testPort = 34782
      val testSerial = "testserial"
      val testResponse = HeartbeatRes(ZonedDateTime.of(2017, 7, 7, 12, 30, 6, 0, ZoneId.of("UTC")))
      val serverStarted = Promise[Unit]()

      val server = new OcppJsonServer(testPort, Version.V16) {
        def handleConnection(cpSerial: String, remote: OutgoingEndpoint): CentralSystemRequestHandler = {
          (req: CentralSystemReq) =>
            req match {
              case HeartbeatReq =>
                testResponse
              case _ =>
                sys.error(s"Unexpected request in test server: $req")
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
            sys.error("No incoming charge point request expected in this test"): ChargePointRes
          }
        }

        Await.result(client.send(HeartbeatReq), 1.second) mustEqual testResponse
      } finally server.stop()
    }

    "exchange server-initiated request-response" in {
      val testPort = 34783
      val testSerial = "testserial"
      val serverTestResponse = HeartbeatRes(ZonedDateTime.of(2017, 7, 7, 12, 30, 6, 0, ZoneId.of("UTC")))
      val clientTestResponse = GetLocalListVersionRes(AuthListSupported(42))
      val serverStarted = Promise[Unit]()

      val clientResponsePromise = Promise[GetLocalListVersionRes]()

      val server = new OcppJsonServer(testPort, Version.V16) {

        def handleConnection(cpSerial: String, remote: OutgoingEndpoint): CentralSystemRequestHandler = {
          (req: CentralSystemReq) =>
            req match {
              case HeartbeatReq =>
                clientResponsePromise.completeWith {
                                                     remote.send(GetLocalListVersionReq)
                                                   }

                serverTestResponse
              case _ =>
                sys.error(s"Unexpected request to server in test: $req")
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
        }

        client.send(HeartbeatReq)

        Await.result(clientResponsePromise.future, 1.second) mustEqual clientTestResponse

      } finally server.stop()
    }
  }
}
