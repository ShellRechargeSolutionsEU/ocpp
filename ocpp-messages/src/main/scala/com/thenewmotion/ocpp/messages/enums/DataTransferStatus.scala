package com.thenewmotion.ocpp
package messages.enums

/**
 * @author Yaroslav Klymko
 */
object DataTransferStatus extends Enumeration {
  val Accepted,
  Rejected,
  UnknownMessageId,
  UnknownVendorId = Value
}
