package com.thenewmotion.ocpp

import org.specs2.mutable.SpecificationWithJUnit
import xml.XML

/**
 * @author Yaroslav Klymko
 */
class ActionSpec extends SpecificationWithJUnit {
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
        header => Action.fromHeader(header)
      }

      actions.size mustEqual headers.size
    }
  }
}
