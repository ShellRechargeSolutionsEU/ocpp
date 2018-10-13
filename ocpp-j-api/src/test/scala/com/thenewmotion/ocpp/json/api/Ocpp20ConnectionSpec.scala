package com.thenewmotion.ocpp
package json
package api

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import org.json4s.JsonDSL._
import org.json4s.JsonAST.JObject
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import org.specs2.mock.Mockito
import messages.v20._

class Ocpp20ConnectionSpec extends Specification with Mockito {

  "DefaultOcppConnection" should {

    "handle incoming requests" in {

      "responding with a response message" in new TestScope {
        onRequest.apply(anyObject) returns RequestStartTransactionResponse(
          RequestStartStopStatus.Accepted,
          transactionId = None
        )

        val result = workingCsConnection.onSrpcCall(srpcRequestStartTransactionRequest)

        Await.result(result, 1.second) must beAnInstanceOf[SrpcCallResult]
      }

      "responding with the applicable error message if processing throws an OCPP error" in new TestScope {
        onRequest.apply(anyObject) throws new OcppException(OcppError(PayloadErrorCode.FormationViolation, "aargh!"))

        val result = workingCsConnection.onSrpcCall(srpcRequestStartTransactionRequest)

        Await.result(result, 1.second) must beAnInstanceOf[SrpcCallError]
      }

      "responding with an InternalError error message if processing throws an unexpected exception" in new TestScope {
        onRequest.apply(anyObject) throws new RuntimeException("bork")

        val result = workingCsConnection.onSrpcCall(srpcRequestStartTransactionRequest)

        Await.result(result, 1.second) mustEqual SrpcCallError(
          PayloadErrorCode.InternalError,
          "Unexpected error processing request",
          JObject(List())
        )
      }

      "responding with a NotImplemented error for unrecognized operations" in new TestScope {
        val result = workingCsConnection.onSrpcCall(srpcNonexistentOperationRequest)

        Await.result(result, 1.second) must beLike {
          case SrpcCallError(errCode, _, _) => errCode mustEqual PayloadErrorCode.NotImplemented
        }
      }

      "responding with a NotSupported error for recognized but unsupported operations" in new TestScope {
        val result = workingCsConnection.onSrpcCall(srpcNotSupportedOperationReq)

        Await.result(result, 1.second) must beLike {
          case SrpcCallError(errCode, _, _) => errCode mustEqual PayloadErrorCode.NotSupported
        }
      }.pendingUntilFixed("Not all OCPP 2.0 messages added yet")
    }

    "handle outgoing requests" in {

      "giving incoming responses back to the caller" in new TestScope {
        val srpcHeartbeatRes = SrpcCallResult("currentTime" -> "2014-03-31T14:00:00Z")
        workingCsConnection.srpcConnection.sendCall(srpcHeartbeatRequest) returns Future.successful(
          srpcHeartbeatRes
        )

        val result = workingCsConnection.ocppConnection.sendRequest(HeartbeatRequest())

        Await.result(result, 1.second) must beAnInstanceOf[HeartbeatResponse]
      }

      "returning a failed future when a request is responded to with an error message" in new TestScope {
        val errorRes = SrpcCallError(PayloadErrorCode.SecurityError, "Hee! Da mag nie!", "allowed" -> "no")
        workingCsConnection.srpcConnection.sendCall(srpcHeartbeatRequest) returns Future.successful(errorRes)

        val result = workingCsConnection.ocppConnection.sendRequest(HeartbeatRequest())

        val expectedError = OcppError(PayloadErrorCode.SecurityError, "Hee! Da mag nie!")

        implicit val executionContext = ExecutionContext.Implicits.global

        Await.result(
          result.failed.map(_.asInstanceOf[OcppException].ocppError),
          1.second
        ) must beEqualTo(expectedError)
      }

      "returning a failed future when the request cannot be sent" in new TestScope {
        workingCsConnection.srpcConnection.sendCall(srpcHeartbeatRequest) throws new RuntimeException("testfaal")

        val result = workingCsConnection.ocppConnection.sendRequest(HeartbeatRequest())

        Await.result(result.failed, 1.second) must beAnInstanceOf[Exception]
      }
    }
  }

  trait TestScope extends Scope {
    val srpcHeartbeatRequest =
      SrpcCall("Heartbeat", JObject(Nil))

    val srpcRequestStartTransactionRequest =
      SrpcCall(
        "RequestStartTransaction",
        ("idToken" -> (("idToken" -> "04EC2CC2552280") ~ ("type" -> "ISO14443"))) ~
          ("remoteStartId" -> 1337)
      )

    val srpcNonexistentOperationRequest =
      SrpcCall("WorldDomination", "complete" -> 1)

    val srpcNotSupportedOperationReq =
      SrpcCall(
        "DataTransfer",
        ("vendorId" -> "TheNewMotion") ~ ("messageId" -> "GaKoffieHalen") ~ ("data" -> "met suiker, zonder melk")
      )

    // workaround to prevent errors when making Mockito throw OcppException
    trait OcppRequestHandler extends (Request => Response) {
      @throws[OcppException]
      def apply(req: Request): Response
    }

    val onRequest = mock[OcppRequestHandler]

    sealed trait SrpcMockType
    case object Failing extends SrpcMockType
    case object FillingFuture extends SrpcMockType

    def csConnection(srpcConn: SrpcMockType) =
      new CsOcpp20ConnectionComponent with SrpcComponent {

        implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

        val srpcConnection = mock[SrpcConnection]

        def onRequest[REQ <: CsRequest, RES <: CsResponse](
          request: REQ
        )(
          implicit reqRes: CsReqRes[REQ, RES]
        ): Future[RES] = Future { TestScope.this.onRequest(request).asInstanceOf[RES] }

        def onSrpcDisconnect(): Unit = {}
      }

    val workingCsConnection = csConnection(FillingFuture)
    val failingCsConnection = csConnection(Failing)

    val csmsConnection = new CsmsOcpp20ConnectionComponent with SrpcComponent {

      implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

      val srpcConnection = mock[SrpcConnection]

      def onRequest[REQ <: CsmsRequest, RES <: CsmsResponse](
        request: REQ
      )(
        implicit reqRes: CsmsReqRes[REQ, RES]
      ): Future[RES] = Future { TestScope.this.onRequest(request).asInstanceOf[RES] }

      def onSrpcDisconnect(): Unit = {}
    }
  }
}

