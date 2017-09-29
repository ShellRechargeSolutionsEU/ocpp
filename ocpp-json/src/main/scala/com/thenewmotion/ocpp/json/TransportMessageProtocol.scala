package com.thenewmotion.ocpp
package json

import enums.reflection.EnumUtils.{Nameable, Enumerable}
import org.json4s._

sealed trait TransportMessageType extends Nameable { def id: BigInt }
object TransportMessageType extends Enumerable[TransportMessageType] {
  object CALL       extends TransportMessageType { val id: BigInt = 2 }
  object CALLRESULT extends TransportMessageType { val id: BigInt = 3 }
  object CALLERROR  extends TransportMessageType { val id: BigInt = 4 }

  val values = Set(CALL, CALLRESULT, CALLERROR)
}

// Helper object to ensure proper use of error codes within the payload.
sealed trait PayloadErrorCode extends Nameable
object PayloadErrorCode extends Enumerable[PayloadErrorCode] {

  object NotImplemented                extends PayloadErrorCode
  object NotSupported                  extends PayloadErrorCode
  object InternalError                 extends PayloadErrorCode
  object ProtocolError                 extends PayloadErrorCode
  object SecurityError                 extends PayloadErrorCode
  object FormationViolation            extends PayloadErrorCode
  object PropertyConstraintViolation   extends PayloadErrorCode
  object OccurrenceConstraintViolation extends PayloadErrorCode
  object TypeConstraintViolation       extends PayloadErrorCode
  object GenericError                  extends PayloadErrorCode

  val values = Set(
    NotImplemented,
    NotSupported,
    InternalError,
    ProtocolError,
    SecurityError,
    FormationViolation,
    PropertyConstraintViolation,
    OccurrenceConstraintViolation,
    TypeConstraintViolation,
    GenericError
  )
}

import TransportMessageType._

sealed trait TransportMessage {
  def MsgType: TransportMessageType
}
// generic message structure (used to transform from and to json)
case class RequestMessage(callId: String, procedureName: String, payload: JValue) extends TransportMessage {
  def MsgType: TransportMessageType = CALL
}

case class ResponseMessage(callId: String, payload: JValue) extends TransportMessage {
  def MsgType: TransportMessageType = CALLRESULT
}

//
case class ErrorResponseMessage(callId: String, code: PayloadErrorCode, description: String, details: JValue) extends TransportMessage {
  def MsgType: TransportMessageType = CALLERROR
}

object ErrorResponseMessage {
  def apply(callId: String, code: PayloadErrorCode, description: String): ErrorResponseMessage =
    ErrorResponseMessage(callId, code, description, JObject(List()))
}

