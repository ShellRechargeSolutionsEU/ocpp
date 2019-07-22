package com.thenewmotion.ocpp
package json
package api

import java.time.ZonedDateTime
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods.{pretty, render}
import org.json4s.JsonAST.JObject
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import org.specs2.mock.Mockito
import messages.v1x._

class Ocpp1XConnectionSpec extends Specification with Mockito {

  "DefaultOcppConnection" should {

    "handle incoming requests" in {

      "responding with a response message" in new TestScope {
        onRequest.apply(anyObject) returns RemoteStopTransactionRes(accepted = true)

        val result = chargePointConnectionV16.onSrpcCall(srpcRemoteStopTransactionReq)

        Await.result(result, 1.second) must beAnInstanceOf[SrpcCallResult]
      }

      "responding with the applicable error message if processing throws an OCPP error" in new TestScope {
        onRequest.apply(anyObject) throws new OcppException(OcppError(PayloadErrorCode.FormationViolation, "aargh!"))

        val result = chargePointConnectionV16.onSrpcCall(srpcRemoteStopTransactionReq)

        Await.result(result, 1.second) must beAnInstanceOf[SrpcCallError]
      }

      "responding with an InternalError error message if processing throws an unexpected exception" in new TestScope {
        onRequest.apply(anyObject) throws new RuntimeException("bork")

        val result = chargePointConnectionV16.onSrpcCall(srpcRemoteStopTransactionReq)

        Await.result(result, 1.second) mustEqual SrpcCallError(
          PayloadErrorCode.InternalError,
          "Unexpected error processing request",
          JObject(List())
        )
      }

      "responding with a NotImplemented error for unrecognized operations" in new TestScope {
        val result = chargePointConnectionV16.onSrpcCall(srpcNonexistentOperationReq)

        Await.result(result, 1.second) must beLike {
          case SrpcCallError(errCode, _, _) => errCode mustEqual PayloadErrorCode.NotImplemented
        }
      }

      "responding with a NotSupported error for requests for the wrong version of OCPP" in new TestScope {
        val result = chargePointConnectionV15.onSrpcCall(srpcTriggerMessageReq)

        Await.result(result, 1.second) must beLike {
          case e@SrpcCallError(errCode, _, _) => errCode mustEqual PayloadErrorCode.NotSupported
        }
      }
    }

    "handle outgoing requests" in {

      "giving incoming responses back to the caller" in new TestScope {
        val srpcHeartbeatRes = SrpcCallResult("currentTime" -> "2014-03-31T14:00:00Z")
        chargePointConnectionV16.srpcConnection.sendCall(srpcHeartbeatReq) returns Future.successful(
          srpcHeartbeatRes
        )

        val result = chargePointConnectionV16.ocppConnection.sendRequest(HeartbeatReq)

        Await.result(result, 1.second) must beAnInstanceOf[HeartbeatRes]
      }

      "returning a failed future when a request is responded to with an error message" in new TestScope {
        val errorRes = SrpcCallError(PayloadErrorCode.SecurityError, "Hee! Da mag nie!", "allowed" -> "no")
        chargePointConnectionV16.srpcConnection.sendCall(srpcHeartbeatReq) returns Future.successful(errorRes)

        val result = chargePointConnectionV16.ocppConnection.sendRequest(HeartbeatReq)

        val expectedError = OcppError(PayloadErrorCode.SecurityError, "Hee! Da mag nie!")

        implicit val executionContext = ExecutionContext.Implicits.global

        Await.result(
          result.failed.map(_.asInstanceOf[OcppException].ocppError),
          1.second
        ) must beEqualTo(expectedError)
      }

      "returning a failed future when a request is sent for the wrong version of OCPP" in new TestScope {
        val v16Message = TriggerMessageReq(MessageTriggerWithoutScope.Heartbeat)

        val result = centralSystemConnectionV15.ocppConnection.sendRequest(v16Message)

        Await.result(result.failed, 1.second) must beLike {
          case err: OcppException =>
            err.ocppError.error mustEqual PayloadErrorCode.NotSupported
        }
      }

      "returning a failed future when the request cannot be sent" in new TestScope {
          chargePointConnectionV16.srpcConnection.sendCall(srpcHeartbeatReq) throws new RuntimeException("testfaal")

          val result = chargePointConnectionV16.ocppConnection.sendRequest(HeartbeatReq)

          Await.result(result.failed, 1.second) must beAnInstanceOf[Exception]
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

        chargePointConnectionV15.srpcConnection.sendCall(any[SrpcCall]()) returns Future.failed(
          new RuntimeException("no successful result needed")
        )

        chargePointConnectionV15.ocppConnection.sendRequest(statusReq)

        val cap = capture[SrpcCall]
        there was one(chargePointConnectionV15.srpcConnection).sendCall(cap.capture)

        cap.value must beLike {
          case srpcRequest: SrpcCall =>
            val payloadText = pretty(render(srpcRequest.payload))
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

        chargePointConnectionV16.srpcConnection.sendCall(any[SrpcCall]()) returns Future.failed(
          new RuntimeException("no successful result needed")
        )

        chargePointConnectionV16.ocppConnection.sendRequest(statusReq)

        val cap = capture[SrpcCall]
        there was one(chargePointConnectionV16.srpcConnection).sendCall(cap.capture)

        cap.value must beLike {
          case srpcRequest: SrpcCall =>
            val payloadText = pretty(render(srpcRequest.payload))
            payloadText must not contain "Occupied"
            payloadText must contain("Charging")
        }
      }
    }
  }

  trait TestScope extends Scope {
    val srpcHeartbeatReq =
      SrpcCall("Heartbeat", JObject(Nil))

    val srpcRemoteStopTransactionReq =
      SrpcCall("RemoteStopTransaction", "transactionId" -> 3)

    val srpcNonexistentOperationReq =
      SrpcCall("WorldDomination", "complete" -> 1)

    val srpcTriggerMessageReq =
      SrpcCall("TriggerMessage", "requestMessage" -> "Heartbeat")

    // workaround to prevent errors when making Mockito throw OcppException
    trait OcppRequestHandler extends (Req => Res) {
      @throws[OcppException]
      def apply(req: Req): Res
    }

    val onRequest = mock[OcppRequestHandler]

    sealed trait SrpcMockType
    case object Failing extends SrpcMockType
    case object FillingFuture extends SrpcMockType

    def chargePointConnection(version: Version, srpcConn: SrpcMockType) =
      new ChargePointOcpp1XConnectionComponent with SrpcComponent {

        implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

        val srpcConnection = mock[SrpcConnection]

        val ocppVersion = version

        val ocppConnection = defaultChargePointOcppConnection

        def onRequest[REQ <: ChargePointReq, RES <: ChargePointRes](
          request: REQ
        )(
          implicit reqRes: ChargePointReqRes[REQ, RES]
        ): Future[RES] = Future { TestScope.this.onRequest(request).asInstanceOf[RES] }

        def onSrpcDisconnect(): Unit = {}
      }

    val chargePointConnectionV15 = chargePointConnection(Version.V15, FillingFuture)
    val chargePointConnectionV16 = chargePointConnection(Version.V16, FillingFuture)
    val failingChargePointConnection = chargePointConnection(Version.V16, Failing)

    val centralSystemConnectionV15 = new CentralSystemOcpp1XConnectionComponent with SrpcComponent {

      implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

      val srpcConnection = mock[SrpcConnection]

      val ocppVersion = Version.V15

      val ocppConnection = defaultCentralSystemOcppConnection

      def onRequest[REQ <: CentralSystemReq, RES <: CentralSystemRes](
        request: REQ
      )(
        implicit reqRes: CentralSystemReqRes[REQ, RES]
      ): Future[RES] = Future { TestScope.this.onRequest(request).asInstanceOf[RES] }

      def onSrpcDisconnect(): Unit = {}
    }
  }
}
