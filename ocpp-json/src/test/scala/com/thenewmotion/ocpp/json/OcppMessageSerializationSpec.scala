package com.thenewmotion.ocpp
package json

import scala.io.Source
import scala.concurrent.duration._
import java.time.{ZoneId, ZonedDateTime}
import java.net.URI

import org.specs2.mutable.Specification
import org.specs2.matcher.MatchResult
import org.json4s._
import org.json4s.native.JsonParser

import v15.Ocpp15J
import messages._
import messages.Meter._
import JsonDeserializable._

class OcppMessageSerializationSpec extends Specification {

  "OCPP message deserialization" should {

    "deserialize all message types" in {

      for (testMsg <- testMsgs) yield {
        withJsonFromFile(testMsg.jsonFileBase) { (messageAST: JValue) =>
          Ocpp15J.deserialize(messageAST)(testMsg.jsonSerializable) mustEqual testMsg.msg
        }
      }
    }

    "read StartTransaction requests with a local time in them" in {
      withJsonFromFile("starttransaction.localtime.request") { (messageAST: JValue) =>
        Ocpp15J.deserialize(messageAST)(jsonDeserializable[StartTransactionReq]) mustEqual TestMsgs.startTransactionReqWithLocalTime
      }
    }
  }

  "OCPP message serialization" should {

    "serialize messages of all types" in {
      for (testMsg <- testMsgs) yield {
        withJsonFromFile(testMsg.jsonFileBase) { (messageAST: JValue) =>
          Ocpp15J.serialize(testMsg.msg) must beEqualToJson(messageAST)
        }
      }
    }
  }

