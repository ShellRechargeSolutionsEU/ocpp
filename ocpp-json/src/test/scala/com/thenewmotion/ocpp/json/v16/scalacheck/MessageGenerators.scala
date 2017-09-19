package com.thenewmotion.ocpp
package json.v16
package scalacheck

import org.scalacheck.Gen, Gen._

object MessageGenerators {

  import Helpers._

  def bootNotificationReqGen: Gen[BootNotificationReq] =
    for {
      chargePointVendor <- alphaNumStr
      chargePointModel <- words
      chargePointSerialNumber <- option(alphaNumStr)
      chargeBoxSerialNumber <- option(alphaNumStr)
      firmwareVersion <- option(alphaNumStr)
      iccid <- option(numStr)
      imsi <- option(numStr)
      meterType <- option(alphaNumStr)
      meterSerialNumber <- option(alphaNumStr)
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
      interval <- chooseNum(0, 100000)
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

  def stopTransactionReqGen: Gen[StopTransactionReq] =
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
        const("NoError")
      info <- if (status == "Faulted")
        option(words)
      else
        const(None)
      timestamp <- option(dateTimeGen)
      vendorId <- option(alphaNumStr)
      vendorErrorCode <- if (status == "Faulted")
        option(alphaNumStr)
      else
        const(None)
    } yield StatusNotificationReq(connectorId, status, errorCode, info, timestamp, vendorId, vendorErrorCode)

  def statusNotificationResGen: Gen[StatusNotificationRes] =
    const(StatusNotificationRes())

  def remoteStartTransactionReqGen: Gen[RemoteStartTransactionReq] =
    for {
      idTag <- idTagGen
      connectorId <- option(connectorIdGen)
      chargingProfile <- option(chargingProfileGen)
    } yield RemoteStartTransactionReq(idTag, connectorId, chargingProfile)

  def remoteStartTransactionResGen: Gen[RemoteStartTransactionRes] =
    acceptanceGen.map(RemoteStartTransactionRes)

  def remoteStopTransactionReqGen: Gen[RemoteStopTransactionReq] =
    transactionIdGen.map(RemoteStopTransactionReq)

  def remoteStopTransactionResGen: Gen[RemoteStopTransactionRes] =
    acceptanceGen.map(RemoteStopTransactionRes)

  def heartbeatReqGen: Gen[HeartbeatReq] = const(HeartbeatReq())

  def heartbeatResGen: Gen[HeartbeatRes] =
    dateTimeGen.map(HeartbeatRes)

  def updateFirmwareReqGen: Gen[UpdateFirmwareReq] =
    for {
      retrieveDate <- dateTimeGen
      location <- uriGen
      retries <- option(chooseNum(1, 5))
      retryInterval <- option(chooseNum(0, 600))
    } yield UpdateFirmwareReq(retrieveDate, location, retries, retryInterval)

  def updateFirmwareResGen: Gen[UpdateFirmwareRes] = const(UpdateFirmwareRes())

  def firmwareStatusNotificationReqGen: Gen[FirmwareStatusNotificationReq] =
    enumerableNameGen(messages.FirmwareStatus).map(FirmwareStatusNotificationReq)

  def firmwareStatusNotificationResGen: Gen[FirmwareStatusNotificationRes] =
    const(FirmwareStatusNotificationRes())

  def getDiagnosticsReqGen: Gen[GetDiagnosticsReq] =
    for {
      location <- uriGen
      startTime <- option(dateTimeGen)
      stopTime <- option(dateTimeGen)
      retries <- option(chooseNum(1, 5))
      retryInterval <- option(chooseNum(0, 600))
    } yield GetDiagnosticsReq(location, startTime, stopTime, retries, retryInterval)

  def getDiagnosticsResGen: Gen[GetDiagnosticsRes] =
    option(alphaNumStr).map(GetDiagnosticsRes)

  def diagnosticsStatusNotificationReqGen: Gen[DiagnosticsStatusNotificationReq] =
    enumerableNameGen(messages.DiagnosticsStatus).map(DiagnosticsStatusNotificationReq)

