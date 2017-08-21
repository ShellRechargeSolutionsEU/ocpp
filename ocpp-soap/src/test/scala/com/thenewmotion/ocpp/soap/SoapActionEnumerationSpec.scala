package com.thenewmotion.ocpp
package soap

import org.specs2.mutable.Specification
import com.thenewmotion.ocpp.messages.CentralSystemAction

/**
 * @author Yaroslav Klymko
 */
class SoapActionEnumerationSpec extends Specification {
  "Action" should {
    "parse action from header" in {
      val headers = Set(
        "/AUTHORIZE",
        "/starttransaction",
        "/stoptransaction",
        "/bootnotification",
        "/diagnosticsstatusnotification",
        "/firmwarestatusnotification",
        "/heartbeat",
        "/metervalues",
        "/statusnotification")

      val actions = headers.flatMap {
        header => SoapActionEnumeration.fromHeader(CentralSystemAction, header)
      }

      actions.size mustEqual headers.size
    }
  }
}
