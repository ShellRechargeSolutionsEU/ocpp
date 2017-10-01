package com.thenewmotion.ocpp
package json
package api

import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import org.specs2.mock.Mockito
import scala.concurrent.{Await, Future, Promise}
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods.{render, pretty}
import java.time.ZonedDateTime
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import messages._

class DefaultOcppConnectionSpec extends Specification with Mockito {

  "DefaultOcppConnection" should {

    "handle incoming requests" in {

      "responding with a response message" in new TestScope {
        onRequest.apply(anyObject) returns RemoteStopTransactionRes(accepted = true)

        chargePointConnectionV16.onSrpcMessage(srpcRemoteStopTransactionReq)

        awaitFirstSentMessage must beAnInstanceOf[ResponseMessage]
      }

      "responding with the same call ID" in new TestScope {
        onRequest.apply(anyObject) returns RemoteStopTransactionRes(accepted = true)

        chargePointConnectionV16.onSrpcMessage(srpcRemoteStopTransactionReq)

        awaitFirstSentMessage must beLike {
          case ResponseMessage(callId, _) => callId mustEqual srpcRemoteStopTransactionReq.callId
        }
      }

      "responding with the applicable error message if processing throws an OCPP error" in new TestScope {
        onRequest.apply(anyObject) throws new OcppException(OcppError(PayloadErrorCode.FormationViolation, "aargh!"))

        chargePointConnectionV16.onSrpcMessage(srpcRemoteStopTransactionReq)

        awaitFirstSentMessage must beAnInstanceOf[ErrorResponseMessage]
      }

      "responding with an InternalError error message if processing throws an unexpected exception" in new TestScope {
        onRequest.apply(anyObject) throws new RuntimeException("bork")

        chargePointConnectionV16.onSrpcMessage(srpcRemoteStopTransactionReq)

        awaitFirstSentMessage must beLike {
          case ErrorResponseMessage(callId, errorCode, description, details) =>
            (callId, errorCode) mustEqual (srpcRemoteStopTransactionReq.callId -> PayloadErrorCode.InternalError)
        }
      }

      "responding with a NotImplemented error for unrecognized operations" in new TestScope {
        chargePointConnectionV16.onSrpcMessage(srpcNonexistentOperationReq)

        awaitFirstSentMessage must beLike {
          case ErrorResponseMessage(callId, errorCode, description, details) =>
            (callId, errorCode) mustEqual (srpcNonexistentOperationReq.callId -> PayloadErrorCode.NotImplemented)
        }
      }

      "responding with a NotSupported error for recognized but unsupported operations" in new TestScope {
        chargePointConnectionV16.onSrpcMessage(srpcNotSupportedOperationReq)

        awaitFirstSentMessage must beLike {
          case ErrorResponseMessage(callId, errorCode, description, details) =>
            (callId, errorCode) mustEqual (srpcNotSupportedOperationReq.callId -> PayloadErrorCode.NotSupported)
        }
      }
    }

    "handle outgoing requests" in {

      "giving incoming responses back to the caller" in new TestScope {
        val futureResponse = chargePointConnectionV16.ocppConnection.sendRequest(HeartbeatReq)

        val callId = awaitFirstSentMessage.asInstanceOf[RequestMessage].callId
        val srpcHeartbeatRes = ResponseMessage(callId, "currentTime" -> "2014-03-31T14:00:00Z")

        chargePointConnectionV16.onSrpcMessage(srpcHeartbeatRes)

        Await.result(futureResponse, FiniteDuration(1, "second")) must beAnInstanceOf[HeartbeatRes]
      }

      "returning an error to the caller when a request is responded to with an error message" in new TestScope {
        val futureResponse = chargePointConnectionV16.ocppConnection.sendRequest(HeartbeatReq)

        val callId = awaitFirstSentMessage.asInstanceOf[RequestMessage].callId
        val errorRes = ErrorResponseMessage(callId, PayloadErrorCode.SecurityError, "Hee! Da mag nie!", "allowed" -> "no")

        chargePointConnectionV16.onSrpcMessage(errorRes)

        val expectedError = OcppError(PayloadErrorCode.SecurityError, "Hee! Da mag nie!")
        val result = Await.result(futureResponse.failed, FiniteDuration(1, "seconds"))
        result.asInstanceOf[OcppException].ocppError mustEqual expectedError
      }

      "returnning an error to the caller when tries to send a request for the wrong verson of OCPP" in new TestScope {
        val v16Message = TriggerMessageReq(MessageTriggerWithoutScope.Heartbeat)

        val futureResponse = centralSystemConnectionV15.ocppConnection.sendRequest(v16Message)

        Await.result(futureResponse.failed, 1.seconds) must beLike {
          case err: OcppException =>
            err.ocppError.error mustEqual PayloadErrorCode.NotSupported
        }
      }

      "calling error handler if an OCPP error comes in for no particular request" in new TestScope {
        chargePointConnectionV16.onSrpcMessage(ErrorResponseMessage("not-before-seen", PayloadErrorCode.InternalError, "aargh!"))

        there was one(onError).apply(OcppError(PayloadErrorCode.InternalError, "aargh!"))
      }

      "serializing messages in 1.5 format for 1.5 connections" in new TestScope {
        val statusReq = StatusNotificationReq(
          scope = ConnectorScope(0),
          status = ChargePointStatus.Occupied(
            Some(OccupancyKind.Charging),
            info = None
          ),
          timestamp = Some(ZonedDateTime.now()),
          vendorId = None
        )

        chargePointConnectionV15.ocppConnection.sendRequest(statusReq)
        awaitFirstSentMessage must beLike {
          case transportLevelRequest: RequestMessage =>
            val payloadText = pretty(render(transportLevelRequest.payload))
            payloadText must contain("Occupied")
            payloadText must not contain "Charging"
        }
      }

      "serializing messages in 1.6 format for 1.6 connections" in new TestScope {
        val statusReq = StatusNotificationReq(
          scope = ConnectorScope(0),
          status = ChargePointStatus.Occupied(
            Some(OccupancyKind.Charging),
            info = None
          ),
          timestamp = Some(ZonedDateTime.now()),
          vendorId = None
        )

        chargePointConnectionV16.ocppConnection.sendRequest(statusReq)
        awaitFirstSentMessage must beLike {
          case transportLevelRequest: RequestMessage =>
            val payloadText = pretty(render(transportLevelRequest.payload))
            payloadText must not contain "Occupied"
            payloadText must contain("Charging")
        }
      }
    }

    "translate OCPP versions to WebSocket subprotocol identifiers" in new TestScope {
      chargePointConnectionV15.requestedSubProtocols mustEqual List("ocpp1.5")
      chargePointConnectionV16.requestedSubProtocols mustEqual List("ocpp1.6")
    }
  }

