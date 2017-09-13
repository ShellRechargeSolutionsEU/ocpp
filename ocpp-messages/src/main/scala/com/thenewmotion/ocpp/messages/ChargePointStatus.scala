package com.thenewmotion.ocpp
package messages

import enums.reflection.EnumUtils.Enumerable
import enums.reflection.EnumUtils.Nameable

sealed trait ChargePointStatus { def info: Option[String] }
object ChargePointStatus {
  final case class Occupied(
    reason: Option[OccupiedReason], // only set in ocpp 1.6
    info: Option[String] = None
  ) extends ChargePointStatus
  final case class Faulted(
    errorCode: Option[ChargePointErrorCode],
    info: Option[String] = None,
    vendorErrorCode: Option[String]
  ) extends ChargePointStatus
  final case class Available(info: Option[String] = None) extends ChargePointStatus
  final case class Unavailable(info: Option[String] = None) extends ChargePointStatus
  final case class Reserved(info: Option[String] = None) extends ChargePointStatus // since OCPP 1.5
}

// ocpp 1.6
sealed trait OccupiedReason extends Nameable
object OccupiedReason extends Enumerable[Nameable] {
  case object Preparing     extends OccupiedReason
  case object Charging      extends OccupiedReason
  case object SuspendedEVSE extends OccupiedReason
  case object SuspendedEV   extends OccupiedReason
  case object Finishing     extends OccupiedReason
  val values = Set(Preparing, Charging, SuspendedEVSE, SuspendedEV, Finishing)
}
