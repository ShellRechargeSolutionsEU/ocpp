package com.thenewmotion.ocpp
package json
package v1x
package v16

import scala.reflect.{ClassTag, classTag}
import org.scalacheck.Gen
import org.scalacheck.Prop.forAll
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import scalacheck.MessageGenerators._
import SerializationV16._

class SerializationV16Spec extends Specification with ScalaCheck {
  "Conversion to/from OCPP 1.6-J format" should {

    testMessageClass(bootNotificationReq)(BootNotificationReqV16Variant)
    testMessageClass(bootNotificationRes)(BootNotificationResV16Variant)
    testMessageClass(authorizeReq)(AuthorizeReqV16Variant)
    testMessageClass(authorizeRes)(AuthorizeResV16Variant)
    testMessageClass(dataTransferReq)(DataTransferReqV16Variant)
    testMessageClass(dataTransferRes)(DataTransferResV16Variant)
    testMessageClass(startTransactionReq)(StartTransactionReqV16Variant)
    testMessageClass(startTransactionRes)(StartTransactionResV16Variant)
    testMessageClass(stopTransactionReq)(StopTransactionReqV16Variant)
    testMessageClass(stopTransactionRes)(StopTransactionResV16Variant)
    testMessageClass(unlockConnectorReq)(UnlockConnectorReqV16Variant)
    testMessageClass(unlockConnectorRes)(UnlockConnectorResV16Variant)
    testMessageClass(resetReq)(ResetReqV16Variant)
    testMessageClass(resetRes)(ResetResV16Variant)
    testMessageClass(changeAvailabilityReq)(ChangeAvailabilityReqV16Variant)
    testMessageClass(changeAvailabilityRes)(ChangeAvailabilityResV16Variant)
    testMessageClass(statusNotificationReq)(StatusNotificationReqV16Variant)
    testMessageClass(statusNotificationRes)(StatusNotificationResV16Variant)
    testMessageClass(remoteStartTransactionReq)(RemoteStartTransactionReqV16Variant)
    testMessageClass(remoteStartTransactionRes)(RemoteStartTransactionResV16Variant)
    testMessageClass(remoteStopTransactionReq)(RemoteStopTransactionReqV16Variant)
    testMessageClass(remoteStopTransactionRes)(RemoteStopTransactionResV16Variant)
    testMessageClass(heartbeatReq)(HeartbeatReqV16Variant)
    testMessageClass(heartbeatRes)(HeartbeatResV16Variant)
    testMessageClass(updateFirmwareReq)(UpdateFirmwareReqV16Variant)
    testMessageClass(updateFirmwareRes)(UpdateFirmwareResV16Variant)
    testMessageClass(firmwareStatusNotificationReq)(FirmwareStatusNotificationReqV16Variant)
    testMessageClass(firmwareStatusNotificationRes)(FirmwareStatusNotificationResV16Variant)
    testMessageClass(getDiagnosticsReq)(GetDiagnosticsReqV16Variant)
    testMessageClass(getDiagnosticsRes)(GetDiagnosticsResV16Variant)
    testMessageClass(diagnosticsStatusNotificationReq)(DiagnosticsStatusNotificationReqV16Variant)
    testMessageClass(diagnosticsStatusNotificationRes)(DiagnosticsStatusNotificationResV16Variant)
    testMessageClass(meterValuesReq)(MeterValuesReqV16Variant)
    testMessageClass(meterValuesRes)(MeterValuesResV16Variant)
    testMessageClass(changeConfigurationReq)(ChangeConfigurationReqV16Variant)
    testMessageClass(changeConfigurationRes)(ChangeConfigurationResV16Variant)
    testMessageClass(clearCacheReq)(ClearCacheReqV16Variant)
    testMessageClass(clearCacheRes)(ClearCacheResV16Variant)
    testMessageClass(getConfigurationReq)(GetConfigurationReqV16Variant)
    testMessageClass(getConfigurationRes)(GetConfigurationResV16Variant)
    testMessageClass(getLocalListVersionReq)(GetLocalListVersionReqV16Variant)
    testMessageClass(getLocalListVersionRes)(GetLocalListVersionResV16Variant)
    testMessageClass(reserveNowReq)(ReserveNowReqV16Variant)
    testMessageClass(reserveNowRes)(ReserveNowResV16Variant)
    testMessageClass(cancelReservationReq)(CancelReservationReqV16Variant)
    testMessageClass(cancelReservationRes)(CancelReservationResV16Variant)
    testMessageClass(clearChargingProfileReq)(ClearChargingProfileReqV16Variant)
    testMessageClass(clearChargingProfileRes)(ClearChargingProfileResV16Variant)
    testMessageClass(getCompositeScheduleReq)(GetCompositeScheduleReqV16Variant)
    testMessageClass(getCompositeScheduleRes)(GetCompositeScheduleResV16Variant)
    testMessageClass(setChargingProfileReq)(SetChargingProfileReqV16Variant)
    testMessageClass(setChargingProfileRes)(SetChargingProfileResV16Variant)
    testMessageClass(triggerMessageReq)(TriggerMessageReqV16Variant)
    testMessageClass(triggerMessageRes)(TriggerMessageResV16Variant)
  }

  private def testMessageClass[T <: Message : ClassTag, M <: messages.v1x.Message]
    (messageGen: Gen[T])
    (ser: OcppMessageSerializer[M, Version.V16.type]) = {

    val className = classTag[T].runtimeClass.getSimpleName

    s"result in the same object after v16->generic->v16 transformation of $className" in {
      forAll(messageGen) { msg =>
        val after = ser.to(ser.from(msg.asInstanceOf[ser.VersionSpecific]))
        after == msg
      }
    }
  }
}

