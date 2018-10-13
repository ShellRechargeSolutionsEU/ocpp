package com.thenewmotion.ocpp
package json
package v20

import org.scalacheck.Gen
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import org.scalacheck.Prop.forAll
import org.specs2.specification.core.Fragment
import scalacheck.MessageGenerators._
import messages.v20.Message


class SerializationSpec extends Specification with ScalaCheck {

  "OCPP 2.0 message serialization and deserialization" should {
    testMessage(requestStartTransactionRequest)
    testMessage(requestStartTransactionResponse)

    testMessage(bootNotificationRequest)
    testMessage(bootNotificationResponse)
    testMessage(heartbeatRequest)
    testMessage(heartbeatResponse)
  }

  private def testMessage[M <: Message : Manifest](messageGen: Gen[M]): Fragment = {
    val msgName = manifest[M].runtimeClass.getSimpleName

    s"result in the same object when serializing and deserializing $msgName messages" in {
      forAll(messageGen) { msg =>
        val str = Serialization.write(msg)
        val after = Serialization.read[M](str)
        after == msg
      }
    }
  }
}
