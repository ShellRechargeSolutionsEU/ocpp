package com.thenewmotion.ocpp
package json
package example

import java.net.URI

import scala.concurrent._
import ExecutionContext.Implicits.global
import messages.v1x._
import api._

object JsonClientTestApp extends App {

  /*
   * First, let's figure out what kind of connection we have to set up: which
   * charge point identity, which central system endpoint URI, and which OCPP
   * version...
   */
  private val chargerId = args.headOption.getOrElse("test-charger")
  private val centralSystemUri = if (args.length >= 2) args(1) else "ws://localhost:8080/ocppws"
  private val versions =
    if (args.length >= 3)
      args(2).split(",").map { version =>
        Version.withName(version).getOrElse(sys.error(s"Unrecognized version(s): ${args(2)}"))
      }.toSeq.collect({ case v: Version1X => v })
    else
      Seq(Version.V16)

  /*
   * Then, we create an OcppJsonClient with those settings. Also, we give as an
   * argument in a second list a ChargePoint instance that specifies how we want
   * to handle incoming requests.
   */
  val ocppJsonClient = OcppJsonClient.forVersion1x(chargerId, new URI(centralSystemUri), versions) {

    /*
     * Here we define how we handle OCPP requests from the Central System to us.
     * The example app answers to GetConfiguration requests that it doesn't have
     * any configuration. To other requests it just answers that that message
     * type is not supported.
     */
    new ChargePoint {
      def getConfiguration(req: GetConfigurationReq): Future[GetConfigurationRes] =
        Future.successful(GetConfigurationRes(
          values = List(),
          unknownKeys = req.keys
        ))

      def remoteStartTransaction(q: RemoteStartTransactionReq): Future[RemoteStartTransactionRes] =
        notSupported("Remote Start Transaction")

      def remoteStopTransaction(q: RemoteStopTransactionReq): Future[RemoteStopTransactionRes] =
        notSupported("Remote Stop Transaction")

      def unlockConnector(q: UnlockConnectorReq): Future[UnlockConnectorRes] =
        notSupported("Unlock Connector")

      def getDiagnostics(req: GetDiagnosticsReq): Future[GetDiagnosticsRes] =
        notSupported("Get Diagnostics")

      def changeConfiguration(req: ChangeConfigurationReq): Future[ChangeConfigurationRes] =
        notSupported("Change Configuration")

      def changeAvailability(req: ChangeAvailabilityReq): Future[ChangeAvailabilityRes] =
        notSupported("Change Availability")

      def clearCache: Future[ClearCacheRes] =
        notSupported("Clear Cache")

      def reset(req: ResetReq): Future[ResetRes] =
        notSupported("Reset")

      def updateFirmware(req: UpdateFirmwareReq): Future[Unit] =
        notSupported("Update Firmware")

      def sendLocalList(req: SendLocalListReq): Future[SendLocalListRes] =
        notSupported("Send Local List")

      def getLocalListVersion: Future[GetLocalListVersionRes] =
        notSupported("Get Local List Version")

      def dataTransfer(q: ChargePointDataTransferReq): Future[ChargePointDataTransferRes] =
        notSupported("Data Transfer")

      def reserveNow(q: ReserveNowReq): Future[ReserveNowRes] =
        notSupported("Reserve Now")

      def cancelReservation(q: CancelReservationReq): Future[CancelReservationRes] =
        notSupported("Cancel Reservation")

      def clearChargingProfile(req: ClearChargingProfileReq): Future[ClearChargingProfileRes] =
        notSupported("Clear Charging Profile")

      def getCompositeSchedule(req: GetCompositeScheduleReq): Future[GetCompositeScheduleRes] =
        notSupported("Get Composite Schedule")

      def setChargingProfile(req: SetChargingProfileReq): Future[SetChargingProfileRes] =
        notSupported("Set Charging Profile")

      def triggerMessage(req: TriggerMessageReq): Future[TriggerMessageRes] =
        notSupported("Trigger Message")

      def notSupported(opName: String): Future[Nothing] =
        Future.failed(OcppException(
          PayloadErrorCode.NotSupported,
          s"Demo app doesn't support $opName"
        ))
    }
  }

  /*
   * Set up a callback that will print a message once the connection is
   * closed
   */
  ocppJsonClient.onClose.foreach(_ => println("OCPP connection closed"))

  println(s"Connected using OCPP version ${ocppJsonClient.ocppVersion}")

  /*
   * Now let's send some OCPP requests to that Central System! Just like a real
   * charge point!
   */
  for {
    _ <- ocppJsonClient.send(BootNotificationReq(
      chargePointVendor = "The New Motion",
      chargePointModel = "Lolo 47.6",
      chargePointSerialNumber = Some("123456"),
      chargeBoxSerialNumber = None,
      firmwareVersion = None,
      iccid = None,
      imsi = None,
      meterType = None,
      meterSerialNumber = None))

    _ <- ocppJsonClient.send(HeartbeatReq)

    _ <- ocppJsonClient.send(StatusNotificationReq(
      scope = ChargePointScope,
      status = ChargePointStatus.Unavailable(info = None),
      timestamp = None,
      vendorId = None
    ))

    _ <- ocppJsonClient.send(AuthorizeReq(idTag = "12345678")).map { res =>
      if (res.idTag.status == AuthorizationStatus.Accepted)
        println("12345678 is authorized.")
      else
        println("12345678 has been rejected. No power to him!")
    }
  } yield ()

  /*
   * Wait for the above code to do its thing (in a real use case, you'd do this
   * in a nicer way)
   */
  Thread.sleep(7000)

  /* Bye, we're done. */
  ocppJsonClient.close()

  System.exit(0)
}
