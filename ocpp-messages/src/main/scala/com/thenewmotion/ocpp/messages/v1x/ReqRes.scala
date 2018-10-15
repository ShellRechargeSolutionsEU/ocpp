package com.thenewmotion.ocpp
package messages
package v1x

import scala.concurrent.{Future, ExecutionContext}

sealed trait ChargePointReqRes[REQ <: ChargePointReq, RES <: ChargePointRes] extends ReqRes[REQ, RES] {
  def apply(req: REQ)(implicit service: SyncChargePoint): RES
  def applyAsync(req: REQ)(implicit service: ChargePoint, ec: ExecutionContext): Future[RES]
}

sealed trait CentralSystemReqRes[REQ <: CentralSystemReq, RES <: CentralSystemRes] extends ReqRes[REQ, RES] {
  def apply(req: REQ)(implicit service: SyncCentralSystem): RES
  def applyAsync(req: REQ)(implicit service: CentralSystem, ec: ExecutionContext): Future[RES]
}

trait ReqResInstances extends ChargePointReqResInstances with CentralSystemReqResInstances

trait ChargePointReqResInstances {

  implicit object RemoteStartTransactionReqRes extends ChargePointReqRes[RemoteStartTransactionReq, RemoteStartTransactionRes] {
    def apply(req: RemoteStartTransactionReq)(implicit service: SyncChargePoint) = service.remoteStartTransaction(req)

    def applyAsync(req: RemoteStartTransactionReq)
      (implicit service: ChargePoint, ec: ExecutionContext) = service.remoteStartTransaction(req)
  }

  implicit object RemoteStopTransactionReqRes extends ChargePointReqRes[RemoteStopTransactionReq, RemoteStopTransactionRes] {
    def apply(req: RemoteStopTransactionReq)(implicit service: SyncChargePoint) = service.remoteStopTransaction(req)

    def applyAsync(req: RemoteStopTransactionReq)
      (implicit service: ChargePoint, ec: ExecutionContext) = service.remoteStopTransaction(req)
  }

  implicit object UnlockConnectorReqRes extends ChargePointReqRes[UnlockConnectorReq, UnlockConnectorRes] {
    def apply(req: UnlockConnectorReq)(implicit service: SyncChargePoint) = service.unlockConnector(req)

    def applyAsync(req: UnlockConnectorReq)
      (implicit service: ChargePoint, ec: ExecutionContext) = service.unlockConnector(req)
  }

  implicit object GetDiagnosticsReqRes extends ChargePointReqRes[GetDiagnosticsReq, GetDiagnosticsRes] {
    def apply(req: GetDiagnosticsReq)(implicit service: SyncChargePoint) = service.getDiagnostics(req)

    def applyAsync(req: GetDiagnosticsReq)
      (implicit service: ChargePoint, ec: ExecutionContext) = service.getDiagnostics(req)
  }

  implicit object ChangeConfigurationReqRes extends ChargePointReqRes[ChangeConfigurationReq, ChangeConfigurationRes] {
    def apply(req: ChangeConfigurationReq)(implicit service: SyncChargePoint) = service.changeConfiguration(req)

    def applyAsync(req: ChangeConfigurationReq)
      (implicit service: ChargePoint, ec: ExecutionContext) = service.changeConfiguration(req)
  }

  implicit object GetConfigurationReqRes extends ChargePointReqRes[GetConfigurationReq, GetConfigurationRes] {
    def apply(req: GetConfigurationReq)(implicit service: SyncChargePoint) = service.getConfiguration(req)

    def applyAsync(req: GetConfigurationReq)
      (implicit service: ChargePoint, ec: ExecutionContext) = service.getConfiguration(req)
  }

  implicit object ChangeAvailabilityReqRes extends ChargePointReqRes[ChangeAvailabilityReq, ChangeAvailabilityRes] {
    def apply(req: ChangeAvailabilityReq)(implicit service: SyncChargePoint) = service.changeAvailability(req)

    def applyAsync(req: ChangeAvailabilityReq)
      (implicit service: ChargePoint, ec: ExecutionContext) = service.changeAvailability(req)
  }

  implicit object ClearCacheReqRes extends ChargePointReqRes[ClearCacheReq.type, ClearCacheRes] {
    def apply(req: ClearCacheReq.type)(implicit service: SyncChargePoint) = service.clearCache

    def applyAsync(req: ClearCacheReq.type)(implicit service: ChargePoint, ec: ExecutionContext) = service.clearCache
  }

