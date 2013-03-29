package com.thenewmotion.ocpp

import soapenvelope12.Envelope
import scalax.richAny
import xml.Elem

/**
 * @author Yaroslav Klymko
 */
object ChargeBoxAddress {
  val addressingUri = "http://www.w3.org/2005/08/addressing"
  val anonymousUri = addressingUri + "/anonymous"

  def unapply(env: Envelope): Option[String] = {
    val headers = for {
      header <- env.Header.toSeq
      dataRecord <- header.any
      elem <- dataRecord.value.asInstanceOfOpt[Elem] if (elem.namespace == addressingUri)
    } yield elem

    def loop(xs: List[String]): Option[String] = xs match {
      case Nil => None
      case h :: t => StringOption(h) match {
        case Some(`anonymousUri`) => loop(t)
        case None => loop(t)
        case some => some
      }
    }

    loop(List(
      headers \\ "From" \ "Address" text,
      headers \\ "ReplyTo" \ "Address" text))
  }
}
