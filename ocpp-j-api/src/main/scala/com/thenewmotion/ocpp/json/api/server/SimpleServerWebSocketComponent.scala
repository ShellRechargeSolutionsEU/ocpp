package com.thenewmotion.ocpp
package json.api
package server

import org.java_websocket.WebSocket
import org.json4s.JValue
import org.json4s.native.JsonMethods.{compact, render, parse}

trait SimpleServerWebSocketComponent extends WebSocketComponent {

  self: SrpcComponent =>

  trait SimpleServerWebSocketConnection extends WebSocketConnection {

    def webSocket: WebSocket

    def send(msg: JValue): Unit = webSocket.send(compact(render(msg)))

    def close(): Unit = webSocket.close()
  }

  def feedIncomingMessage(msg: String): Unit = self.onMessage(parse(msg))

  def feedIncomingDisconnect(): Unit = self.onWebSocketDisconnect()

  def feedIncomingError(err: Exception): Unit = self.onError(err)
}


