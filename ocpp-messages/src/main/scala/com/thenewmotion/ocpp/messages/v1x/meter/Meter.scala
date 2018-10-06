package com.thenewmotion.ocpp
package messages
package v1x
package meter

import java.time.ZonedDateTime
import enums.reflection.EnumUtils.{Nameable, Enumerable}

case class Meter(timestamp: ZonedDateTime, values: List[Value] = Nil)

case class Value(
  value: String,
  context: ReadingContext,
  format: ValueFormat,
  measurand: Measurand,
  phase: Option[Phase], // ocpp 1.6
  location: Location,
  unit: UnitOfMeasure
)

object DefaultValue {
  val readingContext = ReadingContext.SamplePeriodic
  val format = ValueFormat.Raw
  val measurand = Measurand.EnergyActiveImportRegister
  val location = Location.Outlet
  val unitOfMeasure = UnitOfMeasure.Wh
  val phase = None

  def apply(value: Int): Value = Value(value.toString, readingContext, format, measurand, phase, location, unitOfMeasure)

  def unapply(x: Value): Option[Int] = PartialFunction.condOpt(x) {
    case Value(value, `readingContext`, `format`, `measurand`, `phase`, `location`, `unitOfMeasure`) => value.toFloat.round
  }
}

sealed trait Measurand extends Nameable
object Measurand extends EnumerableWithDefault[Measurand] {
  case object EnergyActiveExportRegister extends Measurand { override def name = "Energy.Active.Export.Register" }
  case object EnergyActiveImportRegister extends Measurand { override def name = "Energy.Active.Import.Register" }
  case object EnergyReactiveExportRegister extends Measurand { override def name = "Energy.Reactive.Export.Register" }
  case object EnergyReactiveImportRegister extends Measurand { override def name = "Energy.Reactive.Import.Register" }
  case object EnergyActiveExportInterval extends Measurand { override def name = "Energy.Active.Export.Interval" }
  case object EnergyActiveImportInterval extends Measurand { override def name = "Energy.Active.Import.Interval" }
  case object EnergyReactiveExportInterval extends Measurand { override def name = "Energy.Reactive.Export.Interval" }
  case object EnergyReactiveImportInterval extends Measurand { override def name = "Energy.Reactive.Import.Interval" }
  case object PowerActiveExport extends Measurand { override def name = "Power.Active.Export" }
  case object PowerActiveImport extends Measurand { override def name = "Power.Active.Import" }
  case object PowerReactiveExport extends Measurand { override def name = "Power.Reactive.Export" }
  case object PowerReactiveImport extends Measurand { override def name = "Power.Reactive.Import" }
  case object PowerFactor extends Measurand { override def name = "Power.Factor" } // ocpp 1.6
  case object PowerOffered extends Measurand { override def name = "Power.Offered" } // ocpp 1.6
  case object CurrentExport extends Measurand { override def name = "Current.Export" }
  case object CurrentImport extends Measurand { override def name = "Current.Import" }
  case object CurrentOffered extends Measurand { override def name = "Current.Offered" } // ocpp 1.6
  case object Voltage extends Measurand { override def name = "Voltage" }
  case object Temperature extends Measurand { override def name = "Temperature" }
  case object Frequency extends Measurand { override def name = "Frequency" } // ocpp 1.6
  case object FanSpeedInRevolutionsPerMinute extends Measurand { override def name = "RPM" } // ocpp 1.6
  case object StateOfChargeInPercentage extends Measurand { override def name = "SoC" } // ocpp 1.6

  val values = Set(
    EnergyActiveExportRegister,
    EnergyActiveImportRegister,
    EnergyReactiveExportRegister,
    EnergyReactiveImportRegister,
    EnergyActiveExportInterval,
    EnergyActiveImportInterval,
    EnergyReactiveExportInterval,
    EnergyReactiveImportInterval,
    PowerActiveExport,
    PowerActiveImport,
    PowerReactiveExport,
    PowerReactiveImport,
    PowerFactor,
    PowerOffered,
    CurrentExport,
    CurrentImport,
    CurrentOffered,
    Voltage,
    Temperature,
    Frequency,
    FanSpeedInRevolutionsPerMinute,
    StateOfChargeInPercentage
  )

