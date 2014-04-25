package com.thenewmotion.ocpp
package json

import org.json4s._

object TransportMessageType extends Enumeration {
  type MessageType = Value
  val NOTUSED1, NOTUSED2, CALL, CALLRESULT, CALLERROR = Value
}

// Helper object to ensure proper use of error codes within the payload.
object PayloadErrorCode extends Enumeration {
  type ErrorCode = Value

  val NotImplemented = Value
  val NotSupported = Value
  val InternalError = Value
  val ProtocolError = Value
  val SecurityError = Value
  val FormationViolation = Value
  val PropertyConstraintViolation = Value
  val OccurrenceConstraintViolation = Value
  val TypeConstraintViolation = Value
  val GenericError = Value
}

// Helper object to ensure registered protocol version.
object Protocol extends Enumeration {
  type Protocol = Value
  val Ocpp12 = Value("ocpp1.2")
  val Ocpp15 = Value("ocpp1.5")
}

import TransportMessageType._

sealed trait TransportMessage {
  def MsgType: MessageType
}
// generic message structure (used to transform from and to json)
case class RequestMessage(callId: String, procedureName: String, payload: JValue) extends TransportMessage {
  def MsgType: TransportMessageType.MessageType = CALL
}

case class ResponseMessage(callId: String, payload: JValue) extends TransportMessage {
  def MsgType: TransportMessageType.MessageType = CALLRESULT
}

//
case class ErrorResponseMessage(callId: String, code: PayloadErrorCode.Value, description: String, details: JValue) extends TransportMessage {
  def MsgType: TransportMessageType.MessageType = CALLERROR
}

object ErrorResponseMessage {
  def apply(callId: String, code: PayloadErrorCode.Value, description: String): ErrorResponseMessage =
    ErrorResponseMessage(callId, code, description, JObject(List()))
}

