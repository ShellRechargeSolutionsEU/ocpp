package com.thenewmotion.ocpp
package json

import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.Scope
import scala.io.Source
import net.liftweb.json._
import com.thenewmotion.time.Imports._
import scala.concurrent.duration._
import net.liftweb.json.JsonDSL._
import java.net.URI
import messages._
import messages.Meter._
import com.thenewmotion.ocpp.json.v15.OcppJ15
import JsonSerializable._

class OcppMessageSerializationSpec extends SpecificationWithJUnit {

  implicit val formats: Formats = DefaultFormats ++ OcppMessageJsonSerializers()

  "OCPP message deserialization, new style tests" should {
    for (testMsg <- testMsgs) {
      s"read ${testMsg.msg.getClass.getName} messages" in new MessageTestScope(testMsg.jsonFileBase) {
         OcppJ15.deserialize(messageAST)(testMsg.jsonSerializable) mustEqual testMsg.msg
      }
    }
  }

  "OCPP message serialization, new style tests" should {
    for (testMsg <- testMsgs) {
      s"serialize ${testMsg.msg.getClass.getName} messages" in new MessageTestScope(testMsg.jsonFileBase) {
        OcppJ15.serialize(testMsg.msg) must beEqualToJson(messageAST)
      }
    }
  }

  "OCPP message deserialization" should {

    "read BootNotification requests" in new RequestTestScope("bootnotification") {
      messageAST.extract[BootNotificationReq] mustEqual TestMsgs.bootNotificationReq
    }

    "read Authorize requests" in new RequestTestScope("authorize") {
      messageAST.extract[AuthorizeReq] mustEqual TestMsgs.authorizeReq
    }

    "read StartTransaction requests" in new RequestTestScope("starttransaction") {
      messageAST.extract[StartTransactionReq] mustEqual TestMsgs.startTransactionReq
    }

    "read StartTransaction requests with a local time in them" in new RequestTestScope("starttransaction.localtime") {
      messageAST.extract[StartTransactionReq] mustEqual TestMsgs.startTransactionReqWithLocalTime
    }

    "read StopTransaction requests" in new RequestTestScope("stoptransaction") {
      messageAST.extract[StopTransactionReq] mustEqual TestMsgs.stopTransactionReq
    }

    "read UnlockConnector responses" in new ResponseTestScope("unlockconnector") {
      messageAST.extract[UnlockConnectorRes] mustEqual TestMsgs.unlockConnectorRes
    }

    "read Reset responses" in new ResponseTestScope("reset") {
      messageAST.extract[ResetRes] mustEqual TestMsgs.resetRes
    }

    "read ChangeAvailability responses" in new ResponseTestScope("changeavailability") {
      messageAST.extract[ChangeAvailabilityRes] mustEqual TestMsgs.changeAvailabilityRes
    }

    "read StatusNotification requests" in new RequestTestScope("statusnotification") {
      messageAST.extract[StatusNotificationReq] mustEqual TestMsgs.statusNotificationReq
    }

    "read StatusNotification requests with an error" in new RequestTestScope("statusnotification.inerror") {
      messageAST.extract[StatusNotificationReq] mustEqual TestMsgs.statusNotificationReqInError
    }

    "read RemoteStartTransaction responses" in new ResponseTestScope("remotestarttransaction") {
      messageAST.extract[RemoteStartTransactionRes] mustEqual TestMsgs.remoteStartTransactionRes
    }

    "read RemoteStopTransaction responses" in new ResponseTestScope("remotestoptransaction") {
      messageAST.extract[RemoteStopTransactionRes] mustEqual TestMsgs.remoteStopTransactionRes
    }

    "read Heartbeat requests" in new RequestTestScope("heartbeat") {
      messageAST.extract[HeartbeatReq.type] mustEqual TestMsgs.heartbeatReq
    }

    "read UpdateFirmware responses" in new ResponseTestScope("updatefirmware") {
      messageAST.extract[UpdateFirmwareRes.type] mustEqual TestMsgs.updateFirmwareRes
    }

    "read FirmwareStatusNotification requests" in new RequestTestScope("firmwarestatusnotification") {
      messageAST.extract[FirmwareStatusNotificationReq] mustEqual TestMsgs.firmwareStatusNotificationReq
    }

    "read GetDiagnostics responses" in new ResponseTestScope("getdiagnostics") {
      messageAST.extract[GetDiagnosticsRes] mustEqual TestMsgs.getDiagnosticsRes
    }

    "read DiagnosticsStatusNotification requests" in new RequestTestScope("diagnosticsstatusnotification") {
      messageAST.extract[DiagnosticsStatusNotificationReq] mustEqual TestMsgs.diagnosticsStatusNotificationReq
    }

    "read MeterValues requests" in new RequestTestScope("metervalues") {
      messageAST.extract[MeterValuesReq] mustEqual TestMsgs.meterValuesReq
    }

    "read ChangeConfiguration responses" in new ResponseTestScope("changeconfiguration") {
      messageAST.extract[ChangeConfigurationRes] mustEqual TestMsgs.changeConfigurationRes
    }

    "read ClearCache responses" in new ResponseTestScope("clearcache") {
      messageAST.extract[ClearCacheRes] mustEqual TestMsgs.clearCacheRes
    }

    "read GetConfiguration responses" in new ResponseTestScope("getconfiguration") {
      messageAST.extract[GetConfigurationRes] mustEqual TestMsgs.getConfigurationRes
    }

    "read GetLocalListVersion responses" in new ResponseTestScope("getlocallistversion") {
      messageAST.extract[GetLocalListVersionRes] mustEqual TestMsgs.getLocalListVersionRes
    }

    "read SendLocalList responses" in new ResponseTestScope("sendlocallist") {
      messageAST.extract[SendLocalListRes] mustEqual TestMsgs.sendLocalListRes
    }

    "read ReserveNow responses" in new ResponseTestScope("reservenow") {
      messageAST.extract[ReserveNowRes] mustEqual TestMsgs.reserveNowRes
    }

    "read CancelReservation responses" in new ResponseTestScope("cancelreservation") {
      messageAST.extract[CancelReservationRes] mustEqual TestMsgs.cancelReservationRes
    }
  }

