package com.thenewmotion.ocpp.messages.v1x

import scala.concurrent.{ExecutionContext, Future}

trait ChargePoint {

  def remoteStartTransaction(req: RemoteStartTransactionReq): Future[RemoteStartTransactionRes]
  def remoteStopTransaction(req: RemoteStopTransactionReq): Future[RemoteStopTransactionRes]
  def unlockConnector(req: UnlockConnectorReq): Future[UnlockConnectorRes]
  def getDiagnostics(req: GetDiagnosticsReq): Future[GetDiagnosticsRes]
  def changeConfiguration(req: ChangeConfigurationReq): Future[ChangeConfigurationRes]
  def getConfiguration(req: GetConfigurationReq): Future[GetConfigurationRes]
  def changeAvailability(req: ChangeAvailabilityReq): Future[ChangeAvailabilityRes]
  def clearCache: Future[ClearCacheRes]
  def reset(req: ResetReq): Future[ResetRes]
  def updateFirmware(req: UpdateFirmwareReq): Future[Unit]
  def sendLocalList(req: SendLocalListReq): Future[SendLocalListRes]
  def getLocalListVersion: Future[GetLocalListVersionRes]
  def dataTransfer(req: ChargePointDataTransferReq): Future[ChargePointDataTransferRes]
  def reserveNow(req: ReserveNowReq): Future[ReserveNowRes]
  def cancelReservation(req: CancelReservationReq): Future[CancelReservationRes]
  def clearChargingProfile(req: ClearChargingProfileReq): Future[ClearChargingProfileRes]
  def getCompositeSchedule(req: GetCompositeScheduleReq): Future[GetCompositeScheduleRes]
  def setChargingProfile(req: SetChargingProfileReq): Future[SetChargingProfileRes]
  def triggerMessage(req: TriggerMessageReq): Future[TriggerMessageRes]

  def apply[REQ <: ChargePointReq, RES <: ChargePointRes](req: REQ)(
    implicit reqRes: ChargePointReqRes[REQ, RES],
    ec: ExecutionContext
  ): Future[RES] =
    reqRes.applyAsync(req)(this, ec)
}
