package com.thenewmotion.ocpp
package json

import org.json4s._
import SrpcMessageType._

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
        SrpcCall(opName, payload)
      case (CALLRESULT.`id`, payload :: Nil) =>
        SrpcCallResult(payload)
      case (CALLERROR.`id`, JString(errorName) :: JString(errorDesc) :: errorDetails :: Nil) =>
        SrpcCallError(
          enumerableFromJsonString(PayloadErrorCode, errorName),
          errorDesc,
          errorDetails
        )
      case _ =>
        throw new MappingException("Malformed content in SRPC array")
    }

  private def toCallType(payload: SrpcMessage): BigInt =
    payload match {
      case _: SrpcCall => CALL.id
      case _: SrpcCallResult => CALLRESULT.id
      case _: SrpcCallError => CALLERROR.id
    }

  private def toContents(msg: SrpcMessage): List[JValue] =
    msg match {
      case SrpcCall(procedureName, payload) =>
        JString(procedureName) ::
        payload                :: Nil
      case SrpcCallResult(payload) =>
        payload :: Nil
      case SrpcCallError(code, desc, details) =>
        JString(code.name) ::
        JString(desc)      ::
        details            :: Nil
    }
}
