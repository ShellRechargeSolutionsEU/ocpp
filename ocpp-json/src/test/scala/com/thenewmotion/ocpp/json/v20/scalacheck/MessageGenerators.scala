package com.thenewmotion.ocpp
package json
package v20
package scalacheck

import org.scalacheck.Gen
import Gen._
import Helpers._
import CommonGenerators._
import messages.v20._

object MessageGenerators {

  def getBaseReportRequest: Gen[GetBaseReportRequest] = for {
    requestId <- ocppInteger
    reportBase <- enumerableGen(ReportBase)
  } yield GetBaseReportRequest(requestId, reportBase)

  def getBaseReportResponse: Gen[GetBaseReportResponse] =
    enumerableGen(GenericDeviceModelStatus).map(GetBaseReportResponse)

  def getTransactionStatusRequest: Gen[GetTransactionStatusRequest] =
    option(ocppIdentifierString(36)).map(GetTransactionStatusRequest)

  def getTransactionStatusResponse: Gen[GetTransactionStatusResponse] = for {
    ongoingIndicator <- option(oneOf(true, false))
    messagesInQueue <- oneOf(true, false)
  } yield GetTransactionStatusResponse(ongoingIndicator, messagesInQueue)

  def getVariablesRequest: Gen[GetVariablesRequest] =
    nonEmptyListOf(getVariableData).map(GetVariablesRequest)

  def getVariablesResponse: Gen[GetVariablesResponse] =
    nonEmptyListOf(getVariableResult).map(GetVariablesResponse)

  def requestStartTransactionRequest: Gen[RequestStartTransactionRequest] =
    for {
      // kom maar op met dat laadplein
      evseId <- option(chooseNum(1, 1000))
      remoteStartId <- chooseNum(0, Int.MaxValue)
      idToken <- idToken
      chargingProfile <- option(chargingProfile)
    } yield RequestStartTransactionRequest(evseId, remoteStartId, idToken, chargingProfile)

  def requestStartTransactionResponse: Gen[RequestStartTransactionResponse] =
    for {
      status <- enumerableGen(RequestStartStopStatus)
      transactionId <- option(ocppIdentifierString(36))
    } yield RequestStartTransactionResponse(status, transactionId)

  def setVariablesRequest: Gen[SetVariablesRequest] =
    nonEmptyListOf(setVariableData).map(SetVariablesRequest)

  def setVariablesResponse: Gen[SetVariablesResponse] =
    nonEmptyListOf(setVariableResult).map(SetVariablesResponse)

  def sendLocalListRequest: Gen[SendLocalListRequest] =
    for {
      version <- chooseNum(0, 60000)
      updateType <- enumerableGen(Update)
      localAuthData <- authorizationData
    } yield SendLocalListRequest(version, updateType, localAuthData)

  def sendLocalListResponse: Gen[SendLocalListResponse] =
    enumerableGen(UpdateStatus).map(SendLocalListResponse)

  def bootNotificationRequest: Gen[BootNotificationRequest] =
    for {
      chargingStation <- chargingStation
      reason <- enumerableGen(BootReason)
    } yield BootNotificationRequest(chargingStation, reason)

  def bootNotificationResponse: Gen[BootNotificationResponse] =
    for {
      currentTime <- instant
      interval <- chooseNum(0, 100000)
      status <- enumerableGen(BootNotificationStatus)
    } yield BootNotificationResponse(currentTime, interval, status)

  def heartbeatRequest: Gen[HeartbeatRequest] = const(HeartbeatRequest())

  def heartbeatResponse: Gen[HeartbeatResponse] = instant.map(HeartbeatResponse)

  def notifyReportRequest: Gen[NotifyReportRequest] = for {
    requestId <- option(ocppInteger)
    generatedAt <- instant
    tbc <- oneOf(true, false)
    seqNo <- ocppInteger
    reportData <- nonEmptyListOf(reportData)
  } yield NotifyReportRequest(requestId, generatedAt, tbc, seqNo, reportData)

  def notifyReportResponse: Gen[NotifyReportResponse] = const(NotifyReportResponse())

  def transactionEventRequest: Gen[TransactionEventRequest] = for {
    eventType <- transactionEvent
    meterValues <- option(nonEmptyListOf(meterValue))
    timestamp <- instant
    triggerReason <- enumerableGen(TriggerReason)
    seqNo <- chooseNum(0, 12345)
    offline <- option(oneOf(false, true))
    numberOfPhasesUsed <- option(chooseNum(1, 3))
    cableMaxCurrent <- option(bigDecimal)
    reservationId <- option(posNum[Int])
    transactionData <- transaction
    evse <- option(evse)
    idToken <- option(idToken)
  } yield TransactionEventRequest(
    eventType,
    meterValues,
    timestamp,
    triggerReason,
    seqNo,
    offline,
    numberOfPhasesUsed,
    cableMaxCurrent,
    reservationId,
    transactionData,
    evse,
    idToken
  )

  def transactionEventResponse: Gen[TransactionEventResponse] = for {
    totalCost <- option(chooseNum(-20000, 20000).map((i: Int) => BigDecimal(i) / 100))
    prio <- option(chargingPriority)
    idTokenInfo <- option(idTokenInfo)
    updatedMsg <- option(messageContent)
  } yield TransactionEventResponse(totalCost, prio, idTokenInfo, updatedMsg)

  def statusNotificationRequest: Gen[StatusNotificationRequest] = for {
    timestamp <- instant
    connectorStatus <- enumerableGen(ConnectorStatus)
    eId <- evseId
    connectorId <- chooseNum(1, 5)
  } yield StatusNotificationRequest(timestamp, connectorStatus, eId, connectorId)

  def statusNotificationResponse: Gen[StatusNotificationResponse] = const(StatusNotificationResponse())

  def authorizeRequest: Gen[AuthorizeRequest] = for {
    evseIds <- option(nonEmptyListOf(evseId))
    idT <- idToken
    ocspReqData <- option(resize(4, nonEmptyListOf(ocspRequestData)))
  } yield AuthorizeRequest(evseIds, idT, ocspReqData)

  def authorizeResponse: Gen[AuthorizeResponse] = for {
    certStat <- option(enumerableGen(CertificateStatus))
    eId <- option(nonEmptyListOf(evseId))
    idTokenI <- idTokenInfo
  } yield AuthorizeResponse(certStat, eId, idTokenI)

  def requestStopTransactionRequest: Gen[RequestStopTransactionRequest] =
    ocppIdentifierString(36).map(RequestStopTransactionRequest)

  def requestStopTransactionResponse: Gen[RequestStopTransactionResponse] =
    enumerableGen(RequestStartStopStatus).map(RequestStopTransactionResponse)
}
