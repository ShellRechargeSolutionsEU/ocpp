package com.thenewmotion.ocpp
package messages

import java.time.ZonedDateTime

import enums.reflection.EnumUtils.Enumerable
import enums.reflection.EnumUtils.Nameable

sealed trait RecurrencyKind extends Nameable
object RecurrencyKind extends Enumerable[RecurrencyKind] {
  case object Daily extends RecurrencyKind
  case object Weekly extends RecurrencyKind
  val values = Set(Daily, Weekly)
}

sealed trait ChargingProfileKind
object ChargingProfileKind {
  case object Absolute extends ChargingProfileKind // startSchedule can't be None
  case object Relative extends ChargingProfileKind // startSchedule should be None
  final case class Recurring(kind: RecurrencyKind) extends ChargingProfileKind // startSchedule?
}

sealed trait ChargingProfilePurpose extends Nameable
object ChargingProfilePurpose extends Enumerable[ChargingProfilePurpose] {
  case object TxProfile extends ChargingProfilePurpose
  case object TxDefaultProfile extends ChargingProfilePurpose
  case object ChargePointMaxProfile extends ChargingProfilePurpose
  val values = Set(TxProfile, TxDefaultProfile, ChargePointMaxProfile)
}

sealed trait UnitOfChargingRate extends Nameable
object UnitOfChargeRate extends Enumerable[UnitOfChargingRate] {
  case object Watts extends UnitOfChargingRate { override def name = "W" }
  case object Amperes extends UnitOfChargingRate { override def name = "A" }
  val values = Set(Watts, Amperes)
}

sealed trait ChargingProfileStatus extends Nameable
object ChargingProfileStatus extends Enumerable[ChargingProfileStatus] {
  case object Accepted extends ChargingProfileStatus
  case object Rejected extends ChargingProfileStatus
  case object NotSupported extends ChargingProfileStatus
  val values = Set(Accepted, Rejected, NotSupported)
}

sealed trait ClearChargingProfileStatus extends Nameable
object ClearChargingProfileStatus extends Enumerable[ClearChargingProfileStatus] {
  case object Accepted extends ClearChargingProfileStatus
  case object Unknown extends ClearChargingProfileStatus
  val values = Set(Accepted, Unknown)
}

sealed trait GetCompositeScheduleStatus
object GetCompositeScheduleStatus {
  final case class Accepted(
    connector: Scope,
    scheduleStart: Option[ZonedDateTime],
    chargingSchedule: Option[ChargingSchedule]
  ) extends GetCompositeScheduleStatus
  case object Rejected extends GetCompositeScheduleStatus
}
