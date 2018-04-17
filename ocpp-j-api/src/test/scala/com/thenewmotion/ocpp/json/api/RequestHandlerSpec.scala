package com.thenewmotion.ocpp
package json
package api

import scala.concurrent.duration.DurationInt
import scala.concurrent.ExecutionContext.Implicits.global
import org.specs2.mutable.Specification
import com.thenewmotion.ocpp.messages._
import RequestHandler._

import scala.concurrent.Await

class RequestHandlerSpec extends Specification {

  "syncFunctionAsChargePointRequestHandler" should {
    "turn OcppException's into failed futures" in {
      val syncReqH: ChargePointRequestHandler = {
        _ => throw OcppException(PayloadErrorCode.GenericError, "haai")
      }: ChargePointReq => ChargePointRes

      val futureResult = syncReqH.apply(ClearCacheReq)
      Await.result(futureResult, 1.second) must throwA[OcppException]
    }
  }

  "syncChargePointAsChargePointRequestHandler" should {
    "turn OcppException's into failed futures" in {
      val syncReqH: ChargePointRequestHandler = new SyncChargePoint {
        def remoteStartTransaction(req: RemoteStartTransactionReq): RemoteStartTransactionRes = ???
        def remoteStopTransaction(req: RemoteStopTransactionReq): RemoteStopTransactionRes = ???
        def unlockConnector(req: UnlockConnectorReq): UnlockConnectorRes = ???
        def getDiagnostics(req: GetDiagnosticsReq): GetDiagnosticsRes = ???
        def changeConfiguration(req: ChangeConfigurationReq): ChangeConfigurationRes = ???
        def getConfiguration(req: GetConfigurationReq): GetConfigurationRes = ???
        def changeAvailability(req: ChangeAvailabilityReq): ChangeAvailabilityRes = ???
        def clearCache: ClearCacheRes =
          throw OcppException(PayloadErrorCode.GenericError, "Incompetent Operator error")
        def reset(req: ResetReq): ResetRes = ???
        def updateFirmware(req: UpdateFirmwareReq): Unit = ???
        def sendLocalList(req: SendLocalListReq): SendLocalListRes = ???
        def getLocalListVersion: GetLocalListVersionRes = ???
        def dataTransfer(req: ChargePointDataTransferReq): ChargePointDataTransferRes = ???
        def reserveNow(req: ReserveNowReq): ReserveNowRes = ???
        def cancelReservation(req: CancelReservationReq): CancelReservationRes = ???
        def clearChargingProfile(req: ClearChargingProfileReq): ClearChargingProfileRes = ???
        def getCompositeSchedule(req: GetCompositeScheduleReq): GetCompositeScheduleRes = ???
        def setChargingProfile(req: SetChargingProfileReq): SetChargingProfileRes = ???
        def triggerMessage(req: TriggerMessageReq): TriggerMessageRes = ???
      }

      val futureResult = syncReqH.apply(ClearCacheReq)
      Await.result(futureResult, 1.second) must throwA[OcppException]
    }
  }
}
