package com.thenewmotion.ocpp.spray

import xml.NodeSeq
import spray.http._
import spray.http.HttpCharsets._
import spray.http.HttpResponse

/**
 * @author Yaroslav Klymko
 */
object SoapResponse {
  val contentType = ContentType(MediaTypes.`application/soap+xml`, `UTF-8`)

  def apply(xml: NodeSeq) = HttpResponse(
    entity = HttpEntity(contentType, xml.toString()))
}