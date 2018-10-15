package com.thenewmotion.ocpp
package json
package v1x
package v16
package scalacheck

import java.time.{Instant, ZoneId, ZonedDateTime}
import java.net.URI

import org.scalacheck.Gen
import Gen._
import enums.reflection.EnumUtils.Nameable
import CommonGenerators._
import messages.v1x
import v1x.{ChargingProfilePurpose, UnitOfChargingRate}

object Helpers {

  def transactionIdGen: Gen[Int] = chooseNum(1, 4000)
  def stopReasonGen: Gen[Option[String]] = enumerableWithDefaultNameGen(v1x.StopReason)

  def txnDataGen: Gen[Option[List[Meter]]] = optionalNonEmptyList(meterGen)

  def connectorIdGen: Gen[Int] = chooseNum(1, 4)
  def connectorIdIncludingChargePointGen: Gen[Int] = chooseNum(0, 4)
  def idTagGen: Gen[String] = Gen.resize(20, alphaNumStr.filter(_.nonEmpty))

  def idTagInfoGen: Gen[IdTagInfo] =
    for {
      status <- enumerableNameGen(v1x.AuthorizationStatus)
      expiryDate <- option(dateTimeGen)
      parentIdTag <- option(idTagGen)
    } yield IdTagInfo(status, expiryDate, parentIdTag)

  def meterStartGen: Gen[Int] = chooseNum(0, 6000000)
  def meterStopGen: Gen[Int] = chooseNum(0, 6000000)
  def reservationIdGen: Gen[Int] = choose(0, 100)

  def acceptanceGen: Gen[String] = oneOf(const("Accepted"), const("Rejected"))

  def dateTimeGen: Gen[ZonedDateTime] =
    for {
      randomInstantMillis <- chooseNum(1, Integer.MAX_VALUE.toLong)
    } yield {
      ZonedDateTime.ofInstant(Instant.ofEpochMilli(randomInstantMillis), ZoneId.of("UTC"))
    }

  def meterGen: Gen[Meter] = for {
    timestamp <- dateTimeGen
    sampledValue <- listOf(meterValueGen)
  } yield Meter(ZonedDateTime.now, sampledValue)

  def meterValueGen: Gen[MeterValue] = for {
    value <- alphaNumStr
    context <- enumerableWithDefaultNameGen(v1x.meter.ReadingContext)
    format <- enumerableWithDefaultNameGen(v1x.meter.ValueFormat)
    measurand <- enumerableWithDefaultNameGen(v1x.meter.Measurand)
    phase <- option(enumerableNameGen(v1x.meter.Phase))
    location <- enumerableWithDefaultNameGen(v1x.meter.Location)
    unit <- enumerableWithDefaultNameGen(v1x.meter.UnitOfMeasure)
  } yield MeterValue(value, context, format, measurand, phase, location, unit)


  def uriGen: Gen[String] = for {
    scheme <- alphaStr.filter(_.nonEmpty)
    host <- alphaNumStr.filter(_.nonEmpty)
    path <- listOf(alphaNumStr).map(elems => "/" + elems.mkString("/"))
    fragment <- alphaNumStr
  } yield new URI(scheme, host, path, fragment).toString

  def chargePointStatusGen: Gen[String] =
    oneOf(
      "Available", "Preparing", "Charging", "SuspendedEV", "SuspendedEVSE",
      "Finishing", "Unavailable", "Reserved", "Faulted"
    )

  def rateLimitGen: Gen[Float] = chooseNum(0, 32).map(x => (x * 10).toFloat / 10)

  def chargingProfileIdGen: Gen[Int] = choose(1, 32500)

  def chargingProfilePurposeGen: Gen[String] = enumerableNameGen(ChargingProfilePurpose)

  def stackLevelGen: Gen[Int] = choose(1, 10)

  def chargingRateUnitGen: Gen[String] = enumerableNameGen(UnitOfChargingRate)

  def chargingSchedulePeriodGen: Gen[ChargingSchedulePeriod] =
    for {
      startPeriod <- chooseNum(1, 4000000)
      limit <- rateLimitGen
      numberPhases <- option(oneOf(1, 2, 3))
    } yield ChargingSchedulePeriod(startPeriod, limit, numberPhases)

  def chargingScheduleGen: Gen[ChargingSchedule] =
    for {
      chargingRateUnit <- chargingRateUnitGen
      chargingSchedulePeriod <- listOf(chargingSchedulePeriodGen)
      duration <- option(chooseNum(1, 4000000))
      startSchedule <- option(dateTimeGen)
      minChargingRate <- option(rateLimitGen)
    } yield ChargingSchedule(chargingRateUnit, chargingSchedulePeriod, duration, startSchedule,minChargingRate)

  def chargingProfileGen: Gen[ChargingProfile] =
    for {
      id <- chargingProfileIdGen
      stackLevel <- stackLevelGen
      purpose <- chargingProfilePurposeGen
      kind <- oneOf("Relative", "Absolute", "Recurring")
      schedule <- chargingScheduleGen
      transactionId <- option(transactionIdGen)
      recurrencyKind <- if (kind == "Recurring") some(oneOf("Daily", "Weekly")) else const(None)
      validFrom <- option(dateTimeGen)
      validTo <- option(dateTimeGen)
    } yield ChargingProfile(id, stackLevel, purpose, kind, schedule, transactionId, recurrencyKind, validFrom, validTo)

  def configurationEntryGen: Gen[ConfigurationEntry] =
    for {
      key <- Gen.resize(50,alphaNumStr)
      readOnly <- oneOf(true, false)
      value <- option(Gen.resize(500, words))
    } yield ConfigurationEntry(key, readOnly, value)

  def authorisationDataGen: Gen[AuthorisationData] =
    for {
      tag <- idTagGen
      info <- option(idTagInfoGen)
    } yield AuthorisationData(tag, info)

  def words: Gen[String] = listOf(oneOf(const(' '), alphaNumChar)).map(_.mkString)

  /**
    * Some fields in OCPP-J messages are optional strings, where the absence of
    * the field means that the message should be processed as if the field was
    * present with a default value. In those cases, we always want to encode
    * the message with the field absent instead of with an unnecessary string
    * value.
    *
    * So we have this generator that makes sure that whenever we generate either
    * a non-default value, or None.
    *
    * @param e
    * @tparam T
    * @return
    */
  def enumerableWithDefaultNameGen[T <: Nameable](e: v1x.EnumerableWithDefault[T]): Gen[Option[String]] =
    enumerableGen(e) map { value =>
      if (value == e.default)
        None
      else
        Some(value.name)
    }

  /**
   * Some fields in OCPP-J messages are optional lists of things, where absence of the field means the same as an empty
   * list. In those cases, we always want to encode the message with the field absent instead of with an empty list.
   *
   * Deciding on one option keeps things simple, makes it testable, and even saves a few bytes of bandwidth.
   *
   * @param gen
   * @tparam A
   * @return
   */
  def optionalNonEmptyList[A](gen: Gen[A]): Gen[Option[List[A]]] =
    oneOf(const(None), some(listOf(gen).filter(_.nonEmpty)))
}
