package com.thenewmotion.ocpp
package messages

/**
 * @author Yaroslav Klymko
 */
object DataTransferStatus extends Enumeration {
  val Accepted,
  Rejected,
  UnknownMessageId,
  UnknownVendorId = Value
}
