package com.thenewmotion.ocpp
package json
package example

import java.net.URI
import org.slf4j.LoggerFactory

import messages._
import api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._

object JsonClientTestApp extends App {

  private val chargerId = args.headOption.getOrElse("test-charger")
  private val centralSystemUri = if (args.length >= 2) args(1) else "ws://localhost:8080/ocppws"
  private val authPassword = if (args.length >= 3) Some(args(2)) else None

  private val logger = LoggerFactory.getLogger(JsonClientTestApp.getClass)

  val connection = new OcppJsonClient(chargerId, new URI(centralSystemUri), authPassword) {
    def onRequest(req: ChargePointReq): Future[ChargePointRes] = Future {
      req match {
        case GetLocalListVersionReq =>
          logger.info("Received GetLocalListVersionReq")
          GetLocalListVersionRes(AuthListNotSupported)
        case x =>
          logger.info(s"Received $x")
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

  System.exit(0)
}
