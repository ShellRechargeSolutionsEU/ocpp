package com.thenewmotion.ocpp

import org.specs2.mutable.SpecificationWithJUnit
import com.thenewmotion.ocpp.soap.{ChargeBoxIdentity, SoapUtils}

/**
 * @author Yaroslav Klymko
 */
class ChargeBoxIdentitySpec extends SpecificationWithJUnit with SoapUtils{
  "ChargeBoxIdentity" should {
    "parse chargeBoxIdentity from envelope" in {
      ChargeBoxIdentity.unapply(envelopeFrom("v15/heartbeatRequest.xml")) must beSome("chargeBoxIdentity")
    }
  }
}
