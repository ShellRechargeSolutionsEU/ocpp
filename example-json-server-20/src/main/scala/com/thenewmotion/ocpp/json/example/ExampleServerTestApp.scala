package com.thenewmotion.ocpp
package json
package example

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.Source
import api._
import com.thenewmotion.ocpp.messages.v20._

object ExampleServerTestApp extends App {

  val server = new Ocpp20JsonServer(2345) {

    override def handleConnection(chargePointIdentity: String, conn: Ocpp20JsonServer.OutgoingEndpoint): CsmsRequestHandler = {

      conn.onClose.foreach(_ => println(s"Disconnected client $chargePointIdentity"))

      {
        (req: CsmsRequest) =>

          println(s"Received request $req from client $chargePointIdentity")

          (req match {
            case req: HeartbeatRequest => HeartbeatResponse(currentTime = Instant.now())
            case req: TransactionEventRequest if req.idToken.exists(_.idToken == "04EC2CC2552281") =>
              TransactionEventResponse(
                totalCost = None,
                chargingPriority = None,
                idTokenInfo = Some(IdTokenInfo(
                  status = AuthorizationStatus.Blocked,
                  cacheExpiryDateTime =  None,
                  chargingPriority = None,
                  groupIdToken = None,
                  language1 = None,
                  language2 = None,
                  personalMessage = None
                )),
                updatedPersonalMessage = None
              )
            case req: TransactionEventRequest =>
              TransactionEventResponse(
                totalCost = None,
                chargingPriority = None,
                idTokenInfo = Some(IdTokenInfo(
                  status = AuthorizationStatus.Accepted,
                  cacheExpiryDateTime =  None,
                  chargingPriority = None,
                  groupIdToken = None,
                  language1 = None,
                  language2 = None,
                  personalMessage = None
                )),
                updatedPersonalMessage = None
              )
            case req: StatusNotificationRequest => StatusNotificationResponse()
            case _ =>
              throw OcppException(PayloadErrorCode.NotImplemented, "Request type not implemented")
          }): CsmsResponse
      }
    }
  }

  server.start()

  println("Example server listening at port 2345. Press ENTER to exit.")
  Source.stdin.bufferedReader.readLine()

  server.stop()
}
