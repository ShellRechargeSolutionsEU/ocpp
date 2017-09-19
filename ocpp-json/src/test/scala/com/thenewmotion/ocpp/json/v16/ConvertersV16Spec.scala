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
  def idTagGen = Gen.alphaNumStr.filter(_.nonEmpty)

  def idTagInfoGen: Gen[IdTagInfo] =
    for {
      status <- enumerableNameGen(messages.AuthorizationStatus)
      expiryDate <- Gen.option(dateTimeGen)
      parentIdTag <- Gen.option(idTagGen)
    } yield IdTagInfo(status, expiryDate, parentIdTag)

  def meterStartGen = Gen.chooseNum(0, 6000000)
  def meterStopGen = Gen.chooseNum(0, 6000000)
  def reservationIdGen = Gen.choose(0, 100)

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

  def chargingProfileIdGen: Gen[Int] = Gen.choose(1, 32500)

  def chargingProfilePurposeGen: Gen[String] = enumerableNameGen(messages.ChargingProfilePurpose)

  def stackLevelGen: Gen[Int] = Gen.choose(1, 10)

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
      id <- chargingProfileIdGen
      stackLevel <- stackLevelGen
      purpose <- chargingProfilePurposeGen
      kind <- Gen.oneOf("Relative", "Absolute", "Recurring")
      schedule <- chargingScheduleGen
      transactionId <- Gen.option(transactionIdGen)
      recurrencyKind <- if (kind == "Recurring") Gen.some(Gen.oneOf("Daily", "Weekly")) else Gen.const(None)
      validFrom <- Gen.option(dateTimeGen)
      validTo <- Gen.option(dateTimeGen)
    } yield ChargingProfile(id, stackLevel, purpose, kind, schedule, transactionId, recurrencyKind, validFrom, validTo)

  def configurationEntryGen: Gen[ConfigurationEntry] =
    for {
      key <- Gen.alphaNumStr
      readOnly <- Gen.oneOf(true, false)
      value <- Gen.option(words)
    } yield ConfigurationEntry(key, readOnly, value)

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
      reservationId <- Gen.option(reservationIdGen)
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

  def remoteStartTransactionResGen: Gen[RemoteStartTransactionRes] =
    acceptanceGen.map(RemoteStartTransactionRes)

  def remoteStopTransactionReqGen: Gen[RemoteStopTransactionReq] =
    transactionIdGen.map(RemoteStopTransactionReq)

  def remoteStopTransactionResGen: Gen[RemoteStopTransactionRes] =
    acceptanceGen.map(RemoteStopTransactionRes)

  def heartbeatReqGen: Gen[HeartbeatReq] = Gen.const(HeartbeatReq())

  def heartbeatResGen: Gen[HeartbeatRes] =
    dateTimeGen.map(HeartbeatRes)

  def updateFirmwareReqGen: Gen[UpdateFirmwareReq] =
    for {
      retrieveDate <- dateTimeGen
      location <- uriGen
      retries <- Gen.option(Gen.chooseNum(1, 5))
      retryInterval <- Gen.option(Gen.chooseNum(0, 600))
    } yield UpdateFirmwareReq(retrieveDate, location, retries, retryInterval)

  def updateFirmwareResGen: Gen[UpdateFirmwareRes] = Gen.const(UpdateFirmwareRes())

  def firmwareStatusNotificationReqGen: Gen[FirmwareStatusNotificationReq] =
    enumerableNameGen(messages.FirmwareStatus).map(FirmwareStatusNotificationReq)

  def firmwareStatusNotificationResGen: Gen[FirmwareStatusNotificationRes] =
    Gen.const(FirmwareStatusNotificationRes())

  def getDiagnosticsReqGen: Gen[GetDiagnosticsReq] =
    for {
      location <- uriGen
      startTime <- Gen.option(dateTimeGen)
      stopTime <- Gen.option(dateTimeGen)
      retries <- Gen.option(Gen.chooseNum(1, 5))
      retryInterval <- Gen.option(Gen.chooseNum(0, 600))
    } yield GetDiagnosticsReq(location, startTime, stopTime, retries, retryInterval)

  def getDiagnosticsResGen: Gen[GetDiagnosticsRes] =
    Gen.option(Gen.alphaNumStr).map(GetDiagnosticsRes)

  def diagnosticsStatusNotificationReqGen: Gen[DiagnosticsStatusNotificationReq] =
    enumerableNameGen(messages.DiagnosticsStatus).map(DiagnosticsStatusNotificationReq)

  def diagnosticsStatusNotificationResGen: Gen[DiagnosticsStatusNotificationRes] =
    Gen.const(DiagnosticsStatusNotificationRes())

  def meterValuesReqGen: Gen[MeterValuesReq] =
    for {
      connectorId <- connectorIdIncludingChargePointGen
      transactionId <- Gen.option(transactionIdGen)
      meters <- Gen.listOf(meterGen)
    } yield MeterValuesReq(connectorId, transactionId, meters)

  def meterValuesResGen: Gen[MeterValuesRes] = Gen.const(MeterValuesRes())

  def changeConfigurationReqGen: Gen[ChangeConfigurationReq] =
    for {
      key <- Gen.alphaNumStr
      value <- words
    } yield ChangeConfigurationReq(key, value)

  def changeConfigurationResGen: Gen[ChangeConfigurationRes] =
    enumerableNameGen(messages.ConfigurationStatus).map(ChangeConfigurationRes)

  def clearCacheReqGen: Gen[ClearCacheReq] = Gen.const(ClearCacheReq())

  def clearCacheResGen: Gen[ClearCacheRes] =
    acceptanceGen.map(ClearCacheRes)

  def getConfigurationReqGen: Gen[GetConfigurationReq] =
    for {
      // TODO None comes back as Some(List())
      keys <- Gen.some(Gen.listOf(Gen.alphaNumStr))
    } yield GetConfigurationReq(keys)

  def getConfigurationResGen:Gen[GetConfigurationRes] =
    for {
      // TODO also here, None comes back as Some(List())
      entries <- Gen.some(Gen.listOf(configurationEntryGen))
      unknownKeys <- Gen.some(Gen.listOf(Gen.alphaNumStr))
    } yield GetConfigurationRes(entries, unknownKeys)

  def getLocalListVersionReqGen: Gen[GetLocalListVersionReq] = Gen.const(GetLocalListVersionReq())

  def getLocalListVersionResGen: Gen[GetLocalListVersionRes] =
    Gen.chooseNum(1, 500).map(GetLocalListVersionRes)

  def reserveNowReqGen: Gen[ReserveNowReq] =
    for {
      connectorId <- connectorIdIncludingChargePointGen
      expiryDate <- dateTimeGen
      idTag <- idTagGen
      parentIdTag <- Gen.option(idTagGen)
      reservationId <- reservationIdGen
    } yield ReserveNowReq(connectorId, expiryDate, idTag, parentIdTag, reservationId)

  def reserveNowResGen: Gen[ReserveNowRes] =
    enumerableNameGen(messages.Reservation).map(ReserveNowRes)

  def cancelReservationReqGen: Gen[CancelReservationReq] =
    reservationIdGen.map(CancelReservationReq)

  def cancelReservationResGen: Gen[CancelReservationRes] =
    acceptanceGen.map(CancelReservationRes)

  def clearChargingProfileReqGen: Gen[ClearChargingProfileReq] =
    for {
      id <- Gen.option(chargingProfileIdGen)
      connectorId <- Gen.option(connectorIdIncludingChargePointGen)
      chargingProfilePurpose <- Gen.option(chargingProfilePurposeGen)
      stackLevel <- Gen.option(stackLevelGen)
    } yield ClearChargingProfileReq(id, connectorId, chargingProfilePurpose, stackLevel)

  def clearChargingProfileResGen: Gen[ClearChargingProfileRes] =
    enumerableNameGen(messages.ClearChargingProfileStatus).map(ClearChargingProfileRes)

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
      remoteStartTransactionResGen,
      remoteStopTransactionReqGen,
      remoteStopTransactionResGen,
      heartbeatReqGen,
      heartbeatResGen,
      updateFirmwareReqGen,
      updateFirmwareResGen,
      firmwareStatusNotificationReqGen,
      firmwareStatusNotificationResGen,
      getDiagnosticsReqGen,
      getDiagnosticsResGen,
      diagnosticsStatusNotificationReqGen,
      diagnosticsStatusNotificationResGen,
      meterValuesReqGen,
      meterValuesResGen,
      changeConfigurationReqGen,
      changeConfigurationResGen,
      clearCacheReqGen,
      clearCacheResGen,
      getConfigurationReqGen,
      getConfigurationResGen,
      getLocalListVersionReqGen,
      getLocalListVersionResGen,
      reserveNowReqGen,
      reserveNowResGen,
      cancelReservationReqGen,
      cancelReservationResGen,
      clearChargingProfileReqGen,
      clearChargingProfileResGen
    )
}

