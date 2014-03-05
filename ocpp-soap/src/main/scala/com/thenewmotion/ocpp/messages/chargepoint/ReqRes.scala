package com.thenewmotion.ocpp
package messages
package chargepoint

/**
 * @author Yaroslav Klymko
 */
trait ReqRes[REQ <: Req, RES <: Res] {
  def apply(req: REQ)(implicit service: ChargePoint): RES
}

object ReqRes {
  implicit object RemoteStartTransactionReqRes extends ReqRes[RemoteStartTransactionReq, RemoteStartTransactionRes] {
    def apply(req: RemoteStartTransactionReq)(implicit service: ChargePoint) = service.remoteStartTransaction(req)
  }

  implicit object RemoteStopTransactionReqRes extends ReqRes[RemoteStopTransactionReq, RemoteStopTransactionRes] {
    def apply(req: RemoteStopTransactionReq)(implicit service: ChargePoint) = service.remoteStopTransaction(req)
  }

  implicit object UnlockConnectorReqRes extends ReqRes[UnlockConnectorReq, UnlockConnectorRes] {
    def apply(req: UnlockConnectorReq)(implicit service: ChargePoint) = service.unlockConnector(req)
  }

  implicit object GetDiagnosticsReqRes extends ReqRes[GetDiagnosticsReq, GetDiagnosticsRes] {
    def apply(req: GetDiagnosticsReq)(implicit service: ChargePoint) = service.getDiagnostics(req)
  }

  implicit object ChangeConfigurationReqRes extends ReqRes[ChangeConfigurationReq, ChangeConfigurationRes] {
    def apply(req: ChangeConfigurationReq)(implicit service: ChargePoint) = service.changeConfiguration(req)
  }

  implicit object GetConfigurationReqRes extends ReqRes[GetConfigurationReq, GetConfigurationRes] {
    def apply(req: GetConfigurationReq)(implicit service: ChargePoint) = service.getConfiguration(req)
  }

  implicit object ChangeAvailabilityReqRes extends ReqRes[ChangeAvailabilityReq, ChangeAvailabilityRes] {
    def apply(req: ChangeAvailabilityReq)(implicit service: ChargePoint) = service.changeAvailability(req)
  }

  implicit object ClearCacheReqRes extends ReqRes[ClearCacheReq.type, ClearCacheRes] {
    def apply(req: ClearCacheReq.type)(implicit service: ChargePoint) = service.clearCache
  }

  implicit object ResetReqRes extends ReqRes[ResetReq, ResetRes] {
    def apply(req: ResetReq)(implicit service: ChargePoint) = service.reset(req)
  }

  implicit object UpdateFirmwareReqRes extends ReqRes[UpdateFirmwareReq, UpdateFirmwareRes.type] {
    def apply(req: UpdateFirmwareReq)(implicit service: ChargePoint) = {
      service.updateFirmware(req)
      UpdateFirmwareRes
    }
  }

  implicit object SendLocalListReqRes extends ReqRes[SendLocalListReq, SendLocalListRes] {
    def apply(req: SendLocalListReq)(implicit service: ChargePoint) = service.sendLocalList(req)
  }

  implicit object GetLocalListVersionReqRes extends ReqRes[GetLocalListVersionReq.type, GetLocalListVersionRes] {
    def apply(req: GetLocalListVersionReq.type)(implicit service: ChargePoint) = service.getLocalListVersion
  }

  implicit object DataTransferReqRes extends ReqRes[DataTransferReq, DataTransferRes] {
    def apply(req: DataTransferReq)(implicit service: ChargePoint) = service.dataTransfer(req)
  }

  implicit object ReserveNowReqRes extends ReqRes[ReserveNowReq, ReserveNowRes] {
    def apply(req: ReserveNowReq)(implicit service: ChargePoint) = service.reserveNow(req)
  }

  implicit object CancelReservationReqRes extends ReqRes[CancelReservationReq, CancelReservationRes] {
    def apply(req: CancelReservationReq)(implicit service: ChargePoint) = service.cancelReservation(req)
  }

  implicit object ReqRes extends ReqRes[Req, Res] {
    def apply(req: Req)(implicit service: ChargePoint) = req match {
      case x: RemoteStartTransactionReq => RemoteStartTransactionReqRes(x)
      case x: RemoteStopTransactionReq => RemoteStopTransactionReqRes(x)
      case x: UnlockConnectorReq => UnlockConnectorReqRes(x)
      case x: GetDiagnosticsReq => GetDiagnosticsReqRes(x)
      case x: ChangeConfigurationReq => ChangeConfigurationReqRes(x)
      case x: GetConfigurationReq => GetConfigurationReqRes(x)
      case x: ChangeAvailabilityReq => ChangeAvailabilityReqRes(x)
      case x: ClearCacheReq.type => ClearCacheReqRes(x)
      case x: ResetReq => ResetReqRes(x)
      case x: UpdateFirmwareReq => UpdateFirmwareReqRes(x)
      case x: SendLocalListReq => SendLocalListReqRes(x)
      case x: GetLocalListVersionReq.type => GetLocalListVersionReqRes(x)
      case x: DataTransferReq => DataTransferReqRes(x)
      case x: ReserveNowReq => ReserveNowReqRes(x)
      case x: CancelReservationReq => CancelReservationReqRes(x)
    }
  }
}