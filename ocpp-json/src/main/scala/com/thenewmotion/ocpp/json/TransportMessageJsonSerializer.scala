package com.thenewmotion.ocpp
package json

import org.json4s._
import TransportMessageType._

object TransportMessageJsonSerializer extends SerializationCommon {

  def apply(): Serializer[SrpcEnvelope] = new CustomSerializer[SrpcEnvelope](format => (
    {
      case JArray(
        JInt(callType) ::
        JString(callId) ::
        contents
      ) =>
        SrpcEnvelope(callId, toPayload(callType, contents))
    },

    {
      case msg: SrpcEnvelope =>
        JArray(
          JInt(toCallType(msg.payload)) ::
          JString(msg.callId) ::
          toContents(msg.payload)
        )
    }
  ))

  private def toPayload(callType: BigInt, contents: List[JValue]): SrpcMessage =
    (callType, contents) match {
      case (CALL.`id`, JString(opName) :: payload :: Nil) =>
        RequestMessage(opName, payload)
      case (CALLRESULT.`id`, payload :: Nil) =>
        ResponseMessage(payload)
      case (CALLERROR.`id`, JString(errorName) :: JString(errorDesc) :: errorDetails :: Nil) =>
        ErrorResponseMessage(
          enumerableFromJsonString(PayloadErrorCode, errorName),
          errorDesc,
          errorDetails
        )
    }

  private def toCallType(payload: SrpcMessage): BigInt =
    payload match {
      case _: RequestMessage => CALL.id
      case _: ResponseMessage => CALLRESULT.id
      case _: ErrorResponseMessage => CALLERROR.id
    }

  private def toContents(msg: SrpcMessage): List[JValue] =
    msg match {
      case RequestMessage(procedureName, payload) =>
        JString(procedureName) ::
        payload                :: Nil
      case ResponseMessage(payload) =>
        payload :: Nil
      case ErrorResponseMessage(code, desc, details) =>
        JString(code.name) ::
        JString(desc)      ::
        details            :: Nil

    }
}
