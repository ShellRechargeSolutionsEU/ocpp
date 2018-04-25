package com.thenewmotion.ocpp
package json
package api

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future, Promise}
import org.json4s.JValue
import org.json4s.JsonAST.{JArray, JInt, JObject, JString}
import org.json4s.native.{JsonMethods, JsonParser}
import JsonMethods.{compact, render}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

class DefaultSrpcConnectionSpec extends Specification with Mockito {

  "DefaultSrpcConnection" should {

    "respond with the same call ID to an incoming request" in new TestScope {

      onRequest.apply(testRequest) returns Future.successful(testResponse)

      srpcComponent.onMessage(testRequestJson)

      awaitFirstSentMessage must beEqualTo(testResponseJson)
    }

    "respond with an internal error to an incoming request when processing in higher layers fails" in new TestScope {

      onRequest.apply(testRequest) returns Future.failed(new RuntimeException("aargh"))

      srpcComponent.onMessage(testRequestJson)

      TransportMessageParser.parse(awaitFirstSentMessage) must beLike {
        case SrpcEnvelope("callid1", SrpcCallError(PayloadErrorCode.InternalError, _, _)) => ok
      }
    }

    "deliver the response to an outgoing request, matching it by call ID" in new TestScope {

      val deliveredResponse = srpcComponent.srpcConnection.sendCall(testRequest)

      val sentRequestJson = compact(render(awaitFirstSentMessage))
      val CallIdRegex = "^\\[\\d+,\"([^\"]+)\".*$".r
      val callId = sentRequestJson match {
        case CallIdRegex(cId) => cId
      }

      srpcComponent.onMessage(JArray(JInt(3) :: JString(callId) :: JString("znal") :: Nil))

      Await.result(deliveredResponse, 5.seconds) mustEqual SrpcCallResult(JString("znal"))
    }

    "return errors when sending the WebSocket message as failed futures" in new TestScope {
      sentWsMessagePromise.success(JInt(3))

      // srpcComponent.webSocketConnection.send throws an exception now because promise already fulfilled
      Await.result(srpcComponent.srpcConnection.sendCall(testRequest), 1.second) must throwA[IllegalStateException]
    }

    "close the WebSocket connection only when incoming requests have been responded to" in new TestScope {
      val outgoingResponsePromise = Promise[SrpcCallResult]()

      onRequest.apply(any[SrpcCall]()) returns outgoingResponsePromise.future

      srpcComponent.onMessage(testRequestJson)

      val srpcCloseFuture = srpcComponent.srpcConnection.close()

      srpcCloseFuture.isCompleted must beFalse
      webSocketCloseFuture.isCompleted must beFalse

      outgoingResponsePromise.success(testResponse)

      Await.result(webSocketCloseFuture, 1.second) mustEqual (())
    }

    "close the WebSocket connection immediately when there are no unanswered incoming requests" in new TestScope {
      srpcComponent.srpcConnection.close()

      Await.result(webSocketCloseFuture, 1.second) mustEqual (())
    }

    "close the WebSocket connection immediately when there are unanswered requests and forceClose is called" in new TestScope {
        val outgoingResponsePromise = Promise[SrpcCallResult]()

        onRequest.apply(any[SrpcCall]()) returns outgoingResponsePromise.future

        srpcComponent.onMessage(testRequestJson)

        srpcComponent.srpcConnection.forceClose()

        Await.result(webSocketCloseFuture, 1.second) mustEqual (())
      }

    "refuse new incoming requests with GenericError when SRPC connection close is waiting" in new TestScope {

      val outgoingResponsePromise = Promise[SrpcCallResult]()

      onRequest.apply(any[SrpcCall]()) returns outgoingResponsePromise.future

      srpcComponent.onMessage(testRequestJson)

      srpcComponent.srpcConnection.close()

      srpcComponent.onMessage(anotherTestRequestJson)

      TransportMessageParser.parse(awaitFirstSentMessage) must beLike {
        case SrpcEnvelope("callid2", SrpcCallError(PayloadErrorCode.GenericError, _, _)) => ok
      }
    }

    "throw an exception when attempting to send a new request when SRPC connection close is waiting" in new TestScope {

      val outgoingResponsePromise = Promise[SrpcCallResult]()

      onRequest.apply(any[SrpcCall]()) returns outgoingResponsePromise.future

      srpcComponent.onMessage(testRequestJson)

      srpcComponent.srpcConnection.close()

      Await.result(srpcComponent.srpcConnection.sendCall(testRequest), 1.second) must throwA[IllegalStateException]
    }

    "throw an exception when attempting to send a new request when the connection was closed by the other side" in new TestScope {

      srpcComponent.onWebSocketDisconnect()

      Await.result(srpcComponent.srpcConnection.sendCall(testRequest), 1.second) must throwA[IllegalStateException]
    }

    "complete the close future when the other side disconnects while we are waiting to close gracefully" in new TestScope {

      val outgoingResponsePromise = Promise[SrpcCallResult]()

      onRequest.apply(any[SrpcCall]()) returns outgoingResponsePromise.future

      srpcComponent.onMessage(testRequestJson)

      val srpcCloseFuture = srpcComponent.srpcConnection.close()

      srpcComponent.onWebSocketDisconnect()

      outgoingResponsePromise.success(testResponse)

      Await.result(srpcCloseFuture, 1.second) mustEqual (())
    }

    "leave onClose uncompleted while the connection is open" in new TestScope {
      srpcComponent.srpcConnection.onClose.isCompleted must beFalse
    }

    "complete the onClose future once the WebSocket connection is closed" in new TestScope {
      srpcComponent.onWebSocketDisconnect()

      Await.result(srpcComponent.srpcConnection.onClose, 1.second) mustEqual (())
    }

    "complete the onClose future only once the WebSocket has been closed" in new TestScope {
      val outgoingResponsePromise = Promise[SrpcCallResult]()

      val srpcCloseFuture = srpcComponent.srpcConnection.close()

      Await.result(webSocketCloseFuture, 1.second) mustEqual (())
      srpcComponent.srpcConnection.onClose.isCompleted must beFalse
      srpcCloseFuture.isCompleted must beFalse

      srpcComponent.onWebSocketDisconnect()

      Await.result(srpcComponent.srpcConnection.onClose, 1.second) mustEqual (())
      Await.result(srpcCloseFuture, 1.second) mustEqual (())
    }
  }

