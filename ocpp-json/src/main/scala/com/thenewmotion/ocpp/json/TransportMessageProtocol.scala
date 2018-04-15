package com.thenewmotion.ocpp
package json

import enums.reflection.EnumUtils.{Nameable, Enumerable}
import org.json4s._

sealed trait SrpcMessageType extends Nameable { def id: BigInt }
object SrpcMessageType extends Enumerable[SrpcMessageType] {
  object CALL       extends SrpcMessageType { val id: BigInt = 2 }
  object CALLRESULT extends SrpcMessageType { val id: BigInt = 3 }
  object CALLERROR  extends SrpcMessageType { val id: BigInt = 4 }

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

case class SrpcEnvelope(
  callId: String,
  payload: SrpcMessage
)

sealed trait SrpcMessage

case class SrpcCall(procedureName: String, payload: JValue) extends SrpcMessage

// TODO swap result and response
sealed trait SrpcResponse extends SrpcMessage

case class SrpcCallResult(payload: JValue) extends SrpcResponse

case class SrpcCallError(
  code: PayloadErrorCode,
  description: String,
  details: JValue = JObject(List())
) extends SrpcResponse

