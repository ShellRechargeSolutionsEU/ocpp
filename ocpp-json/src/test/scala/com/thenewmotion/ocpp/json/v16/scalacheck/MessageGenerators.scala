package com.thenewmotion.ocpp
package json.v16
package scalacheck

import org.scalacheck.Gen, Gen._

object MessageGenerators {

  import Helpers._

  def bootNotificationReq: Gen[BootNotificationReq] =
    for {
      chargePointVendor <- Gen.resize(20, alphaNumStr)
      chargePointModel <- Gen.resize(20, words)
      chargePointSerialNumber <- option(Gen.resize(25, alphaNumStr))
      chargeBoxSerialNumber <- option(Gen.resize(25, alphaNumStr))
      firmwareVersion <- option(Gen.resize(50, alphaNumStr))
      iccid <- option(Gen.resize(20, numStr))
      imsi <- option(Gen.resize(20, numStr))
      meterType <- option(Gen.resize(25, alphaNumStr))
      meterSerialNumber <- option(Gen.resize(25, alphaNumStr))
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

  def bootNotificationRes: Gen[BootNotificationRes] =
    for {
      status <- enumerableNameGen(messages.RegistrationStatus)
      currentTime <- dateTimeGen
      interval <- chooseNum(0, 100000)
    } yield BootNotificationRes(status, currentTime, interval)

  def authorizeReq: Gen[AuthorizeReq] =
    idTagGen.map(AuthorizeReq)

  def authorizeRes: Gen[AuthorizeRes] =
    idTagInfoGen.map(AuthorizeRes)

  def startTransactionReq: Gen[StartTransactionReq] =
    for {
      connectorId <- connectorIdGen
      idTag <- idTagGen
      timestamp <- dateTimeGen
      meterStart <- meterStartGen
      reservationId <- option(reservationIdGen)
    } yield {
      StartTransactionReq(
        connectorId,
        idTag,
        timestamp,
        meterStart,
        reservationId
      )
    }
  def startTransactionRes: Gen[StartTransactionRes] = ???

  def stopTransactionReq: Gen[StopTransactionReq] =
    for {
      transactionId <- transactionIdGen
      idTag <- option(idTagGen)
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

  def stopTransactionRes: Gen[StopTransactionRes] = ???

  def unlockConnectorReq: Gen[UnlockConnectorReq] =
    connectorIdGen.map(UnlockConnectorReq)

  def unlockConnectorRes: Gen [UnlockConnectorRes] =
    enumerableNameGen(messages.UnlockStatus).map(UnlockConnectorRes)

  def resetReq: Gen[ResetReq] =
    enumerableNameGen(messages.ResetType).map(ResetReq)

  def resetRes: Gen[ResetRes] =
    acceptanceGen.map(ResetRes)

  def changeAvailabilityReq: Gen[ChangeAvailabilityReq] =
    for {
      connectorId <- connectorIdIncludingChargePointGen
      avaType <- enumerableNameGen(messages.AvailabilityType)
    } yield ChangeAvailabilityReq(connectorId, avaType)

  def changeAvailabilityRes: Gen[ChangeAvailabilityRes] =
    enumerableNameGen(messages.AvailabilityStatus).map(ChangeAvailabilityRes)

  def statusNotificationReq: Gen[StatusNotificationReq] =
    for {
      connectorId <- connectorIdIncludingChargePointGen
      status <- chargePointStatusGen
      errorCode <- if (status == "Faulted")
        enumerableNameGen(messages.ChargePointErrorCode)
      else
        const("NoError")
      info <- if (status == "Faulted")
        option(Gen.resize(50, words))
      else
        const(None)
      timestamp <- option(dateTimeGen)
      vendorId <- option(Gen.resize(255, alphaNumStr))
      vendorErrorCode <- if (status == "Faulted")
        option(Gen.resize(50, alphaNumStr))
      else
        const(None)
    } yield StatusNotificationReq(connectorId, status, errorCode, info, timestamp, vendorId, vendorErrorCode)

  def statusNotificationRes: Gen[StatusNotificationRes] =
    const(StatusNotificationRes())

  def remoteStartTransactionReq: Gen[RemoteStartTransactionReq] =
    for {
      idTag <- idTagGen
      connectorId <- option(connectorIdGen)
      chargingProfile <- option(chargingProfileGen)
    } yield RemoteStartTransactionReq(idTag, connectorId, chargingProfile)

  def remoteStartTransactionRes: Gen[RemoteStartTransactionRes] =
    acceptanceGen.map(RemoteStartTransactionRes)

  def remoteStopTransactionReq: Gen[RemoteStopTransactionReq] =
    transactionIdGen.map(RemoteStopTransactionReq)

  def remoteStopTransactionRes: Gen[RemoteStopTransactionRes] =
    acceptanceGen.map(RemoteStopTransactionRes)

  def heartbeatReq: Gen[HeartbeatReq] = const(HeartbeatReq())

  def heartbeatRes: Gen[HeartbeatRes] =
    dateTimeGen.map(HeartbeatRes)

  def updateFirmwareReq: Gen[UpdateFirmwareReq] =
    for {
      retrieveDate <- dateTimeGen
      location <- uriGen
      retries <- option(chooseNum(1, 5))
      retryInterval <- option(chooseNum(0, 600))
    } yield UpdateFirmwareReq(retrieveDate, location, retries, retryInterval)

  def updateFirmwareRes: Gen[UpdateFirmwareRes] = const(UpdateFirmwareRes())

  def firmwareStatusNotificationReq: Gen[FirmwareStatusNotificationReq] =
    enumerableNameGen(messages.FirmwareStatus).map(FirmwareStatusNotificationReq)

  def firmwareStatusNotificationRes: Gen[FirmwareStatusNotificationRes] =
    const(FirmwareStatusNotificationRes())

  def getDiagnosticsReq: Gen[GetDiagnosticsReq] =
    for {
      location <- uriGen
      startTime <- option(dateTimeGen)
      stopTime <- option(dateTimeGen)
      retries <- option(chooseNum(1, 5))
      retryInterval <- option(chooseNum(0, 600))
    } yield GetDiagnosticsReq(location, startTime, stopTime, retries, retryInterval)

  def getDiagnosticsRes: Gen[GetDiagnosticsRes] =
    option(alphaNumStr).map(GetDiagnosticsRes)

  def diagnosticsStatusNotificationReq: Gen[DiagnosticsStatusNotificationReq] =
    enumerableNameGen(messages.DiagnosticsStatus).map(DiagnosticsStatusNotificationReq)

  def diagnosticsStatusNotificationRes: Gen[DiagnosticsStatusNotificationRes] =
    const(DiagnosticsStatusNotificationRes())

  def meterValuesReq: Gen[MeterValuesReq] =
    for {
      connectorId <- connectorIdIncludingChargePointGen
      transactionId <- option(transactionIdGen)
      meters <- listOf(meterGen)
    } yield MeterValuesReq(connectorId, transactionId, meters)

  def meterValuesRes: Gen[MeterValuesRes] = const(MeterValuesRes())

  def changeConfigurationReq: Gen[ChangeConfigurationReq] =
    for {
      key <- Gen.resize(50, alphaNumStr)
      value <- Gen.resize(500, words)
    } yield ChangeConfigurationReq(key, value)

  def changeConfigurationRes: Gen[ChangeConfigurationRes] =
    enumerableNameGen(messages.ConfigurationStatus).map(ChangeConfigurationRes)

  def clearCacheReq: Gen[ClearCacheReq] = const(ClearCacheReq())

  def clearCacheRes: Gen[ClearCacheRes] =
    acceptanceGen.map(ClearCacheRes)

  def getConfigurationReq: Gen[GetConfigurationReq] =
    for {
      keys <- optionalNonEmptyList(Gen.resize(50, alphaNumStr))
    } yield GetConfigurationReq(keys)

  def getConfigurationRes: Gen[GetConfigurationRes] =
    for {
      entries <- optionalNonEmptyList(configurationEntryGen)
      unknownKeys <- optionalNonEmptyList(Gen.resize(50,alphaNumStr))
    } yield GetConfigurationRes(entries, unknownKeys)

  def getLocalListVersionReq: Gen[GetLocalListVersionReq] = const(GetLocalListVersionReq())

  def getLocalListVersionRes: Gen[GetLocalListVersionRes] =
    chooseNum(1, 500).map(GetLocalListVersionRes)

  def reserveNowReq: Gen[ReserveNowReq] =
    for {
      connectorId <- connectorIdIncludingChargePointGen
      expiryDate <- dateTimeGen
      idTag <- idTagGen
      parentIdTag <- option(idTagGen)
      reservationId <- reservationIdGen
    } yield ReserveNowReq(connectorId, expiryDate, idTag, parentIdTag, reservationId)

  def reserveNowRes: Gen[ReserveNowRes] =
    enumerableNameGen(messages.Reservation).map(ReserveNowRes)

  def cancelReservationReq: Gen[CancelReservationReq] =
    reservationIdGen.map(CancelReservationReq)

  def cancelReservationRes: Gen[CancelReservationRes] =
    acceptanceGen.map(CancelReservationRes)

  def clearChargingProfileReq: Gen[ClearChargingProfileReq] =
    for {
      id <- option(chargingProfileIdGen)
      connectorId <- option(connectorIdIncludingChargePointGen)
      chargingProfilePurpose <- option(chargingProfilePurposeGen)
      stackLevel <- option(stackLevelGen)
    } yield ClearChargingProfileReq(id, connectorId, chargingProfilePurpose, stackLevel)

  def clearChargingProfileRes: Gen[ClearChargingProfileRes] =
    enumerableNameGen(messages.ClearChargingProfileStatus).map(ClearChargingProfileRes)

  def getCompositeScheduleReq: Gen[GetCompositeScheduleReq] =
    for {
      connectorId <- connectorIdIncludingChargePointGen
      duration <- chooseNum(1, 10000000)
      chargingRateUnit <- option(chargingRateUnitGen)
    } yield GetCompositeScheduleReq(connectorId, duration, chargingRateUnit)

  def getCompositeScheduleRes: Gen[GetCompositeScheduleRes] =
    for {
      status <- acceptanceGen
      connectorId <- if (status == "Accepted") some(connectorIdIncludingChargePointGen) else const(None)
      scheduleStart <- if (status == "Accepted") some(dateTimeGen) else const(None)
      schedule <- if (status == "Accepted") some(chargingScheduleGen) else const(None)
    } yield GetCompositeScheduleRes(status, connectorId, scheduleStart, schedule)

  def setChargingProfileReq: Gen[SetChargingProfileReq] =
    for {
      connectorId <- connectorIdIncludingChargePointGen
      chargingProfile <- chargingProfileGen
    } yield SetChargingProfileReq(connectorId, chargingProfile)

  def setChargingProfileRes: Gen[SetChargingProfileRes] =
    enumerableNameGen(messages.ChargingProfileStatus).map(SetChargingProfileRes)

  def triggerMessageReq: Gen[TriggerMessageReq] =
    for {
      requestedMessage <- oneOf(
        enumerableNameGen(messages.MessageTriggerWithoutScope),
        const("StatusNotification"),
        const("MeterValues"))
      connectorId <- requestedMessage match {
        case "StatusNotification" | "MeterValues" => option(connectorIdIncludingChargePointGen)
        case _ => const(None)
      }
    } yield TriggerMessageReq(requestedMessage, connectorId)

  def triggerMessageRes: Gen[TriggerMessageRes] =
    enumerableNameGen(messages.TriggerMessageStatus).map(TriggerMessageRes)
}