  trait TestScope extends Scope {
    val onRequest = mock[SrpcCall => Future[SrpcResponse]]

    val sentWsMessagePromise = Promise[JValue]()
    val sentWsMessage: Future[JValue] = sentWsMessagePromise.future

    val webSocketClosePromise = Promise[Unit]()
    val webSocketCloseFuture = webSocketClosePromise.future

    val onSrpcDisconnectCalledPromise = Promise[Unit]()
    val onSrpcDisconnectCalledFuture = onSrpcDisconnectCalledPromise.future

    def awaitFirstSentMessage: JValue = Await.result(sentWsMessage, 1.second)

    val srpcComponent = new DefaultSrpcComponent with WebSocketComponent {

      override val executionContext = ExecutionContext.global

      val srpcConnection: DefaultSrpcConnection = new DefaultSrpcConnection

      val webSocketConnection: WebSocketConnection = new WebSocketConnection {
        def send(msg: JValue): Unit = {
          sentWsMessagePromise.success(msg)
          ()
        }

        def close(): Unit = {
          webSocketClosePromise.success(())
          ()
        }
      }

      def onSrpcDisconnect(): Unit = {
        onSrpcDisconnectCalledPromise.success(())
        ()
      }

      def onSrpcCall(msg: SrpcCall) = onRequest.apply(msg)
    }

    val testRequest = SrpcCall("FireMissiles", JObject("aargh" -> JInt(42)))
    val testRequestJson = JsonParser.parse("""[2, "callid1", "FireMissiles", {"aargh": 42}]""")
    val anotherTestRequestJson = JsonParser.parse("""[2, "callid2", "FireMissiles", {"aargh": 42}]""")
    val testResponse = SrpcCallResult(JObject("urgh" -> JInt(2)))
    val testResponseJson = JArray(JInt(3) :: JString("callid1") :: testResponse.payload :: Nil)
  }
}
