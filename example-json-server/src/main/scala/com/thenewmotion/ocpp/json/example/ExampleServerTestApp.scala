package com.thenewmotion.ocpp
package json
package example

import java.time.ZonedDateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.io.Source

import messages._
import api._
import OcppJsonServer.{IncomingEndpoint, OutgoingEndpoint}

object ExampleServerTestApp extends App {

  val server = new OcppJsonServer(2345, Version.V15) {

    override def handleConnection(chargePointIdentity: String, conn: OutgoingEndpoint): IncomingEndpoint = {

      println(s"Received incoming connection from $chargePointIdentity")

      new IncomingEndpoint {

        override def requestHandler: RequestHandler[CentralSystemReq, CentralSystemRes, CentralSystemReqRes] = {
          (req: CentralSystemReq) =>
            println(s"Received request $req from client $chargePointIdentity")

            req match {
              case req: BootNotificationReq =>
                // let's send a GetConfiguration to see what this charge point is up to
                conn.send(GetConfigurationReq(Nil)) foreach { res =>
                  println(s"Charge point $chargePointIdentity responded to GetConfiguration: $res")
                }

                BootNotificationRes(
                  status = RegistrationStatus.Accepted,
                  currentTime = ZonedDateTime.now(),
                  interval = 5.minutes
                )

              case _ =>
                throw OcppException(PayloadErrorCode.NotImplemented, "Request type not implemented")
            }
        }

        override def onDisconnect(): Unit =
          println(s"Disconnected client $chargePointIdentity")

        override def onError(error: OcppError): Unit =
          println(s"Error occurred for client $chargePointIdentity: $error")
      }
    }
  }

  server.start()

  println("Example server listening at port 2345. Press ENTER to exit.")
  Source.stdin.bufferedReader.readLine()

  server.stop()
}
