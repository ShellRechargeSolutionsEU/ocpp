package com.thenewmotion.ocpp
package json
package v20

import scala.collection.JavaConverters._
import scala.reflect.{ClassTag, classTag}
import com.fasterxml.jackson.databind.JsonNode
import com.github.fge.jsonschema.main.{JsonSchemaFactory, JsonValidator}
import org.json4s.jackson.JsonMethods.{asJsonNode, parse}
import org.scalacheck.Gen
import org.scalacheck.Prop.forAll
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import org.specs2.specification.core.Fragment
import messages.v20._
import scalacheck.MessageGenerators._

class JsonSchemaValidationSpec extends Specification with ScalaCheck {

  val validator: JsonValidator = JsonSchemaFactory.byDefault().getValidator

  "OCPP 2.0 message serialization" should {

    validateJson(getBaseReportRequest)
    validateJson(getBaseReportResponse)

    validateJson(getTransactionStatusRequest)
    validateJson(getTransactionStatusResponse)

    validateJson(getVariablesRequest)
    validateJson(getVariablesResponse)

    validateJson(notifyReportRequest)
    validateJson(notifyReportResponse)

    validateJson(requestStartTransactionRequest)
    validateJson(requestStartTransactionResponse)

    validateJson(requestStopTransactionRequest)
    validateJson(requestStopTransactionResponse)

    validateJson(sendLocalListRequest)
    validateJson(sendLocalListResponse)

    validateJson(setVariablesRequest)
    validateJson(setVariablesResponse)

    validateJson(authorizeRequest)
    validateJson(authorizeResponse)

    validateJson(bootNotificationRequest)
    validateJson(bootNotificationResponse)

    validateJson(heartbeatRequest)
    validateJson(heartbeatResponse)

    validateJson(statusNotificationRequest)
    validateJson(statusNotificationResponse)

    validateJson(Gen.resize(10, transactionEventRequest))
    validateJson(transactionEventResponse)
  }

  private def validateJson[T <: Message : ClassTag](messageGen: Gen[T]): Fragment = {

    val className = classTag[T].runtimeClass.getSimpleName
    val schema = className + ".json"

    val jsonSchemaText: String =
      scala.io.Source.fromInputStream({
        getClass.getResourceAsStream(s"schemas/$schema")
      }).getLines.mkString("\n")

    val jsonSchema: JsonNode = asJsonNode(parse(jsonSchemaText))

    s"serialize $className according to schema" in {
      forAll(messageGen) { msg =>
        val sd = Serialization.serialize(msg)
        val json: JsonNode = asJsonNode(sd)
        val validationReport = validator.validate(jsonSchema, json)
        if (validationReport.isSuccess) {
          success
        } else {
          failure {
            "Failed JSON validation:\n\n\t" +
            validationReport.asScala.map(_.getMessage).mkString("\n") + "\n\n" +
            "Offending JSON:\n\n\t" +
            sd
          }
        }
      }
    }
  }

}
