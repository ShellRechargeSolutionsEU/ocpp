package com.thenewmotion.ocpp
package soap

import org.specs2.mutable.Specification
import ChargeBoxAddress.unapply

/**
 * @author Yaroslav Klymko
 */
class ChargeBoxAddressSpec extends Specification with SoapUtils {
  "ChargeBoxAddress" should {
    "parse address" in {
      unapply(envelopeFrom("v15/heartbeatRequest.xml")) must beSome(new Uri("http://address.com:8081"))
    }
    "ignore 'http://www.w3.org/2005/08/addressing/anonymous'" in {
      unapply(envelopeFrom("v12/heartbeatRequest.xml")) must beNone
    }
  }
}
