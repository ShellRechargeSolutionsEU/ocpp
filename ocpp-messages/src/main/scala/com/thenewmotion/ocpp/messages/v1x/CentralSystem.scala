package com.thenewmotion.ocpp.messages.v1x

import scala.concurrent.{ExecutionContext, Future}

trait CentralSystem {

  def authorize(req: AuthorizeReq): Future[AuthorizeRes]
  def startTransaction(req: StartTransactionReq): Future[StartTransactionRes]
  def stopTransaction(req: StopTransactionReq): Future[StopTransactionRes]
  def heartbeat: Future[HeartbeatRes]
  def meterValues(req: MeterValuesReq): Future[Unit]
  def bootNotification(req: BootNotificationReq): Future[BootNotificationRes]
  def statusNotification(req: StatusNotificationReq): Future[Unit]
  def firmwareStatusNotification(req: FirmwareStatusNotificationReq): Future[Unit]
  def diagnosticsStatusNotification(req: DiagnosticsStatusNotificationReq): Future[Unit]
  def dataTransfer(req: CentralSystemDataTransferReq): Future[CentralSystemDataTransferRes]

  def apply[REQ <: CentralSystemReq, RES <: CentralSystemRes](req: REQ)(
    implicit reqRes: CentralSystemReqRes[REQ, RES],
    ec: ExecutionContext
  ): Future[RES] = reqRes.applyAsync(req)(this, ec)
}
