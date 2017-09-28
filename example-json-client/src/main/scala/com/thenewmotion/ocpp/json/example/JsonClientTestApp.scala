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
  private val version =
    if (args.length >= 3)
      Version.withName(args(2))
        .getOrElse(sys.error(s"Unrecognized version ${args(3)}"))
    else
      Version.V16

  private val logger = LoggerFactory.getLogger(JsonClientTestApp.getClass)

  // TODO make authPassword optional to make example app code cleaner
  val connection = new OcppJsonClient(chargerId, new URI(centralSystemUri), None, version) {
    // TODO: get rid of asInstanceOf, by creating specific onBootNotification etc callbacks? Or moving it into OcppConnection?
    def onRequest[REQ <: ChargePointReq, RES <: ChargePointRes](req: REQ)(implicit reqRes: ReqRes[REQ, RES]): Future[RES] = Future {
      req match {
        case GetLocalListVersionReq =>
          logger.info("Received GetLocalListVersionReq")
          GetLocalListVersionRes(AuthListNotSupported).asInstanceOf[RES]
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
