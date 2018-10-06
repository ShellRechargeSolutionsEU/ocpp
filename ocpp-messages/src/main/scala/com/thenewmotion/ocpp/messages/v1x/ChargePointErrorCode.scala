package com.thenewmotion.ocpp.messages.v1x

import enums.reflection.EnumUtils.{Enumerable, Nameable}

sealed trait ChargePointErrorCode extends Nameable
object ChargePointErrorCode extends Enumerable[ChargePointErrorCode] {
  case object ConnectorLockFailure extends ChargePointErrorCode
  case object HighTemperature      extends ChargePointErrorCode
  case object EVCommunicationError extends ChargePointErrorCode // ocpp 1.6: renamed from Mode3Error
  case object PowerMeterFailure    extends ChargePointErrorCode
  case object PowerSwitchFailure   extends ChargePointErrorCode
  case object ReaderFailure        extends ChargePointErrorCode
  case object ResetFailure         extends ChargePointErrorCode
  case object GroundFailure        extends ChargePointErrorCode // ocpp 1.5
  case object OverCurrentFailure   extends ChargePointErrorCode
  case object OverVoltage          extends ChargePointErrorCode // ocpp 1.6
  case object UnderVoltage         extends ChargePointErrorCode
  case object WeakSignal           extends ChargePointErrorCode
  case object InternalError        extends ChargePointErrorCode // ocpp 1.6
  case object LocalListConflict    extends ChargePointErrorCode // ocpp 1.6
  case object OtherError           extends ChargePointErrorCode

  val values = Set(
    ConnectorLockFailure,
    HighTemperature,
    EVCommunicationError,
    PowerMeterFailure,
    PowerSwitchFailure,
    ReaderFailure,
    ResetFailure,
    GroundFailure,
    OverCurrentFailure,
    OverVoltage,
    UnderVoltage,
    WeakSignal,
    InternalError,
    LocalListConflict,
    OtherError
  )
}
