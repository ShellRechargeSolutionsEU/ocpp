package com.thenewmotion.ocpp
package messages
package v1x

trait SyncCentralSystem {
  def authorize(req: AuthorizeReq): AuthorizeRes
  def startTransaction(req: StartTransactionReq): StartTransactionRes
  def stopTransaction(req: StopTransactionReq): StopTransactionRes
  def heartbeat: HeartbeatRes
  def meterValues(req: MeterValuesReq)
  def bootNotification(req: BootNotificationReq): BootNotificationRes
  def statusNotification(req: StatusNotificationReq)
  def firmwareStatusNotification(req: FirmwareStatusNotificationReq)
  def diagnosticsStatusNotification(req: DiagnosticsStatusNotificationReq)
  def dataTransfer(req: CentralSystemDataTransferReq): CentralSystemDataTransferRes

  def apply[REQ <: CentralSystemReq, RES <: CentralSystemRes](req: REQ)(implicit reqRes: CentralSystemReqRes[REQ, RES]): RES = reqRes(req)(this)
}
