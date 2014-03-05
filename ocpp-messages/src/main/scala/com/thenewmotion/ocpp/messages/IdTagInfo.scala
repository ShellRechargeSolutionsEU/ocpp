package com.thenewmotion.ocpp
package messages

import org.joda.time.DateTime

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
                     expiryDate: Option[DateTime] = None,
                     parentIdTag: Option[String] = None)