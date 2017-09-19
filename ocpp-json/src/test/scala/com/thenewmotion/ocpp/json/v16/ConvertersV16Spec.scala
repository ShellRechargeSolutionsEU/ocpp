package com.thenewmotion.ocpp
package json
package v16

import org.scalacheck.Gen
import org.scalacheck.Prop.forAll
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

import scala.reflect.{ClassTag, classTag}

object ConvertersV16Spec extends Specification with ScalaCheck {

  import scalacheck.MessageGenerators._

  "Conversion to/from OCPP 1.6-J format" should {

    testMessageClass(bootNotificationReq)
    testMessageClass(bootNotificationRes)
    testMessageClass(authorizeReq)
    testMessageClass(authorizeRes)
    testMessageClass(startTransactionReq)
    testMessageClass(stopTransactionReq)
    testMessageClass(unlockConnectorReq)
    testMessageClass(unlockConnectorRes)
    testMessageClass(resetReq)
    testMessageClass(resetRes)
    testMessageClass(changeAvailabilityReq)
    testMessageClass(changeAvailabilityRes)
    testMessageClass(statusNotificationReq)
    testMessageClass(statusNotificationRes)
    testMessageClass(remoteStartTransactionReq)
    testMessageClass(remoteStartTransactionRes)
    testMessageClass(remoteStopTransactionReq)
    testMessageClass(remoteStopTransactionRes)
    testMessageClass(heartbeatReq)
    testMessageClass(heartbeatRes)
    testMessageClass(updateFirmwareReq)
    testMessageClass(updateFirmwareRes)
    testMessageClass(firmwareStatusNotificationReq)
    testMessageClass(firmwareStatusNotificationRes)
    testMessageClass(getDiagnosticsReq)
    testMessageClass(getDiagnosticsRes)
    testMessageClass(diagnosticsStatusNotificationReq)
    testMessageClass(diagnosticsStatusNotificationRes)
    testMessageClass(meterValuesReq)
    testMessageClass(meterValuesRes)
    testMessageClass(changeConfigurationReq)
    testMessageClass(changeConfigurationRes)
    testMessageClass(clearCacheReq)
    testMessageClass(clearCacheRes)
    testMessageClass(getConfigurationReq)
    testMessageClass(getConfigurationRes)
    testMessageClass(getLocalListVersionReq)
    testMessageClass(getLocalListVersionRes)
    testMessageClass(reserveNowReq)
    testMessageClass(reserveNowRes)
    testMessageClass(cancelReservationReq)
    testMessageClass(cancelReservationRes)
    testMessageClass(clearChargingProfileReq)
    testMessageClass(clearChargingProfileRes)
    testMessageClass(getCompositeScheduleReq)
    testMessageClass(getCompositeScheduleRes)
    testMessageClass(setChargingProfileReq)
    testMessageClass(setChargingProfileRes)
    testMessageClass(triggerMessageReq)
    testMessageClass(triggerMessageRes)
  }

  private def testMessageClass[T <: v16.Message : ClassTag](messageGen: Gen[T]) = {
    val className = classTag[T].runtimeClass.getSimpleName

    s"result in the same object after v16->generic->v16 transformation of $className" in {
      forAll(messageGen) { msg =>
        val after = ConvertersV16.toV16(ConvertersV16.fromV16(msg))
        after == msg
      }
    }
  }
}

