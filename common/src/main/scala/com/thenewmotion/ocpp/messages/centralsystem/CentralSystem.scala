package com.thenewmotion.ocpp
package messages
package centralsystem

/**
 * @author Yaroslav Klymko
 */
trait CentralSystem {
  def authorize(req: AuthorizeReq): AuthorizeRes
  def startTransaction(req: StartTransactionReq): StartTransactionRes
  def stopTransaction(req: StopTransactionReq): StopTransactionRes
  def heartbeat: HeartbeatRes
  def meterValues(req: MeterValuesReq)
  def bootNotification(req: BootNotificationReq): BootNotificationRes
  def statusNotification(req: StatusNotificationReq)
  def firmwareStatusNotification(req: FirmwareStatusNotificationReq)
  def diagnosticsStatusNotification(req: DiagnosticsStatusNotificationReq)
  def dataTransfer(req: DataTransferReq): DataTransferRes

  def apply[REQ <: Req, RES <: Res](req: REQ)(implicit reqRes: ReqRes[REQ, RES]): RES = reqRes(req)(this)
}
