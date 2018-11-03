package com.thenewmotion.ocpp
package messages
package v20

import java.time.Instant

import enums.reflection.EnumUtils.{Enumerable, Nameable}

trait Message extends messages.Message

trait Request extends Message with messages.Request
trait Response extends Message with messages.Response

trait CsRequest extends Request
trait CsResponse extends Response
trait CsmsRequest extends Request
trait CsmsResponse extends Response

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

case class HeartbeatRequest() extends CsmsRequest

case class HeartbeatResponse(
  currentTime: Instant
) extends CsmsResponse

case class TransactionEventRequest(
  eventType: TransactionEvent,
  meterValue: Option[List[MeterValue]],
  timestamp: Instant,
  triggerReason: TriggerReason,
  seqNo: Int,
  offline: Option[Boolean],
  numberOfPhasesUsed: Option[Int],
  cableMaxCurrent: Option[BigDecimal],
  reservationid: Option[Int],
  transactionData: Transaction,
  evse: EVSE,
  idToken: IdToken
) extends CsmsRequest

sealed trait TransactionEvent extends Nameable
object TransactionEvent extends Enumerable[TransactionEvent] {
  case object Started extends TransactionEvent
  case object Updated extends TransactionEvent
  case object Ended   extends TransactionEvent

  val values = List(Started, Updated, Ended)
}

case class MeterValue(
  sampledValue: List[SampledValue],
  timestamp: Instant
)

case class SampledValue(
  value: BigDecimal,
  context: Option[ReadingContext],
  measurand: Option[Measurand],
  phase: Option[Phase],
  location: Option[Location],
  signedMeterValue: Option[SignedMeterValue],
  unitOfMeasure: Option[UnitOfMeasure]
)

sealed trait ReadingContext extends Nameable
object ReadingContext extends Enumerable[ReadingContext] {
  case object `Interruption.Begin` extends ReadingContext
  case object `Interruption.End` extends ReadingContext
  case object Other extends ReadingContext
  case object `Sample.Clock` extends ReadingContext
  case object `Sample.Periodic` extends ReadingContext
  case object `Transaction.Begin` extends ReadingContext
  case object `Transaction.End` extends ReadingContext
  case object Trigger extends ReadingContext

  val values = List(
    `Interruption.Begin`, `Interruption.End`, Other, `Sample.Clock`,
    `Sample.Periodic`, `Transaction.Begin`, `Transaction.End`, Trigger
  )
}

sealed trait Measurand extends Nameable
object Measurand extends Enumerable[Measurand] {
  case object `Current.Export` extends Measurand
  case object `Current.Import` extends Measurand
  case object `Current.Offered` extends Measurand
  case object `Energy.Active.Export.Register` extends Measurand
  case object `Energy.Active.Import.Register` extends Measurand
  case object `Energy.Reactive.Export.Register` extends Measurand
  case object `Energy.Reactive.Import.Register` extends Measurand
  case object `Energy.Active.Export.Interval` extends Measurand
  case object `Energy.Active.Import.Interval` extends Measurand
  case object `Energy.Active.Net` extends Measurand
  case object `Energy.Reactive.Export.Interval` extends Measurand
  case object `Energy.Reactive.Import.Interval` extends Measurand
  case object `Energy.Reactive.Net` extends Measurand
  case object `Energy.Apparent.Net` extends Measurand
  case object `Energy.Apparent.Import` extends Measurand
  case object `Energy.Apparent.Export` extends Measurand
  case object Frequency extends Measurand
  case object `Power.Active.Export` extends Measurand
  case object `Power.Active.Import` extends Measurand
  case object `Power.Factor` extends Measurand
  case object `Power.Offered` extends Measurand
  case object `Power.Reactive.Export` extends Measurand
  case object `Power.Reactive.Import` extends Measurand
  case object SoC extends Measurand
  case object Voltage extends Measurand

  val values = List(
   `Current.Export` ,
   `Current.Import` ,
   `Current.Offered` ,
   `Energy.Active.Export.Register` ,
   `Energy.Active.Import.Register` ,
   `Energy.Reactive.Export.Register` ,
   `Energy.Reactive.Import.Register` ,
   `Energy.Active.Export.Interval` ,
   `Energy.Active.Import.Interval` ,
   `Energy.Active.Net` ,
   `Energy.Reactive.Export.Interval` ,
   `Energy.Reactive.Import.Interval` ,
   `Energy.Reactive.Net` ,
   `Energy.Apparent.Net` ,
   `Energy.Apparent.Import` ,
   `Energy.Apparent.Export` ,
   Frequency ,
   `Power.Active.Export` ,
   `Power.Active.Import` ,
   `Power.Factor` ,
   `Power.Offered` ,
   `Power.Reactive.Export` ,
   `Power.Reactive.Import` ,
   SoC ,
   Voltage
  )
}

sealed trait Phase extends Nameable
object Phase extends Enumerable[Phase] {
  case object L1 extends Phase
  case object L2 extends Phase
  case object L3 extends Phase
  case object N extends Phase
  case object `L1-N` extends Phase
  case object `L2-N` extends Phase
  case object `L3-N` extends Phase
  case object `L1-L2` extends Phase
  case object `L2-L3` extends Phase
  case object `L3-L1` extends Phase

