package com.thenewmotion.ocpp
package json
package v1x

import java.time.{ZoneId, ZonedDateTime}
import scala.io.Source
import v15.SerializationV15._
import messages.v1x.meter._
import messages.v1x._
import org.json4s._
import org.json4s.native.JsonParser
import org.specs2.mutable.Specification

class GirOcppMessageSerializationSpec extends Specification {

  "OCPP message deserialization" should {

    "deserialize and serialize all message types" in {
      for (testMsg <- testMsgs) yield {

        testMsg.msg match {
          case Some(m) => testMsg.deserialized mustEqual m
          case None => testMsg.serializedAfterDeserialized must beEqualToJson(testMsg.toJson)
        }

      }
    }
  }

  // copy of beEqualTo that has the argument type set to JValue, so we can use Lift's JSON DSL without type annotations
  // everywhere
  private def beEqualToJson(jval: JValue) = beTypedEqualTo[JValue](jval) ^^^ stripJNothingFields

  private def stripJNothingFields(jval: JValue): JValue =
    jval match {
      case JArray(xs) => JArray(xs.map(stripJNothingFields))
      case JObject(xs) => JObject(xs.collect(stripJNothingInJField))
      case x => x
    }

  private def stripJNothingInJField: PartialFunction[JField, JField] = {
    case JField(field, value) if value != JNothing => JField(field, stripJNothingFields(value))
  }

  case class TestMsg[M <: Message](versionVariant: OcppMessageSerializer[M, Version.V15.type], jsonFileBase: String, msg: Option[M] = None) {
    def serializedAfterDeserialized: JValue = Serialization.serialize(deserialized)(versionVariant)
    def deserialized: M = Serialization.deserialize(toJson)(versionVariant)
    def toJson: JValue = JsonParser.parse(loadRequestJSON)
    private def loadRequestJSON: String = {
      val requestFileName = s"ocpp15/gir_without_srpc/$jsonFileBase.json"
      Source.fromURL(this.getClass.getResource(requestFileName)).mkString
    }
  }
  object TestMsg {
    def forFile[M <: Message](jsonFileBase: String, msg: Option[M] = None)(implicit msgSer: OcppMessageSerializer[M, Version.V15.type]): TestMsg[M] =
      TestMsg(msgSer, jsonFileBase, msg)
  }

  private def utcDateTime(year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Int) =
    ZonedDateTime
      .of(year, month, day, hour, minute, second, 0, ZoneId.of("UTC"))

  private val testTime = utcDateTime(2013,2,1,15,9,18)

  val stopTransactionReq: StopTransactionReq = {
    val testTimestamp = testTime
    val meterTimestamp = utcDateTime(2013, 3, 7, 16, 52, 16)
    val testMeter = Meter(timestamp = meterTimestamp, values = List(
      meter.Value(value = "0",
        context = ReadingContext.SamplePeriodic,
        measurand = Measurand.EnergyActiveImportRegister,
        phase = None,
        format = ValueFormat.Raw,
        location = Location.Outlet,
        unit = UnitOfMeasure.Wh),
      meter.Value(value = "0",
        context = ReadingContext.SamplePeriodic,
        measurand = Measurand.EnergyReactiveImportRegister,
        phase = None,
        format = ValueFormat.Raw,
        location = Location.Outlet,
        unit = UnitOfMeasure.Varh)))

    StopTransactionReq(transactionId = 2,
      idTag = Some("04F8692AA23E80"),
      timestamp = testTimestamp,
      meterStop = 20,
      reason = StopReason.Local,
      meters = List(testMeter, testMeter)
    )
  }

  val meterValuesReq = MeterValuesReq(
    scope = ConnectorScope(1),
    transactionId = Some(2),
    meters = List(
      Meter(
        timestamp = utcDateTime(2013, 3, 7, 16, 52, 16),
        values = List(
          meter.Value(
            value = "0",
            context = ReadingContext.SamplePeriodic,
            unit = UnitOfMeasure.Wh,
            measurand = Measurand.EnergyActiveImportRegister,
            phase = None,
            format = ValueFormat.Raw,
            location = Location.Outlet),
          meter.Value(
            value = "0",
            context = ReadingContext.SamplePeriodic,
            unit = UnitOfMeasure.Varh,
            phase = None,
            measurand = Measurand.EnergyReactiveImportRegister,
            format = ValueFormat.Raw,
            location = Location.Outlet))),
      Meter(
        timestamp = utcDateTime(2013, 3, 7, 19, 52, 16),
        values = List(
          meter.Value(
            value = "20",
            context = ReadingContext.SamplePeriodic,
            unit = UnitOfMeasure.Wh,
            phase = None,
            measurand = Measurand.EnergyActiveImportRegister,
            format = ValueFormat.Raw,
            location = Location.Outlet),
          meter.Value(
            value = "20",
            context = ReadingContext.SamplePeriodic,
            unit = UnitOfMeasure.Varh,
            measurand = Measurand.EnergyReactiveImportRegister,
            phase = None,
            format = ValueFormat.Raw,
            location = Location.Outlet)))))

