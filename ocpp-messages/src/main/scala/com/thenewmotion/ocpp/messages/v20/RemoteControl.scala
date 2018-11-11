package com.thenewmotion.ocpp
package messages
package v20

import java.time.Instant
import enums.reflection.EnumUtils.{Enumerable, Nameable}

case class RequestStartTransactionRequest(
  evseId: Option[Int],
  remoteStartId: Int,
  idToken: IdToken,
  chargingProfile: Option[ChargingProfile]
) extends CsRequest

case class IdToken(
  idToken: String,
  `type`: IdTokenType,
  additionalInfo: Option[Seq[AdditionalInfo]]
)

sealed trait IdTokenType extends Nameable
object IdTokenType extends Enumerable[IdTokenType] {
  case object Central extends IdTokenType
  case object eMAID extends IdTokenType
  case object ISO14443 extends IdTokenType
  case object KeyCode extends IdTokenType
  case object Local extends IdTokenType
  case object NoAuthorization extends IdTokenType
  case object ISO15693 extends IdTokenType

  val values = List(
    Central, eMAID, ISO14443, KeyCode, Local, NoAuthorization, ISO15693
  )
}

case class AdditionalInfo(
  additionalIdToken: String,
  `type`: String
)

case class ChargingProfile(
  id: Int,
  stackLevel: Int,
  primary: Option[Boolean],
  chargingProfilePurpose: ChargingProfilePurpose,
  chargingProfileKind: ChargingProfileKind,
  recurrencyKind: Option[RecurrencyKind],
  validFrom: Option[Instant],
  validTo: Option[Instant],
  transactionId: Option[String],
  chargingSchedule: ChargingSchedule
)

sealed trait ChargingProfilePurpose extends Nameable
object ChargingProfilePurpose extends Enumerable[ChargingProfilePurpose] {
  case object ChargingStationExternalConstraints extends ChargingProfilePurpose
  case object ChargingStationMaxProfile extends ChargingProfilePurpose
  case object TxDefaultProfile extends ChargingProfilePurpose
  case object TxProfile extends ChargingProfilePurpose

  val values = List(
    ChargingStationExternalConstraints, ChargingStationMaxProfile,
    TxDefaultProfile, TxProfile
  )
}

sealed trait ChargingProfileKind extends Nameable
object ChargingProfileKind extends Enumerable[ChargingProfileKind] {
  case object Absolute extends ChargingProfileKind
  case object Recurring extends ChargingProfileKind
  case object Relative extends ChargingProfileKind

  val values = List(Absolute, Recurring, Relative)
}

sealed trait RecurrencyKind extends Nameable
object RecurrencyKind extends Enumerable[RecurrencyKind] {
  case object Daily extends RecurrencyKind
  case object Weekly extends RecurrencyKind

  val values = List(Daily, Weekly)
}

case class ChargingSchedule(
  startSchedule: Option[Instant],
  duration: Option[Int],
  chargingRateUnit: ChargingRateUnit,
  minChargingRate: Option[ChargingRate],
  chargingSchedulePeriod: Seq[ChargingSchedulePeriod]
)

/**
  * ChargingRate is a type that wraps Double so we can make sure to serialize
  * charging rate numbers properly with 1 decimal place, as OCPP requires.
  *
  * @param rate
  */
case class ChargingRate(rate: Double)

sealed trait ChargingRateUnit extends Nameable
object ChargingRateUnit extends Enumerable[ChargingRateUnit] {
  case object W extends ChargingRateUnit
  case object A extends ChargingRateUnit

  val values = List(W, A)
}

case class ChargingSchedulePeriod(
  startPeriod: Int,
  limit: ChargingRate,
  numberPhases: Option[Int],
  phaseToUse: Option[Int]
)

case class RequestStartTransactionResponse(
  status: RequestStartStopStatus,
  transactionId: Option[String]
) extends CsResponse

sealed trait RequestStartStopStatus extends Nameable
object RequestStartStopStatus extends Enumerable[RequestStartStopStatus] {
  case object Accepted extends RequestStartStopStatus
  case object Rejected extends RequestStartStopStatus

  val values = List(Accepted, Rejected)
}

case class RequestStopTransactionRequest(
  transactionId: String
) extends CsRequest

case class RequestStopTransactionResponse(
  status: RequestStartStopStatus
) extends CsResponse
