package com.thenewmotion
package ocpp

import org.specs2.specification.{Scope => SpecsScope}
import soapenvelope12.Body
import com.thenewmotion.time.Imports._
import scala.concurrent.duration._

import org.specs2.mock.Mockito
import org.specs2.mutable.SpecificationWithJUnit
import ChargePointAction._
import ChargePoint._

class ChargePointDispatcherSpec extends SpecificationWithJUnit with Mockito {

  "ChargePointDispatcherV15" should {
    import v15.{ChargePointService => _, AvailabilityStatus => _, AvailabilityType => _, _}

    "call cancelReservation and return its false result" in new TestScope {
      cpService.apply(CancelReservationReq(13)) returns CancelReservationRes(accepted = false)

      val xml = <CancelReservation xmlns={v15PointNamespace}><reservationId>13</reservationId></CancelReservation>
      val body = ChargePointDispatcherV15.dispatch(CancelReservation, xml, cpService)

      body must beResponse(CancelReservationResponse(CancelReservationStatus.fromString("Rejected")))
    }

    "call cancelReservation and return its true result" in new TestScope {
      cpService.apply(CancelReservationReq(4)) returns CancelReservationRes(accepted = true)

      val xml = <CancelReservation xmlns={v15PointNamespace}><reservationId>4</reservationId></CancelReservation>
      val body = ChargePointDispatcherV15.dispatch(CancelReservation, xml, cpService)

      body must beResponse(CancelReservationResponse(CancelReservationStatus.fromString("Accepted")))
    }

    "call changeAvailability with charge point scope if connector is 0" in new TestScope {
      cpService.apply(any[ChangeAvailabilityReq]) returns ChangeAvailabilityRes(AvailabilityStatus.Accepted)

      val xml = <ChangeAvailability xmlns={v15PointNamespace}>
                  <connectorId>0</connectorId>
                  <type>Operative</type>
                </ChangeAvailability>
      ChargePointDispatcherV15.dispatch(ChangeAvailability, xml, cpService)

      there was one(cpService).apply(ChangeAvailabilityReq(ChargePointScope, AvailabilityType.Operative))
    }

    "return result of changeAvailability in the right way" in new TestScope {
      cpService.apply(any[ChangeAvailabilityReq])
        .returns(ChangeAvailabilityRes(AvailabilityStatus.Accepted))
        .thenReturns(ChangeAvailabilityRes(AvailabilityStatus.Scheduled))
        .thenReturns(ChangeAvailabilityRes(AvailabilityStatus.Rejected))

      val xml = <ChangeAvailability xmlns={v15PointNamespace}>
                  <connectorId>0</connectorId>
                  <type>Operative</type>
                </ChangeAvailability>
      val dispatcher = ChargePointDispatcherV15
      def dispatchChangeAv: Body =  dispatcher.dispatch(ChangeAvailability, xml, cpService)
      val result = (dispatchChangeAv, dispatchChangeAv, dispatchChangeAv)

      result._1 must beResponse(ChangeAvailabilityResponse(v15.AvailabilityStatus.fromString("Accepted")))
      result._2 must beResponse(ChangeAvailabilityResponse(v15.AvailabilityStatus.fromString("Scheduled")))
      result._3 must beResponse(ChangeAvailabilityResponse(v15.AvailabilityStatus.fromString("Rejected")))
    }

    "call changeConfiguration with the right parameters" in new TestScope {
      cpService.apply(any[ChangeConfigurationReq]) returns ChangeConfigurationRes(ocpp.ConfigurationStatus.Accepted)

      val xml = <ChangeConfiguration xmlns={v15PointNamespace}>
                  <key>aap</key><value>noot</value>
                </ChangeConfiguration>
      ChargePointDispatcherV15.dispatch(ChangeConfiguration, xml, cpService)

      there was one(cpService).apply(ChangeConfigurationReq("aap", "noot"))
    }

    "return values from changeConfiguration" in new TestScope {
      cpService.apply(any[ChangeConfigurationReq])
        .returns(ChangeConfigurationRes(ocpp.ConfigurationStatus.Accepted))
        .thenReturns(ChangeConfigurationRes(ocpp.ConfigurationStatus.Rejected))
        .thenReturns(ChangeConfigurationRes(ocpp.ConfigurationStatus.NotSupported))

      val xml = <ChangeConfiguration xmlns={v15PointNamespace}>
        <key>aap</key><value>noot</value>
      </ChangeConfiguration>
      val dispatcher = ChargePointDispatcherV15
      def dispatch = dispatcher.dispatch(ChangeConfiguration, xml, cpService)
      val result = (dispatch, dispatch, dispatch)

      result._1 must beResponse(ChangeConfigurationResponse(ConfigurationStatus.fromString("Accepted")))
      result._2 must beResponse(ChangeConfigurationResponse(ConfigurationStatus.fromString("Rejected")))
      result._3 must beResponse(ChangeConfigurationResponse(ConfigurationStatus.fromString("NotSupported")))
    }

    "call clearCache and return its result" in new TestScope {
      cpService.apply(ClearCacheReq) returns ClearCacheRes(accepted = true) thenReturns ClearCacheRes(accepted = false)

      val xml = <ClearCache xmlns={v15PointNamespace}/>
      val dispatcher = ChargePointDispatcherV15
      def dispatch = dispatcher.dispatch(ClearCache, xml, cpService)
      val result = (dispatch, dispatch)

      result._1 must beResponse(ClearCacheResponse(ClearCacheStatus.fromString("Accepted")))
      result._2 must beResponse(ClearCacheResponse(ClearCacheStatus.fromString("Rejected")))
    }

    "call getConfiguration with the right parameters" in new TestScope {
      cpService.apply(any[GetConfigurationReq]) returns GetConfigurationRes(List.empty, List.empty)

      val xml = <GetConfiguration xmlns={v15PointNamespace}>
                  <key>sleutel</key><key>Schlüssel</key><key>clef</key>
                </GetConfiguration>
      ChargePointDispatcherV15.dispatch(GetConfiguration, xml, cpService)

      there was one(cpService).apply(GetConfigurationReq(List("sleutel", "Schlüssel", "clef")))
    }

    "return the result of getConfiguration" in new TestScope {
      cpService.apply(any[GetConfigurationReq]) returns GetConfigurationRes(
        List(ocpp.KeyValue("aap", readonly = false, None), ocpp.KeyValue("noot", readonly = true, Some("mies"))),
        List("boom", "roos", "vis"))

//      cpService.getConfiguration(any) returns Configuration()

      val xml = <GetConfiguration/>
      val result = ChargePointDispatcherV15.dispatch(GetConfiguration, xml, cpService)

      result must beResponse(GetConfigurationResponse(List(v15.KeyValue("aap", readonly = false, None),
                                                           v15.KeyValue("noot", readonly = true, Some("mies"))),
                                                      List("boom", "roos", "vis")))
    }

    "call getDiagnostics with the right parameters" in new TestScope {
      cpService.apply(any[GetDiagnosticsReq]) returns GetDiagnosticsRes(Some("aargh.xml"))

      val xml = <GetDiagnostics xmlns={v15PointNamespace}>
                  <location>ftp://example.org/uploaddir</location>
                  <startTime>2013-06-04T13:30:00Z</startTime>
                  <stopTime>2013-06-04T14:30:00Z</stopTime>
                  <retries>4</retries>
                  <retryInterval>90</retryInterval>
                </GetDiagnostics>

      ChargePointDispatcherV15.dispatch(GetDiagnostics, xml, cpService)

      val expectedStartTime = new DateTime(2013, 6, 4, 13, 30, DateTimeZone.UTC).toDateTime(DateTimeZone.getDefault)
      val expectedStopTime  = new DateTime(2013, 6, 4, 14, 30, DateTimeZone.UTC).toDateTime(DateTimeZone.getDefault)
      val expectedRetries   = Retries(Some(4), Some(scala.concurrent.duration.Duration(90, SECONDS)))
      there was one(cpService).apply(GetDiagnosticsReq(
        new Uri("ftp://example.org/uploaddir"),
        Some(expectedStartTime),
        Some(expectedStopTime),
        expectedRetries))
    }

    "call getDiagnostics with the right parameters if optional parameters are not specified" in new TestScope {
      cpService.apply(any[GetDiagnosticsReq]) returns GetDiagnosticsRes(Some("something.xml"))
      val xml = simpleDiagnosticsReqXML

      ChargePointDispatcherV15.dispatch(GetDiagnostics, xml, cpService)

      there was one(cpService).apply(GetDiagnosticsReq(new Uri("ftp://example.org/uploaddir"), None, None, Retries(None, None)))
    }

    "return upload URL reported by charge point service to client" in new TestScope {
      cpService.apply(any[GetDiagnosticsReq]) returns GetDiagnosticsRes(Some("test.xml"))
      val xml = simpleDiagnosticsReqXML

      val result = ChargePointDispatcherV15.dispatch(GetDiagnostics, xml, cpService)

      result must beResponse(GetDiagnosticsResponse(Some("test.xml")))
    }

    "return auth list version reported by charge point service to client" in new TestScope {
      cpService.apply(GetLocalListVersionReq) returns GetLocalListVersionRes(AuthListSupported(37))

      val xml = <GetLocalListVersion xmlns={v15PointNamespace}/>
      val body = ChargePointDispatcherV15.dispatch(GetLocalListVersion, xml, cpService)
      body must beResponse(GetLocalListVersionResponse(37))
    }

    "return auth list version -1 to client if service says local auth list is not supported" in new TestScope {
      cpService.apply(GetLocalListVersionReq) returns GetLocalListVersionRes(AuthListNotSupported)

      val xml = <GetLocalListVersion xmlns={v15PointNamespace}/>
      val body = ChargePointDispatcherV15.dispatch(GetLocalListVersion, xml, cpService)

      body must beResponse(GetLocalListVersionResponse(-1))
    }

    "call remoteStartTransaction with the right parameters" in new TestScope {
      val xml = <RemoteStartTransaction xmlns={v15PointNamespace}>
                  <idTag>fedcba</idTag>
                  <connectorId>3</connectorId>
                </RemoteStartTransaction>

      ChargePointDispatcherV15.dispatch(RemoteStartTransaction, xml, cpService) must throwAn[Exception]

      there was one(cpService).apply(RemoteStartTransactionReq("fedcba", Some(new ConnectorScope(2))))
    }

    "call remoteStartTransaction with the right parameters if connectorId is not specified" in new TestScope {
      val xml = <RemoteStartTransaction xmlns={v15PointNamespace}>
                  <idTag>123456</idTag>
                </RemoteStartTransaction>

      ChargePointDispatcherV15.dispatch(RemoteStartTransaction, xml, cpService) must throwAn[Exception]

      there was one(cpService).apply(RemoteStartTransactionReq("123456", None))
    }


    "return status of remote start transaction as reported by service to client" in new TestScope {
      cpService.apply(any[RemoteStartTransactionReq]) returns RemoteStartTransactionRes(true) thenReturns RemoteStartTransactionRes(false)

      val xml = <RemoteStartTransaction xmlns={v15PointNamespace}>
                  <idTag>abcdef</idTag>
                </RemoteStartTransaction>

      def dispatch = ChargePointDispatcherV15.dispatch(RemoteStartTransaction, xml, cpService)
      val result = (dispatch, dispatch)

      result._1 must beResponse(RemoteStartTransactionResponse(RemoteStartStopStatus.fromString("Accepted")))
      result._2 must beResponse(RemoteStartTransactionResponse(RemoteStartStopStatus.fromString("Rejected")))
    }

    "call remoteStopTransaction with the right parameters and return its status" in new TestScope {
      cpService.apply(RemoteStopTransactionReq(42)) returns RemoteStopTransactionRes(true)

      val xml = <RemoteStopTransaction xmlns={v15PointNamespace}>
                  <transactionId>42</transactionId>
                </RemoteStopTransaction>

      val body = ChargePointDispatcherV15.dispatch(RemoteStopTransaction, xml, cpService)

      body must beResponse(RemoteStopTransactionResponse(RemoteStartStopStatus.fromString("Accepted")))
    }

    "call reserveNow with the right parameters and return its status" in new TestScope {
      cpService.apply(ReserveNowReq(
        new ConnectorScope(0),
        new DateTime(2013, 4, 5, 6, 7, 0, DateTimeZone.UTC).toDateTime(DateTimeZone.getDefault()),
        "ababab", Some("acacac"), 42)) returns ReserveNowRes(Reservation.Occupied)

      val xml = <ReserveNow xmlns={v15PointNamespace}>
                  <connectorId>1</connectorId>
                  <expiryDate>2013-04-05T06:07:00Z</expiryDate>
                  <idTag>ababab</idTag>
                  <parentIdTag>acacac</parentIdTag>
                  <reservationId>42</reservationId>
                </ReserveNow>

      val body = ChargePointDispatcherV15.dispatch(ReserveNow, xml, cpService)

      body must beResponse(ReserveNowResponse(ReservationStatus.fromString("Occupied")))
    }

    "call reset with the right parameters and return its result" in new TestScope {
      cpService.apply(ResetReq(com.thenewmotion.ocpp.ResetType.Hard)) returns ResetRes(accepted = true)

      val xml = <Reset xmlns={v15PointNamespace}>
                  <type>Hard</type>
                </Reset>

      val body = ChargePointDispatcherV15.dispatch(Reset, xml, cpService)

      body must beResponse(ResetResponse(ResetStatus.fromString("Accepted")))
    }

    "call sendLocalList with the right parameters" in new TestScope {
      cpService.apply(any[SendLocalListReq]) returns SendLocalListRes(com.thenewmotion.ocpp.UpdateStatus.VersionMismatch)

      val xml = <SendLocalList xmlns={v15PointNamespace}>
                  <updateType>Differential</updateType>
                  <listVersion>233</listVersion>
                  <localAuthorisationList>
                    <idTag>ababab</idTag>
                  </localAuthorisationList>
                  <localAuthorisationList>
                    <idTag>acacac</idTag>
                    <idTagInfo>
                      <status>Blocked</status>
                      <expiryDate>2013-05-07T08:30:00Z</expiryDate>
                      <parentIdTag>adadad</parentIdTag>
                    </idTagInfo>
                  </localAuthorisationList>
                  <hash>0123456789abcdef</hash>
                </SendLocalList>

      ChargePointDispatcherV15.dispatch(SendLocalList, xml, cpService)

      val expectedExpiryDate = new DateTime(2013, 5, 7, 8, 30, 0, DateTimeZone.UTC).toDateTime(DateTimeZone.getDefault)
      val expectedAuthList =  List(AuthorisationRemove("ababab"),
                                   AuthorisationAdd("acacac",
                                                    ocpp.IdTagInfo(ocpp.AuthorizationStatus.IdTagBlocked,
                                                                   Some(expectedExpiryDate),
                                                                   Some("adadad"))))

      there was one(cpService).apply(SendLocalListReq(
        ocpp.UpdateType.Differential,
        AuthListSupported(233),
        expectedAuthList,
        Some("0123456789abcdef")))
    }

    "call SendLocalList with the right parameters if everything optional is left out" in new TestScope {
      cpService.apply(any[SendLocalListReq]) returns SendLocalListRes(com.thenewmotion.ocpp.UpdateStatus.VersionMismatch)

      ChargePointDispatcherV15.dispatch(SendLocalList, simpleSendLocalListXML, cpService)

      val expectedAuthList =  List(AuthorisationAdd("acacac",
                                   ocpp.IdTagInfo(ocpp.AuthorizationStatus.Accepted, None, None)))
      there was one(cpService).apply(SendLocalListReq(ocpp.UpdateType.Full, AuthListSupported(1), expectedAuthList, None))
    }

    "return the update status as reported by sendLocalList to the client" in new TestScope {
      import ocpp.UpdateStatus._

      cpService.apply(any[SendLocalListReq])
        .returns(SendLocalListRes(UpdateAccepted(Some("binary blaargh"))))
        .thenReturns(SendLocalListRes(UpdateFailed))
        .thenReturns(SendLocalListRes(HashError))
        .thenReturns(SendLocalListRes(NotSupportedValue))
        .thenReturns(SendLocalListRes(VersionMismatch))

      def dispatch = ChargePointDispatcherV15.dispatch(SendLocalList, simpleSendLocalListXML, cpService)
      val bodies = (dispatch, dispatch, dispatch, dispatch, dispatch)

      bodies._1 must beResponse(SendLocalListResponse(UpdateStatus.fromString("Accepted"), Some("binary blaargh")))
      bodies._2 must beResponse(SendLocalListResponse(UpdateStatus.fromString("Failed")))
      bodies._3 must beResponse(SendLocalListResponse(UpdateStatus.fromString("HashError")))
      bodies._4 must beResponse(SendLocalListResponse(UpdateStatus.fromString("NotSupported")))
      bodies._5 must beResponse(SendLocalListResponse(UpdateStatus.fromString("VersionMismatch")))
    }

    "call unlockConnector with the right parameters and return its result" in new TestScope {
      cpService.apply(UnlockConnectorReq(ConnectorScope(2))) returns UnlockConnectorRes(false)
      val xml = <UnlockConnector xmlns={v15PointNamespace}>
                  <connectorId>3</connectorId>
                </UnlockConnector>

      val body = ChargePointDispatcherV15.dispatch(UnlockConnector, xml, cpService)

      body must beResponse(UnlockConnectorResponse(UnlockStatus.fromString("Rejected")))
    }

    "call updateFirmware with the right parameters" in new TestScope {
      val xml = <UpdateFirmware xmlns={v15PointNamespace}>
                  <retrieveDate>2013-06-07T15:30:00+03:00</retrieveDate>
                  <location>ftp://example.org/exciting-new-stuff.tgz</location>
                  <retries>13</retries>
                  <retryInterval>42</retryInterval>
                </UpdateFirmware>

      ChargePointDispatcherV15.dispatch(UpdateFirmware, xml, cpService)

      val expectedRetrieveDate =
        new DateTime(2013, 6, 7, 12, 30, 0, DateTimeZone.UTC).toDateTime(DateTimeZone.getDefault())
      there was one(cpService).apply(UpdateFirmwareReq(
        expectedRetrieveDate,
        new Uri("ftp://example.org/exciting-new-stuff.tgz"),
        Retries(Some(13), Some(scala.concurrent.duration.Duration(42, SECONDS)))))
    }
  }
  
  private trait TestScope extends SpecsScope {
    val cpService = mock[ChargePoint]

    val v15PointNamespace = "urn://Ocpp/Cp/2012/06/"
    val simpleDiagnosticsReqXML = <GetDiagnostics xmlns={v15PointNamespace}>
                                    <location>ftp://example.org/uploaddir</location>
                                  </GetDiagnostics>

    val simpleSendLocalListXML = <SendLocalList xmlns={v15PointNamespace}>
                                   <updateType>Full</updateType>
                                   <listVersion>1</listVersion>
                                   <localAuthorisationList>
                                     <idTag>acacac</idTag>
                                     <idTagInfo>
                                       <status>Accepted</status>
                                     </idTagInfo>
                                   </localAuthorisationList>
                                 </SendLocalList>

    def beResponse(response: Any) = (be_==(1)        ^^ { (b: Body) => b.any.size }) and
                                    (be_==(response) ^^ { (b: Body) => b.any(0).value })
  }
}
