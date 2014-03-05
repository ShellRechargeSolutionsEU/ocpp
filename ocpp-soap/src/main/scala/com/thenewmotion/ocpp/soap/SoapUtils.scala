package com.thenewmotion.ocpp.soap

import scala.xml.{NodeSeq, XML}
import soapenvelope12.{Envelope, Body}

/**
 * @author Yaroslav Klymko
 */
trait SoapUtils {
  def xmlFrom(path: String): NodeSeq = XML.load(getClass.getResourceAsStream(path))

  def envelopeFrom(path: String): Envelope = scalaxb.fromXML[Envelope](xmlFrom(path))

  def bodyFrom(path: String): Body = envelopeFrom(path).Body
}
