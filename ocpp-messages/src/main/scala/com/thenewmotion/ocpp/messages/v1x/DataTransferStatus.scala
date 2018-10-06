package com.thenewmotion.ocpp.messages.v1x

import enums.reflection.EnumUtils.{Enumerable, Nameable}

sealed trait DataTransferStatus extends Nameable
object DataTransferStatus extends Enumerable[DataTransferStatus] {
  case object Accepted extends DataTransferStatus
  case object Rejected extends DataTransferStatus
  case object UnknownMessageId extends DataTransferStatus
  case object UnknownVendorId extends DataTransferStatus

  val values = Set(Accepted, Rejected, UnknownMessageId, UnknownVendorId)
}