  "Old-style OCPP message serialization" should {

    "serialize BootNotification responses" in {
      Extraction.decompose(TestMsgs.bootNotificationRes) must beEqualToJson(
        ("status" -> "Accepted") ~
          ("currentTime" -> "2013-09-27T14:03:00Z") ~
          ("heartbeatInterval" -> 600))
    }

    /*
    "serialize Authorize requests" in new RequestTestScope("authorize") {
      OcppJ15.serialize(TestMsgs.authorizeReq) must beEqualToJson(messageAST)
    }
    */

    "serialize Authorize responses" in new ResponseTestScope("authorize") {
      Extraction.decompose(TestMsgs.authorizeRes) must beEqualToJson(messageAST)
    }

    "serialize StartTransaction responses" in new ResponseTestScope("starttransaction") {
      Extraction.decompose(TestMsgs.startTransactionRes) must beEqualToJson(messageAST)
    }

    "serialize StopTransaction responses" in new ResponseTestScope("stoptransaction") {
      Extraction.decompose(TestMsgs.stopTransactionRes) must beEqualToJson(messageAST)
    }

    "serialize UnlockConnector requests" in new RequestTestScope("unlockconnector") {
      Extraction.decompose(TestMsgs.unlockConnectorReq) must beEqualToJson(messageAST)
    }

    "serialize GetConfigurationReq requests" in new RequestTestScope("getconfiguration") {
      Extraction.decompose(TestMsgs.getConfigurationReq) must beEqualToJson(messageAST)
    }

    "serialize Heartbeat responses" in new ResponseTestScope("heartbeat") {
      Extraction.decompose(TestMsgs.heartbeatRes) must beEqualToJson(messageAST)
    }

    "serialize Reset requests" in new RequestTestScope("reset") {
      Extraction.decompose(TestMsgs.resetReq) must beEqualToJson(messageAST)
    }

    "serialize ChangeAvailability requests" in new RequestTestScope("changeavailability") {
      Extraction.decompose(TestMsgs.changeAvailabilityReq) must beEqualToJson(messageAST)
    }

    "serialize ChangeAvailability requests for the whole charge point" in new RequestTestScope("changeavailability.wholecharger") {
      Extraction.decompose(TestMsgs.changeAvailabilityReqForWholeCharger) must beEqualToJson(messageAST)
    }

    "serialize StatusNotification responses" in {
      Extraction.decompose(StatusNotificationRes) must beEmptyJObject
    }

    "serialize RemoteStartTransaction requests" in {
      val testReq = RemoteStartTransactionReq(idTag = "044943121F1D80", connector = Some(ConnectorScope(1)))

      Extraction.decompose(testReq) must beEqualToJson(
        ("idTag" -> "044943121F1D80") ~
          ("connectorId" -> 2))
    }

    "serialize RemoteStopTransaction requests" in {
      val testReq = RemoteStopTransactionReq(1)

      Extraction.decompose(testReq) must beEqualToJson("transactionId" -> 1)
    }

    "serialize UpdateFirmware requests" in {
      val testReq = UpdateFirmwareReq(new DateTime(2013, 2, 1, 15, 9, 18, DateTimeZone.UTC),
        location = new URI("ftp://root:root@fork.gir.foo/tmp/kvcbx-updt.amx"),
        retries = Retries.fromInts(Some(4), Some(20)))

      Extraction.decompose(testReq) must beEqualToJson(
        ("retrieveDate" -> "2013-02-01T15:09:18Z") ~
          ("location" -> "ftp://root:root@fork.gir.foo/tmp/kvcbx-updt.amx") ~
          ("retries" -> 4) ~
          ("retryInterval" -> 20))
    }

    "serialize UpdateFirmware requests with missing retry configuration parts" in {
      val testReq = UpdateFirmwareReq(new DateTime(2013, 2, 1, 15, 9, 18, DateTimeZone.UTC),
        location = new URI("ftp://root:root@fork.gir.foo/tmp/kvcbx-updt.amx"),
        retries = Retries.fromInts(None, Some(20)))

      Extraction.decompose(testReq) must beEqualToJson(
        ("retrieveDate" -> "2013-02-01T15:09:18Z") ~
          ("location" -> "ftp://root:root@fork.gir.foo/tmp/kvcbx-updt.amx") ~
          ("retries" -> JNothing) ~
          ("retryInterval" -> 20))
    }

    "serialize FirmwareStatusNotification responses" in {
      Extraction.decompose(FirmwareStatusNotificationRes) must beEmptyJObject
    }

    "serialize GetDiagnostics requests" in {
      val testReq = GetDiagnosticsReq(location = new URI("ftp://root:root@axis.gir.foo/tmp"),
        retries = Retries.fromInts(Some(4), Some(20)),
        startTime = Some(new DateTime(2013, 2, 1, 17, 9, 18, DateTimeZone.forOffsetHours(2))),
        stopTime = Some(new DateTime(2013, 2, 1, 18, 9, 18, DateTimeZone.forOffsetHours(2))))

      Extraction.decompose(testReq) must beEqualToJson(
        ("location" -> "ftp://root:root@axis.gir.foo/tmp") ~
          ("startTime" -> "2013-02-01T15:09:18Z") ~
          ("stopTime" -> "2013-02-01T16:09:18Z") ~
          ("retries" -> 4) ~
          ("retryInterval" -> 20))
    }

    "serialize MeterValues responses" in {
      Extraction.decompose(MeterValuesRes) must beEmptyJObject
    }

    "serialize ChangeConfiguration requests" in {
      val testReq = ChangeConfigurationReq(key = "KVCBX_LANG", value = "FR")

      Extraction.decompose(testReq) must beEqualToJson(
        ("key" -> "KVCBX_LANG") ~
          ("value" -> "FR"))
    }

    "serialize ClearCache requests" in {
      Extraction.decompose(ClearCacheReq) must beEmptyJObject
    }

    "serialize GetLocalListVersion requests" in {
      Extraction.decompose(GetLocalListVersionReq) must beEmptyJObject
    }

    "serialize SendLocalList requests" in {
      val testReq = SendLocalListReq(updateType = UpdateType.Full,
        listVersion = AuthListSupported(1),
        localAuthorisationList = List(
          AuthorisationData(idTag = "044943121F1D80",
            idTagInfo = Some(
              IdTagInfo(status = AuthorizationStatus.Accepted,
                expiryDate = Some(new DateTime(2013, 2, 1, 15, 9, 18, DateTimeZone.UTC)),
                parentIdTag = Some(""))))),
        hash = Some(""))

      Extraction.decompose(testReq) must beEqualToJson(
        ("updateType" -> "Full") ~
          ("listVersion" -> 1) ~
          ("localAuthorisationList" -> List(
            ("idTag" -> "044943121F1D80") ~
              ("idTagInfo" -> (
                ("status" -> "Accepted") ~
                ("expiryDate" -> "2013-02-01T15:09:18Z") ~
                ("parentIdTag" -> ""))))) ~
            ("hash" -> ""))
    }

    "serialize ReserveNow requests" in {
      val testReq = ReserveNowReq(connector = ChargePointScope,
        expiryDate = new DateTime(2013, 2, 1, 15, 9, 18, DateTimeZone.UTC),
        idTag = "044943121F1D80",
        parentIdTag = Some(""),
        reservationId = 0)

      Extraction.decompose(testReq) must beEqualToJson(
        ("connectorId" -> 0) ~
          ("expiryDate" -> "2013-02-01T15:09:18Z") ~
          ("idTag" -> "044943121F1D80") ~
          ("parentIdTag" -> "") ~
          ("reservationId" -> 0))
    }

    "serialize CancelReservation requests" in {
      Extraction.decompose(CancelReservationReq(reservationId = 0)) must beEqualToJson("reservationId" -> 0)
    }
  }

