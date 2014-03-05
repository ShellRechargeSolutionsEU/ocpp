package com.thenewmotion.ocpp
package soap

import soapenvelope12._
import javax.xml.namespace.QName

/**
 * @author Yaroslav Klymko
 */
object Fault {

  object TnsSender extends FaultcodeEnum {
    override def toString = "soap12:Sender"
  }

  object TnsReceiver extends FaultcodeEnum {
    override def toString = "soap12:Receiver"
  }

  val SecurityError = apply(TnsSender, "SecurityError")
  val IdentityMismatch = apply(TnsSender, "IdentityMismatch")
  val UrlMismatch = apply(TnsSender, "UrlMismatch")
  val ProtocolError = apply(TnsSender, "ProtocolError")
  val InternalError = apply(TnsReceiver, "InternalError")
  val NotSupported = apply(TnsReceiver, "NotSupported")

  def apply(code: FaultcodeEnum, subcode: String): (String => Fault) = (reason: String) => {
    soapenvelope12.Fault(Faultcode(code, Some(Subcode(new QName(subcode)))), Faultreason(Reasontext(reason, "en-US")))
  }
}