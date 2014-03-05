package com.thenewmotion.ocpp
package messages
package chargepoint

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
  def dataTransfer(req: DataTransferReq): DataTransferRes
  def reserveNow(req: ReserveNowReq): ReserveNowRes
  def cancelReservation(req: CancelReservationReq): CancelReservationRes

  def apply[REQ <: Req, RES <: Res](req: REQ)(implicit reqRes: ReqRes[REQ, RES]): RES = reqRes(req)(this)
}

