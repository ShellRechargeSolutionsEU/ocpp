package com.thenewmotion.ocpp.messages.v1x

import enums.reflection.EnumUtils.{Enumerable, Nameable}

sealed trait ChargePointAction extends Nameable
object ChargePointAction        extends Enumerable[ChargePointAction] {
  object CancelReservation      extends ChargePointAction
  object ChangeAvailability     extends ChargePointAction
  object ChangeConfiguration    extends ChargePointAction
  object ClearCache             extends ChargePointAction
  object DataTransfer           extends ChargePointAction
  object GetConfiguration       extends ChargePointAction
  object GetDiagnostics         extends ChargePointAction
  object GetLocalListVersion    extends ChargePointAction
  object RemoteStartTransaction extends ChargePointAction
  object RemoteStopTransaction  extends ChargePointAction
  object ReserveNow             extends ChargePointAction
  object Reset                  extends ChargePointAction
  object SendLocalList          extends ChargePointAction
  object UnlockConnector        extends ChargePointAction
  object UpdateFirmware         extends ChargePointAction
  object SetChargingProfile     extends ChargePointAction
  object ClearChargingProfile   extends ChargePointAction
  object GetCompositeSchedule   extends ChargePointAction
  object TriggerMessage         extends ChargePointAction

  val values = Set(
    CancelReservation,
    ChangeAvailability,
    ChangeConfiguration,
    ClearCache,
    DataTransfer,
    GetConfiguration,
    GetDiagnostics,
    GetLocalListVersion,
    RemoteStartTransaction,
    RemoteStopTransaction,
    ReserveNow,
    Reset,
    SendLocalList,
    UnlockConnector,
    UpdateFirmware,
    SetChargingProfile,
    ClearChargingProfile,
    GetCompositeSchedule,
    TriggerMessage
  )
}