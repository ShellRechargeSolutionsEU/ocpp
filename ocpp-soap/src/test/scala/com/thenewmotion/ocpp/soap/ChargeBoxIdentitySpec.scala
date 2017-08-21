package com.thenewmotion.ocpp
package soap

import org.specs2.mutable.Specification

/**
 * @author Yaroslav Klymko
 */
class ChargeBoxIdentitySpec extends Specification with SoapUtils{
  "ChargeBoxIdentity" should {
    "parse chargeBoxIdentity from envelope" in {
      ChargeBoxIdentity.unapply(envelopeFrom("v15/heartbeatRequest.xml")) must beSome("chargeBoxIdentity")
    }
  }
}
