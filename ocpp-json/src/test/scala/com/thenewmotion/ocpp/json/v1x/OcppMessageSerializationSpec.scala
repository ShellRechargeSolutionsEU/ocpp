package com.thenewmotion.ocpp
package json
package v1x

import java.net.URI
import java.time.{ZoneId, ZonedDateTime}
import scala.concurrent.duration._
import scala.io.Source
import messages.v1x.meter._
import messages.v1x._
import org.json4s._
import org.json4s.native.JsonParser
import org.specs2.matcher.MatchResult
import org.specs2.mutable.Specification
import v15.SerializationV15._

class OcppMessageSerializationSpec extends Specification {


  "OCPP message deserialization" should {

    "deserialize all message types" in {

      for (testMsg <- testMsgs) yield {
        withJsonFromFile(testMsg.jsonFileBase) { messageAST: JValue =>
          Serialization.deserialize(messageAST)(testMsg.versionVariant) mustEqual testMsg.msg
        }
      }
    }

    "read StartTransaction requests with a local time in them" in {
      withJsonFromFile("starttransaction.localtime.request") { messageAST: JValue =>
        Serialization.deserialize[StartTransactionReq, Version.V15.type](messageAST) mustEqual TestMsgs.startTransactionReqWithLocalTime
      }
    }
  }

  "OCPP message serialization" should {

    "serialize messages of all types" in {
      for (testMsg <- testMsgs) yield {
        withJsonFromFile(testMsg.jsonFileBase) { messageAST: JValue =>
          testMsg.serialize must beEqualToJson(messageAST)
        }
      }
    }
  }

