package com.thenewmotion.ocpp
package json.api
package server

import org.java_websocket.WebSocket
import org.json4s.JValue
import org.json4s.native.JsonMethods.{compact, render}

trait SimpleServerWebSocketComponent extends WebSocketComponent {

  trait SimpleServerWebSocketConnection extends WebSocketConnection {

    def webSocket: WebSocket

    def send(msg: JValue): Unit = webSocket.send(compact(render(msg)))

    def close(): Unit = webSocket.close()
  }
}