  def diagnosticsStatusNotificationResGen: Gen[DiagnosticsStatusNotificationRes] =
    const(DiagnosticsStatusNotificationRes())

  def meterValuesReqGen: Gen[MeterValuesReq] =
    for {
      connectorId <- connectorIdIncludingChargePointGen
      transactionId <- option(transactionIdGen)
      meters <- listOf(meterGen)
    } yield MeterValuesReq(connectorId, transactionId, meters)

  def meterValuesResGen: Gen[MeterValuesRes] = const(MeterValuesRes())

  def changeConfigurationReqGen: Gen[ChangeConfigurationReq] =
    for {
      key <- alphaNumStr
      value <- words
    } yield ChangeConfigurationReq(key, value)

  def changeConfigurationResGen: Gen[ChangeConfigurationRes] =
    enumerableNameGen(messages.ConfigurationStatus).map(ChangeConfigurationRes)

  def clearCacheReqGen: Gen[ClearCacheReq] = const(ClearCacheReq())

  def clearCacheResGen: Gen[ClearCacheRes] =
    acceptanceGen.map(ClearCacheRes)

  def getConfigurationReqGen: Gen[GetConfigurationReq] =
    for {
    // TODO None comes back as Some(List())
      keys <- some(listOf(alphaNumStr))
    } yield GetConfigurationReq(keys)

  def getConfigurationResGen:Gen[GetConfigurationRes] =
    for {
    // TODO also here, None comes back as Some(List())
      entries <- some(listOf(configurationEntryGen))
      unknownKeys <- some(listOf(alphaNumStr))
    } yield GetConfigurationRes(entries, unknownKeys)

  def getLocalListVersionReqGen: Gen[GetLocalListVersionReq] = const(GetLocalListVersionReq())

  def getLocalListVersionResGen: Gen[GetLocalListVersionRes] =
    chooseNum(1, 500).map(GetLocalListVersionRes)

  def reserveNowReqGen: Gen[ReserveNowReq] =
    for {
      connectorId <- connectorIdIncludingChargePointGen
      expiryDate <- dateTimeGen
      idTag <- idTagGen
      parentIdTag <- option(idTagGen)
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
      id <- option(chargingProfileIdGen)
      connectorId <- option(connectorIdIncludingChargePointGen)
      chargingProfilePurpose <- option(chargingProfilePurposeGen)
      stackLevel <- option(stackLevelGen)
    } yield ClearChargingProfileReq(id, connectorId, chargingProfilePurpose, stackLevel)

  def clearChargingProfileResGen: Gen[ClearChargingProfileRes] =
    enumerableNameGen(messages.ClearChargingProfileStatus).map(ClearChargingProfileRes)

  def getCompositeScheduleReqGen: Gen[GetCompositeScheduleReq] =
    for {
      connectorId <- connectorIdIncludingChargePointGen
      duration <- chooseNum(1, 10000000)
      chargingRateUnit <- option(chargingRateUnitGen)
    } yield GetCompositeScheduleReq(connectorId, duration, chargingRateUnit)

  def getCompositeScheduleResGen: Gen[GetCompositeScheduleRes] =
    for {
      status <- acceptanceGen
      connectorId <- if (status == "Accepted") some(connectorIdIncludingChargePointGen) else const(None)
      scheduleStart <- if (status == "Accepted") some(dateTimeGen) else const(None)
      schedule <- if (status == "Accepted") some(chargingScheduleGen) else const(None)
    } yield GetCompositeScheduleRes(status, connectorId, scheduleStart, schedule)

  def setChargingProfileReqGen: Gen[SetChargingProfileReq] =
    for {
      connectorId <- connectorIdIncludingChargePointGen
      chargingProfile <- chargingProfileGen
    } yield SetChargingProfileReq(connectorId, chargingProfile)

  def setChargingProfileResGen: Gen[SetChargingProfileRes] =
    enumerableNameGen(messages.ChargingProfileStatus).map(SetChargingProfileRes)

  def triggerMessageReqGen: Gen[TriggerMessageReq] =
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

  def triggerMessageResGen: Gen[TriggerMessageRes] =
    enumerableNameGen(messages.TriggerMessageStatus).map(TriggerMessageRes)
}
