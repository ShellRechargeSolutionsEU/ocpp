package com.thenewmotion.ocpp
package json
package api

import java.net.URI
import java.time.{Instant, ZoneId, ZonedDateTime}
import java.util.concurrent.atomic.AtomicInteger
import scala.concurrent.{Await, Promise}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.Success
import org.specs2.mutable.Specification
import messages.v1x._
import messages.v20._
import org.java_websocket.WebSocket

class ClientServerIntegrationSpec extends Specification {
  sequential

  // we generate incrementing port numbers for successive tests so they're
  // not using the same port quickly in succession, which leads to strange
  // errors
  // also use a random starting point for the incrementing ports so that it is
  // unlikely that tests fail if this Spec file is run repeatedly in rapid
  // succession, although the chance of that happening is still there :-(
  object testPort {
    val basePortNumber =
      new AtomicInteger(1024 + java.security.SecureRandom.getInstanceStrong.nextInt(20000))

    def apply(): Int = basePortNumber.getAndIncrement()
  }

  "Client and server" should {

    "on version 1.6" in {

      "exchange client-initiated request-response" in {

        val port = testPort()
        val testSerial = "testserial"
        val testResponse = HeartbeatRes(ZonedDateTime.of(2017, 7, 7, 12, 30, 6, 0, ZoneId.of("UTC")))
        val serverStarted = Promise[Unit]()

        val server = new Ocpp1XJsonServer(port, Version.V16) {
          def handleConnection(
            cpSerial: String,
            remote: Ocpp1XJsonServer.OutgoingEndpoint
          ): CentralSystemRequestHandler = {
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
            new URI(s"http://127.0.0.1:$port/"),
            versions = List(Version.V16)
          ) {
              (req: ChargePointReq) =>
                sys.error("No incoming charge point request expected in this test"): ChargePointRes
            }

          Await.result(client.send(HeartbeatReq), 1.second) mustEqual testResponse
        } finally server.stop()
      }

      "exchange server-initiated request-response" in {
        val port = testPort()
        val testSerial = "testserial"
        val serverTestResponse = HeartbeatRes(ZonedDateTime.of(2017, 7, 7, 12, 30, 6, 0, ZoneId.of("UTC")))
        val clientTestResponse = GetLocalListVersionRes(AuthListSupported(42))
        val serverStarted = Promise[Unit]()

        val clientResponsePromise = Promise[GetLocalListVersionRes]()

        val server = new Ocpp1XJsonServer(port, Version.V16) {

          def handleConnection(
            cpSerial: String,
            remote: Ocpp1XJsonServer.OutgoingEndpoint
          ): CentralSystemRequestHandler = {
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
            new URI(s"http://127.0.0.1:$port/"),
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

      "process client-initiated close" in {
        val port = testPort()
        val testSerial = "testserial"
        val serverStarted = Promise[Unit]()
        val connectionClosedRemotelyForServer = Promise[Boolean]()

        val server = new Ocpp1XJsonServer(port, Version.V16) {
          def handleConnection(
            cpSerial: String,
            remote: Ocpp1XJsonServer.OutgoingEndpoint
          ): CentralSystemRequestHandler = {
            (req: CentralSystemReq) =>
              req match {
                case _ =>
                  sys.error(s"Unexpected request in test server: $req"): CentralSystemRes
              }
          }

          override def onStart(): Unit = {
            serverStarted.complete(Success(()))
            ()
          }

          override def onClose(conn: WebSocket, code: Int, reason: String, remote: Boolean): Unit = {
            connectionClosedRemotelyForServer.complete(Success(remote))
            ()
          }
        }

        server.start()

        try {
          Await.result(serverStarted.future, 2.seconds)

          val client = OcppJsonClient.forVersion1x(
            testSerial,
            new URI(s"http://127.0.0.1:$port/"),
            versions = List(Version.V16)
          ) {
              (req: ChargePointReq) =>
                sys.error("No incoming charge point request expected in this test"): ChargePointRes
            }

          Await.result(client.close(), 1.second) mustEqual (())
          Await.result(connectionClosedRemotelyForServer.future, 1.second) must beTrue
        } finally server.stop()
      }

      "process server-initiated close" in {
        val port = testPort()
        val testSerial = "testserial"
        val serverTestResponse = HeartbeatRes(ZonedDateTime.of(2017, 7, 7, 12, 30, 6, 0, ZoneId.of("UTC")))
        val clientTestResponse = GetLocalListVersionRes(AuthListSupported(42))
        val serverStarted = Promise[Unit]()

        val clientResponsePromise = Promise[GetLocalListVersionRes]()

        val server = new Ocpp1XJsonServer(port, Version.V16) {

          def handleConnection(
            cpSerial: String,
            remote: Ocpp1XJsonServer.OutgoingEndpoint
          ): CentralSystemRequestHandler = {
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
            new URI(s"http://127.0.0.1:$port/"),
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

    "on version 2.0" in {

      "exchange client-initiated request-response" in {

        val port = testPort()
        val testSerial = "testserial"
        val testRequest = BootNotificationRequest(
          ChargingStation(
            serialNumber = None,
            model = "Lolo 1337",
            modem = None,
            vendorName = "Ny Bevegelse",
            firmwareVersion = None
          ),
          BootReason.PowerUp
        )
        val testResponse = BootNotificationResponse(
          Instant.now(),
          interval = 300,
          status = BootNotificationStatus.Accepted
        )
        val serverStarted = Promise[Unit]()

        val server: Ocpp20JsonServer = new Ocpp20JsonServer(port) {
          def handleConnection(cpSerial: String, remote: Ocpp20JsonServer.OutgoingEndpoint): CsmsRequestHandler = {
            req: CsmsRequest =>

              req match {
                case BootNotificationRequest(cs, r) =>
                  testResponse
                case _ =>
                  throw OcppException(PayloadErrorCode.InternalError, s"Unexpected request in test server: $req")
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
            new URI(s"http://127.0.0.1:$port/")
          ) {
              _: CsRequest =>
                throw OcppException(
                  PayloadErrorCode.InternalError,
                  "No incoming charging station request expected in this test"
                )
            }

          Await.result(client.send(testRequest), 1.second) mustEqual testResponse
        } finally server.stop()
      }

      "exchange server-initiated request-response" in {

        val port = testPort()
        val testSerial = "testserial"
        val serverStarted = Promise[Unit]()
        val connectionClosedRemotelyForServer = Promise[Boolean]()

        val server: Ocpp20JsonServer = new Ocpp20JsonServer(port) {
          def handleConnection(cpSerial: String, remote: Ocpp20JsonServer.OutgoingEndpoint): CsmsRequestHandler = {
            remote.close()

            {
              req: CsmsRequest =>
                throw OcppException(PayloadErrorCode.InternalError, s"Unexpected request in test server: $req")
            }
          }

          override def onStart(): Unit = {
            serverStarted.complete(Success(()))
            ()
          }

          override def onClose(conn: WebSocket, code: Int, reason: String, remote: Boolean) = {
            connectionClosedRemotelyForServer.complete(Success(remote))
            ()
          }
        }

        server.start()

        try {
          Await.result(serverStarted.future, 2.seconds)

          OcppJsonClient.forVersion20(
            testSerial,
            new URI(s"http://127.0.0.1:$port/")
          ) {
            req: CsRequest =>
              (throw OcppException(PayloadErrorCode.InternalError, s"Unexpected request in test client: $req")): CsResponse
          }

          Await.result(connectionClosedRemotelyForServer.future, 1.second) mustEqual false
        } finally server.stop()
      }
    }
  }
}