  val default = EnergyActiveImportRegister
}


sealed trait ValueFormat extends Nameable
object ValueFormat extends EnumerableWithDefault[ValueFormat] {
  case object Raw extends ValueFormat
  case object SignedData extends ValueFormat

  val values = Set(Raw, SignedData)

  val default = Raw
}

sealed trait ReadingContext extends Nameable
object ReadingContext extends EnumerableWithDefault[ReadingContext] {
  case object InterruptionBegin extends ReadingContext { override def name = "Interruption.Begin" }
  case object InterruptionEnd extends ReadingContext { override def name = "Interruption.End" }
  case object SampleClock extends ReadingContext { override def name = "Sample.Clock" }
  case object SamplePeriodic extends ReadingContext { override def name = "Sample.Periodic" }
  case object TransactionBegin extends ReadingContext { override def name = "Transaction.Begin" }
  case object TransactionEnd extends ReadingContext { override def name = "Transaction.End" }
  case object Trigger extends ReadingContext { override def name = "Trigger" } // ocpp 1.6
  case object Other extends ReadingContext { override def name = "Other" } // ocpp 1.6

  val values = Set(InterruptionBegin, InterruptionEnd, SampleClock, SamplePeriodic, TransactionBegin, TransactionEnd, Trigger, Other)

  val default = SamplePeriodic
}

sealed trait Phase extends Nameable
object Phase extends Enumerable[Phase] {
  case object L1 extends Phase
  case object L2 extends Phase
  case object L3 extends Phase
  case object N extends Phase
  case object L1N extends Phase { override def name = "L1-N"}
  case object L2N extends Phase { override def name = "L2-N"}
  case object L3N extends Phase { override def name = "L3-N"}
  case object L1L2 extends Phase { override def name = "L1-L2"}
  case object L2L3 extends Phase { override def name = "L2-L3"}
  case object L3L1 extends Phase { override def name = "L3-L1"}
  val values = Set(L1, L2, L3, N, L1N, L2N, L3N, L1L2, L2L3, L3L1)
}

sealed trait Location extends Nameable
object Location extends EnumerableWithDefault[Location] {
  case object Inlet extends Location
  case object Outlet extends Location
  case object Body extends Location

  // ocpp 1.6
  case object Cable extends Location
  case object Ev extends Location { override def name = "EV" }
  val values = Set(Inlet, Outlet, Body, Cable, Ev)
  val default = Outlet
}

sealed trait UnitOfMeasure extends Nameable
object UnitOfMeasure extends EnumerableWithDefault[UnitOfMeasure] {
  case object Wh         extends UnitOfMeasure { override def name = "Wh" }
  case object Kwh        extends UnitOfMeasure { override def name = "kWh" }
  case object Varh       extends UnitOfMeasure { override def name = "varh" }
  case object Kvarh      extends UnitOfMeasure { override def name = "kvarh" }
  case object W          extends UnitOfMeasure { override def name = "W" }
  case object Kw         extends UnitOfMeasure { override def name = "kW" }
  case object Var        extends UnitOfMeasure { override def name = "var" }
  case object Kvar       extends UnitOfMeasure { override def name = "kvar" }
  case object Amp        extends UnitOfMeasure { override def name = "A" }
  case object Volt       extends UnitOfMeasure { override def name = "V" }
  case object Celsius    extends UnitOfMeasure { override def name = "Celsius" }

  // ocpp 1.6
  case object Fahrenheit extends UnitOfMeasure { override def name =  "Fahrenheit" }
  case object Kelvin     extends UnitOfMeasure { override def name = "K" }
  case object Va         extends UnitOfMeasure { override def name = "VA" }
  case object Kva        extends UnitOfMeasure { override def name = "kVA" }
  case object Percent    extends UnitOfMeasure { override def name = "Percent" }

  def values = Set(Wh, Kwh, Varh, Kvarh, W, Kw, Var, Kvar, Amp, Volt, Celsius,
    Fahrenheit, Kelvin, Va, Kva, Percent)

  val default = Wh
}
