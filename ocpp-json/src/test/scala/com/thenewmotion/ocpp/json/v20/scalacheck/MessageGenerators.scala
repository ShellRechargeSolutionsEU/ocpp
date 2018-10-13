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

  def bootNotificationRequest: Gen[BootNotificationRequest] =
    for {
      chargingStation <- chargingStation
      reason <- enumerableGen(BootReason)
    } yield BootNotificationRequest(chargingStation, reason)

  def chargingStation: Gen[ChargingStation] =
    for {
      serialNumber <- option(ocppString(20))
      model <- ocppString(20)
      vendorName <- ocppString(20)
      firmwareVersion <- option(ocppString(50))
      modem <- option(modem)
    } yield ChargingStation(serialNumber, model, vendorName, firmwareVersion, modem)
}
