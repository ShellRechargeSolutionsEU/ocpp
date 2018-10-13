package com.thenewmotion.ocpp
package json
package example

import java.time.ZonedDateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.io.Source
import messages.v1x._
import api._

object ExampleServerTestApp extends App {

  val server = new Ocpp1XJsonServer(2345, Version.V15) {

    override def handleConnection(chargePointIdentity: String, conn: Ocpp1XJsonServer.OutgoingEndpoint): CentralSystemRequestHandler = {

      conn.onClose.foreach(_ => println(s"Disconnected client $chargePointIdentity"))

      {
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
    }
  }

  server.start()

  println("Example server listening at port 2345. Press ENTER to exit.")
  Source.stdin.bufferedReader.readLine()

  server.stop()
}
