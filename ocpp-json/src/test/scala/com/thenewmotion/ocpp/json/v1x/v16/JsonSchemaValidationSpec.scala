package com.thenewmotion.ocpp
package json
package v1x
package v16

import com.fasterxml.jackson.databind.JsonNode
import com.github.fge.jsonschema.main.{JsonSchemaFactory, JsonValidator}
import org.scalacheck.Gen
import org.scalacheck.Prop.forAll
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import org.json4s.jackson.JsonMethods._

import scala.collection.JavaConverters._
import scala.reflect.{ClassTag, classTag}

class JsonSchemaValidationSpec extends Specification with ScalaCheck {

  import SerializationV16._
  import scalacheck.MessageGenerators._

  val validator: JsonValidator = JsonSchemaFactory.byDefault().getValidator

  "Validating serialized OCPP 1.6 messages against the schema" should {
    validateJson(bootNotificationReq)(BootNotificationReqV16Variant, "BootNotification.json")
    validateJson(bootNotificationRes)(BootNotificationResV16Variant, "BootNotificationResponse.json")
    validateJson(authorizeReq)(AuthorizeReqV16Variant, "Authorize.json")
    validateJson(authorizeRes)(AuthorizeResV16Variant, "AuthorizeResponse.json")
    validateJson(startTransactionReq)(StartTransactionReqV16Variant, "StartTransaction.json")
    validateJson(startTransactionRes)(StartTransactionResV16Variant, "StartTransactionResponse.json")
    validateJson(stopTransactionReq)(StopTransactionReqV16Variant, "StopTransaction.json")
    validateJson(stopTransactionRes)(StopTransactionResV16Variant, "StopTransactionResponse.json")
    validateJson(unlockConnectorReq)(UnlockConnectorReqV16Variant, "UnlockConnector.json")
    validateJson(unlockConnectorRes)(UnlockConnectorResV16Variant, "UnlockConnectorResponse.json")
    validateJson(resetReq)(ResetReqV16Variant, "Reset.json")
    validateJson(resetRes)(ResetResV16Variant, "ResetResponse.json")
    validateJson(changeAvailabilityReq)(ChangeAvailabilityReqV16Variant, "ChangeAvailability.json")
    validateJson(changeAvailabilityRes)(ChangeAvailabilityResV16Variant, "ChangeAvailabilityResponse.json")
    validateJson(statusNotificationReq)(StatusNotificationReqV16Variant, "StatusNotification.json")
    validateJson(statusNotificationRes)(StatusNotificationResV16Variant, "StatusNotificationResponse.json")
    validateJson(remoteStartTransactionReq)(RemoteStartTransactionReqV16Variant, "RemoteStartTransaction.json")
    validateJson(remoteStartTransactionRes)(RemoteStartTransactionResV16Variant, "RemoteStartTransactionResponse.json")
    validateJson(remoteStopTransactionReq)(RemoteStopTransactionReqV16Variant, "RemoteStopTransaction.json")
    validateJson(remoteStopTransactionRes)(RemoteStopTransactionResV16Variant, "RemoteStopTransactionResponse.json")
    validateJson(heartbeatReq)(HeartbeatReqV16Variant, "Heartbeat.json")
    validateJson(heartbeatRes)(HeartbeatResV16Variant, "HeartbeatResponse.json")
    validateJson(updateFirmwareReq)(UpdateFirmwareReqV16Variant, "UpdateFirmware.json")
    validateJson(updateFirmwareRes)(UpdateFirmwareResV16Variant, "UpdateFirmwareResponse.json")
    validateJson(firmwareStatusNotificationReq)(FirmwareStatusNotificationReqV16Variant, "FirmwareStatusNotification.json")
    validateJson(firmwareStatusNotificationRes)(FirmwareStatusNotificationResV16Variant, "FirmwareStatusNotificationResponse.json")
    validateJson(getDiagnosticsReq)(GetDiagnosticsReqV16Variant, "GetDiagnostics.json")
    validateJson(getDiagnosticsRes)(GetDiagnosticsResV16Variant, "GetDiagnosticsResponse.json")
    validateJson(diagnosticsStatusNotificationReq)(DiagnosticsStatusNotificationReqV16Variant, "DiagnosticsStatusNotification.json")
    validateJson(diagnosticsStatusNotificationRes)(DiagnosticsStatusNotificationResV16Variant, "DiagnosticsStatusNotificationResponse.json")
    validateJson(meterValuesReq)(MeterValuesReqV16Variant, "MeterValues.json")
    validateJson(meterValuesRes)(MeterValuesResV16Variant, "MeterValuesResponse.json")
    validateJson(changeConfigurationReq)(ChangeConfigurationReqV16Variant, "ChangeConfiguration.json")
    validateJson(changeConfigurationRes)(ChangeConfigurationResV16Variant, "ChangeConfigurationResponse.json")
    validateJson(clearCacheReq)(ClearCacheReqV16Variant, "ClearCache.json")
    validateJson(clearCacheRes)(ClearCacheResV16Variant, "ClearCacheResponse.json")
    validateJson(getConfigurationReq)(GetConfigurationReqV16Variant, "GetConfiguration.json")
    validateJson(getConfigurationRes)(GetConfigurationResV16Variant, "GetConfigurationResponse.json")
    validateJson(getLocalListVersionReq)(GetLocalListVersionReqV16Variant, "GetLocalListVersion.json")
    validateJson(getLocalListVersionRes)(GetLocalListVersionResV16Variant, "GetLocalListVersionResponse.json")
    validateJson(sendLocalListReq)(SendLocalListReqV16Variant, "SendLocalList.json")
    validateJson(sendLocalListRes)(SendLocalListResV16Variant, "SendLocalListResponse.json")
    validateJson(reserveNowReq)(ReserveNowReqV16Variant, "ReserveNow.json")
    validateJson(reserveNowRes)(ReserveNowResV16Variant, "ReserveNowResponse.json")
    validateJson(cancelReservationReq)(CancelReservationReqV16Variant, "CancelReservation.json")
    validateJson(cancelReservationRes)(CancelReservationResV16Variant, "CancelReservationResponse.json")
    validateJson(clearChargingProfileReq)(ClearChargingProfileReqV16Variant, "ClearChargingProfile.json")
    validateJson(clearChargingProfileRes)(ClearChargingProfileResV16Variant, "ClearChargingProfileResponse.json")
    validateJson(getCompositeScheduleReq)(GetCompositeScheduleReqV16Variant, "GetCompositeSchedule.json")
    validateJson(getCompositeScheduleRes)(GetCompositeScheduleResV16Variant, "GetCompositeScheduleResponse.json")
    validateJson(setChargingProfileReq)(SetChargingProfileReqV16Variant, "SetChargingProfile.json")
    validateJson(setChargingProfileRes)(SetChargingProfileResV16Variant, "SetChargingProfileResponse.json")
    validateJson(triggerMessageReq)(TriggerMessageReqV16Variant, "TriggerMessage.json")
    validateJson(triggerMessageRes)(TriggerMessageResV16Variant, "TriggerMessageResponse.json")
  }
  
  private def validateJson[T <: Message : ClassTag, M <: messages.v1x.Message]
  (messageGen: Gen[T])
  (ser: OcppMessageSerializer[M, Version.V16.type], schema:String) = {

    val schemaFile:String = scala.io.Source.fromInputStream({
      getClass.getResourceAsStream(s"schemas/$schema")
    }).getLines.mkString("\n")

    val jsonSchema: JsonNode = asJsonNode(parse(schemaFile))

    val className = classTag[T].runtimeClass.getSimpleName

    s"result in successful validation of $className" in {
      forAll(messageGen) { msg =>
        val json:JsonNode = asJsonNode(ser.serialize(ser.from(msg.asInstanceOf[ser.VersionSpecific])))
        val validationReport = validator.validate(jsonSchema, json)
        if (validationReport.isSuccess) {
          success
        } else {
          failure(validationReport.asScala.map(_.getMessage).mkString("\n"))
        }
      }
    }
  }
}

