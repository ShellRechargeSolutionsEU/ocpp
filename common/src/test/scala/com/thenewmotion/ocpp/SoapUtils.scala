package com.thenewmotion.ocpp

import xml.XML
import soapenvelope12.{Envelope, Body}

/**
 * @author Yaroslav Klymko
 */
trait SoapUtils {
  def envelopeFrom(path: String): Envelope =
    scalaxb.fromXML[Envelope](XML.load(getClass.getResourceAsStream(path)))

  def bodyFrom(path: String): Body = envelopeFrom(path).Body
}
