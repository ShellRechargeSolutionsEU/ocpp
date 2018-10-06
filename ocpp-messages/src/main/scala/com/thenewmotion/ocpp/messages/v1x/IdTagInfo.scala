package com.thenewmotion.ocpp.messages.v1x

import java.time.ZonedDateTime
import enums.reflection.EnumUtils.{Nameable, Enumerable}

sealed trait AuthorizationStatus extends Nameable
object AuthorizationStatus extends Enumerable[AuthorizationStatus] {
  case object Accepted extends AuthorizationStatus
  case object IdTagBlocked extends AuthorizationStatus { override def name = "Blocked" }
  case object IdTagExpired extends AuthorizationStatus { override def name = "Expired" }
  case object IdTagInvalid extends AuthorizationStatus { override def name = "Invalid" }
  case object ConcurrentTx extends AuthorizationStatus

  val values = Set(Accepted, IdTagBlocked, IdTagExpired, IdTagInvalid, ConcurrentTx)
}

case class IdTagInfo(
  status: AuthorizationStatus,
  expiryDate: Option[ZonedDateTime] = None,
  parentIdTag: Option[String] = None
)
