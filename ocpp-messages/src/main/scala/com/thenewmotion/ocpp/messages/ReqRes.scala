package com.thenewmotion.ocpp.messages

/**
 * ReqRes works as a multi-parameter typeclass for associating response types to request types. That is, if an implicit
 * value with type ReqRes[REQ, RES] is in scope, it means that RES is the response type that belongs to a request of
 * type REQ.
 *
 * @tparam REQ
 * @tparam RES
 */
trait ReqRes[REQ <: Req, RES <: Res]

trait ChargePointReqRes[REQ <: ChargePointReq, RES <: ChargePointRes] extends ReqRes[REQ, RES] {
  def apply(req: REQ)(implicit service: ChargePoint): RES
}

trait CentralSystemReqRes[REQ <: CentralSystemReq, RES <: CentralSystemRes] extends ReqRes[REQ, RES] {
  def apply(req: REQ)(implicit service: CentralSystem): RES
}

object ReqRes {
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

  implicit object ChargePointDataTransferReqRes extends ChargePointReqRes[ChargePointDataTransferReq, ChargePointDataTransferRes] {
    def apply(req: ChargePointDataTransferReq)(implicit service: ChargePoint) = service.dataTransfer(req)
  }

  implicit object ReserveNowReqRes extends ChargePointReqRes[ReserveNowReq, ReserveNowRes] {
    def apply(req: ReserveNowReq)(implicit service: ChargePoint) = service.reserveNow(req)
  }

  implicit object CancelReservationReqRes extends ChargePointReqRes[CancelReservationReq, CancelReservationRes] {
    def apply(req: CancelReservationReq)(implicit service: ChargePoint) = service.cancelReservation(req)
  }

  implicit object GenericChargePointReqRes extends ChargePointReqRes[ChargePointReq, ChargePointRes] {
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
      case x: ChargePointDataTransferReq => ChargePointDataTransferReqRes(x)(service)
      case x: ReserveNowReq => ReserveNowReqRes(x)
      case x: CancelReservationReq => CancelReservationReqRes(x)
    }
  }

  implicit object AuthorizeReqRes extends CentralSystemReqRes[AuthorizeReq, AuthorizeRes] {
    def apply(req: AuthorizeReq)(implicit service: CentralSystem) = service.authorize(req)
  }

  implicit object StartTransactionReqRes extends CentralSystemReqRes[StartTransactionReq, StartTransactionRes] {
    def apply(req: StartTransactionReq)(implicit service: CentralSystem) = service.startTransaction(req)
  }

  implicit object StopTransactionReqRes extends CentralSystemReqRes[StopTransactionReq, StopTransactionRes] {
    def apply(req: StopTransactionReq)(implicit service: CentralSystem) = service.stopTransaction(req)
  }

  implicit object HeartbeatReqRes extends CentralSystemReqRes[HeartbeatReq.type, HeartbeatRes] {
    def apply(req: HeartbeatReq.type)(implicit service: CentralSystem) = service.heartbeat
  }

  implicit object MeterValuesReqRes extends CentralSystemReqRes[MeterValuesReq, MeterValuesRes.type] {
    def apply(req: MeterValuesReq)(implicit service: CentralSystem) = {
      service.meterValues(req)
      MeterValuesRes
    }
  }

  implicit object BootNotificationReqRes extends CentralSystemReqRes[BootNotificationReq, BootNotificationRes] {
    def apply(req: BootNotificationReq)(implicit service: CentralSystem) = service.bootNotification(req)
  }

  implicit object CentralSystemDataTransferReqRes extends CentralSystemReqRes[CentralSystemDataTransferReq, CentralSystemDataTransferRes] {
    def apply(req: CentralSystemDataTransferReq)(implicit service: CentralSystem) = service.dataTransfer(req)
  }

  implicit object StatusNotificationReqRes extends CentralSystemReqRes[StatusNotificationReq, StatusNotificationRes.type] {
    def apply(req: StatusNotificationReq)(implicit service: CentralSystem) = {
      service.statusNotification(req)
      StatusNotificationRes
    }
  }

  implicit object FirmwareStatusNotificationReqRes extends CentralSystemReqRes[FirmwareStatusNotificationReq, FirmwareStatusNotificationRes.type] {
    def apply(req: FirmwareStatusNotificationReq)(implicit service: CentralSystem) = {
      service.firmwareStatusNotification(req)
      FirmwareStatusNotificationRes
    }
  }

  implicit object DiagnosticsStatusNotificationReqRes extends CentralSystemReqRes[DiagnosticsStatusNotificationReq, DiagnosticsStatusNotificationRes.type] {
    def apply(req: DiagnosticsStatusNotificationReq)(implicit service: CentralSystem) = {
      service.diagnosticsStatusNotification(req)
      DiagnosticsStatusNotificationRes
    }
  }

  implicit object GenericCentralSystemReqRes extends CentralSystemReqRes[CentralSystemReq, CentralSystemRes] {
    def apply(req: CentralSystemReq)(implicit service: CentralSystem) = req match {
      case x: AuthorizeReq => AuthorizeReqRes(x)
      case x: StartTransactionReq => StartTransactionReqRes(x)
      case x: StopTransactionReq => StopTransactionReqRes(x)
      case x: HeartbeatReq.type => HeartbeatReqRes(x)
      case x: MeterValuesReq => MeterValuesReqRes(x)
      case x: BootNotificationReq => BootNotificationReqRes(x)
      case x: StatusNotificationReq => StatusNotificationReqRes(x)
      case x: FirmwareStatusNotificationReq => FirmwareStatusNotificationReqRes(x)
      case x: DiagnosticsStatusNotificationReq => DiagnosticsStatusNotificationReqRes(x)
      case x: CentralSystemDataTransferReq => CentralSystemDataTransferReqRes(x)(service)
    }
  }
}
