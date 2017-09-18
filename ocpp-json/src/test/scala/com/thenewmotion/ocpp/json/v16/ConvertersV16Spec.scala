package com.thenewmotion.ocpp
package json
package v16

import java.time.{ZonedDateTime, Instant, ZoneId}
import java.net.URI

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
        val after = ConvertersV16.toV16(ConvertersV16.fromV16(msg))
        after == msg
      }
    }
  }
}

object Generators {

  def transactionIdGen: Gen[Int] = Gen.chooseNum(1, 4000)
  def stopReasonGen: Gen[Option[String]] = enumerableWithDefaultNameGen(messages.StopReason)

  // currently None goes to Some(List()) after two-way conversion
  def txnDataGen: Gen[Option[List[Meter]]] = Gen.some(Gen.listOf(meterGen))

  def connectorIdGen: Gen[Int] = Gen.chooseNum(1, 4)
  def connectorIdIncludingChargePointGen: Gen[Int] = Gen.chooseNum(0, 4)
  def idTagGen = Gen.listOf(Gen.alphaNumChar).map(_.mkString).suchThat(_.nonEmpty) // alphaNumStr

  def idTagInfoGen: Gen[IdTagInfo] =
    for {
      status <- enumerableNameGen(messages.AuthorizationStatus)
      expiryDate <- Gen.option(dateTimeGen)
      parentIdTag <- Gen.option(idTagGen)
    } yield IdTagInfo(status, expiryDate, parentIdTag)

  def meterStartGen = Gen.chooseNum(0, 6000000)
  def meterStopGen = Gen.chooseNum(0, 6000000)
  def reservationIdGen = Gen.option(Gen.choose(0, 100))

  def acceptanceGen = Gen.oneOf(Gen.const("Accepted"), Gen.const("Rejected"))

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


  def uriGen: Gen[String] = for {
    scheme <- Gen.alphaStr.filter(_.nonEmpty)
    host <- Gen.alphaNumStr.filter(_.nonEmpty)
    path <- Gen.listOf(Gen.alphaNumStr).map(elems => "/" + elems.mkString("/"))
    fragment <- Gen.alphaNumStr
  } yield new URI(scheme, host, path, fragment).toString

  def chargePointStatusGen: Gen[String] =
    Gen.oneOf(
      "Available", "Preparing", "Charging", "SuspendedEV", "SuspendedEVSE",
      "Finishing", "Unavailable", "Reserved", "Faulted"
    )

  def rateLimitGen: Gen[Float] = Gen.chooseNum(0, 32).map(x => (x * 10).toFloat / 10)

  def chargingSchedulePeriodGen: Gen[ChargingSchedulePeriod] =
    for {
      startPeriod <- Gen.chooseNum(1, 4000000)
      limit <- rateLimitGen
      numberPhases <- Gen.option(Gen.oneOf(1, 2, 3))
    } yield ChargingSchedulePeriod(startPeriod, limit, numberPhases)

  def chargingScheduleGen: Gen[ChargingSchedule] =
    for {
      chargingRateUnit <- enumerableNameGen(messages.UnitOfChargeRate)
      chargingSchedulePeriod <- Gen.listOf(chargingSchedulePeriodGen)
      duration <- Gen.option(Gen.chooseNum(1, 4000000))
      startSchedule <- Gen.option(dateTimeGen)
      minChargingRate <- Gen.option(rateLimitGen)
    } yield ChargingSchedule(chargingRateUnit, chargingSchedulePeriod, duration, startSchedule,minChargingRate)

  def chargingProfileGen: Gen[ChargingProfile] =
    for {
      id <- Gen.choose(1, 32500)
      stackLevel <- Gen.choose(1, 10)
      purpose <- enumerableNameGen(messages.ChargingProfilePurpose)
      kind <- Gen.oneOf("Relative", "Absolute", "Recurring")
      schedule <- chargingScheduleGen
      transactionId <- Gen.option(transactionIdGen)
      recurrencyKind <- if (kind == "Recurring") Gen.some(Gen.oneOf("Daily", "Weekly")) else Gen.const(None)
      validFrom <- Gen.option(dateTimeGen)
      validTo <- Gen.option(dateTimeGen)
    } yield ChargingProfile(id, stackLevel, purpose, kind, schedule, transactionId, recurrencyKind, validFrom, validTo)

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

  def words: Gen[String] = Gen.listOf(Gen.oneOf(Gen.const(' '), Gen.alphaNumChar)).map(_.mkString)

