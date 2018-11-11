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

  def transactionEventRequest: Gen[TransactionEventRequest] = for {
    eventType <- transactionEvent
    meterValues <- option(nonEmptyListOf(meterValue))
    timestamp <- instant
    triggerReason <- enumerableGen(TriggerReason)
    seqNo <- chooseNum(0, 12345)
    offline <- option(oneOf(false, true))
    numberOfPhasesUsed <- option(chooseNum(1,3))
    cableMaxCurrent <- option(bigDecimal)
    reservationId <- option(posNum[Int])
    transactionData <- transaction
    evse <- evse
    idToken <- idToken
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
}