  private trait TestScope extends Scope {
    val srpcRemoteStopTransactionReq =
      RequestMessage("test-call-id", "RemoteStopTransaction", "transactionId" -> 3)

    val srpcNonexistentOperationReq =
      RequestMessage("test-call-id", "WorldDomination", "complete" -> 1)

    val srpcNotSupportedOperationReq =
      RequestMessage("another-test-call-id", "DataTransfer",
        ("vendorId" -> "TheNewMotion") ~ ("messageId" -> "GaKoffieHalen") ~ ("data" -> "met suiker, zonder melk"))

    // workaround to prevent errors when making Mockito throw OcppException
    trait OcppRequestHandler extends (Req => Res) {
      @throws[OcppException]
      def apply(req: Req): Res
    }

    val onRequest = mock[OcppRequestHandler]
    val onError = mock[OcppError => Unit]

    val sentSrpcMessagePromise = Promise[TransportMessage]()
    val sentSrpcMessage: Future[TransportMessage] = sentSrpcMessagePromise.future

    def awaitFirstSentMessage: TransportMessage = Await.result(sentSrpcMessage, FiniteDuration(1, "second"))

    def chargePointConnection(version: Version) = new ChargePointOcppConnectionComponent with SrpcComponent {
      val srpcConnection = new SrpcConnection {
        def send(msg: TransportMessage) = {
          sentSrpcMessagePromise.success(msg)
          ()
        }
      }
      val ocppConnection = defaultChargePointOcppConnection(version)

      def onRequest[REQ <: ChargePointReq, RES <: ChargePointRes](
        request: REQ
      )(
        implicit reqRes: ChargePointReqRes[REQ, RES]
      ): Future[RES] = Future { TestScope.this.onRequest(request).asInstanceOf[RES] }

      def onOcppError(err: OcppError) = TestScope.this.onError(err)

      def requestedVersions: List[Version] = List(version)
    }

    val chargePointConnectionV15 = chargePointConnection(Version.V15)
    val chargePointConnectionV16 = chargePointConnection(Version.V16)

    val centralSystemConnectionV15 = new CentralSystemOcppConnectionComponent with SrpcComponent {

      def srpcConnection = new SrpcConnection {
        def send(msg: TransportMessage) = {
          sentSrpcMessagePromise.success(msg)
          ()
        }
      }

      val ocppConnection = defaultCentralSystemOcppConnection(Version.V15)

      def onRequest[REQ <: CentralSystemReq, RES <: CentralSystemRes](
        request: REQ
      )(
        implicit reqRes: CentralSystemReqRes[REQ, RES]
      ): Future[RES] = Future { TestScope.this.onRequest(request).asInstanceOf[RES] }

      def onOcppError(err: OcppError) = TestScope.this.onError(err)

      def requestedVersions: List[Version] = List(Version.V15)
    }
  }
}
