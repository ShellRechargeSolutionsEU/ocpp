package com.thenewmotion.ocpp
package messages
package v20

import java.time.Instant

import enums.reflection.EnumUtils.{Enumerable, Nameable}

case class HeartbeatRequest() extends CsmsRequest

case class HeartbeatResponse(
  currentTime: Instant
) extends CsmsResponse

case class StatusNotificationRequest(
  timestamp: Instant,
  connectorStatus: ConnectorStatus,
  evseId: Int,
  connectorId: Int
) extends CsmsRequest

sealed trait ConnectorStatus extends Nameable
object ConnectorStatus extends Enumerable[ConnectorStatus] {
  case object Available   extends ConnectorStatus
  case object Occupied    extends ConnectorStatus
  case object Reserved    extends ConnectorStatus
  case object Unavailable extends ConnectorStatus
  case object Faulted     extends ConnectorStatus

  val values = List(
    Available,
    Occupied,
    Reserved,
    Unavailable,
    Faulted
  )
}

case class StatusNotificationResponse() extends CsmsResponse
