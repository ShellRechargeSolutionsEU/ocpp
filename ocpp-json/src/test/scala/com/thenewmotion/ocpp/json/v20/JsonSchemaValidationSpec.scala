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

    validateJson[BootNotificationRequest](bootNotificationRequest)
  }

  private def validateJson[T <: Message : ClassTag](messageGen: Gen[T]): Fragment = {

    val className = classTag[T].runtimeClass.getSimpleName
    val schema = className + ".json"
    System.err.println(s"Loading schema file $schema")

    val jsonSchemaText: String =
      scala.io.Source.fromInputStream({
        getClass.getResourceAsStream(s"schemas/$schema")
      }).getLines.mkString("\n")

    val jsonSchema: JsonNode = asJsonNode(parse(jsonSchemaText))

    s"serialize $className according to schema" in {
      forAll(messageGen) { msg =>
        val json: JsonNode = asJsonNode(Serialization.serialize(msg))
        val validationReport = validator.validate(jsonSchema, json)
        if (validationReport.isSuccess) {
          success
        } else {
          failure(validationReport.asScala.map(_.getMessage).mkString("\n"))
        }
      }
    }
  }

}
