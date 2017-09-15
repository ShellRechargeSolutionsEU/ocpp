package com.thenewmotion.ocpp
package messages

import java.time.ZonedDateTime
import enums.reflection.EnumUtils.{Nameable, Enumerable}

sealed trait AuthorizationStatus extends Nameable
object AuthorizationStatus extends Enumerable[AuthorizationStatus] {
  case object Accepted extends AuthorizationStatus
  case object IdTagBlocked extends AuthorizationStatus
  case object IdTagExpired extends AuthorizationStatus
  case object IdTagInvalid extends AuthorizationStatus
  case object ConcurrentTx extends AuthorizationStatus

  val values = Set(Accepted, IdTagBlocked, IdTagExpired, IdTagInvalid, ConcurrentTx)
}

case class IdTagInfo(
  status: AuthorizationStatus,
  expiryDate: Option[ZonedDateTime] = None,
  parentIdTag: Option[String] = None
)
