package com.thenewmotion.ocpp.messages

/**
 * @author Yaroslav Klymko
 */
trait ChargePoint {
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

  def apply[REQ <: ChargePointReq, RES <: ChargePointRes](req: REQ)(implicit reqRes: ChargePointReqRes[REQ, RES]): RES = reqRes(req)(this)
}

