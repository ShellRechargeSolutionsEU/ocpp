package com.thenewmotion.ocpp.spray

import javax.xml.soap.SOAPConstants
import spray.http.StatusCodes._

import spray.http.{HttpHeader, HttpMethods, HttpRequest, StatusCode}
import com.thenewmotion.http.{MediaProperty, MediaType}

/**
 * @author Yaroslav Klymko
 */

object SoapPost {
  def unapply(req: HttpRequest): Either[StatusCode, List[MediaProperty]] =
    req.method -> MediaType(req.headers.collectFirst {
      case x: HttpHeader if x is "content-type" => x.value
    }.getOrElse("")) match {
      case (HttpMethods.POST, MediaType(SOAPConstants.SOAP_1_2_CONTENT_TYPE, ps)) => Right(ps)
      case (HttpMethods.POST, _) => Left(UnsupportedMediaType)
      case _ => Left(MethodNotAllowed)
    }
}