  "OCPP message serialization error handling" should {
    "throw a MappingException in case a message contains an invalid URL" in
      withJsonFromFile("updatefirmware.invalidurl.request") { messageAST =>
        Ocpp15J.deserialize[UpdateFirmwareReq](messageAST) must throwA[MappingException]
      }

    "throw a MappingException in case a MeterValues request mentions an invalid property name" in
      withJsonFromFile("metervalues.invalidproperty.request") { messageAST =>
        Ocpp15J.deserialize[MeterValuesReq](messageAST) must throwA[MappingException]
      }

    "throw a MappingException in case an invalid status enum is given" in
      withJsonFromFile("changeavailability.invalidstatus.response") { messageAST =>
        Ocpp15J.deserialize[ChangeAvailabilityRes](messageAST) must throwA[MappingException]
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

  case class TestableMsg[T <: Message](jsonSerializable: JsonDeserializable[T], msg: T, jsonFileBase: String)
  object TestableMsg {
    def apply[T <: Message : JsonDeserializable](msg: T, jsonFileBase: String) =
      new TestableMsg(jsonDeserializable[T], msg, jsonFileBase)
  }

  private def withJsonFromFile(jsonFileBase: String)(test: JValue => MatchResult[_]) = {

    def loadRequestJSON: String = {
      val requestFileName = s"ocpp15/without_srpc/$jsonFileBase.json"
      Source.fromURL(this.getClass.getResource(requestFileName)).mkString
    }

    test(JsonParser.parse(loadRequestJSON))
  }

  private val testTime = utcDateTime(2013,2,1,15,9,18)

  private val testMsgs = List(
    TestableMsg(TestMsgs.bootNotificationReq, "bootnotification.request"),
    TestableMsg(TestMsgs.bootNotificationRes, "bootnotification.response"),
    TestableMsg(TestMsgs.authorizeReq, "authorize.request"),
    TestableMsg(TestMsgs.authorizeRes, "authorize.response"),
    TestableMsg(TestMsgs.startTransactionReq, "starttransaction.request"),
    TestableMsg(TestMsgs.startTransactionRes, "starttransaction.response"),
    TestableMsg(TestMsgs.stopTransactionReq, "stoptransaction.request"),
    TestableMsg(TestMsgs.stopTransactionRes, "stoptransaction.response"),
    TestableMsg(TestMsgs.unlockConnectorReq, "unlockconnector.request"),
    TestableMsg(TestMsgs.unlockConnectorRes, "unlockconnector.response"),
    TestableMsg(TestMsgs.resetReq, "reset.request"),
    TestableMsg(TestMsgs.resetRes, "reset.response"),
    TestableMsg(TestMsgs.changeAvailabilityReq, "changeavailability.request"),
    TestableMsg(TestMsgs.changeAvailabilityReqForWholeCharger, "changeavailability.wholecharger.request"),
    TestableMsg(TestMsgs.changeAvailabilityRes, "changeavailability.response"),
    TestableMsg(TestMsgs.statusNotificationReq, "statusnotification.request"),
    TestableMsg(TestMsgs.statusNotificationReqInError, "statusnotification.inerror.request"),
    // The case that there is an error but the message gives NoError as the error code. Makes no sense but the OCPP
    // standard doesn't disallow it.
    TestableMsg(TestMsgs.statusNotificationReqInErrorNoError, "statusnotification.inerror-noerror.request"),
    TestableMsg(TestMsgs.statusNotificationRes, "statusnotification.response"),
    TestableMsg(TestMsgs.remoteStartTransactionReq, "remotestarttransaction.request"),
    TestableMsg(TestMsgs.remoteStartTransactionRes, "remotestarttransaction.response"),
    TestableMsg(TestMsgs.remoteStopTransactionReq, "remotestoptransaction.request"),
    TestableMsg(TestMsgs.remoteStopTransactionRes, "remotestoptransaction.response"),
    TestableMsg(TestMsgs.heartbeatReq, "heartbeat.request"),
    TestableMsg(TestMsgs.heartbeatRes, "heartbeat.response"),
    TestableMsg(TestMsgs.updateFirmwareReq, "updatefirmware.request"),
    TestableMsg(TestMsgs.updateFirmwareRes, "updatefirmware.response"),
    TestableMsg(TestMsgs.firmwareStatusNotificationReq, "firmwarestatusnotification.request"),
    TestableMsg(TestMsgs.firmwareStatusNotificationRes, "firmwarestatusnotification.response"),
    TestableMsg(TestMsgs.getDiagnosticsReq, "getdiagnostics.request"),
    TestableMsg(TestMsgs.getDiagnosticsRes, "getdiagnostics.response"),
    TestableMsg(TestMsgs.diagnosticsStatusNotificationReq, "diagnosticsstatusnotification.request"),
    TestableMsg(TestMsgs.diagnosticsStatusNotificationRes, "diagnosticsstatusnotification.response"),
    TestableMsg(TestMsgs.meterValuesReq, "metervalues.request"),
    TestableMsg(TestMsgs.meterValuesRes, "metervalues.response"),
    TestableMsg(TestMsgs.changeConfigurationReq, "changeconfiguration.request"),
    TestableMsg(TestMsgs.changeConfigurationRes, "changeconfiguration.response"),
    TestableMsg(TestMsgs.clearCacheReq, "clearcache.request"),
    TestableMsg(TestMsgs.clearCacheRes, "clearcache.response"),
    TestableMsg(TestMsgs.getConfigurationReq, "getconfiguration.request"),
    TestableMsg(TestMsgs.getConfigurationRes, "getconfiguration.response"),
    TestableMsg(TestMsgs.getLocalListVersionReq, "getlocallistversion.request"),
    TestableMsg(TestMsgs.getLocalListVersionRes, "getlocallistversion.response"),
    TestableMsg(TestMsgs.sendLocalListReq, "sendlocallist.request"),
    TestableMsg(TestMsgs.sendLocalListRes, "sendlocallist.response"),
    TestableMsg(TestMsgs.reserveNowReq, "reservenow.request"),
    TestableMsg(TestMsgs.reserveNowRes, "reservenow.response"),
    TestableMsg(TestMsgs.cancelReservationReq, "cancelreservation.request"),
    TestableMsg(TestMsgs.cancelReservationRes, "cancelreservation.response")
  )

  private object TestMsgs {

    val authorizeReq = AuthorizeReq(idTag = "B4F62CEF")
    val authorizeRes = AuthorizeRes(
      IdTagInfo(status = AuthorizationStatus.Accepted,
        expiryDate = Some(testTime),
        parentIdTag = Some("PARENT")))

    val bootNotificationReq = BootNotificationReq(chargePointVendor = "DBT",
      chargePointModel = "NQC-ACDC",
      chargePointSerialNumber = Some("gir.vat.mx.000e48"),
      chargeBoxSerialNumber = Some("gir.vat.mx.000e48"),
      firmwareVersion = Some("1.0.49"),
      iccid = Some(""),
      imsi = Some(""),
      meterType = Some("DBT NQC-ACDC"),
      meterSerialNumber = Some("gir.vat.mx.000e48"))
    val bootNotificationRes = BootNotificationRes(registrationAccepted = true,
      currentTime = utcDateTime(2013, 9, 27, 14, 3, 0),
      heartbeatInterval = FiniteDuration(600, "seconds"))

    val startTransactionReq = StartTransactionReq(connector = ConnectorScope(1),
      idTag = "B4F62CEF",
      timestamp = testTime,
      meterStart = 0,
      reservationId = Some(0))
    val startTransactionReqWithLocalTime = StartTransactionReq(connector = ConnectorScope(1),
      idTag = "B4F62CEF",
      timestamp = testTime,
      meterStart = 0,
      reservationId = Some(0))
    val startTransactionRes = StartTransactionRes(transactionId = 0,
      idTag = IdTagInfo(status = AuthorizationStatus.Accepted,
        expiryDate = Some(testTime),
        parentIdTag = Some("PARENT")))

    val stopTransactionReq = {
      val testTimestamp = testTime
      val meterTimestamp = utcDateTime(2013, 3, 7, 16, 52, 16)
      val testMeter = Meter(timestamp = meterTimestamp, values = List(
        Meter.Value(value = "0",
          context = ReadingContext.SamplePeriodic,
          measurand = Measurand.EnergyActiveImportRegister,
          format = ValueFormat.Raw,
          location = Location.Outlet,
          unit = UnitOfMeasure.Wh),
        Meter.Value(value = "0",
          context = ReadingContext.SamplePeriodic,
          measurand = Measurand.EnergyReactiveImportRegister,
          format = ValueFormat.Raw,
          location = Location.Outlet,
          unit = UnitOfMeasure.Varh)))

      StopTransactionReq(transactionId = 0,
        idTag = Some("B4F62CEF"),
        //"2013­08­14T14:29:31.540+02:00"
        timestamp = testTimestamp,
        meterStop = 20,
        transactionData = List(
          TransactionData(List(testMeter)),
          TransactionData(List(testMeter))))
    }
    val stopTransactionRes = StopTransactionRes(idTag = Some(IdTagInfo(status = AuthorizationStatus.IdTagExpired,
      expiryDate = Some(utcDateTime(2013, 2, 1, 15, 9, 18)),
      parentIdTag = Some("PARENT"))))

    val unlockConnectorReq = UnlockConnectorReq(connector = ConnectorScope(0))
    val unlockConnectorRes = UnlockConnectorRes(accepted = true)

    val resetReq = ResetReq(resetType = ResetType.Soft)
    val resetRes = ResetRes(accepted = true)

    val changeAvailabilityReq =
      ChangeAvailabilityReq(scope = ConnectorScope(0), availabilityType = AvailabilityType.Inoperative)
    val changeAvailabilityReqForWholeCharger =
      ChangeAvailabilityReq(scope = ChargePointScope, availabilityType = AvailabilityType.Operative)
    val changeAvailabilityRes = ChangeAvailabilityRes(status = AvailabilityStatus.Accepted)

    val statusNotificationReq = StatusNotificationReq(scope = ConnectorScope(1),
      status = Available(Some("Info msg")), timestamp = Some(testTime),
      vendorId = Some(""))
    val statusNotificationReqInError = {
      val faultedStatus = Faulted(errorCode = Some(ChargePointErrorCode.PowerMeterFailure),
        info = Some("Die meter is kats doorgefikt joh"), vendorErrorCode = Some("MeterB0rk3d"))

      StatusNotificationReq(scope = ConnectorScope(1),
        status = faultedStatus, timestamp = Some(testTime), vendorId = Some("TNM"))
    }
    val statusNotificationReqInErrorNoError = {
      val faultedStatus = Faulted(errorCode = None,
        info = Some("Het laadpunt is een beetje in de bonen"), vendorErrorCode = Some("Lolwut?"))

      StatusNotificationReq(scope = ConnectorScope(1),
        status = faultedStatus, timestamp = Some(testTime), vendorId = Some("TNM"))
    }
    val statusNotificationRes = StatusNotificationRes

    val remoteStartTransactionReq = RemoteStartTransactionReq(idTag = "044943121F1D80",
      connector = Some(ConnectorScope(1)))
    val remoteStartTransactionRes = RemoteStartTransactionRes(accepted = true)

    val remoteStopTransactionReq = RemoteStopTransactionReq(transactionId = 1)
    val remoteStopTransactionRes = RemoteStopTransactionRes(accepted = false)

    val heartbeatReq = HeartbeatReq
    val heartbeatRes = HeartbeatRes(currentTime = testTime)

    val updateFirmwareReq = UpdateFirmwareReq(retrieveDate = testTime,
      location = new URI("ftp://root:root@fork.gir.foo/tmp/kvcbx-updt.amx"),
      retries = Retries(Some(4), Some(FiniteDuration(20, SECONDS))))
    val updateFirmwareRes = UpdateFirmwareRes

    val firmwareStatusNotificationReq = FirmwareStatusNotificationReq(status = FirmwareStatus.DownloadFailed)
    val firmwareStatusNotificationRes = FirmwareStatusNotificationRes

    val getDiagnosticsReq = GetDiagnosticsReq(location = new URI("ftp://root:root@axis.gir.foo/tmp"),
      startTime = Some(testTime),
      stopTime = Some(utcDateTime(2013,8,8,16,9,18)),
      retries = Retries(Some(4), Some(FiniteDuration(20, SECONDS))))
    val getDiagnosticsRes = GetDiagnosticsRes(fileName = Some("diag-gir.vat.mx.000e48-20130131132608.txt"))

    val diagnosticsStatusNotificationReq = DiagnosticsStatusNotificationReq(uploaded = true)
    val diagnosticsStatusNotificationRes = DiagnosticsStatusNotificationRes

    val meterValuesReq = MeterValuesReq(
      scope = ConnectorScope(1),
      transactionId = Some(0),
      meters = List(
        Meter(
          timestamp = utcDateTime(2013, 3, 7, 16, 52, 16),
          values = List(
            Meter.Value(
              value = "0",
              context = ReadingContext.SamplePeriodic,
              unit = UnitOfMeasure.Wh,
              measurand = Measurand.EnergyActiveImportRegister,
              format = ValueFormat.Raw,
              location = Location.Outlet),
            Meter.Value(
              value = "0",
              context = ReadingContext.SamplePeriodic,
              unit = UnitOfMeasure.Varh,
              measurand = Measurand.EnergyReactiveImportRegister,
              format = ValueFormat.Raw,
              location = Location.Outlet))),
        Meter(
          timestamp = utcDateTime(2013, 3, 7, 19, 52, 16),
          values = List(
            Meter.Value(
              value = "20",
              context = ReadingContext.SamplePeriodic,
              unit = UnitOfMeasure.Wh,
              measurand = Measurand.EnergyActiveImportRegister,
              format = ValueFormat.Raw,
              location = Location.Outlet),
            Meter.Value(
              value = "20",
              context = ReadingContext.SamplePeriodic,
              unit = UnitOfMeasure.Varh,
              measurand = Measurand.EnergyReactiveImportRegister,
              format = ValueFormat.Raw,
              location = Location.Outlet)))))
    val meterValuesRes = MeterValuesRes

    val changeConfigurationReq = ChangeConfigurationReq(key = "KVCBX_LANG", value = "FR")
    val changeConfigurationRes = ChangeConfigurationRes(ConfigurationStatus.Accepted)

    val clearCacheReq = ClearCacheReq
    val clearCacheRes = ClearCacheRes(accepted = true)

    val getConfigurationReq = GetConfigurationReq(keys = List("apeschaap", "hompeschomp"))
    val getConfigurationRes = GetConfigurationRes(
      values = List(KeyValue(key = "KVCBX_PROFILE", readonly = true, value = Some("NQC-ACDC"))),
      unknownKeys = List())

    val getLocalListVersionReq = GetLocalListVersionReq
    val getLocalListVersionRes = GetLocalListVersionRes(AuthListSupported(0))

    val sendLocalListReq = SendLocalListReq(UpdateType.Full, listVersion = AuthListSupported(1),
      localAuthorisationList = List(AuthorisationData(idTag = "044943121F1D80",
        idTagInfo = Some(IdTagInfo(AuthorizationStatus.Accepted, Some(testTime), Some(""))))), hash = Some(""))
    val sendLocalListRes = SendLocalListRes(status = UpdateStatus.UpdateAccepted(Some("")))

    val reserveNowReq = ReserveNowReq(connector = ChargePointScope,
      expiryDate = testTime,
      idTag = "044943121F1D80",
      parentIdTag = Some(""),
      reservationId = 0)
    val reserveNowRes = ReserveNowRes(status = Reservation.Accepted)

    val cancelReservationReq = CancelReservationReq(42)
    val cancelReservationRes = CancelReservationRes(accepted = true)
  }

  private def utcDateTime(year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Int) =
    ZonedDateTime
      .of(year, month, day, hour, minute, second, 0, ZoneId.of("UTC"))
}
