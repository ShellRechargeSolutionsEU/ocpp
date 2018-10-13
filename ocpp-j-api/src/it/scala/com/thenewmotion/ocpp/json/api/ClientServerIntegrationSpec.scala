package com.thenewmotion.ocpp
package json
package api

import java.net.URI
import java.time.{Instant, ZoneId, ZonedDateTime}

import scala.concurrent.{Await, ExecutionContext, Future, Promise}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.Success
import org.specs2.mutable.Specification
import messages.v1x._
import messages.v20._

class ClientServerIntegrationSpec extends Specification {
  sequential

  "Client and server" should {

    "on version 1.6" in {

      "exchange client-initiated request-response" in {

        val testPort = 34782
        val testSerial = "testserial"
        val testResponse = HeartbeatRes(ZonedDateTime.of(2017, 7, 7, 12, 30, 6, 0, ZoneId.of("UTC")))
        val serverStarted = Promise[Unit]()

        val server = new Ocpp1XJsonServer(testPort, Version.V16) {
          def handleConnection(cpSerial: String, remote: Ocpp1XJsonServer.OutgoingEndpoint): CentralSystemRequestHandler = {
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

          val client = OcppJsonClient.forVersion1x(
            testSerial,
            new URI(s"http://127.0.0.1:$testPort/"),
            versions = List(Version.V16)
          ) {
              (req: ChargePointReq) =>
                sys.error("No incoming charge point request expected in this test"): ChargePointRes
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

        val server = new Ocpp1XJsonServer(testPort, Version.V16) {

          def handleConnection(cpSerial: String, remote: Ocpp1XJsonServer.OutgoingEndpoint): CentralSystemRequestHandler = {
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

          val client = OcppJsonClient.forVersion1x(
            testSerial,
            new URI(s"http://127.0.0.1:$testPort/"),
            versions = List(Version.V16)
          ) { (req: ChargePointReq) =>
            req match {
              case GetLocalListVersionReq => GetLocalListVersionRes(AuthListSupported(42))
              case _ => sys.error(s"Unexpected request to client in test: $req")
            }
            }

          client.send(HeartbeatReq)

          Await.result(clientResponsePromise.future, 1.second) mustEqual clientTestResponse

        } finally server.stop()
      }
    }
  }

  "on version 2.0" in {

    "exchange client-initiated request-response" in {

      val testPort = 34782
      val testSerial = "testserial"
      val testRequest = BootNotificationRequest(
        ChargingStation(
          serialNumber = None,
          model = "Lolo 1337",
          modem = None,
          vendorName = "Nueva Moción",
          firmwareVersion = None),
        BootReason.PowerUp
      )
      val testResponse = BootNotificationResponse(
        Instant.now(),
        interval = 300,
        status = BootNotificationStatus.Accepted
      )
      val serverStarted = Promise[Unit]()

      val server: Ocpp20JsonServer = new Ocpp20JsonServer(testPort) {
        def handleConnection(cpSerial: String, remote: Ocpp20JsonServer.OutgoingEndpoint): CsmsRequestHandler =

          new CsmsRequestHandler {

            def apply[REQ <: CsmsRequest, RES <: CsmsResponse](req: REQ)
              (implicit reqRes: CsmsReqRes[REQ, RES], ec: ExecutionContext): Future[RES] = {

              req match {
                case BootNotificationRequest(cs, r) =>
                  Future.successful(testResponse.asInstanceOf[RES])
                case _ =>
                  Future.failed(OcppException(PayloadErrorCode.InternalError, s"Unexpected request in test server: $req"))
              }
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

        val client = OcppJsonClient.forVersion20(
          testSerial,
          new URI(s"http://127.0.0.1:$testPort/")
        ) {

            new CsRequestHandler {

              def apply[REQ <: CsRequest, RES <: CsResponse](req: REQ)
                (implicit reqRes: CsReqRes[REQ, RES], ec: ExecutionContext): Future[RES] = {

                Future.failed(OcppException(PayloadErrorCode.InternalError, "No incoming charge point request expected in this test"))
              }
            }
          }

        Await.result(client.send(testRequest), 1.second) mustEqual testResponse
      } finally server.stop()
    }
  }
}