  "OCPP message serialization error handling" should {
    "throw a MappingException in case a message contains an invalid URL" in
      withJsonFromFile("updatefirmware.invalidurl.request") { messageAST =>
        Serialization.deserialize[UpdateFirmwareReq, Version.V15.type](messageAST) must throwA[MappingException]
      }

    "throw a MappingException in case a MeterValues request mentions an invalid property name" in
      withJsonFromFile("metervalues.invalidproperty.request") { messageAST =>
        Serialization.deserialize[MeterValuesReq, Version.V15.type](messageAST) must throwA[MappingException]
      }

    "throw a MappingException in case an invalid status enum is given" in
      withJsonFromFile("changeavailability.invalidstatus.response") { messageAST =>
        Serialization.deserialize[ChangeAvailabilityRes, Version.V15.type](messageAST) must throwA[MappingException]
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

  case class TestableMsg[M <: Message](msg: M, jsonFileBase: String, versionVariant: OcppMessageSerializer[M, Version.V15.type]) {
    def serialize: JValue = Serialization.serialize(msg)(versionVariant)
  }
  object TestableMsg {
    def forFile[M <: Message](msg: M, jsonFileBase: String)(implicit msgSer: OcppMessageSerializer[M, Version.V15.type]): TestableMsg[M] =
      TestableMsg(msg, jsonFileBase, msgSer)
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
    TestableMsg.forFile(TestMsgs.bootNotificationReq, "bootnotification.request"),
    TestableMsg.forFile(TestMsgs.bootNotificationRes, "bootnotification.response"),
    TestableMsg.forFile(TestMsgs.authorizeReq, "authorize.request"),
    TestableMsg.forFile(TestMsgs.authorizeRes, "authorize.response"),
    TestableMsg.forFile(TestMsgs.startTransactionReq, "starttransaction.request"),
    TestableMsg.forFile(TestMsgs.startTransactionRes, "starttransaction.response"),
    TestableMsg.forFile(TestMsgs.stopTransactionReq, "stoptransaction.request"),
    TestableMsg.forFile(TestMsgs.stopTransactionRes, "stoptransaction.response"),
    TestableMsg.forFile(TestMsgs.unlockConnectorReq, "unlockconnector.request"),
    TestableMsg.forFile(TestMsgs.unlockConnectorRes, "unlockconnector.response"),
    TestableMsg.forFile(TestMsgs.resetReq, "reset.request"),
    TestableMsg.forFile(TestMsgs.resetRes, "reset.response"),
    TestableMsg.forFile(TestMsgs.changeAvailabilityReq, "changeavailability.request"),
    TestableMsg.forFile(TestMsgs.changeAvailabilityReqForWholeCharger, "changeavailability.wholecharger.request"),
    TestableMsg.forFile(TestMsgs.changeAvailabilityRes, "changeavailability.response"),
    TestableMsg.forFile(TestMsgs.statusNotificationReq, "statusnotification.request"),
    TestableMsg.forFile(TestMsgs.statusNotificationReqInError, "statusnotification.inerror.request"),
    // The case that there is an error but the message gives NoError as the error code. Makes no sense but the OCPP
    // standard doesn't disallow it.
    TestableMsg.forFile(TestMsgs.statusNotificationReqInErrorNoError, "statusnotification.inerror-noerror.request"),
    TestableMsg.forFile(TestMsgs.statusNotificationRes, "statusnotification.response"),
    TestableMsg.forFile(TestMsgs.remoteStartTransactionReq, "remotestarttransaction.request"),
    TestableMsg.forFile(TestMsgs.remoteStartTransactionRes, "remotestarttransaction.response"),
    TestableMsg.forFile(TestMsgs.remoteStopTransactionReq, "remotestoptransaction.request"),
    TestableMsg.forFile(TestMsgs.remoteStopTransactionRes, "remotestoptransaction.response"),
    TestableMsg.forFile(TestMsgs.heartbeatReq, "heartbeat.request"),
    TestableMsg.forFile(TestMsgs.heartbeatRes, "heartbeat.response"),
    TestableMsg.forFile(TestMsgs.updateFirmwareReq, "updatefirmware.request"),
    TestableMsg.forFile(TestMsgs.updateFirmwareRes, "updatefirmware.response"),
    TestableMsg.forFile(TestMsgs.firmwareStatusNotificationReq, "firmwarestatusnotification.request"),
    TestableMsg.forFile(TestMsgs.firmwareStatusNotificationRes, "firmwarestatusnotification.response"),
    TestableMsg.forFile(TestMsgs.getDiagnosticsReq, "getdiagnostics.request"),
    TestableMsg.forFile(TestMsgs.getDiagnosticsRes, "getdiagnostics.response"),
    TestableMsg.forFile(TestMsgs.diagnosticsStatusNotificationReq, "diagnosticsstatusnotification.request"),
    TestableMsg.forFile(TestMsgs.diagnosticsStatusNotificationRes, "diagnosticsstatusnotification.response"),
    TestableMsg.forFile(TestMsgs.meterValuesReq, "metervalues.request"),
    TestableMsg.forFile(TestMsgs.meterValuesRes, "metervalues.response"),
    TestableMsg.forFile(TestMsgs.changeConfigurationReq, "changeconfiguration.request"),
    TestableMsg.forFile(TestMsgs.changeConfigurationRes, "changeconfiguration.response"),
    TestableMsg.forFile(TestMsgs.clearCacheReq, "clearcache.request"),
    TestableMsg.forFile(TestMsgs.clearCacheRes, "clearcache.response"),
    TestableMsg.forFile(TestMsgs.getConfigurationReq, "getconfiguration.request"),
    TestableMsg.forFile(TestMsgs.getConfigurationRes, "getconfiguration.response"),
    TestableMsg.forFile(TestMsgs.getLocalListVersionReq, "getlocallistversion.request"),
    TestableMsg.forFile(TestMsgs.getLocalListVersionRes, "getlocallistversion.response"),
    TestableMsg.forFile(TestMsgs.sendLocalListReq, "sendlocallist.request"),
    TestableMsg.forFile(TestMsgs.sendLocalListRes, "sendlocallist.response"),
    TestableMsg.forFile(TestMsgs.reserveNowReq, "reservenow.request"),
    TestableMsg.forFile(TestMsgs.reserveNowRes, "reservenow.response"),
    TestableMsg.forFile(TestMsgs.cancelReservationReq, "cancelreservation.request"),
    TestableMsg.forFile(TestMsgs.cancelReservationRes, "cancelreservation.response"),
    TestableMsg.forFile(TestMsgs.centralSystemDataTransferReq, "centralsystemdatatransfer.request"),
    TestableMsg.forFile(TestMsgs.centralSystemDataTransferRes, "centralsystemdatatransfer.response"),
    TestableMsg.forFile(TestMsgs.chargePointDataTransferReq, "chargepointdatatransfer.request"),
    TestableMsg.forFile(TestMsgs.chargePointDataTransferRes, "chargepointdatatransfer.response")
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
    val bootNotificationRes = BootNotificationRes(
      status = RegistrationStatus.Accepted,
      currentTime = utcDateTime(2013, 9, 27, 14, 3, 0),
      interval = FiniteDuration(600, "seconds"))

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

      StopTransactionReq(transactionId = 0,
        idTag = Some("B4F62CEF"),
        //"2013­08­14T14:29:31.540+02:00"
        timestamp = testTimestamp,
        meterStop = 20,
        reason = StopReason.Local,
        meters = List(testMeter, testMeter)
      )
    }
    val stopTransactionRes = StopTransactionRes(idTag = Some(IdTagInfo(status = AuthorizationStatus.IdTagExpired,
      expiryDate = Some(utcDateTime(2013, 2, 1, 15, 9, 18)),
      parentIdTag = Some("PARENT"))))

    val unlockConnectorReq = UnlockConnectorReq(connector = ConnectorScope(0))
    val unlockConnectorRes = UnlockConnectorRes(UnlockStatus.Unlocked)

    val resetReq = ResetReq(resetType = ResetType.Soft)
    val resetRes = ResetRes(accepted = true)

    val changeAvailabilityReq =
      ChangeAvailabilityReq(scope = ConnectorScope(0), availabilityType = AvailabilityType.Inoperative)
    val changeAvailabilityReqForWholeCharger =
      ChangeAvailabilityReq(scope = ChargePointScope, availabilityType = AvailabilityType.Operative)
    val changeAvailabilityRes = ChangeAvailabilityRes(status = AvailabilityStatus.Accepted)

    val statusNotificationReq = StatusNotificationReq(
      scope = ConnectorScope(1),
      status = ChargePointStatus.Available(Some("Info msg")),
      timestamp = Some(testTime),
      vendorId = Some("")
    )
    val statusNotificationReqInError = {
      val faultedStatus = ChargePointStatus.Faulted(
        errorCode = Some(ChargePointErrorCode.PowerMeterFailure),
        info = Some("Die meter is kats doorgefikt joh"),
        vendorErrorCode = Some("MeterB0rk3d")
      )

      StatusNotificationReq(scope = ConnectorScope(1),
        status = faultedStatus, timestamp = Some(testTime), vendorId = Some("TNM"))
    }
    val statusNotificationReqInErrorNoError = {
      val faultedStatus = ChargePointStatus.Faulted(
        errorCode = None,
        info = Some("Het laadpunt is een beetje in de bonen"),
        vendorErrorCode = Some("Lolwut?")
      )

      StatusNotificationReq(scope = ConnectorScope(1),
        status = faultedStatus, timestamp = Some(testTime), vendorId = Some("TNM"))
    }
    val statusNotificationRes = StatusNotificationRes

    val remoteStartTransactionReq = RemoteStartTransactionReq(idTag = "044943121F1D80",
      connector = Some(ConnectorScope(1)), chargingProfile = None)
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

    val diagnosticsStatusNotificationReq = DiagnosticsStatusNotificationReq(
      DiagnosticsStatus.Uploaded
    )
    val diagnosticsStatusNotificationRes = DiagnosticsStatusNotificationRes

    val meterValuesReq = MeterValuesReq(
      scope = ConnectorScope(1),
      transactionId = Some(0),
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
    val meterValuesRes = MeterValuesRes

    val changeConfigurationReq = ChangeConfigurationReq(key = "KVCBX_LANG", value = "FR")
    val changeConfigurationRes = ChangeConfigurationRes(ConfigurationStatus.Accepted)

    val clearCacheReq = ClearCacheReq
    val clearCacheRes = ClearCacheRes(accepted = true)

    val getConfigurationReq = GetConfigurationReq(keys = List("apeschaap", "hompeschomp"))
    val getConfigurationRes = GetConfigurationRes(
      values = List(KeyValue(key = "KVCBX_PROFILE", readonly = true, value = Some("NQC-ACDC"))),
      unknownKeys = List("UNKNOWN_KEY1", "UNKNOWN_KEY2"))

    val getLocalListVersionReq = GetLocalListVersionReq
    val getLocalListVersionRes = GetLocalListVersionRes(AuthListSupported(0))

    val sendLocalListReq = SendLocalListReq(UpdateType.Full, listVersion = AuthListSupported(1),
      localAuthorisationList = List(AuthorisationData(idTag = "044943121F1D80",
        idTagInfo = Some(IdTagInfo(AuthorizationStatus.Accepted, Some(testTime), Some(""))))), hash = Some(""))
    val sendLocalListRes = SendLocalListRes(status = UpdateStatusWithHash.Accepted(Some("")))

    val reserveNowReq = ReserveNowReq(connector = ChargePointScope,
      expiryDate = testTime,
      idTag = "044943121F1D80",
      parentIdTag = Some(""),
      reservationId = 0)
    val reserveNowRes = ReserveNowRes(status = Reservation.Accepted)

    val cancelReservationReq = CancelReservationReq(42)
    val cancelReservationRes = CancelReservationRes(accepted = true)

    val centralSystemDataTransferReq = CentralSystemDataTransferReq("com.vendor", Some("pi"), Some("3.14159265359"))
    val centralSystemDataTransferRes = CentralSystemDataTransferRes(DataTransferStatus.Accepted)
    val chargePointDataTransferReq = ChargePointDataTransferReq("com.vendor", Some("pi"), Some("3.14159265359"))
    val chargePointDataTransferRes = ChargePointDataTransferRes(DataTransferStatus.Accepted)
  }

  private def utcDateTime(year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Int) =
    ZonedDateTime
      .of(year, month, day, hour, minute, second, 0, ZoneId.of("UTC"))
}
