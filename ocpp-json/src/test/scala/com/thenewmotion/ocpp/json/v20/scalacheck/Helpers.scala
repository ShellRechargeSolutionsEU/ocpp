package com.thenewmotion.ocpp
package json
package v20.scalacheck

import java.time.Instant
import org.scalacheck.Gen
import Gen._
import messages.v20._
import CommonGenerators._

object Helpers {

  /**
    * According to the OCPP 2.0 spec, the only restriction on "string" is that
    * it's Unicode. Be careful what you wish for people, but here you go.
    *
    * @param maxSize
    * @return
    */
  def ocppString(maxSize: Int): Gen[String] =
    resize(maxSize, listOf(chooseNum(0, 0x10FFFF).map(Character.toChars)))
      .map(_.flatten.mkString)

  def ocppIdentifierString(maxSize: Int): Gen[String] = resize(
    maxSize,
    listOf(oneOf(
      alphaNumChar,
      oneOf('*', '-', '_', '=', ':', '+', '|', '@', '.')
    )).map(_.mkString)
  )

  def chargingStation: Gen[ChargingStation] =
    for {
      serialNumber <- option(ocppString(20))
      model <- ocppString(20)
      vendorName <- ocppString(20)
      firmwareVersion <- option(ocppString(50))
      modem <- option(modem)
    } yield ChargingStation(serialNumber, model, vendorName, firmwareVersion, modem)

  def modem: Gen[Modem] =
    for {
      iccid <- option(ocppIdentifierString(20))
      imsi <- option(ocppIdentifierString(20))
    } yield Modem(iccid, imsi)

  // 4 trillion millis after epoch is October 2096
  def instant: Gen[Instant] = chooseNum(0, 4000000000000L).map(Instant.ofEpochMilli)

  def idToken: Gen[IdToken] =
    for {
      idToken <- ocppIdentifierString(36)
      _type <- enumerableGen(IdTokenType)
      additionalInfo <- option(nonEmptyListOf(additionalInfo))
    } yield IdToken(idToken, _type, additionalInfo)

  def additionalInfo: Gen[AdditionalInfo] =
    for {
      additionalIdToken <- ocppIdentifierString(36)
      _type <- ocppString(50)
    } yield AdditionalInfo(additionalIdToken, _type)

  def chargingProfile: Gen[ChargingProfile] =
    for {
      id <- chooseNum(0, Int.MaxValue)
      stackLevel <- chooseNum(0, Int.MaxValue)
      primary <- option(oneOf(true, false))
      chargingProfilePurpose <- enumerableGen(ChargingProfilePurpose)
      chargingProfileKind <- enumerableGen(ChargingProfileKind)
      recurrencyKind <- option(enumerableGen(RecurrencyKind))
      validFrom <- option(instant)
      validTo <- option(instant)
      transactionId <- option(ocppIdentifierString(36))
      chargingSchedule <- chargingSchedule
    } yield ChargingProfile(
      id,
      stackLevel,
      primary,
      chargingProfilePurpose,
      chargingProfileKind,
      recurrencyKind,
      validFrom,
      validTo,
      transactionId,
      chargingSchedule
    )

  def chargingSchedule: Gen[ChargingSchedule] =
    for {
      startSchedule <- option(instant)
      duration <- option(chooseNum(0, 100000))
      chargingRateUnit <- enumerableGen(ChargingRateUnit)
      minChargingRate <- option(chargingRate)
      chargingSchedulePeriod <- nonEmptyListOf(chargingSchedulePeriod)
    } yield ChargingSchedule(startSchedule, duration, chargingRateUnit, minChargingRate, chargingSchedulePeriod)

  def chargingSchedulePeriod: Gen[ChargingSchedulePeriod] =
    for {
      startPeriod <- chooseNum(0, 100000)
      limit <- chargingRate
      numberPhases <- option(oneOf(1, 2, 3))
      phaseToUse <- option(oneOf(1, 2, 3))

    } yield ChargingSchedulePeriod(startPeriod, limit, numberPhases, phaseToUse)

  def chargingRate: Gen[ChargingRate] =
    chooseNum(0, 3000000).map(_.toDouble / 10).map(ChargingRate)
}
