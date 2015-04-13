package com.thenewmotion.ocpp.soap

import soapenvelope12.Envelope
import com.github.t3hnar.scalax.{RichAny, StringOption}
import xml.Elem

/**
 * @author Yaroslav Klymko
 */
object ChargeBoxIdentity {
  def unapply(env: Envelope): Option[String] = (for {
    header <- env.Header.toSeq
    data <- header.any
    elem <- data.value.asInstanceOfOpt[Elem]
    text <- StringOption(elem.text) if (elem.label equalsIgnoreCase "chargeBoxIdentity")
  } yield text).headOption
}