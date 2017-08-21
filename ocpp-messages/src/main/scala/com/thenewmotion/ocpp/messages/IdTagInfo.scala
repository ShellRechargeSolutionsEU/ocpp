package com.thenewmotion.ocpp
package messages

import java.time.ZonedDateTime

/**
 * @author Yaroslav Klymko
 */
object AuthorizationStatus extends Enumeration {
  val Accepted,
  IdTagBlocked,
  IdTagExpired,
  IdTagInvalid,
  ConcurrentTx = Value
}

case class IdTagInfo(status: AuthorizationStatus.Value,
                     expiryDate: Option[ZonedDateTime] = None,
                     parentIdTag: Option[String] = None)