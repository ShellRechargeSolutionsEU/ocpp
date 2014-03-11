package com.thenewmotion.ocpp
package json

import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.Scope
import scala.io.Source
import net.liftweb.json._
import com.thenewmotion.time.Imports._
import scala.concurrent.duration.FiniteDuration
import net.liftweb.json.JsonDSL._
import java.net.URI
import messages._
import messages.Meter._

class OcppMessageSerializationSpec extends SpecificationWithJUnit {

  implicit val formats: Formats = DefaultFormats ++ OcppMessageJsonSerializers()

  "OCPP message deserialization" should {

    "read BootNotification requests" in new RequestTestScope("bootnotification") {
      requestAST.extract[BootNotificationReq] mustEqual BootNotificationReq(chargePointVendor = "DBT",
        chargePointModel = "NQC-ACDC",
        chargePointSerialNumber = Some("gir.vat.mx.000e48"),
        chargeBoxSerialNumber = Some("gir.vat.mx.000e48"),
        firmwareVersion = Some("1.0.49"),
        iccid = Some(""),
        imsi = Some(""),
        meterType = Some("DBT NQC-ACDC"),
        meterSerialNumber = Some("gir.vat.mx.000e48"))
    }

    "read Authorize requests" in new RequestTestScope("authorize") {
      requestAST.extract[AuthorizeReq] mustEqual AuthorizeReq(idTag = "B4F62CEF")
    }

    "read StartTransaction requests" in new RequestTestScope("starttransaction") {
      requestAST.extract[StartTransactionReq] mustEqual StartTransactionReq(connector = ConnectorScope(1),
        idTag = "B4F62CEF",
        timestamp = localTimeForUTCFields(2013, 2, 1, 15, 9, 18),
        meterStart = 0,
        reservationId = Some(0))
    }

    "read StartTransaction requests with a local time in them" in new RequestTestScope("starttransaction.localtime") {
      requestAST.extract[StartTransactionReq] mustEqual StartTransactionReq(connector = ConnectorScope(1),
        idTag = "B4F62CEF",
        timestamp = localTimeForUTCFields(2013, 2, 1, 15, 9, 18),
        meterStart = 0,
        reservationId = Some(0))
    }

    "read StopTransaction requests" in new RequestTestScope("stoptransaction") {
      val testTimestamp = localTimeForUTCFields(2013, 2, 1, 15, 9, 18)
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

      requestAST.extract[StopTransactionReq] mustEqual StopTransactionReq(transactionId = 0,
        idTag = Some("B4F62CEF"),
        //"2013­08­14T14:29:31.540+02:00"
        timestamp = testTimestamp,
        meterStop = 20,
        transactionData = List(
          TransactionData(List(testMeter)),
          TransactionData(List(testMeter))))
    }

    "read UnlockConnector responses" in new ResponseTestScope("unlockconnector") {
      requestAST.extract[UnlockConnectorRes] mustEqual UnlockConnectorRes(accepted = true)
    }

    "read Reset responses" in new ResponseTestScope("reset") {
      requestAST.extract[ResetRes] mustEqual ResetRes(accepted = true)
    }

    "read ChangeAvailability responses" in new ResponseTestScope("changeavailability") {
      requestAST.extract[ChangeAvailabilityRes] mustEqual ChangeAvailabilityRes(status = AvailabilityStatus.Accepted)
    }

    "read StatusNotification requests" in new RequestTestScope("statusnotification") {
      requestAST.extract[StatusNotificationReq] mustEqual StatusNotificationReq(scope = ConnectorScope(1),
        status = Available, timestamp = Some(localTimeForUTCFields(2013, 2, 1, 15, 9, 18)),
        vendorId = Some(""))
    }

    "read StatusNotification requests with an error" in new RequestTestScope("statusnotification.inerror") {
      val faultedStatus = Faulted(errorCode = Some(ChargePointErrorCode.PowerMeterFailure),
        info = Some("Die meter is kats doorgefikt joh"), vendorErrorCode = Some("MeterB0rk3d"))

      requestAST.extract[StatusNotificationReq] mustEqual StatusNotificationReq(scope = ConnectorScope(1),
        status = faultedStatus, timestamp = Some(localTimeForUTCFields(2013, 2, 1, 15, 9, 18)),
        vendorId = Some("TNM"))
    }

    "read RemoteStartTransaction responses" in new ResponseTestScope("remotestarttransaction") {
      requestAST.extract[RemoteStartTransactionRes] mustEqual RemoteStartTransactionRes(accepted = true)
    }

    "read RemoteStopTransaction responses" in new ResponseTestScope("remotestoptransaction") {
      requestAST.extract[RemoteStopTransactionRes] mustEqual RemoteStopTransactionRes(accepted = false)
    }

    "read Heartbeat requests" in new RequestTestScope("heartbeat") {
      requestAST.extract[HeartbeatReq.type] mustEqual HeartbeatReq
    }

    "read UpdateFirmware responses" in new ResponseTestScope("updatefirmware") {
      requestAST.extract[UpdateFirmwareRes.type] mustEqual UpdateFirmwareRes
    }

    "read FirmwareStatusNotification requests" in new RequestTestScope("firmwarestatusnotification") {
      requestAST.extract[FirmwareStatusNotificationReq] mustEqual FirmwareStatusNotificationReq(
        status = FirmwareStatus.DownloadFailed)
    }

    "read GetDiagnostics responses" in new ResponseTestScope("getdiagnostics") {
      requestAST.extract[GetDiagnosticsRes] mustEqual GetDiagnosticsRes(
        fileName = Some("diag-gir.vat.mx.000e48-20130131132608.txt"))
    }

    "read DiagnosticsStatusNotification requests" in new RequestTestScope("diagnosticsstatusnotification") {
      requestAST.extract[DiagnosticsStatusNotificationReq] mustEqual DiagnosticsStatusNotificationReq(
        uploaded = true)
    }

    "read MeterValues requests" in new RequestTestScope("metervalues") {
      requestAST.extract[MeterValuesReq] mustEqual MeterValuesReq(
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
    }

    "read ChangeConfiguration responses" in new ResponseTestScope("changeconfiguration") {
      requestAST.extract[ChangeConfigurationRes] mustEqual ChangeConfigurationRes(ConfigurationStatus.Accepted)
    }

    "read ClearCache responses" in new ResponseTestScope("clearcache") {
      requestAST.extract[ClearCacheRes] mustEqual ClearCacheRes(accepted = true)
    }

    "read GetConfiguration responses" in new ResponseTestScope("getconfiguration") {
      requestAST.extract[GetConfigurationRes] mustEqual GetConfigurationRes(
        values = List(KeyValue(key = "KVCBX_PROFILE", readonly = true, value = Some("NQC-ACDC"))),
        unknownKeys = List())
    }

    "read GetLocalListVersion responses" in new ResponseTestScope("getlocallistversion") {
      requestAST.extract[GetLocalListVersionRes] mustEqual GetLocalListVersionRes(AuthListSupported(0))
    }

    "read SendLocalList responses" in new ResponseTestScope("sendlocallist") {
      requestAST.extract[SendLocalListRes] mustEqual SendLocalListRes(status = UpdateStatus.UpdateAccepted(Some("")))
    }

    "read ReserveNow responses" in new ResponseTestScope("reservenow") {
      requestAST.extract[ReserveNowRes] mustEqual ReserveNowRes(status = Reservation.Accepted)
    }

    "read CancelReservation responses" in new ResponseTestScope("cancelreservation") {
      requestAST.extract[CancelReservationRes] mustEqual CancelReservationRes(accepted = true)
    }
  }

  "OCPP message serialization" should {

    "serialize BootNotification responses" in {
      val testRes = BootNotificationRes(registrationAccepted = true,
        currentTime = new DateTime(2013, 9, 27, 16, 3, 0, DateTimeZone.forOffsetHours(2)),
        heartbeatInterval = FiniteDuration(10, "minutes"))

      Extraction.decompose(testRes) must beEqualToJson(
        ("status" -> "Accepted") ~
          ("currentTime" -> "2013-09-27T14:03:00Z") ~
          ("heartbeatInterval" -> 600))
    }

    "serialize Authorize responses" in {
      val testRes = AuthorizeRes(idTag = IdTagInfo(status = AuthorizationStatus.IdTagInvalid,
        expiryDate = Some(new DateTime(2013, 9, 27, 16, 27, 0, DateTimeZone.forOffsetHours(2))),
        parentIdTag = None))

      Extraction.decompose(testRes) must beEqualToJson("idTagInfo" -> (
        ("status" -> "Invalid") ~
        ("expiryDate" -> "2013-09-27T14:27:00Z") ~
        ("parentIdTag" -> JNothing)))
    }

    "serialize StopTransaction responses" in {
      val testRes = StopTransactionRes(idTag = Some(IdTagInfo(status = AuthorizationStatus.IdTagExpired,
        expiryDate = Some(new DateTime(2013, 2, 1, 16, 9, 18, DateTimeZone.forOffsetHours(1))),
        parentIdTag = Some("PARENT"))))

      Extraction.decompose(testRes) must beEqualToJson("idTagInfo" -> (
        ("status" -> "Expired") ~
        ("expiryDate" -> "2013-02-01T15:09:18Z") ~
        ("parentIdTag" -> "PARENT")))
    }

    "serialize UnlockConnector requests" in {
      val testReq = UnlockConnectorReq(connector = ConnectorScope(0))

      Extraction.decompose(testReq) must beEqualToJson("connectorId" -> 1)
    }

    "serialize GetConfigurationReq requests" in {
      val testReq = GetConfigurationReq(keys = List("apeschaap", "hompeschomp"))

      Extraction.decompose(testReq) must beEqualToJson("key" -> List("apeschaap", "hompeschomp"))
    }

    "serialize Heartbeat responses" in {
      val testRes = HeartbeatRes(currentTime = new DateTime(2013, 2, 1, 15, 9, 18, DateTimeZone.UTC))

      Extraction.decompose(testRes) must beEqualToJson("currentTime" -> "2013-02-01T15:09:18Z")
    }

    "serialize Reset requests" in {
      val testReq = ResetReq(resetType = ResetType.Soft)

      Extraction.decompose(testReq) must beEqualToJson("type" -> "Soft")
    }

    "serialize ChangeAvailability requests" in {
      val testReq = ChangeAvailabilityReq(scope = ConnectorScope(0), availabilityType = AvailabilityType.Inoperative)

      Extraction.decompose(testReq) must beEqualToJson(
        ("connectorId" -> 1) ~
          ("type" -> "Inoperative"))
    }

    "serialize ChangeAvailability requests for the whole charge point" in {
      val testReq = ChangeAvailabilityReq(scope = ChargePointScope, availabilityType = AvailabilityType.Operative)

      Extraction.decompose(testReq) must beEqualToJson(
        ("connectorId" -> 0) ~
          ("type" -> "Operative"))
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
    def localTimeForUTCFields(year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Int) =
      new DateTime(year, month, day, hour, minute, second, DateTimeZone.UTC).withZone(DateTimeZone.getDefault())

    val requestAST = JsonParser.parse(loadRequestJSON)

    private def loadRequestJSON: String = {
      val requestFileName = s"ocpp15/without_srpc/$operationName.$callType.json"
      Source.fromURL(this.getClass.getResource(requestFileName)).mkString
    }
  }

  private class RequestTestScope(operationName: String) extends CallTestScope(operationName, "request")

  private class ResponseTestScope(operationName: String) extends CallTestScope(operationName, "response")

  // copy of beEqualTo that has the argument type set to JValue, so we can use Lift's JSON DSL without type annotations
  // everywhere
  private def beEqualToJson(jval: JValue) = beTypedEqualTo[JValue](jval)

  private def beEmptyJObject = beTypedEqualTo[JValue](JObject(List()))
}
