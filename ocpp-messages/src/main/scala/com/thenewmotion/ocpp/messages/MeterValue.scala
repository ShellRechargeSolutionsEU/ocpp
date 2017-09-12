package com.thenewmotion.ocpp.messages

import java.time.ZonedDateTime
import enums.reflection.EnumUtils.{Nameable, Enumerable}

case class Meter(timestamp: ZonedDateTime, values: List[Meter.Value] = Nil)

object Meter {

  case class Value(value: String,
    context: ReadingContext,
    format: ValueFormat,
    measurand: Measurand,
    location: Location,
    unit: UnitOfMeasure)

  object DefaultValue {
    val readingContext = ReadingContext.SamplePeriodic
    val format = ValueFormat.Raw
    val measurand = Measurand.EnergyActiveImportRegister
    val location = Location.Outlet
    val unitOfMeasure = UnitOfMeasure.Wh

    def apply(value: Int): Value = Value(value.toString, readingContext, format, measurand, location, unitOfMeasure)

    def unapply(x: Value): Option[Int] = PartialFunction.condOpt(x) {
      case Value(value, `readingContext`, `format`, `measurand`, `location`, `unitOfMeasure`) => value.toFloat.round
    }
  }

  sealed trait Measurand extends Nameable
  object Measurand extends Enumerable[Measurand] {
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
    case object CurrentExport extends Measurand { override def name = "Current.Export" }
    case object CurrentImport extends Measurand { override def name = "Current.Import" }
    case object Voltage extends Measurand { override def name = "Voltage" }
    case object Temperature extends Measurand { override def name = "Temperature" }

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
      CurrentExport,
      CurrentImport,
      Voltage,
      Temperature
    )
  }

  sealed trait ValueFormat extends Nameable
  object ValueFormat extends Enumerable[ValueFormat] {
    object Raw extends ValueFormat
    object Signed extends ValueFormat

    val values = Set(Raw, Signed)
  }

  sealed trait ReadingContext extends Nameable
  object ReadingContext extends Enumerable[ReadingContext] {
    case object InterruptionBegin extends ReadingContext { override def name = "Interruption.Begin" }
    case object InterruptionEnd extends ReadingContext { override def name = "Interruption.End" }
    case object SampleClock extends ReadingContext { override def name = "Sample.Clock" }
    case object SamplePeriodic extends ReadingContext { override def name = "Sample.Periodic" }
    case object TransactionBegin extends ReadingContext { override def name = "Transaction.Begin" }
    case object TransactionEnd extends ReadingContext { override def name = "Transaction.End" }

    val values = Set(InterruptionBegin, InterruptionEnd, SampleClock, SamplePeriodic, TransactionBegin, TransactionEnd)
  }

  sealed trait Location extends Nameable

  object Location extends Enumerable[Location] {

    case object Inlet extends Location

    case object Outlet extends Location

    case object Body extends Location

    val values = Set(Inlet, Outlet, Body)
  }

  sealed trait UnitOfMeasure extends Nameable
  object UnitOfMeasure extends Enumerable[UnitOfMeasure] {
    case object Wh      extends UnitOfMeasure { override def name = "Wh" }
    case object Kwh     extends UnitOfMeasure { override def name = "kWh" }
    case object Varh    extends UnitOfMeasure { override def name = "varh" }
    case object Kvarh   extends UnitOfMeasure { override def name = "kvarh" }
    case object W       extends UnitOfMeasure { override def name = "W" }
    case object Kw      extends UnitOfMeasure { override def name = "kW" }
    case object Var     extends UnitOfMeasure { override def name = "var" }
    case object Kvar    extends UnitOfMeasure { override def name = "kvar" }
    case object Amp     extends UnitOfMeasure { override def name = "Amp" }
    case object Volt    extends UnitOfMeasure { override def name = "Volt" }
    case object Celsius extends UnitOfMeasure { override def name = "Celsius" }

    def values = Set(Wh, Kwh, Varh, Kvarh, W, Kw, Var, Kvar, Amp, Volt, Celsius)
  }
}