  implicit object ResetReqRes extends ChargePointReqRes[ResetReq, ResetRes] {
    def apply(req: ResetReq)(implicit service: SyncChargePoint) = service.reset(req)

    def applyAsync(req: ResetReq)(implicit service: ChargePoint, ec: ExecutionContext) = service.reset(req)
  }

  implicit object UpdateFirmwareReqRes extends ChargePointReqRes[UpdateFirmwareReq, UpdateFirmwareRes.type] {
    def apply(req: UpdateFirmwareReq)(implicit service: SyncChargePoint) = {
      service.updateFirmware(req)
      UpdateFirmwareRes
    }

    def applyAsync(req: UpdateFirmwareReq)(implicit service: ChargePoint, ec: ExecutionContext) =
      service.updateFirmware(req).map(_ => UpdateFirmwareRes)
  }

  implicit object SendLocalListReqRes extends ChargePointReqRes[SendLocalListReq, SendLocalListRes] {
    def apply(req: SendLocalListReq)(implicit service: SyncChargePoint) = service.sendLocalList(req)

    def applyAsync(req: SendLocalListReq)(implicit service: ChargePoint, ec: ExecutionContext) = service.sendLocalList(
      req
    )
  }

  implicit object GetLocalListVersionReqRes extends ChargePointReqRes[GetLocalListVersionReq.type, GetLocalListVersionRes] {
    def apply(req: GetLocalListVersionReq.type)(implicit service: SyncChargePoint) = service.getLocalListVersion

    def applyAsync(req: GetLocalListVersionReq.type)
      (implicit service: ChargePoint, ec: ExecutionContext) = service.getLocalListVersion
  }

  implicit object ChargePointDataTransferReqRes extends ChargePointReqRes[ChargePointDataTransferReq, ChargePointDataTransferRes] {
    def apply(req: ChargePointDataTransferReq)(implicit service: SyncChargePoint) = service.dataTransfer(req)

    def applyAsync(req: ChargePointDataTransferReq)
      (implicit service: ChargePoint, ec: ExecutionContext) = service.dataTransfer(req)
  }

  implicit object ReserveNowReqRes extends ChargePointReqRes[ReserveNowReq, ReserveNowRes] {
    def apply(req: ReserveNowReq)(implicit service: SyncChargePoint) = service.reserveNow(req)

    def applyAsync(req: ReserveNowReq)(implicit service: ChargePoint, ec: ExecutionContext) = service.reserveNow(req)
  }

  implicit object CancelReservationReqRes extends ChargePointReqRes[CancelReservationReq, CancelReservationRes] {
    def apply(req: CancelReservationReq)(implicit service: SyncChargePoint) = service.cancelReservation(req)

    def applyAsync(req: CancelReservationReq)
      (implicit service: ChargePoint, ec: ExecutionContext) = service.cancelReservation(req)
  }

  implicit object ClearChargingProfileReqRes extends ChargePointReqRes[ClearChargingProfileReq, ClearChargingProfileRes] {
    def apply(req: ClearChargingProfileReq)(implicit service: SyncChargePoint) = service.clearChargingProfile(req)

    def applyAsync(req: ClearChargingProfileReq)
      (implicit service: ChargePoint, ec: ExecutionContext) = service.clearChargingProfile(req)
  }

  implicit object GetCompositeScheduleReqRes extends ChargePointReqRes[GetCompositeScheduleReq, GetCompositeScheduleRes] {
    def apply(req: GetCompositeScheduleReq)(implicit service: SyncChargePoint) = service.getCompositeSchedule(req)

    def applyAsync(req: GetCompositeScheduleReq)
      (implicit service: ChargePoint, ec: ExecutionContext) = service.getCompositeSchedule(req)
  }

  implicit object SetChargingProfileReqRes extends ChargePointReqRes[SetChargingProfileReq, SetChargingProfileRes] {
    def apply(req: SetChargingProfileReq)(implicit service: SyncChargePoint) = service.setChargingProfile(req)

    def applyAsync(req: SetChargingProfileReq)
      (implicit service: ChargePoint, ec: ExecutionContext) = service.setChargingProfile(req)
  }

  implicit object TriggerMessageReqRes extends ChargePointReqRes[TriggerMessageReq, TriggerMessageRes] {
    def apply(req: TriggerMessageReq)(implicit service: SyncChargePoint) = service.triggerMessage(req)

    def applyAsync(req: TriggerMessageReq)
      (implicit service: ChargePoint, ec: ExecutionContext) = service.triggerMessage(req)
  }

