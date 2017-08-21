package com.thenewmotion.ocpp
package soap

import org.specs2.mutable.Specification

/**
 * @author Yaroslav Klymko
 */
class SoapActionSpec extends Specification with SoapUtils {
  "SoapAction" should {
    "return soap action header out of xml" in {
      val headers = Map("content-type" -> """application/soap+xml; charset=utf-8; action="/Heartbeat"""")
      SoapActionHeader(headers) must beSome("SOAPAction" -> """"/Heartbeat"""")
    }
  }
}