  val values = List(
    L1, L2, L3, N, `L1-N`, `L2-N`, `L3-N`, `L1-L2`, `L2-L3`, `L3-L1`
  )
}

sealed trait Location extends Nameable
object Location extends Enumerable[Location] {
  case object Body extends Location
  case object Cable extends Location
  case object EV extends Location
  case object Inlet extends Location
  case object Outlet extends Location

  val values = List(Body, Cable, EV, Inlet, Outlet)
}

case class SignedMeterValue(
  meterValueSignature: String,
  signatureMethod: SignatureMethod,
  encodingMethod: EncodingMethod,
  encodedMeterValue: String
)

sealed trait SignatureMethod extends Nameable
object SignatureMethod extends Enumerable[SignatureMethod] {
  case object ECDSAP256SHA256 extends SignatureMethod
  case object ECDSAP384SHA384 extends SignatureMethod
  case object ECDSA192SHA256 extends SignatureMethod

  val values = List(ECDSAP256SHA256, ECDSAP384SHA384, ECDSA192SHA256)
}

sealed trait EncodingMethod extends Nameable
object EncodingMethod extends Enumerable[EncodingMethod] {
  case object Other extends EncodingMethod
  case object `DLMS Message` extends EncodingMethod
  case object `COSEM Protected Data` extends EncodingMethod
  case object EDL extends EncodingMethod

  val values = List(Other, `DLMS Message`, `COSEM Protected Data`, EDL)
}

case class UnitOfMeasure(
  unit: Option[String],
  multiplier: Option[Int]
)

sealed trait TriggerReason extends Nameable
object TriggerReason extends Enumerable[TriggerReason] {
  case object Authorized extends TriggerReason
  case object CablePluggedIn extends TriggerReason
  case object ChargingRateChanged extends TriggerReason
  case object ChargingStateChanged extends TriggerReason
  case object Deauthorized extends TriggerReason
  case object EnergyLimitReached extends TriggerReason
  case object EVCommunicationLost extends TriggerReason
  case object EVConnectTimeout extends TriggerReason
  case object MeterValueClock extends TriggerReason
  case object MeterValuePeriodic extends TriggerReason
  case object TimeLimitReached extends TriggerReason
  case object Trigger extends TriggerReason
  case object UnlockCommand extends TriggerReason
  case object StopAuthorized extends TriggerReason
  case object EVDeparted extends TriggerReason
  case object EVDetected extends TriggerReason
  case object RemoteStop extends TriggerReason
  case object RemoteStart extends TriggerReason

  val values = List(
    Authorized,
    CablePluggedIn,
    ChargingRateChanged,
    ChargingStateChanged,
    Deauthorized,
    EnergyLimitReached,
    EVCommunicationLost,
    EVConnectTimeout,
    MeterValueClock,
    MeterValuePeriodic,
    TimeLimitReached,
    Trigger,
    UnlockCommand,
    StopAuthorized,
    EVDeparted,
    EVDetected,
    RemoteStop,
    RemoteStart
  )
}

case class Transaction(
  id: String,
  chargingState: Option[ChargingState],
  timeSpentCharging: Option[Int],
  stoppedReason: Option[Reason],
  remoteStartid: Option[Int]
)

sealed trait ChargingState extends Nameable
object ChargingState extends Enumerable[ChargingState] {
  case object Charging extends ChargingState
  case object EVDetected extends ChargingState
  case object SuspendedEV extends ChargingState
  case object SuspendedEVSE extends ChargingState

  val values = List(
    Charging,
    EVDetected,
    SuspendedEV,
    SuspendedEVSE
  )
}

sealed trait Reason extends Nameable
object Reason extends Enumerable[Reason] {
  case object DeAuthorized extends Reason
  case object EmergencyStop extends Reason
  case object EnergyLimitReached extends Reason
  case object EVDisconnected extends Reason
  case object GroundFault extends Reason
  case object ImmediateReset extends Reason
  case object Local extends Reason
  case object LocalOutOfCredit extends Reason
  case object MasterPass extends Reason
  case object Other extends Reason
  case object OvercurrentFault extends Reason
  case object PowerLoss extends Reason
  case object PowerQuality extends Reason
  case object Reboot extends Reason
  case object Remote extends Reason
  case object SOCLimitReached extends Reason
  case object StoppedByEV extends Reason
  case object TimeLimitReached extends Reason
  case object Timeout extends Reason
  case object UnlockCommand extends Reason

  val values = List(
    DeAuthorized,
    EmergencyStop,
    EnergyLimitReached,
    EVDisconnected,
    GroundFault,
    ImmediateReset,
    Local,
    LocalOutOfCredit,
    MasterPass,
    Other,
    OvercurrentFault,
    PowerLoss,
    PowerQuality,
    Reboot,
    Remote,
    SOCLimitReached,
    StoppedByEV,
    TimeLimitReached,
    Timeout,
    UnlockCommand
  )
}

case class EVSE(
  id: Int,
  // so a connectorId is a property of an EVSE!?
  // *Sigh* Enterpise Architect. Not even once.
  connectorId: Option[Int]
)

case class TransactionEventResponse(

) extends CsmsResponse
