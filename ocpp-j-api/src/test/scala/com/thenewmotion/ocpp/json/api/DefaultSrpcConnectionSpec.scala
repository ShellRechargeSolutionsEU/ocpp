package com.thenewmotion.ocpp
package json
package api

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future, Promise}
import org.json4s.JValue
import org.json4s.JsonAST.{JArray, JInt, JObject, JString}
import org.json4s.native.{JsonMethods, JsonParser}
import JsonMethods.{compact, render}
import org.specs2.concurrent.ExecutionEnv
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
        case SrpcEnvelope("callid1", ErrorResponseMessage(PayloadErrorCode.InternalError, _, _)) => ok
      }
    }

    "deliver the response to an outgoing request, matching it by call ID" in { implicit ee: ExecutionEnv =>
      new TestScope {

        val deliveredResponse = srpcComponent.srpcConnection.sendRequest(testRequest)

        val sentRequestJson = compact(render(awaitFirstSentMessage))
        val CallIdRegex = "^\\[\\d+,\"([^\"]+)\".*$".r
        val callId = sentRequestJson match {
          case CallIdRegex(cId) => cId
        }

        srpcComponent.onMessage(JArray(JInt(3) :: JString(callId) :: JString("znal") :: Nil))

        deliveredResponse must beEqualTo(ResponseMessage(JString("znal"))).await
      }
    }

    "return errors when sending the WebSocket message as failed futures" in { implicit ee: ExecutionEnv =>
      new TestScope {
        sentWsMessagePromise.success(JInt(3))

        // srpcComponent.webSocketConnection.send throws an exception now because promise already fulfilled
        val deliveredResponse = srpcComponent.srpcConnection.sendRequest(testRequest)

        deliveredResponse must throwA[IllegalStateException].await
      }
    }

    "close the WebSocket connection only when incoming requests have been responded to" in { implicit ee: ExecutionEnv =>
      new TestScope {
        val outgoingResponsePromise = Promise[ResponseMessage]()

        onRequest.apply(any[RequestMessage]()) returns outgoingResponsePromise.future

        srpcComponent.onMessage(testRequestJson)

        val srpcCloseFuture = srpcComponent.srpcConnection.close()

        srpcCloseFuture.isCompleted must beFalse
        webSocketCloseFuture.isCompleted must beFalse

        outgoingResponsePromise.success(testResponse)

        srpcCloseFuture must beEqualTo(()).await
        webSocketCloseFuture must beEqualTo(()).await
      }
    }

    "close the WebSocket connection immediately when there are no unanswered incoming requests" in { implicit ee: ExecutionEnv =>
      new TestScope {
        val srpcCloseFuture = srpcComponent.srpcConnection.close()

        srpcCloseFuture must beEqualTo(()).await
        webSocketCloseFuture must beEqualTo(()).await
      }
    }

    "refuse new incoming requests with GenericError when SRPC connection close is waiting" in { implicit ee: ExecutionEnv =>
      new TestScope {

        val outgoingResponsePromise = Promise[ResponseMessage]()

        onRequest.apply(any[RequestMessage]()) returns outgoingResponsePromise.future

        srpcComponent.onMessage(testRequestJson)

        val srpcCloseFuture = srpcComponent.srpcConnection.close()

        srpcComponent.onMessage(anotherTestRequestJson)

        TransportMessageParser.parse(awaitFirstSentMessage) must beLike {
          case SrpcEnvelope("callid2", ErrorResponseMessage(PayloadErrorCode.GenericError, _, _)) => ok
        }
      }
    }

    "throw an exception when attempting to send a new request when SRPC connection close is waiting" in { implicit ee: ExecutionEnv =>
      new TestScope {

        val outgoingResponsePromise = Promise[ResponseMessage]()

        onRequest.apply(any[RequestMessage]()) returns outgoingResponsePromise.future

        srpcComponent.onMessage(testRequestJson)

        srpcComponent.srpcConnection.close()

        srpcComponent.srpcConnection.sendRequest(testRequest) must throwA[IllegalStateException]
      }
    }
  }

  trait TestScope extends Scope {
    val onRequest = mock[RequestMessage => Future[ResultMessage]]

    val sentWsMessagePromise = Promise[JValue]()
    val sentWsMessage: Future[JValue] = sentWsMessagePromise.future

    val webSocketClosePromise = Promise[Unit]()
    val webSocketCloseFuture = webSocketClosePromise.future

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

      def onWebSocketDisconnect() = {}

      def onSrpcRequest(msg: RequestMessage) = onRequest.apply(msg)
    }

    val testRequest = RequestMessage("FireMissiles", JObject("aargh" -> JInt(42)))
    val testRequestJson = JsonParser.parse("""[2, "callid1", "FireMissiles", {"aargh": 42}]""")
    val anotherTestRequestJson = JsonParser.parse("""[2, "callid2", "FireMissiles", {"aargh": 42}]""")
    val testResponse = ResponseMessage(JObject("urgh" -> JInt(2)))
    val testResponseJson = JArray(JInt(3) :: JString("callid1") :: testResponse.payload :: Nil)
  }
}
