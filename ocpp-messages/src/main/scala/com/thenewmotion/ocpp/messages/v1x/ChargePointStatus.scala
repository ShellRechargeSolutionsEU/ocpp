package com.thenewmotion.ocpp.messages.v1x

import enums.reflection.EnumUtils.{Enumerable, Nameable}

sealed trait ChargePointStatus { def info: Option[String] }
object ChargePointStatus {
  final case class Occupied(
    kind: Option[OccupancyKind], // only set in ocpp 1.6
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
sealed trait OccupancyKind extends Nameable
object OccupancyKind extends Enumerable[Nameable] {
  case object Preparing     extends OccupancyKind
  case object Charging      extends OccupancyKind
  case object SuspendedEVSE extends OccupancyKind
  case object SuspendedEV   extends OccupancyKind
  case object Finishing     extends OccupancyKind
  val values = Set(Preparing, Charging, SuspendedEVSE, SuspendedEV, Finishing)
}