  implicit object GenericChargePointReqRes extends ChargePointReqRes[ChargePointReq, ChargePointRes] {
    def apply(req: ChargePointReq)(implicit service: SyncChargePoint) = req match {
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
      case x: ClearChargingProfileReq => ClearChargingProfileReqRes(x)
      case x: GetCompositeScheduleReq => GetCompositeScheduleReqRes(x)
      case x: SetChargingProfileReq => SetChargingProfileReqRes(x)
      case x: TriggerMessageReq => TriggerMessageReqRes(x)
    }

    def applyAsync(req: ChargePointReq)(implicit service: ChargePoint, ec: ExecutionContext) = req match {
      case x: RemoteStartTransactionReq => RemoteStartTransactionReqRes.applyAsync(x)
      case x: RemoteStopTransactionReq => RemoteStopTransactionReqRes.applyAsync(x)
      case x: UnlockConnectorReq => UnlockConnectorReqRes.applyAsync(x)
      case x: GetDiagnosticsReq => GetDiagnosticsReqRes.applyAsync(x)
      case x: ChangeConfigurationReq => ChangeConfigurationReqRes.applyAsync(x)
      case x: GetConfigurationReq => GetConfigurationReqRes.applyAsync(x)
      case x: ChangeAvailabilityReq => ChangeAvailabilityReqRes.applyAsync(x)
      case x: ClearCacheReq.type => ClearCacheReqRes.applyAsync(x)
      case x: ResetReq => ResetReqRes.applyAsync(x)
      case x: UpdateFirmwareReq => UpdateFirmwareReqRes.applyAsync(x)
      case x: SendLocalListReq => SendLocalListReqRes.applyAsync(x)
      case x: GetLocalListVersionReq.type => GetLocalListVersionReqRes.applyAsync(x)
      case x: ChargePointDataTransferReq => ChargePointDataTransferReqRes.applyAsync(x)
      case x: ReserveNowReq => ReserveNowReqRes.applyAsync(x)
      case x: CancelReservationReq => CancelReservationReqRes.applyAsync(x)
      case x: ClearChargingProfileReq => ClearChargingProfileReqRes.applyAsync(x)
      case x: GetCompositeScheduleReq => GetCompositeScheduleReqRes.applyAsync(x)
      case x: SetChargingProfileReq => SetChargingProfileReqRes.applyAsync(x)
      case x: TriggerMessageReq => TriggerMessageReqRes.applyAsync(x)
    }
  }
}

trait CentralSystemReqResInstances {

  implicit object AuthorizeReqRes extends CentralSystemReqRes[AuthorizeReq, AuthorizeRes] {
    def apply(req: AuthorizeReq)(implicit service: SyncCentralSystem) = service.authorize(req)

    def applyAsync(req: AuthorizeReq)(implicit service: CentralSystem, ec: ExecutionContext) = service.authorize(req)
  }

  implicit object StartTransactionReqRes extends CentralSystemReqRes[StartTransactionReq, StartTransactionRes] {
    def apply(req: StartTransactionReq)(implicit service: SyncCentralSystem) = service.startTransaction(req)

    def applyAsync(req: StartTransactionReq)
      (implicit service: CentralSystem, ec: ExecutionContext) = service.startTransaction(req)
  }

  implicit object StopTransactionReqRes extends CentralSystemReqRes[StopTransactionReq, StopTransactionRes] {
    def apply(req: StopTransactionReq)(implicit service: SyncCentralSystem) = service.stopTransaction(req)

    def applyAsync(req: StopTransactionReq)
      (implicit service: CentralSystem, ec: ExecutionContext) = service.stopTransaction(req)
  }

  implicit object HeartbeatReqRes extends CentralSystemReqRes[HeartbeatReq.type, HeartbeatRes] {
    def apply(req: HeartbeatReq.type)(implicit service: SyncCentralSystem) = service.heartbeat

    def applyAsync(req: HeartbeatReq.type)(implicit service: CentralSystem, ec: ExecutionContext) = service.heartbeat
  }

  implicit object MeterValuesReqRes extends CentralSystemReqRes[MeterValuesReq, MeterValuesRes.type] {
    def apply(req: MeterValuesReq)(implicit service: SyncCentralSystem) = {
      service.meterValues(req)
      MeterValuesRes
    }

    def applyAsync(req: MeterValuesReq)(implicit service: CentralSystem, ec: ExecutionContext) =
      service.meterValues(req).map(_ => MeterValuesRes)
  }

