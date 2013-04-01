package com.thenewmotion.ocpp

/**
 * @author Yaroslav Klymko
 */
object Version extends Enumeration {
  val V12 = Value("1.2")
  val V15 = Value("1.5")

  implicit def richVersion(x: Value): RichVersion = new RichVersion(x)

  class RichVersion(self: Value) {
    def namespace: String = self match {
      case V12 => "urn://Ocpp/Cs/2010/08/"
      case V15 => "urn://Ocpp/Cs/2012/06/"
    }
  }
}