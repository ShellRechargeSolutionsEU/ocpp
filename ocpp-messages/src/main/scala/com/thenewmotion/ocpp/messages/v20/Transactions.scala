package com.thenewmotion.ocpp
package messages
package v20

import java.time.Instant
import enums.reflection.EnumUtils.{Enumerable, Nameable}

case class TransactionEventRequest(
  eventType: TransactionEvent,
  meterValue: Option[List[MeterValue]],
  timestamp: Instant,
  triggerReason: TriggerReason,
  seqNo: Int,
  offline: Option[Boolean],
  numberOfPhasesUsed: Option[Int],
  cableMaxCurrent: Option[BigDecimal],
  reservationId: Option[Int],
  transactionData: Transaction,
  evse: Option[EVSE],
  idToken: Option[IdToken]
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
  case object InterruptionBegin extends ReadingContext { override def name = "Interruption.Begin" }
  case object InterruptionEnd extends ReadingContext   { override def name = "Interruption.End" }
  case object Other extends ReadingContext
  case object SampleClock extends ReadingContext       { override def name = "Sample.Clock" }
  case object SamplePeriodic extends ReadingContext    { override def name = "Sample.Periodic" }
  case object TransactionBegin extends ReadingContext  { override def name = "Transaction.Begin" }
  case object TransactionEnd extends ReadingContext    { override def name = "Transaction.End" }
  case object Trigger extends ReadingContext

  val values = List(
    InterruptionBegin, InterruptionEnd, Other, SampleClock,
    SamplePeriodic, TransactionBegin, TransactionEnd, Trigger
  )
}

sealed trait Measurand extends Nameable
object Measurand extends Enumerable[Measurand] {
  case object CurrentExport extends Measurand                { override def name = "Current.Export" }
  case object CurrentImport extends Measurand                { override def name = "Current.Import" }
  case object CurrentOffered extends Measurand               { override def name = "Current.Offered" }
  case object EnergyActiveExportRegister extends Measurand   { override def name = "Energy.Active.Export.Register" }
  case object EnergyActiveImportRegister extends Measurand   { override def name = "Energy.Active.Import.Register" }
  case object EnergyReactiveExportRegister extends Measurand { override def name = "Energy.Reactive.Export.Register" }
  case object EnergyReactiveImportRegister extends Measurand { override def name = "Energy.Reactive.Import.Register" }
  case object EnergyActiveExportInterval extends Measurand   { override def name = "Energy.Active.Export.Interval" }
  case object EnergyActiveImportInterval extends Measurand   { override def name = "Energy.Active.Import.Interval" }
  case object EnergyActiveNet extends Measurand              { override def name = "Energy.Active.Net" }
  case object EnergyReactiveExportInterval extends Measurand { override def name = "Energy.Reactive.Export.Interval" }
  case object EnergyReactiveImportInterval extends Measurand { override def name = "Energy.Reactive.Import.Interval" }
  case object EnergyReactiveNet extends Measurand            { override def name = "Energy.Reactive.Net" }
  case object EnergyApparentNet extends Measurand            { override def name = "Energy.Apparent.Net" }
  case object EnergyApparentImport extends Measurand         { override def name = "Energy.Apparent.Import" }
  case object EnergyApparentExport extends Measurand         { override def name = "Energy.Apparent.Export" }
  case object Frequency extends Measurand
  case object PowerActiveExport extends Measurand            { override def name = "Power.Active.Export" }
  case object PowerActiveImport extends Measurand            { override def name = "Power.Active.Import" }
  case object PowerFactor extends Measurand                  { override def name = "Power.Factor" }
  case object PowerOffered extends Measurand                 { override def name = "Power.Offered" }
  case object PowerReactiveExport extends Measurand          { override def name = "Power.Reactive.Export" }
  case object PowerReactiveImport extends Measurand          { override def name = "Power.Reactive.Import" }
  case object SoC extends Measurand
  case object Voltage extends Measurand

  val values = List(
    CurrentExport ,
    CurrentImport ,
    CurrentOffered ,
    EnergyActiveExportRegister ,
    EnergyActiveImportRegister ,
    EnergyReactiveExportRegister ,
    EnergyReactiveImportRegister ,
    EnergyActiveExportInterval ,
    EnergyActiveImportInterval ,
    EnergyActiveNet ,
    EnergyReactiveExportInterval ,
    EnergyReactiveImportInterval ,
    EnergyReactiveNet ,
    EnergyApparentNet ,
    EnergyApparentImport ,
    EnergyApparentExport ,
    Frequency ,
    PowerActiveExport ,
    PowerActiveImport ,
    PowerFactor ,
    PowerOffered ,
    PowerReactiveExport ,
    PowerReactiveImport ,
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
  case object DLMSMessage extends EncodingMethod        { override def name = "DLMS Message" }
  case object COSEMProtectedData extends EncodingMethod { override def name = "COSEM Protected Data" }
  case object EDL extends EncodingMethod

  val values = List(Other, DLMSMessage, COSEMProtectedData, EDL)
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
  remoteStartId: Option[Int]
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
  connectorId: Option[Int]
)

case class TransactionEventResponse(
  totalCost: Option[BigDecimal],
  chargingPriority: Option[Int],
  idTokenInfo: Option[IdTokenInfo],
  updatedPersonalMessage: Option[MessageContent]
) extends CsmsResponse

case class IdTokenInfo(
  status: AuthorizationStatus,
  cacheExpiryDateTime: Option[Instant],
  chargingPriority: Option[Int],
  groupIdToken: Option[GroupIdToken],
  language1: Option[String],
  language2: Option[String],
  personalMessage: Option[MessageContent]
)

sealed trait AuthorizationStatus extends Nameable
object AuthorizationStatus extends Enumerable[AuthorizationStatus] {
  case object Accepted extends AuthorizationStatus
  case object Blocked extends AuthorizationStatus
  case object ConcurrentTx extends AuthorizationStatus
  case object Expired extends AuthorizationStatus
  case object Invalid extends AuthorizationStatus
  case object NoCredit extends AuthorizationStatus
  case object NotAllowedTypeEVSE extends AuthorizationStatus
  case object NotAtThisLocation extends AuthorizationStatus
  case object NotAtThisTime extends AuthorizationStatus
  case object Unknown extends AuthorizationStatus

  val values = List(
    Accepted,
    Blocked,
    ConcurrentTx,
    Expired,
    Invalid,
    NoCredit,
    NotAllowedTypeEVSE,
    NotAtThisLocation,
    NotAtThisTime,
    Unknown
  )
}

case class GroupIdToken(
  idToken: String,
  `type`: IdTokenType
)

case class MessageContent(
  format: MessageFormat,
  language: Option[String],
  content: String
)

sealed trait MessageFormat extends Nameable
object MessageFormat extends Enumerable[MessageFormat] {
  case object ASCII extends MessageFormat
  case object HTML extends MessageFormat
  case object URI extends MessageFormat
  case object UTF8 extends MessageFormat

  val values = List(ASCII, HTML, URI, UTF8)
}

case class GetTransactionStatusRequest(
  transactionId: Option[String]
) extends CsRequest

case class GetTransactionStatusResponse(
  ongoingIndicator: Option[Boolean],
  messagesInQueue: Boolean
) extends CsResponse
