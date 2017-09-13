package com.thenewmotion.ocpp
package messages

import enums.reflection.EnumUtils.{Enumerable, Nameable}

sealed trait ChargePointErrorCode extends Nameable
object ChargePointErrorCode extends Enumerable[ChargePointErrorCode] {
  object ConnectorLockFailure extends ChargePointErrorCode
  object HighTemperature      extends ChargePointErrorCode
  object EVCommunicationError extends ChargePointErrorCode // ocpp 1.6: renamed from Mode3Error
  object PowerMeterFailure    extends ChargePointErrorCode
  object PowerSwitchFailure   extends ChargePointErrorCode
  object ReaderFailure        extends ChargePointErrorCode
  object ResetFailure         extends ChargePointErrorCode
  object GroundFailure        extends ChargePointErrorCode // ocpp 1.5
  object OverCurrentFailure   extends ChargePointErrorCode
  object OverVoltage          extends ChargePointErrorCode // ocpp 1.6
  object UnderVoltage         extends ChargePointErrorCode
  object WeakSignal           extends ChargePointErrorCode
  object InternalError        extends ChargePointErrorCode // ocpp 1.6
  object LocalListConflict    extends ChargePointErrorCode // ocpp 1.6
  object NoError              extends ChargePointErrorCode // ocpp 1.6
  object OtherError           extends ChargePointErrorCode

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
    NoError,
    OtherError
  )
}

