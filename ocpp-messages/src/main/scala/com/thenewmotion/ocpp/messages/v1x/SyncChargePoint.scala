package com.thenewmotion.ocpp
package messages
package v1x

trait SyncChargePoint {
  def remoteStartTransaction(req: RemoteStartTransactionReq): RemoteStartTransactionRes
  def remoteStopTransaction(req: RemoteStopTransactionReq): RemoteStopTransactionRes
  def unlockConnector(req: UnlockConnectorReq): UnlockConnectorRes
  def getDiagnostics(req: GetDiagnosticsReq): GetDiagnosticsRes
  def changeConfiguration(req: ChangeConfigurationReq): ChangeConfigurationRes
  def getConfiguration(req: GetConfigurationReq): GetConfigurationRes
  def changeAvailability(req: ChangeAvailabilityReq): ChangeAvailabilityRes
  def clearCache: ClearCacheRes
  def reset(req: ResetReq): ResetRes
  def updateFirmware(req: UpdateFirmwareReq)
  def sendLocalList(req: SendLocalListReq): SendLocalListRes
  def getLocalListVersion: GetLocalListVersionRes
  def dataTransfer(req: ChargePointDataTransferReq): ChargePointDataTransferRes
  def reserveNow(req: ReserveNowReq): ReserveNowRes
  def cancelReservation(req: CancelReservationReq): CancelReservationRes
  def clearChargingProfile(req: ClearChargingProfileReq): ClearChargingProfileRes
  def getCompositeSchedule(req: GetCompositeScheduleReq): GetCompositeScheduleRes
  def setChargingProfile(req: SetChargingProfileReq): SetChargingProfileRes
  def triggerMessage(req: TriggerMessageReq): TriggerMessageRes

  def apply[REQ <: ChargePointReq, RES <: ChargePointRes](req: REQ)(implicit reqRes: ChargePointReqRes[REQ, RES]): RES = reqRes(req)(this)
}
