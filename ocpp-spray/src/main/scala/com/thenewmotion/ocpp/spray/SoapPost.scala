package com.thenewmotion.ocpp.spray

import javax.xml.soap.SOAPConstants
import PartialFunction.condOpt
import spray.http.{HttpHeader, HttpMethods, HttpRequest}
import com.thenewmotion.http.{MediaProperty, MediaType}

/**
 * @author Yaroslav Klymko
 */

object SoapPost {
  def unapply(req: HttpRequest): Option[List[MediaProperty]] =
    condOpt(req.method -> MediaType(req.headers.collectFirst {
      case x: HttpHeader if x is "content-type" => x.value
    }.getOrElse(""))) {
      case (HttpMethods.POST, MediaType(SOAPConstants.SOAP_1_2_CONTENT_TYPE, ps)) => ps
    }
}