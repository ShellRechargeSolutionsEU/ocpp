package com.thenewmotion.ocpp
package messages

import enums.reflection.EnumUtils.{Enumerable, Nameable}

sealed trait DataTransferStatus extends Nameable
object DataTransferStatus extends Enumerable[DataTransferStatus] {
  object Accepted extends DataTransferStatus
  object Rejected extends DataTransferStatus
  object UnknownMessageId extends DataTransferStatus
  object UnknownVendorId extends DataTransferStatus

  val values = Set(Accepted, Rejected, UnknownMessageId, UnknownVendorId)
}
