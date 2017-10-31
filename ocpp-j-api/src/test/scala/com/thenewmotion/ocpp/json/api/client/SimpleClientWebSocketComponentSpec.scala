package com.thenewmotion.ocpp.json.api.client

import org.specs2.mutable.Specification

class SimpleClientWebSocketComponentSpec extends Specification {

  "SimpleClientWebSocketComponent" should {

    "encode HTTP Basic Auth credentials with base64 according to RFC 2045" in {

      val b64 = SimpleClientWebSocketComponent.toBase64String(
        "AL1000",
        "0001020304050607FFFFFFFFFFFFFFFFFFFFFFFF"
      )

      b64 mustEqual "QUwxMDAwOgABAgMEBQYH////////////////"
    }
  }
}
