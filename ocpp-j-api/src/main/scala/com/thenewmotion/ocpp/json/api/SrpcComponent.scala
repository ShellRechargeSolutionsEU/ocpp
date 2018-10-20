package com.thenewmotion.ocpp
package json
package api

import scala.concurrent.Future

/**
 * The middle layer in the three-layer protocol stack of OCPP-J: Simple Remote
 * Procedure Call.
 *
 * The SRPC layer relates WebSocket messages to each other as calls,
 * call results and call errors.
 *
 * Although the OCPP 1.6 specification no longer uses the term "SRPC", it is
 * present in the original specification on
 * http://www.gir.fr/ocppjs/ocpp_srpc_spec.shtml, which is referenced by the
 * IANA WebSocket Subprotocol Name Registry at
 * https://www.iana.org/assignments/websocket/websocket.xml. So bureaucracy is
 * on our side in naming this.
 */
trait SrpcComponent {
  trait SrpcConnection {
    /**
      * Send an outgoing call to the remote endpoint
      * @param msg The outgoing call
      * @return The incoming response, asynchronously
      */
    def sendCall(msg: SrpcCall): Future[SrpcResponse]

    /**
      * Close the connection.
      *
      * This will allow all pending incoming calls to be responded to,
      * and only then will close the underlying WebSocket.
      *
      * @return A future that is completed when connection has indeed closed
      */
    def close(): Future[Unit]

    /**
      * Close the connection, without waiting for the call handler to handle
      * calls that were already received.
      *
      * @return
      */
    def forceClose(): Unit

    /**
      * A future that is completed once the SRPC connection is closed
      */
    def onClose: Future[Unit]
  }

  def srpcConnection: SrpcConnection

  /**
    * To be overridden to handle incoming calls
    *
    * @param msg The incoming call
    * @return The outgoing message, asynchronously
    */
  def onSrpcCall(msg: SrpcCall): Future[SrpcResponse]
}

