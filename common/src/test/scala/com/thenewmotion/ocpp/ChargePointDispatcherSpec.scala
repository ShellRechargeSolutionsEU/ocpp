package com.thenewmotion
package ocpp

import org.specs2.specification.{Scope => SpecsScope}
import soapenvelope12.Body
import com.thenewmotion.time.Imports._
import scala.concurrent.duration._

import org.specs2.mock.Mockito
import org.specs2.mutable.SpecificationWithJUnit
import ChargePointAction._

class ChargePointDispatcherSpec extends SpecificationWithJUnit with Mockito {

  "ChargePointDispatcherV15" should {
    import v15.{ChargePointService => _, AvailabilityStatus => _, AvailabilityType => _, _}

    "call cancelReservation and return its false result" in new TestScope {
      cpService.cancelReservation(13) returns false

      val xml = <CancelReservation xmlns={v15PointNamespace}><reservationId>13</reservationId></CancelReservation>
      val body = (new ChargePointDispatcherV15).dispatch(CancelReservation, xml, cpService)

      body must beResponse(CancelReservationResponse(CancelReservationStatus.fromString("Rejected")))
    }

    "call cancelReservation and return its true result" in new TestScope {
      cpService.cancelReservation(4) returns true

      val xml = <CancelReservation xmlns={v15PointNamespace}><reservationId>4</reservationId></CancelReservation>
      val body = (new ChargePointDispatcherV15).dispatch(CancelReservation, xml, cpService)

      body must beResponse(CancelReservationResponse(CancelReservationStatus.fromString("Accepted")))
    }

    "call changeAvailability with charge point scope if connector is 0" in new TestScope {
      cpService.changeAvailability(any, any) returns AvailabilityStatus.Accepted

      val xml = <ChangeAvailability xmlns={v15PointNamespace}>
                  <connectorId>0</connectorId>
                  <type>Operative</type>
                </ChangeAvailability>
      new (ChargePointDispatcherV15).dispatch(ChangeAvailability, xml, cpService)

      there was one(cpService).changeAvailability(ChargePointScope, AvailabilityType.Operative)
    }

    "return result of changeAvailability in the right way" in new TestScope {
      cpService.changeAvailability(any, any).returns(AvailabilityStatus.Accepted)
                                            .thenReturns(AvailabilityStatus.Scheduled)
                                            .thenReturns(AvailabilityStatus.Rejected)

      val xml = <ChangeAvailability xmlns={v15PointNamespace}>
                  <connectorId>0</connectorId>
                  <type>Operative</type>
                </ChangeAvailability>
      val dispatcher = new ChargePointDispatcherV15
      def dispatchChangeAv: Body =  dispatcher.dispatch(ChangeAvailability, xml, cpService)
      val result = (dispatchChangeAv, dispatchChangeAv, dispatchChangeAv)

      result._1 must beResponse(ChangeAvailabilityResponse(v15.AvailabilityStatus.fromString("Accepted")))
      result._2 must beResponse(ChangeAvailabilityResponse(v15.AvailabilityStatus.fromString("Scheduled")))
      result._3 must beResponse(ChangeAvailabilityResponse(v15.AvailabilityStatus.fromString("Rejected")))
    }

    "call changeConfiguration with the right parameters" in new TestScope {
      cpService.changeConfiguration(any, any) returns ocpp.ConfigurationStatus.Accepted

      val xml = <ChangeConfiguration xmlns={v15PointNamespace}>
                  <key>aap</key><value>noot</value>
                </ChangeConfiguration>
      (new ChargePointDispatcherV15).dispatch(ChangeConfiguration, xml, cpService)

      there was one(cpService).changeConfiguration("aap", "noot")
    }

    "return values from changeConfiguration" in new TestScope {
      cpService.changeConfiguration(any, any).returns(ocpp.ConfigurationStatus.Accepted)
                                             .thenReturns(ocpp.ConfigurationStatus.Rejected)
                                             .thenReturns(ocpp.ConfigurationStatus.NotSupported)

      val xml = <ChangeConfiguration xmlns={v15PointNamespace}>
        <key>aap</key><value>noot</value>
      </ChangeConfiguration>
      val dispatcher = new ChargePointDispatcherV15
      def dispatch = dispatcher.dispatch(ChangeConfiguration, xml, cpService)
      val result = (dispatch, dispatch, dispatch)

      result._1 must beResponse(ChangeConfigurationResponse(ConfigurationStatus.fromString("Accepted")))
      result._2 must beResponse(ChangeConfigurationResponse(ConfigurationStatus.fromString("Rejected")))
      result._3 must beResponse(ChangeConfigurationResponse(ConfigurationStatus.fromString("NotSupported")))
    }

    "call clearCache and return its result" in new TestScope {
      cpService.clearCache returns true thenReturns false

      val xml = <ClearCache xmlns={v15PointNamespace}/>
      val dispatcher = new ChargePointDispatcherV15
      def dispatch = dispatcher.dispatch(ClearCache, xml, cpService)
      val result = (dispatch, dispatch)

      result._1 must beResponse(ClearCacheResponse(ClearCacheStatus.fromString("Accepted")))
      result._2 must beResponse(ClearCacheResponse(ClearCacheStatus.fromString("Rejected")))
    }

    "call getConfiguration with the right parameters" in new TestScope {
      cpService.getConfiguration(any) returns Configuration(List.empty, List.empty)

      val xml = <GetConfiguration xmlns={v15PointNamespace}>
                  <key>sleutel</key><key>Schlüssel</key><key>clef</key>
                </GetConfiguration>
      (new ChargePointDispatcherV15).dispatch(GetConfiguration, xml, cpService)

      there was one(cpService).getConfiguration(List("sleutel", "Schlüssel", "clef"))
    }

    "return the result of getConfiguration" in new TestScope {
      cpService.getConfiguration(any) returns Configuration(List(ocpp.KeyValue("aap", readonly = false, None),
                                                                 ocpp.KeyValue("noot", readonly = true, Some("mies"))),
                                                            List("boom", "roos", "vis"))

      val xml = <GetConfiguration/>
      val result = (new ChargePointDispatcherV15).dispatch(GetConfiguration, xml, cpService)

      result must beResponse(GetConfigurationResponse(List(v15.KeyValue("aap", readonly = false, None),
                                                           v15.KeyValue("noot", readonly = true, Some("mies"))),
                                                      List("boom", "roos", "vis")))
    }

    "call getDiagnostics with the right parameters"  in new TestScope {
      cpService.getDiagnostics(any, any, any, any) returns Some("aargh.xml")

      val xml = <GetDiagnostics xmlns={v15PointNamespace}>
                  <location>ftp://example.org/uploaddir</location>
                  <startTime>2013-06-04T13:30:00Z</startTime>
                  <stopTime>2013-06-04T14:30:00Z</stopTime>
                  <retries>4</retries>
                  <retryInterval>90</retryInterval>
                </GetDiagnostics>

      (new ChargePointDispatcherV15).dispatch(GetDiagnostics, xml, cpService)

      val expectedStartTime = new DateTime(2013, 6, 4, 13, 30, DateTimeZone.UTC).toDateTime(DateTimeZone.getDefault)
      val expectedStopTime  = new DateTime(2013, 6, 4, 14, 30, DateTimeZone.UTC).toDateTime(DateTimeZone.getDefault)
      val expectedRetries   = Retries(Some(4), Some(scala.concurrent.duration.Duration(90, SECONDS)))
      there was one(cpService).getDiagnostics(new Uri("ftp://example.org/uploaddir"),
                                              Some(expectedStartTime),
                                              Some(expectedStopTime),
                                              expectedRetries)
    }

    "call getDiagnostics with the right parameters if optional parameters are not specified" in new TestScope {
      cpService.getDiagnostics(any, any, any, any) returns Some("something.xml")
      val xml = simpleDiagnosticsReqXML

      (new ChargePointDispatcherV15).dispatch(GetDiagnostics, xml, cpService)

      there was one(cpService).getDiagnostics(new Uri("ftp://example.org/uploaddir"), None, None, Retries(None, None))
    }

    "return upload URL reported by charge point service to client" in new TestScope {
      cpService.getDiagnostics(any, any, any, any) returns Some("test.xml")
      val xml = simpleDiagnosticsReqXML

      val result = (new ChargePointDispatcherV15).dispatch(GetDiagnostics, xml, cpService)

      result must beResponse(GetDiagnosticsResponse(Some("test.xml")))
    }

    "return auth list version reported by charge point service to client" in new TestScope {
      cpService.getLocalListVersion returns AuthListSupported(37)

      val xml = <GetLocalListVersion xmlns={v15PointNamespace}/>
      val body = (new ChargePointDispatcherV15).dispatch(GetLocalListVersion, xml, cpService)
      body must beResponse(GetLocalListVersionResponse(37))
    }

    "return auth list version -1 to client if service says local auth list is not supported" in new TestScope {
      cpService.getLocalListVersion returns AuthListNotSupported

      val dispatcher = new ChargePointDispatcherV15

      val xml = <GetLocalListVersion xmlns={v15PointNamespace}/>
      val body = dispatcher.dispatch(GetLocalListVersion, xml, cpService)

      body must beResponse(GetLocalListVersionResponse(-1))
    }

    "call remoteStartTransaction with the right parameters" in new TestScope {
      val xml = <RemoteStartTransaction xmlns={v15PointNamespace}>
                  <idTag>fedcba</idTag>
                  <connectorId>3</connectorId>
                </RemoteStartTransaction>

      (new ChargePointDispatcherV15).dispatch(RemoteStartTransaction, xml, cpService)

      there was one(cpService).remoteStartTransaction("fedcba", Some(new ConnectorScope(2)))
    }

    "call remoteStartTransaction with the right parameters if connectorId is not specified" in new TestScope {
      val xml = <RemoteStartTransaction xmlns={v15PointNamespace}>
                  <idTag>123456</idTag>
                </RemoteStartTransaction>

      (new ChargePointDispatcherV15).dispatch(RemoteStartTransaction, xml, cpService)

      there was one(cpService).remoteStartTransaction("123456", None)
    }


    "return status of remote start transaction as reported by service to client" in new TestScope {
      cpService.remoteStartTransaction(any, any) returns true thenReturns false

      val xml = <RemoteStartTransaction xmlns={v15PointNamespace}>
                  <idTag>abcdef</idTag>
                </RemoteStartTransaction>

      val dispatcher = new ChargePointDispatcherV15
      def dispatch = (new ChargePointDispatcherV15).dispatch(RemoteStartTransaction, xml, cpService)
      val result = (dispatch, dispatch)

      result._1 must beResponse(RemoteStartTransactionResponse(RemoteStartStopStatus.fromString("Accepted")))
      result._2 must beResponse(RemoteStartTransactionResponse(RemoteStartStopStatus.fromString("Rejected")))
    }

    "call remoteStopTransaction with the right parameters and return its status" in new TestScope {
      cpService.remoteStopTransaction(42) returns true

      val xml = <RemoteStopTransaction xmlns={v15PointNamespace}>
                  <transactionId>42</transactionId>
                </RemoteStopTransaction>

      val body = (new ChargePointDispatcherV15).dispatch(RemoteStopTransaction, xml, cpService)

      body must beResponse(RemoteStopTransactionResponse(RemoteStartStopStatus.fromString("Accepted")))
    }

    "call reserveNow with the right parameters and return its status" in new TestScope {
      cpService.reserveNow(new ConnectorScope(0),
                           new DateTime(2013, 4, 5, 6, 7, 0, DateTimeZone.UTC).toDateTime(DateTimeZone.getDefault()),
                           "ababab", Some("acacac"), 42) returns Reservation.Occupied

      val xml = <ReserveNow xmlns={v15PointNamespace}>
                  <connectorId>1</connectorId>
                  <expiryDate>2013-04-05T06:07:00Z</expiryDate>
                  <idTag>ababab</idTag>
                  <parentIdTag>acacac</parentIdTag>
                  <reservationId>42</reservationId>
                </ReserveNow>

      val body = (new ChargePointDispatcherV15).dispatch(ReserveNow, xml, cpService)

      body must beResponse(ReserveNowResponse(ReservationStatus.fromString("Occupied")))
    }

    "call reset with the right parameters and return its result" in new TestScope {
      cpService.reset(com.thenewmotion.ocpp.ResetType.Hard) returns true

      val xml = <Reset xmlns={v15PointNamespace}>
                  <type>Hard</type>
                </Reset>

      val body = (new ChargePointDispatcherV15).dispatch(Reset, xml, cpService)

      body must beResponse(ResetResponse(ResetStatus.fromString("Accepted")))
    }

    "call sendLocalList with the right parameters" in new TestScope {
      cpService.sendLocalList(any, any, any, any) returns com.thenewmotion.ocpp.UpdateStatus.VersionMismatch

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

      (new ChargePointDispatcherV15).dispatch(SendLocalList, xml, cpService)

      val expectedExpiryDate = new DateTime(2013, 5, 7, 8, 30, 0, DateTimeZone.UTC).toDateTime(DateTimeZone.getDefault)
      val expectedAuthList =  List(AuthorisationRemove("ababab"),
                                   AuthorisationAdd("acacac",
                                                    ocpp.IdTagInfo(ocpp.AuthorizationStatus.IdTagBlocked,
                                                                   Some(expectedExpiryDate),
                                                                   Some("adadad"))))

      there was one(cpService).sendLocalList(ocpp.UpdateType.Differential,
                                             AuthListSupported(233),
                                             expectedAuthList,
                                             Some("0123456789abcdef"))
    }

    "call SendLocalList with the right parameters if everything optional is left out" in new TestScope {
      cpService.sendLocalList(any, any, any, any) returns com.thenewmotion.ocpp.UpdateStatus.VersionMismatch

      (new ChargePointDispatcherV15).dispatch(SendLocalList, simpleSendLocalListXML, cpService)

      val expectedAuthList =  List(AuthorisationAdd("acacac",
                                   ocpp.IdTagInfo(ocpp.AuthorizationStatus.Accepted, None, None)))
      there was one(cpService).sendLocalList(ocpp.UpdateType.Full, AuthListSupported(1), expectedAuthList, None)
    }

    "return the update status as reported by sendLocalList to the client" in new TestScope {
      import ocpp.UpdateStatus._
      cpService.sendLocalList(any, any, any, any).returns(UpdateAccepted(Some("binary blaargh")))
                                                 .thenReturns(UpdateFailed)
                                                 .thenReturns(HashError)
                                                 .thenReturns(NotSupportedValue)
                                                 .thenReturns(VersionMismatch)

      val dispatcher = new ChargePointDispatcherV15
      def dispatch = dispatcher.dispatch(SendLocalList, simpleSendLocalListXML, cpService)
      val bodies = (dispatch, dispatch, dispatch, dispatch, dispatch)

      bodies._1 must beResponse(SendLocalListResponse(UpdateStatus.fromString("Accepted"), Some("binary blaargh")))
      bodies._2 must beResponse(SendLocalListResponse(UpdateStatus.fromString("Failed")))
      bodies._3 must beResponse(SendLocalListResponse(UpdateStatus.fromString("HashError")))
      bodies._4 must beResponse(SendLocalListResponse(UpdateStatus.fromString("NotSupported")))
      bodies._5 must beResponse(SendLocalListResponse(UpdateStatus.fromString("VersionMismatch")))
    }

    "call unlockConnector with the right parameters and return its result" in new TestScope {
      cpService.unlockConnector(ConnectorScope(2)) returns false
      val xml = <UnlockConnector xmlns={v15PointNamespace}>
                  <connectorId>3</connectorId>
                </UnlockConnector>

      val body = (new ChargePointDispatcherV15).dispatch(UnlockConnector, xml, cpService)

      body must beResponse(UnlockConnectorResponse(UnlockStatus.fromString("Rejected")))
    }

    "call updateFirmware with the right parameters" in new TestScope {
      val xml = <UpdateFirmware xmlns={v15PointNamespace}>
                  <retrieveDate>2013-06-07T15:30:00+03:00</retrieveDate>
                  <location>ftp://example.org/exciting-new-stuff.tgz</location>
                  <retries>13</retries>
                  <retryInterval>42</retryInterval>
                </UpdateFirmware>

      (new ChargePointDispatcherV15).dispatch(UpdateFirmware, xml, cpService)

      val expectedRetrieveDate =
        new DateTime(2013, 6, 7, 12, 30, 0, DateTimeZone.UTC).toDateTime(DateTimeZone.getDefault)
      there was one(cpService).updateFirmware(expectedRetrieveDate,
                                              new Uri("ftp://example.org/exciting-new-stuff.tgz"),
                                              Retries(Some(13), Some(scala.concurrent.duration.Duration(42, SECONDS))))
    }
  }
  
  private trait TestScope extends SpecsScope {
    val cpService = mock[ChargePointService]

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
