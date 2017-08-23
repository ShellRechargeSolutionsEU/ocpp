package com.thenewmotion.ocpp
package json
package api

import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import org.specs2.mock.Mockito

import scala.concurrent.{Await, Future, Promise}
import org.json4s.JsonDSL._

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import messages._

class DefaultOcppConnectionSpec extends Specification with Mockito {

  "DefaultOcppConnection" should {

    "respond to requests with a response message" in new DefaultOcppConnectionScope {
      onRequest.apply(anyObject) returns RemoteStopTransactionRes(accepted = true)

      testConnection.onSrpcMessage(srpcRemoteStopTransactionReq)

      awaitFirstSentMessage must beAnInstanceOf[ResponseMessage]
    }

    "respond to requests with the same call ID" in new DefaultOcppConnectionScope {
      onRequest.apply(anyObject) returns RemoteStopTransactionRes(accepted = true)

      testConnection.onSrpcMessage(srpcRemoteStopTransactionReq)

      awaitFirstSentMessage must beLike {
        case ResponseMessage(callId, _) => callId mustEqual srpcRemoteStopTransactionReq.callId
      }
    }

    "respond to requests with the applicable error message if processing throws an OCPP error" in new DefaultOcppConnectionScope {
      onRequest.apply(anyObject) throws new OcppException(OcppError(PayloadErrorCode.FormationViolation, "aargh!"))

      testConnection.onSrpcMessage(srpcRemoteStopTransactionReq)

      awaitFirstSentMessage must beAnInstanceOf[ErrorResponseMessage]
    }

    "respond to requests with an InternalError error message if processing throws an unexpected exception" in new DefaultOcppConnectionScope {
      onRequest.apply(anyObject) throws new RuntimeException("bork")

      testConnection.onSrpcMessage(srpcRemoteStopTransactionReq)

      awaitFirstSentMessage must beLike {
        case ErrorResponseMessage(callId, errorCode, description, details) =>
          (callId, errorCode) mustEqual (srpcRemoteStopTransactionReq.callId, PayloadErrorCode.InternalError)
      }
    }

    "respond to requests for unrecognized operations with a NotImplemented error" in new DefaultOcppConnectionScope {
      testConnection.onSrpcMessage(srpcNonexistentOperationReq)

      awaitFirstSentMessage must beLike{
        case ErrorResponseMessage(callId, errorCode, description, details) =>
          (callId, errorCode) mustEqual (srpcNonexistentOperationReq.callId, PayloadErrorCode.NotImplemented)
      }
    }

    "respond to requests for recognized but unsupported operations with a NotSupported error" in new DefaultOcppConnectionScope {
      testConnection.onSrpcMessage(srpcNotSupportedOperationReq)

      awaitFirstSentMessage must beLike {
        case ErrorResponseMessage(callId, errorCode, description, details) =>
          (callId, errorCode) mustEqual (srpcNotSupportedOperationReq.callId, PayloadErrorCode.NotSupported)
      }
    }


    "give incoming responses back to the caller" in new DefaultOcppConnectionScope {
      val futureResponse = testConnection.ocppConnection.sendRequest(HeartbeatReq)

      val callId = awaitFirstSentMessage.asInstanceOf[RequestMessage].callId
      val srpcHeartbeatRes = ResponseMessage(callId, "currentTime" -> "2014-03-31T14:00:00Z")

      testConnection.onSrpcMessage(srpcHeartbeatRes)

      Await.result(futureResponse, FiniteDuration(1, "second")) must beAnInstanceOf[HeartbeatRes]
    }

    "return an error to the caller when a request is responded to with an error message" in new DefaultOcppConnectionScope {
      val futureResponse = testConnection.ocppConnection.sendRequest(HeartbeatReq)

      val callId = awaitFirstSentMessage.asInstanceOf[RequestMessage].callId
      val errorRes = ErrorResponseMessage(callId, PayloadErrorCode.SecurityError, "Hee! Da mag nie!", "allowed" -> "no")

      testConnection.onSrpcMessage(errorRes)

      val expectedError = OcppError(PayloadErrorCode.SecurityError, "Hee! Da mag nie!")
      val result = Await.result(futureResponse.failed, FiniteDuration(1, "seconds"))
      result.asInstanceOf[OcppException].ocppError mustEqual expectedError
    }

    "call error handler if an OCPP error comes in for no particular response" in new DefaultOcppConnectionScope {
      testConnection.onSrpcMessage(ErrorResponseMessage("not-before-seen", PayloadErrorCode.InternalError, "aargh!"))

      there was one(onError).apply(OcppError(PayloadErrorCode.InternalError, "aargh!"))
    }
  }

  private trait DefaultOcppConnectionScope extends Scope {
    val srpcRemoteStopTransactionReq =
      RequestMessage("test-call-id", "RemoteStopTransaction", "transactionId" -> 3)

    val srpcNonexistentOperationReq =
      RequestMessage("test-call-id", "WorldDomination", "complete" -> 1)

    val srpcNotSupportedOperationReq =
      RequestMessage("another-test-call-id", "DataTransfer",
        ("vendorId" -> "TheNewMotion") ~ ("messageId" -> "GaKoffieHalen") ~ ("data" -> "met suiker, zonder melk"))

    // workaround to prevent errors when making Mockito throw OcppException
    trait OcppRequestHandler extends (ChargePointReq => ChargePointRes) {
      @throws[OcppException]
      def apply(req: ChargePointReq): ChargePointRes
    }

    val onRequest = mock[OcppRequestHandler]
    val onError = mock[OcppError => Unit]

    val sentSrpcMessagePromise = Promise[TransportMessage]()
    val sentSrpcMessage: Future[TransportMessage] = sentSrpcMessagePromise.future

    def awaitFirstSentMessage: TransportMessage = Await.result(sentSrpcMessage, FiniteDuration(1, "second"))

    val testConnection = new ChargePointOcppConnectionComponent with SrpcComponent {
      val srpcConnection = new SrpcConnection {
        def send(msg: TransportMessage) = sentSrpcMessagePromise.success(msg)
      }
      val ocppConnection = new ChargePointOcppConnection

      def onRequest(request: ChargePointReq) = Future { DefaultOcppConnectionScope.this.onRequest(request) }
      def onOcppError(err: OcppError) = DefaultOcppConnectionScope.this.onError(err)
    }
  }
}
