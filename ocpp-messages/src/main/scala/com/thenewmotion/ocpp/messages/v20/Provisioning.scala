package com.thenewmotion.ocpp
package messages
package v20

import java.time.Instant
import enums.reflection.EnumUtils.{Nameable, Enumerable}

case class BootNotificationRequest(
  chargingStation: ChargingStation,
  reason: BootReason
) extends CsmsRequest

sealed trait BootReason extends Nameable
object BootReason extends Enumerable[BootReason] {
  case object ApplicationReset extends BootReason
  case object FirmwareUpdate extends BootReason
  case object LocalReset extends BootReason
  case object PowerUp extends BootReason
  case object RemoteReset extends BootReason
  case object ScheduledReset extends BootReason
  case object Triggered extends BootReason
  case object Unknown extends BootReason
  case object Watchdog extends BootReason

  val values = List(ApplicationReset, FirmwareUpdate, LocalReset, PowerUp,
                    RemoteReset, ScheduledReset, Triggered, Unknown, Watchdog
  )
}

case class ChargingStation(
  serialNumber: Option[String],
  model: String,
  vendorName: String,
  firmwareVersion: Option[String],
  modem: Option[Modem]
)

case class Modem(
  iccid: Option[String],
  imsi: Option[String]
)

case class BootNotificationResponse(
  currentTime: Instant,
  interval: Int,
  status: BootNotificationStatus
) extends CsmsResponse

sealed trait BootNotificationStatus extends Nameable
case object BootNotificationStatus extends Enumerable[BootNotificationStatus] {
  case object Accepted extends BootNotificationStatus
  case object Pending extends BootNotificationStatus
  case object Rejected extends BootNotificationStatus

  val values = List(Accepted, Pending, Rejected)
}

case class GetVariablesRequest(
  getVariableData: List[GetVariableData]
) extends CsRequest

case class GetVariableData(
  attributeType: Option[Attribute],
  component: Component,
  variable: Variable
)

case class GetVariablesResponse(
  getVariableResult: List[GetVariableResult]
) extends CsResponse

case class GetVariableResult(
  attributeStatus: GetVariableStatus,
  attributeType: Option[Attribute],
  attributeValue: Option[String],
  component: Component,
  variable: Variable
)

sealed trait GetVariableStatus extends Nameable
object GetVariableStatus extends Enumerable[GetVariableStatus] {
  case object Accepted extends GetVariableStatus
  case object Rejected extends GetVariableStatus
  case object UnknownComponent extends GetVariableStatus

  val values = List(Accepted, Rejected, UnknownComponent)
}

sealed trait Attribute extends Nameable
object Attribute extends Enumerable[Attribute] {
  case object Actual extends Attribute
  case object Target extends Attribute
  case object MinSet extends Attribute
  case object MaxSet extends Attribute

  val values = List(Actual, Target, MinSet, MaxSet)
}

case class Component(
  name: String,
  instance: Option[String],
  evse: EVSE
)

case class Variable(
  name: String,
  instance: Option[String]
)

case class SetVariablesRequest(
  setVariableData: List[SetVariableData]
) extends CsRequest

case class SetVariableData(
  attributeType: Option[Attribute],
  attributeValue: String,
  component: Component,
  variable: Variable
)

case class SetVariablesResponse(
  setVariableResult: List[SetVariableResult]
) extends CsResponse

case class SetVariableResult(
  attributeType: Option[Attribute],
  attributeStatus: SetVariableStatus,
  component: Component,
  variable: Variable
)

sealed trait SetVariableStatus extends Nameable
object SetVariableStatus extends Enumerable[SetVariableStatus] {
  case object Accepted extends SetVariableStatus
  case object Rejected extends SetVariableStatus
  case object InvalidValue extends SetVariableStatus
  case object UnknownComponent extends SetVariableStatus
  case object UnknownVariable extends SetVariableStatus
  case object NotSupportedAttributeType extends SetVariableStatus
  case object OutOfRange extends SetVariableStatus
  case object RebootRequired extends SetVariableStatus

  val values = List(
    Accepted,
    Rejected,
    InvalidValue,
    UnknownComponent,
    UnknownVariable,
    NotSupportedAttributeType,
    OutOfRange,
    RebootRequired
  )
}
