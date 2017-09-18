package com.thenewmotion.ocpp
package json
package v16

import java.time.{ZonedDateTime, Instant, ZoneId}

import enums.reflection.EnumUtils.{Nameable, Enumerable}
import org.scalacheck.Gen
import org.scalacheck.Prop.forAll
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

object ConvertersV16Spec extends Specification with ScalaCheck {

  import Generators._

  "Conversion to/from OCPP 1.6-J format" should {

    "result in the same object after v16->generic->v16 transformation" in {
      forAll(messageGen) { msg =>
        ConvertersV16.toV16(ConvertersV16.fromV16(msg)) == msg
      }
    }
  }
}

object Generators {

  def transactionIdGen = Gen.chooseNum(1, 4000)
  def stopReasonGen = enumerableWithDefaultNameGen(messages.StopReason)

  // currently None goes to Some(List()) after two-way conversion
  def txnDataGen = Gen.some(Gen.listOf(meterGen))

  def connectorIdGen = Gen.chooseNum(1, 4)
  def idTagGen = Gen.listOf(Gen.alphaNumChar).map(_.mkString).suchThat(_.nonEmpty) // alphaNumStr
  def meterStartGen = Gen.chooseNum(0, 6000000)
  def meterStopGen = Gen.chooseNum(0, 6000000)
  def reservationIdGen = Gen.option(Gen.choose(0, 100))

  def dateTimeGen: Gen[ZonedDateTime] =
    for {
      randomInstantMillis <- Gen.chooseNum(1, Integer.MAX_VALUE.toLong)
    } yield {
      ZonedDateTime.ofInstant(Instant.ofEpochMilli(randomInstantMillis), ZoneId.of("UTC"))
    }

  def meterGen: Gen[Meter] = for {
    timestamp <- dateTimeGen
    sampledValue <- Gen.listOf(meterValueGen)
  } yield Meter(ZonedDateTime.now, sampledValue)

  def meterValueGen: Gen[MeterValue] = for {
    value <- Gen.alphaNumStr
    context <- enumerableWithDefaultNameGen(messages.Meter.ReadingContext)
    // TODO generating these creates freak java.lang.InternalError
    format <- Gen.const(None) // enumerableNameGenWithDefault(messages.Meter.ValueFormat, messages.Meter.ValueFormat.Raw)
    measurand <- enumerableWithDefaultNameGen(messages.Meter.Measurand)
    // TODO generating these creates freak java.lang.InternalError
    phase <- Gen.const(None) // Gen.option(enumerableNameGen(messages.Meter.Phase))
    // TODO generating these creates freak java.lang.InternalError
    location <- Gen.const(None) // enumerableNameGenWithDefault(messages.Meter.Location, messages.Meter.Location.Outlet)
    unit <- enumerableWithDefaultNameGen(messages.Meter.UnitOfMeasure)
  } yield MeterValue(value, context, format, measurand, phase, location, unit)

  def enumerableGen[T <: Nameable](e: Enumerable[T]): Gen[T]  =
    Gen.oneOf(e.values.toList)

  def enumerableNameGen[T <: Nameable](e: Enumerable[T]): Gen[String] = enumerableGen(e).map(_.name)

  def enumerableNameGenWithDefault[T <: Nameable](e: Enumerable[T], default: T): Gen[Option[String]] =
    enumerableGen(e) map {
      case `default`       => None
      case nonDefaultValue => Some(nonDefaultValue.name)
    }

  def enumerableWithDefaultNameGen[T <: Nameable](e: messages.EnumerableWithDefault[T]): Gen[Option[String]] =
    enumerableGen(e) map { value =>
      if (value == e.default)
        None
      else
        Some(value.name)
    }

  def startTransactionReqGen: Gen[StartTransactionReq] =
    for {
      connectorId <- connectorIdGen
      idTag <- idTagGen
      timestamp <- dateTimeGen
      meterStart <- meterStartGen
      reservationId <- reservationIdGen
    } yield {
      StartTransactionReq(
        connectorId,
        idTag,
        timestamp,
        meterStart,
        reservationId
      )
    }

  def stopTransactionReqGen: Gen[StopTransactionReq] =
    for {
       transactionId <- transactionIdGen
       idTag <- Gen.option(idTagGen)
       timestamp <- dateTimeGen
       meterStop <- meterStopGen
       reason <- stopReasonGen
       transactionData <- txnDataGen
    } yield {
      StopTransactionReq(
        transactionId,
        idTag,
        timestamp,
        meterStop,
        reason,
        transactionData
      )
    }

  def messageGen: Gen[Message] =
    Gen.oneOf(startTransactionReqGen, stopTransactionReqGen)
}

