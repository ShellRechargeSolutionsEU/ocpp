package com.thenewmotion.ocpp

/**
 * @author Yaroslav Klymko
 */
object DataTransferStatus extends Enumeration {
  val Accepted,
  Rejected,
  UnknownMessageId,
  UnknownVendorId = Value
}