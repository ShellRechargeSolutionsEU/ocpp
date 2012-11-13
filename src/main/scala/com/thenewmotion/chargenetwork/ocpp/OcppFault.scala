package com.thenewmotion.chargenetwork.ocpp

import javax.xml.namespace.QName
import soapenvelope12._

/**
 * @author Yaroslav Klymko
 */
object OcppFault {
  val SecurityError = apply(TnsSender, "SecurityError")
  val IdentityMismatch = apply(TnsSender, "IdentityMismatch")
  val UrlMismatch = apply(TnsSender, "UrlMismatch")
  val ProtocolError = apply(TnsSender, "ProtocolError")
  val InternalError = apply(TnsReceiver, "InternalError")
  val NotSupported = apply(TnsReceiver, "NotSupported")

  def apply(code: FaultcodeEnum, subcode: String): (String => Fault) = (reason: String) => {
    Fault(Faultcode(code, Some(Subcode(new QName(subcode)))), Faultreason(Reasontext(reason, "en-US")))
  }
}
