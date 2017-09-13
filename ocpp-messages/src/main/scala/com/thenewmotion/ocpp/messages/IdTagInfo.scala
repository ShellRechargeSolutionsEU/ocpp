package com.thenewmotion.ocpp
package messages

import java.time.ZonedDateTime
import enums.reflection.EnumUtils.{Nameable, Enumerable}

sealed trait AuthorizationStatus extends Nameable
object AuthorizationStatus extends Enumerable[AuthorizationStatus] {
  object Accepted extends AuthorizationStatus
  object IdTagBlocked extends AuthorizationStatus
  object IdTagExpired extends AuthorizationStatus
  object IdTagInvalid extends AuthorizationStatus
  object ConcurrentTx extends AuthorizationStatus

  val values = Set(Accepted, IdTagBlocked, IdTagExpired, IdTagInvalid, ConcurrentTx)
}

case class IdTagInfo(
  status: AuthorizationStatus,
  expiryDate: Option[ZonedDateTime] = None,
  parentIdTag: Option[String] = None
)
