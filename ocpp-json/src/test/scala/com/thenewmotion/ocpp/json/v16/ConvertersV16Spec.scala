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

    testMessageClass(bootNotificationReqGen)
    testMessageClass(bootNotificationResGen)
    testMessageClass(authorizeReqGen)
    testMessageClass(authorizeResGen)
    testMessageClass(startTransactionReqGen)
    testMessageClass(stopTransactionReqGen)
    testMessageClass(unlockConnectorReqGen)
    testMessageClass(unlockConnectorResGen)
    testMessageClass(resetReqGen)
    testMessageClass(resetResGen)
    testMessageClass(changeAvailabilityReqGen)
    testMessageClass(changeAvailabilityResGen)
    testMessageClass(statusNotificationReqGen)
    testMessageClass(statusNotificationResGen)
    testMessageClass(remoteStartTransactionReqGen)
    testMessageClass(remoteStartTransactionResGen)
    testMessageClass(remoteStopTransactionReqGen)
    testMessageClass(remoteStopTransactionResGen)
    testMessageClass(heartbeatReqGen)
    testMessageClass(heartbeatResGen)
    testMessageClass(updateFirmwareReqGen)
    testMessageClass(updateFirmwareResGen)
    testMessageClass(firmwareStatusNotificationReqGen)
    testMessageClass(firmwareStatusNotificationResGen)
    testMessageClass(getDiagnosticsReqGen)
    testMessageClass(getDiagnosticsResGen)
    testMessageClass(diagnosticsStatusNotificationReqGen)
    testMessageClass(diagnosticsStatusNotificationResGen)
    testMessageClass(meterValuesReqGen)
    testMessageClass(meterValuesResGen)
    testMessageClass(changeConfigurationReqGen)
    testMessageClass(changeConfigurationResGen)
    testMessageClass(clearCacheReqGen)
    testMessageClass(clearCacheResGen)
    testMessageClass(getConfigurationReqGen)
    testMessageClass(getConfigurationResGen)
    testMessageClass(getLocalListVersionReqGen)
    testMessageClass(getLocalListVersionResGen)
    testMessageClass(reserveNowReqGen)
    testMessageClass(reserveNowResGen)
    testMessageClass(cancelReservationReqGen)
    testMessageClass(cancelReservationResGen)
    testMessageClass(clearChargingProfileReqGen)
    testMessageClass(clearChargingProfileResGen)
    testMessageClass(getCompositeScheduleReqGen)
    testMessageClass(getCompositeScheduleResGen)
    testMessageClass(setChargingProfileReqGen)
    testMessageClass(setChargingProfileResGen)
    testMessageClass(triggerMessageReqGen)
    testMessageClass(triggerMessageResGen)
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

