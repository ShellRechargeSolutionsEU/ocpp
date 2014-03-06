package com.thenewmotion.ocpp
package spray

import soapenvelope12.{Fault, Body}
import _root_.spray.http.HttpResponse
import soap.{ReachEnvelope, ReachFault}

/**
 * @author Yaroslav Klymko
 */
object OcppResponse {
  def apply(fault: Fault): HttpResponse = apply(fault.asBody)

  def apply(body: => Body): HttpResponse = {
    val env = soapenvelope12.Envelope(None, body, Map())
    SoapResponse(env.toXml)
  }
}

