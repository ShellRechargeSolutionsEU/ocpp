package com.thenewmotion.ocpp.json

import org.specs2.mutable.Specification
import scala.io.Source
import org.json4s._
import org.json4s.native.Serialization
import PayloadErrorCode._

class TransportMessageParserSpec extends Specification {

  implicit val formats = DefaultFormats + TransportMessageJsonSerializer()

  def messageData(location: String): String = {
    Source.fromURL(getClass.getResource(location)).mkString
  }

  "OcppTransport messages" should {

    "Parse and create same call message" in {
      val requestData = messageData("srpc/callmessage.json")
      val result = TransportMessageParser.parse(requestData)
      val requestObject =
        SrpcEnvelope(
          "1234567",
          SrpcCall("CallMessageAction", JObject(List(JField("payload", JString("something")))))
        )
      result must_== requestObject

      val requestJson = TransportMessageParser.write(requestObject)
      requestJson must_== requestData
    }

    "Parse and create same response message" in {
      val requestData = messageData("srpc/responsemessage.json")
      val result = TransportMessageParser.parse(requestData)
      val requestObject =
        SrpcEnvelope(
          "1234567",
          SrpcCallResult(JObject(List(JField("payload", JString("something")))))
        )
      result must_== requestObject

      val requestJson = TransportMessageParser.write(requestObject)
      requestJson must_== requestData
    }

    "Parse and create same error message" in {
      val requestData = messageData("srpc/errorresponsemessage.json")
      val result = TransportMessageParser.parse(requestData)
      val requestObject =
        SrpcEnvelope(
          "1234567",
          SrpcCallError(InternalError, "description", JObject(List(JField("payload", JString("something")))))
        )
      result must_== requestObject

      val requestJson = TransportMessageParser.write(requestObject)
      requestJson must_== requestData
    }

    "Not put JSON null in error messages" in {
      val errorMessageWithoutPayload =
        SrpcEnvelope(
          "1234567890abcdef",
          SrpcCallError(PayloadErrorCode.InternalError, "toedeledokie")
        )

      Serialization.write(errorMessageWithoutPayload) must not(beMatching(".*\\bnull\\b.*"))
    }
  }

  "Faulty messages" should {

    "give no data when the message type is wrong" in {
      val requestData = messageData("srpc/wrongtypemessage.json")
      TransportMessageParser.parse(requestData) must throwA[MappingException]
    }

    "give no data when the message has too many elements in the list" in {
      val requestData = messageData("srpc/wrongelementsmessage-toomany.json")
      TransportMessageParser.parse(requestData) must throwA[MappingException]
    }

    "give no data when the message has too few elements in the list" in {
      val requestData = messageData("srpc/wrongelementsmessage-toofew.json")
      TransportMessageParser.parse(requestData) must throwA[MappingException]
    }
  }
}
