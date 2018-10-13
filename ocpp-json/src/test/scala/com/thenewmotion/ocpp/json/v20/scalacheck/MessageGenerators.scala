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
}