  def bootNotificationReqGen: Gen[BootNotificationReq] =
    for {
      chargePointVendor <- Gen.alphaNumStr
      chargePointModel <- words
      chargePointSerialNumber <- Gen.option(Gen.alphaNumStr)
      chargeBoxSerialNumber <- Gen.option(Gen.alphaNumStr)
      firmwareVersion <- Gen.option(Gen.alphaNumStr)
      iccid <- Gen.option(Gen.numStr)
      imsi <- Gen.option(Gen.numStr)
      meterType <- Gen.option(Gen.alphaNumStr)
      meterSerialNumber <- Gen.option(Gen.alphaNumStr)
    } yield BootNotificationReq(
      chargePointVendor,
      chargePointModel,
      chargePointSerialNumber,
      chargeBoxSerialNumber,
      firmwareVersion,
      iccid,
      imsi,
      meterType,
      meterSerialNumber
    )

  def bootNotificationResGen: Gen[BootNotificationRes] =
    for {
      status <- enumerableNameGen(messages.RegistrationStatus)
      currentTime <- dateTimeGen
      interval <- Gen.chooseNum(0, 100000)
    } yield BootNotificationRes(status, currentTime, interval)

  def authorizeReqGen: Gen[AuthorizeReq] =
    idTagGen.map(AuthorizeReq)

  def authorizeResGen: Gen[AuthorizeRes] =
    idTagInfoGen.map(AuthorizeRes)

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

  def unlockConnectorReqGen: Gen[UnlockConnectorReq] =
    connectorIdGen.map(UnlockConnectorReq)

  def unlockConnectorResGen: Gen [UnlockConnectorRes] =
    enumerableNameGen(messages.UnlockStatus).map(UnlockConnectorRes)

  def resetReqGen: Gen[ResetReq] =
    enumerableNameGen(messages.ResetType).map(ResetReq)

  def resetResGen: Gen[ResetRes] =
    acceptanceGen.map(ResetRes)

  def changeAvailabilityReqGen: Gen[ChangeAvailabilityReq] =
    for {
      connectorId <- connectorIdIncludingChargePointGen
      avaType <- enumerableNameGen(messages.AvailabilityType)
    } yield ChangeAvailabilityReq(connectorId, avaType)

  def changeAvailabilityResGen: Gen[ChangeAvailabilityRes] =
    enumerableNameGen(messages.AvailabilityStatus).map(ChangeAvailabilityRes)

  def statusNotificationReqGen: Gen[StatusNotificationReq] =
    for {
      connectorId <- connectorIdIncludingChargePointGen
      status <- chargePointStatusGen
      errorCode <- if (status == "Faulted")
          enumerableNameGen(messages.ChargePointErrorCode)
        else
          Gen.const("NoError")
      info <- if (status == "Faulted")
          Gen.option(words)
        else
          Gen.const(None)
      timestamp <- Gen.option(dateTimeGen)
      vendorId <- Gen.option(Gen.alphaNumStr)
      vendorErrorCode <- if (status == "Faulted")
          Gen.option(Gen.alphaNumStr)
        else
          Gen.const(None)
    } yield StatusNotificationReq(connectorId, status, errorCode, info, timestamp, vendorId, vendorErrorCode)

  def statusNotificationResGen: Gen[StatusNotificationRes] =
    Gen.const(StatusNotificationRes())

  def remoteStartTransactionReqGen: Gen[RemoteStartTransactionReq] =
    for {
      idTag <- idTagGen
      connectorId <- Gen.option(connectorIdGen)
      chargingProfile <- Gen.option(chargingProfileGen)
    } yield RemoteStartTransactionReq(idTag, connectorId, chargingProfile)

  def updateFirmwareReqGen: Gen[UpdateFirmwareReq] =
    for {
      retrieveDate <- dateTimeGen
      location <- uriGen
      retries <- Gen.option(Gen.chooseNum(1, 5))
      retryInterval <- Gen.option(Gen.chooseNum(0, 600))
    } yield UpdateFirmwareReq(retrieveDate, location, retries, retryInterval)

  def updateFirmwareResGen: Gen[UpdateFirmwareRes] = Gen.const(UpdateFirmwareRes())

  def messageGen: Gen[Message] =
    Gen.oneOf(
      bootNotificationReqGen,
      bootNotificationResGen,
      authorizeReqGen,
      authorizeResGen,
      startTransactionReqGen,
      stopTransactionReqGen,
      unlockConnectorReqGen,
      unlockConnectorResGen,
      resetReqGen,
      resetResGen,
      changeAvailabilityReqGen,
      changeAvailabilityResGen,
      statusNotificationReqGen,
      statusNotificationResGen,
      remoteStartTransactionReqGen,
      updateFirmwareReqGen,
      updateFirmwareResGen
    )
}

