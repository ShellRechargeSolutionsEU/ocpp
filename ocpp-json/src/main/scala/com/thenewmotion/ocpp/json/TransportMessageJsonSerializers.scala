package com.thenewmotion.ocpp
package json

import org.json4s._
import org.json4s.ext.EnumNameSerializer

object TransportMessageJsonSerializers {

  def apply(): Seq[Serializer[_]] = List(new RequestMessageJsonFormat, new ResponseMessageJsonFormat,
    new ErrorMessageJsonFormat, new EnumNameSerializer(PayloadErrorCode))

  class RequestMessageJsonFormat extends CustomSerializer[RequestMessage](format => (
    {
      case JArray(JInt(callType) :: JString(callId) :: JString(action) :: payload :: Nil) =>
        RequestMessage(callId, action, payload)
    },
    {
      case x: RequestMessage =>
        JArray(JInt(BigInt(2)) :: JString(x.callId) :: JString(x.procedureName) :: x.payload :: Nil)
    }))

  class ResponseMessageJsonFormat extends CustomSerializer[ResponseMessage](format => (
    {
      case JArray(JInt(callType) :: JString(callId) :: payload :: Nil) =>
        ResponseMessage(callId, payload)
    },
    {
      case x: ResponseMessage =>
        JArray(JInt(BigInt(3)) :: JString(x.callId) :: x.payload :: Nil)
    }))

  class ErrorMessageJsonFormat extends CustomSerializer[ErrorResponseMessage](format => (
    {
      case JArray(JInt(callType) :: JString(callId) :: errorName :: JString(errorDesc) :: errorDetails :: Nil) =>
        // TODO handle MappingException about the PayloadErrorCode
        ErrorResponseMessage(callId, errorName.extract[PayloadErrorCode.Value](format, manifest), errorDesc,
          errorDetails)
    },
    {
      case x: ErrorResponseMessage =>
        JArray(JInt(BigInt(4)) :: JString(x.callId) :: Extraction.decompose(x.code)(format) :: JString(x.description) ::
          x.details :: Nil)
    }))
}