  private val testMsgs = List(
    TestMsg.forFile[AuthorizeReq]("authorize.request"),
    TestMsg.forFile[AuthorizeRes]("authorize.response"),
    TestMsg.forFile[BootNotificationReq]("bootnotification.request"),
    TestMsg.forFile[BootNotificationRes]("bootnotification.response"),
    TestMsg.forFile[CentralSystemDataTransferReq]("centralsystemdatatransfer.request"),
    TestMsg.forFile[CentralSystemDataTransferRes]("centralsystemdatatransfer.response"),
    TestMsg.forFile[DiagnosticsStatusNotificationReq]("diagnosticsstatusnotification.request"),
    TestMsg.forFile[DiagnosticsStatusNotificationRes.type]("diagnosticsstatusnotification.response"),
    TestMsg.forFile[FirmwareStatusNotificationReq]("firmwarestatusnotification.request"),
    TestMsg.forFile[FirmwareStatusNotificationRes.type]("firmwarestatusnotification.response"),
    TestMsg.forFile[HeartbeatReq.type]("heartbeat.request"),
    TestMsg.forFile[HeartbeatRes]("heartbeat.response"),
    TestMsg.forFile("metervalues.request", Some(meterValuesReq)),
    TestMsg.forFile[MeterValuesRes.type]("metervalues.response"),
    TestMsg.forFile[CancelReservationReq]("remotecancelreservation.request"),
    TestMsg.forFile[CancelReservationRes]("remotecancelreservation.response"),
    TestMsg.forFile[ChangeAvailabilityReq]("remotechangeavailability.request"),
    TestMsg.forFile[ChangeAvailabilityRes]("remotechangeavailability.response"),
    TestMsg.forFile[ChangeConfigurationReq]("remotechangeconfiguration.request"),
    TestMsg.forFile[ChangeConfigurationRes]("remotechangeconfiguration.response"),
    TestMsg.forFile[ChargePointDataTransferReq]("chargepointdatatransfer.request"),
    TestMsg.forFile[ChargePointDataTransferRes]("chargepointdatatransfer.response"),
    TestMsg.forFile[ClearCacheReq.type]("remoteclearcache.request"),
    TestMsg.forFile[ClearCacheRes]("remoteclearcache.response"),
    TestMsg.forFile[GetConfigurationReq]("remotegetconfiguration.request"),
    TestMsg.forFile[GetConfigurationRes]("remotegetconfiguration.response"),
    TestMsg.forFile[GetDiagnosticsReq]("remotegetdiagnostics.request"),
    TestMsg.forFile[GetDiagnosticsRes]("remotegetdiagnostics.response"),
    TestMsg.forFile[GetLocalListVersionReq.type]("remotegetlocallistversion.request"),
    TestMsg.forFile[GetLocalListVersionRes]("remotegetlocallistversion.response"),
    TestMsg.forFile[ReserveNowReq]("remotereservenow.request"),
    TestMsg.forFile[ReserveNowRes]("remotereservenow.response"),
    TestMsg.forFile[ResetReq]("remotereset.request"),
    TestMsg.forFile[ResetRes]("remotereset.response"),
    TestMsg.forFile[SendLocalListReq]("remotesendlocallist.request"),
    TestMsg.forFile[SendLocalListRes]("remotesendlocallist.response"),
    TestMsg.forFile[RemoteStartTransactionReq]("remotestarttransaction.request"),
    TestMsg.forFile[RemoteStartTransactionRes]("remotestarttransaction.response"),
    TestMsg.forFile[RemoteStopTransactionReq]("remotestoptransaction.request"),
    TestMsg.forFile[RemoteStopTransactionRes]("remotestoptransaction.response"),
    TestMsg.forFile[UnlockConnectorReq]("remoteunlockconnector.request"),
    TestMsg.forFile[UnlockConnectorRes]("remoteunlockconnector.response"),
    TestMsg.forFile[UpdateFirmwareReq]("remoteupdatefirmware.request"),
    TestMsg.forFile[UpdateFirmwareRes.type]("remoteupdatefirmware.response"),
    TestMsg.forFile[StartTransactionReq]("starttransaction.request"),
    TestMsg.forFile[StartTransactionRes]("starttransaction.response"),
    TestMsg.forFile[StatusNotificationReq]("statusnotification.request"),
    TestMsg.forFile[StatusNotificationRes.type]("statusnotification.response"),
    TestMsg.forFile[StopTransactionReq]("stoptransaction.request", Some(stopTransactionReq)),
    TestMsg.forFile[StopTransactionRes]("stoptransaction.response")
  )
}