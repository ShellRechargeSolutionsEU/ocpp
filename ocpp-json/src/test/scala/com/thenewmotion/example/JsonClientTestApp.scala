package com.thenewmotion.example

import java.net.URI
import com.typesafe.scalalogging.slf4j.Logging
import com.thenewmotion.ocpp.messages._
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import com.thenewmotion.ocpp.json.{OcppError, PayloadErrorCode, OcppException, OcppJsonClient}

object JsonClientTestApp extends App {
  val connection = new OcppJsonClient("Test Charger", new URI("http://localhost:8080/ocppws")) with Logging {

    def onRequest(req: ChargePointReq): Future[ChargePointRes] = Future {
      req match {
        case GetLocalListVersionReq =>
          GetLocalListVersionRes(AuthListNotSupported)
        case _ =>
          throw OcppException(PayloadErrorCode.NotSupported, "Demo app doesn't support that")
      }
    }

    def onError(err: OcppError) = logger.warn(s"OCPP error: ${err.error} ${err.description}")

    def onDisconnect = logger.warn("WebSocket disconnect")
  }

  connection.send(BootNotificationReq(
    chargePointVendor = "The New Motion",
    chargePointModel = "Lolo 47.6",
    chargePointSerialNumber = Some("123456"),
    chargeBoxSerialNumber = None,
    firmwareVersion = None,
    iccid = None,
    imsi = None,
    meterType = None,
    meterSerialNumber = None))

  Thread.sleep(7000)

  connection.close()
}