  implicit object BootNotificationReqRes extends CentralSystemReqRes[BootNotificationReq, BootNotificationRes] {
    def apply(req: BootNotificationReq)(implicit service: SyncCentralSystem) = service.bootNotification(req)

    def applyAsync(req: BootNotificationReq)
      (implicit service: CentralSystem, ec: ExecutionContext) = service.bootNotification(req)
  }

  implicit object CentralSystemDataTransferReqRes extends CentralSystemReqRes[CentralSystemDataTransferReq, CentralSystemDataTransferRes] {
    def apply(req: CentralSystemDataTransferReq)(implicit service: SyncCentralSystem) = service.dataTransfer(req)

    def applyAsync(req: CentralSystemDataTransferReq)
      (implicit service: CentralSystem, ec: ExecutionContext) = service.dataTransfer(req)
  }

  implicit object StatusNotificationReqRes extends CentralSystemReqRes[StatusNotificationReq, StatusNotificationRes.type] {
    def apply(req: StatusNotificationReq)(implicit service: SyncCentralSystem) = {
      service.statusNotification(req)
      StatusNotificationRes
    }

    def applyAsync(req: StatusNotificationReq)(implicit service: CentralSystem, ec: ExecutionContext) =
      service.statusNotification(req).map(_ => StatusNotificationRes)
  }

  implicit object FirmwareStatusNotificationReqRes extends CentralSystemReqRes[FirmwareStatusNotificationReq, FirmwareStatusNotificationRes.type] {
    def apply(req: FirmwareStatusNotificationReq)(implicit service: SyncCentralSystem) = {
      service.firmwareStatusNotification(req)
      FirmwareStatusNotificationRes
    }

    def applyAsync(req: FirmwareStatusNotificationReq)(implicit service: CentralSystem, ec: ExecutionContext) =
      service.firmwareStatusNotification(req).map(_ => FirmwareStatusNotificationRes)
  }

  implicit object DiagnosticsStatusNotificationReqRes extends CentralSystemReqRes[DiagnosticsStatusNotificationReq, DiagnosticsStatusNotificationRes.type] {
    def apply(req: DiagnosticsStatusNotificationReq)(implicit service: SyncCentralSystem) = {
      service.diagnosticsStatusNotification(req)
      DiagnosticsStatusNotificationRes
    }

    def applyAsync(req: DiagnosticsStatusNotificationReq)(implicit service: CentralSystem, ec: ExecutionContext) =
      service.diagnosticsStatusNotification(req).map(_ => DiagnosticsStatusNotificationRes)
  }

  implicit object GenericCentralSystemReqRes extends CentralSystemReqRes[CentralSystemReq, CentralSystemRes] {
    def apply(req: CentralSystemReq)(implicit service: SyncCentralSystem) = req match {
      case x: AuthorizeReq => AuthorizeReqRes(x)
      case x: StartTransactionReq => StartTransactionReqRes(x)
      case x: StopTransactionReq => StopTransactionReqRes(x)
      case x: HeartbeatReq.type => HeartbeatReqRes(x)
      case x: MeterValuesReq => MeterValuesReqRes(x)
      case x: BootNotificationReq => BootNotificationReqRes(x)
      case x: StatusNotificationReq => StatusNotificationReqRes(x)
      case x: FirmwareStatusNotificationReq => FirmwareStatusNotificationReqRes(x)
      case x: DiagnosticsStatusNotificationReq => DiagnosticsStatusNotificationReqRes(x)
      case x: CentralSystemDataTransferReq => CentralSystemDataTransferReqRes(x)
    }

    def applyAsync(req: CentralSystemReq)(implicit service: CentralSystem, ec: ExecutionContext) = req match {
      case x: AuthorizeReq => AuthorizeReqRes.applyAsync(x)
      case x: StartTransactionReq => StartTransactionReqRes.applyAsync(x)
      case x: StopTransactionReq => StopTransactionReqRes.applyAsync(x)
      case x: HeartbeatReq.type => HeartbeatReqRes.applyAsync(x)
      case x: MeterValuesReq => MeterValuesReqRes.applyAsync(x)
      case x: BootNotificationReq => BootNotificationReqRes.applyAsync(x)
      case x: StatusNotificationReq => StatusNotificationReqRes.applyAsync(x)
      case x: FirmwareStatusNotificationReq => FirmwareStatusNotificationReqRes.applyAsync(x)
      case x: DiagnosticsStatusNotificationReq => DiagnosticsStatusNotificationReqRes.applyAsync(x)
      case x: CentralSystemDataTransferReq => CentralSystemDataTransferReqRes.applyAsync(x)
    }
  }
}
