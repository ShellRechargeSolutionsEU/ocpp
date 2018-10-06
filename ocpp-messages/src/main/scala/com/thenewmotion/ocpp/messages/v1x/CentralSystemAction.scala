package com.thenewmotion.ocpp.messages.v1x

import enums.reflection.EnumUtils.{Enumerable, Nameable}

sealed trait CentralSystemAction extends Nameable
object CentralSystemAction             extends Enumerable[CentralSystemAction] {
  object Authorize                     extends CentralSystemAction
  object StartTransaction              extends CentralSystemAction
  object StopTransaction               extends CentralSystemAction
  object BootNotification              extends CentralSystemAction
  object DiagnosticsStatusNotification extends CentralSystemAction
  object FirmwareStatusNotification    extends CentralSystemAction
  object Heartbeat                     extends CentralSystemAction
  object MeterValues                   extends CentralSystemAction
  object StatusNotification            extends CentralSystemAction
  object DataTransfer                  extends CentralSystemAction

  val values = Set(
    Authorize,
    StartTransaction,
    StopTransaction,
    BootNotification,
    DiagnosticsStatusNotification,
    FirmwareStatusNotification,
    Heartbeat,
    MeterValues,
    StatusNotification,
    DataTransfer
  )
}
