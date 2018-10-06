package com.thenewmotion.ocpp
package json
package api

import scala.concurrent.duration.DurationInt
import scala.concurrent.ExecutionContext.Implicits.global
import org.specs2.mutable.Specification
import com.thenewmotion.ocpp.messages.v1x._
import RequestHandler._

import scala.concurrent.Await

class RequestHandlerSpec extends Specification {

  "RequestHandler.fromChargePointSyncFunction" should {
    "turn OcppException's into failed futures" in {
      val syncReqH: ChargePointRequestHandler = {
        _ => throw OcppException(PayloadErrorCode.GenericError, "haai")
      }: ChargePointReq => ChargePointRes

      val futureResult = syncReqH.apply(ClearCacheReq)
      Await.result(futureResult, 1.second) must throwA[OcppException]
    }
  }

  "RequestHandler.fromSyncChargePoint" should {
    "turn OcppException's into failed futures" in {
      val syncReqH: ChargePointRequestHandler = new SyncChargePoint {
        def remoteStartTransaction(req: RemoteStartTransactionReq): RemoteStartTransactionRes =
          throw OcppException(PayloadErrorCode.GenericError, "")
        def remoteStopTransaction(req: RemoteStopTransactionReq): RemoteStopTransactionRes =
          throw OcppException(PayloadErrorCode.GenericError, "")
        def unlockConnector(req: UnlockConnectorReq): UnlockConnectorRes =
          throw OcppException(PayloadErrorCode.GenericError, "")
        def getDiagnostics(req: GetDiagnosticsReq): GetDiagnosticsRes =
          throw OcppException(PayloadErrorCode.GenericError, "")
        def changeConfiguration(req: ChangeConfigurationReq): ChangeConfigurationRes =
          throw OcppException(PayloadErrorCode.GenericError, "")
        def getConfiguration(req: GetConfigurationReq): GetConfigurationRes =
          throw OcppException(PayloadErrorCode.GenericError, "")
        def changeAvailability(req: ChangeAvailabilityReq): ChangeAvailabilityRes =
          throw OcppException(PayloadErrorCode.GenericError, "")
        def clearCache: ClearCacheRes =
          throw OcppException(PayloadErrorCode.GenericError, "")
        def reset(req: ResetReq): ResetRes =
          throw OcppException(PayloadErrorCode.GenericError, "")
        def updateFirmware(req: UpdateFirmwareReq): Unit =
          throw OcppException(PayloadErrorCode.GenericError, "")
        def sendLocalList(req: SendLocalListReq): SendLocalListRes =
          throw OcppException(PayloadErrorCode.GenericError, "")
        def getLocalListVersion: GetLocalListVersionRes =
          throw OcppException(PayloadErrorCode.GenericError, "")
        def dataTransfer(req: ChargePointDataTransferReq): ChargePointDataTransferRes =
          throw OcppException(PayloadErrorCode.GenericError, "")
        def reserveNow(req: ReserveNowReq): ReserveNowRes =
          throw OcppException(PayloadErrorCode.GenericError, "")
        def cancelReservation(req: CancelReservationReq): CancelReservationRes =
          throw OcppException(PayloadErrorCode.GenericError, "")
        def clearChargingProfile(req: ClearChargingProfileReq): ClearChargingProfileRes =
          throw OcppException(PayloadErrorCode.GenericError, "")
        def getCompositeSchedule(req: GetCompositeScheduleReq): GetCompositeScheduleRes =
          throw OcppException(PayloadErrorCode.GenericError, "")
        def setChargingProfile(req: SetChargingProfileReq): SetChargingProfileRes =
          throw OcppException(PayloadErrorCode.GenericError, "")
        def triggerMessage(req: TriggerMessageReq): TriggerMessageRes =
          throw OcppException(PayloadErrorCode.GenericError, "")
      }

      val futureResult = syncReqH.apply(ClearCacheReq)
      Await.result(futureResult, 1.second) must throwA[OcppException]
    }
  }
}