  private class CallTestScope(operationName: String, callType: String) extends Scope {
    val messageAST = JsonParser.parse(loadRequestJSON)

    private def loadRequestJSON: String = {
      val requestFileName = s"ocpp15/without_srpc/$operationName.$callType.json"
      Source.fromURL(this.getClass.getResource(requestFileName)).mkString
    }
  }

  private class RequestTestScope(operationName: String) extends CallTestScope(operationName, "request")

  private class ResponseTestScope(operationName: String) extends CallTestScope(operationName, "response")

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

  private def beEmptyJObject = beTypedEqualTo[JValue](JObject(List()))

  private def localTimeForUTCFields(year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Int) =
    new DateTime(year, month, day, hour, minute, second, DateTimeZone.UTC).withZone(DateTimeZone.getDefault())

  private val testTime = localTimeForUTCFields(2013,2,1,15,9,18)

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
        currentTime = new DateTime(2013, 9, 27, 16, 3, 0, DateTimeZone.forID("Europe/Brussels")),
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
      val meterTimestamp = localTimeForUTCFields(2013, 3, 7, 16, 52, 16)
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
        expiryDate = Some(new DateTime(2013, 2, 1, 16, 9, 18, DateTimeZone.forID("Europe/Brussels"))),
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
        status = Available, timestamp = Some(testTime),
        vendorId = Some(""))
    val statusNotificationReqInError = {
      val faultedStatus = Faulted(errorCode = Some(ChargePointErrorCode.PowerMeterFailure),
        info = Some("Die meter is kats doorgefikt joh"), vendorErrorCode = Some("MeterB0rk3d"))

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
      stopTime = Some(localTimeForUTCFields(2013,8,8,16,9,18)),
      retries = Retries(Some(4), Some(FiniteDuration(20, SECONDS))))
    val getDiagnosticsRes = GetDiagnosticsRes(fileName = Some("diag-gir.vat.mx.000e48-20130131132608.txt"))

    val diagnosticsStatusNotificationReq = DiagnosticsStatusNotificationReq(uploaded = true)
    val diagnosticsStatusNotificationRes = DiagnosticsStatusNotificationRes

    val meterValuesReq = MeterValuesReq(
        scope = ConnectorScope(1),
        transactionId = Some(0),
        meters = List(
          Meter(
            timestamp = localTimeForUTCFields(2013, 3, 7, 16, 52, 16),
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
            timestamp = localTimeForUTCFields(2013, 3, 7, 19, 52, 16),
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

  case class TestableMsg[T <: Message](jsonSerializable: JsonSerializable[T], msg: T, jsonFileBase: String)
  object TestableMsg {
    def apply[T <: Message : JsonSerializable](msg: T, jsonFileBase: String) =
      new TestableMsg(jsonSerializable[T], msg, jsonFileBase)
  }

  private class MessageTestScope(jsonFileBase: String) extends Scope {
    val messageAST = JsonParser.parse(loadRequestJSON)

    private def loadRequestJSON: String = {
      val requestFileName = s"ocpp15/without_srpc/$jsonFileBase.json"
      Source.fromURL(this.getClass.getResource(requestFileName)).mkString
    }
  }

  private def testSerialization(msg: TestableMsg[_ <: Message]) = new MessageTestScope(msg.jsonFileBase) {
    OcppJ15.serialize(msg.msg) must beEqualToJson(messageAST)
  }

  private val testMsgs = List(
    TestableMsg(TestMsgs.bootNotificationReq, "bootnotification.request"),
    TestableMsg(TestMsgs.bootNotificationRes, "bootnotification.response"),
    TestableMsg(TestMsgs.authorizeReq, "authorize.request"),
    TestableMsg(TestMsgs.authorizeRes, "authorize.response"),
    TestableMsg(jsonSerializable[StartTransactionReq], TestMsgs.startTransactionReq, "starttransaction.request"),
  // TODO this ojsonSerializablefor deserialization
    //TestableMsg(TestMsgs.startTransactionReqWithLocalTime, "starttransaction.localtime.request"),
    TestableMsg(TestMsgs.startTransactionRes, "starttransaction.response"),
    TestableMsg(TestMsgs.stopTransactionReq, "stoptransaction.request"),
    TestableMsg(TestMsgs.stopTransactionRes, "stoptransaction.response"),
    TestableMsg(TestMsgs.unlockConnectorReq, "unlockconnector.request"),
    TestableMsg(TestMsgs.unlockConnectorRes, "unlockconnector.response"),
    TestableMsg(TestMsgs.resetReq, "reset.request"),
    TestableMsg(TestMsgs.resetRes, "reset.response"),
    TestableMsg(TestMsgs.changeAvailabilityReq, "changeavailability.request"),
    TestableMsg(TestMsgs.changeAvailabilityRes, "changeavailability.response"),
    TestableMsg(TestMsgs.statusNotificationReq, "statusnotification.request"),
    TestableMsg(TestMsgs.statusNotificationReqInError, "statusnotification.inerror.request"),
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
}
