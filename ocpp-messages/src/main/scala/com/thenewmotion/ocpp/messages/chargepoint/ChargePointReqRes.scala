package com.thenewmotion.ocpp
package messages
package chargepoint

/**
 * @author Yaroslav Klymko
 */
trait ChargePointReqRes[REQ <: ChargePointReq, RES <: ChargePointRes] extends ReqRes[REQ, RES] {
  def apply(req: REQ)(implicit service: ChargePoint): RES
}

object ChargePointReqRes {
  implicit object RemoteStartTransactionReqRes extends ChargePointReqRes[RemoteStartTransactionReq, RemoteStartTransactionRes] {
    def apply(req: RemoteStartTransactionReq)(implicit service: ChargePoint) = service.remoteStartTransaction(req)
  }

  implicit object RemoteStopTransactionReqRes extends ChargePointReqRes[RemoteStopTransactionReq, RemoteStopTransactionRes] {
    def apply(req: RemoteStopTransactionReq)(implicit service: ChargePoint) = service.remoteStopTransaction(req)
  }

  implicit object UnlockConnectorReqRes extends ChargePointReqRes[UnlockConnectorReq, UnlockConnectorRes] {
    def apply(req: UnlockConnectorReq)(implicit service: ChargePoint) = service.unlockConnector(req)
  }

  implicit object GetDiagnosticsReqRes extends ChargePointReqRes[GetDiagnosticsReq, GetDiagnosticsRes] {
    def apply(req: GetDiagnosticsReq)(implicit service: ChargePoint) = service.getDiagnostics(req)
  }

  implicit object ChangeConfigurationReqRes extends ChargePointReqRes[ChangeConfigurationReq, ChangeConfigurationRes] {
    def apply(req: ChangeConfigurationReq)(implicit service: ChargePoint) = service.changeConfiguration(req)
  }

  implicit object GetConfigurationReqRes extends ChargePointReqRes[GetConfigurationReq, GetConfigurationRes] {
    def apply(req: GetConfigurationReq)(implicit service: ChargePoint) = service.getConfiguration(req)
  }

  implicit object ChangeAvailabilityReqRes extends ChargePointReqRes[ChangeAvailabilityReq, ChangeAvailabilityRes] {
    def apply(req: ChangeAvailabilityReq)(implicit service: ChargePoint) = service.changeAvailability(req)
  }

  implicit object ClearCacheReqRes extends ChargePointReqRes[ClearCacheReq.type, ClearCacheRes] {
    def apply(req: ClearCacheReq.type)(implicit service: ChargePoint) = service.clearCache
  }

  implicit object ResetReqRes extends ChargePointReqRes[ResetReq, ResetRes] {
    def apply(req: ResetReq)(implicit service: ChargePoint) = service.reset(req)
  }

  implicit object UpdateFirmwareReqRes extends ChargePointReqRes[UpdateFirmwareReq, UpdateFirmwareRes.type] {
    def apply(req: UpdateFirmwareReq)(implicit service: ChargePoint) = {
      service.updateFirmware(req)
      UpdateFirmwareRes
    }
  }

  implicit object SendLocalListReqRes extends ChargePointReqRes[SendLocalListReq, SendLocalListRes] {
    def apply(req: SendLocalListReq)(implicit service: ChargePoint) = service.sendLocalList(req)
  }

  implicit object GetLocalListVersionReqRes extends ChargePointReqRes[GetLocalListVersionReq.type, GetLocalListVersionRes] {
    def apply(req: GetLocalListVersionReq.type)(implicit service: ChargePoint) = service.getLocalListVersion
  }

  implicit object DataTransferReqRes extends ChargePointReqRes[DataTransferReq, DataTransferRes] {
    def apply(req: DataTransferReq)(implicit service: ChargePoint) = service.dataTransfer(req)
  }

  implicit object ReserveNowReqRes extends ChargePointReqRes[ReserveNowReq, ReserveNowRes] {
    def apply(req: ReserveNowReq)(implicit service: ChargePoint) = service.reserveNow(req)
  }

  implicit object CancelReservationReqRes extends ChargePointReqRes[CancelReservationReq, CancelReservationRes] {
    def apply(req: CancelReservationReq)(implicit service: ChargePoint) = service.cancelReservation(req)
  }

  implicit object ReqRes extends ChargePointReqRes[ChargePointReq, ChargePointRes] {
    def apply(req: ChargePointReq)(implicit service: ChargePoint) = req match {
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