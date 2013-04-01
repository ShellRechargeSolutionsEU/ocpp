package com.thenewmotion

import soapenvelope12.{Body, Fault, Envelope}
import scalaxb.DataRecord
import scala.xml.{NamespaceBinding, NodeSeq}


/**
 * @author Yaroslav Klymko
 */
package object ocpp {
  val soapEnvelopeUri = "http://www.w3.org/2003/05/soap-envelope"
  val defaultNamespace = scalaxb.toScope(
    Some("s") -> soapEnvelopeUri,
    Some("xs") -> "http://www.w3.org/2001/XMLSchema",
    Some("xsi") -> "http://www.w3.org/2001/XMLSchema-instance")


  implicit def reachFault(x: Fault) = new {
    def asBody: Body = simpleBody(DataRecord(Some(soapEnvelopeUri), Some("Fault"), x))
  }

  implicit def reachEnvelope(x: Envelope) = new {
    def toXml: NodeSeq = {
      val namespace = (for {
        body <- x.Body.any.headOption
        ns <- body.namespace
      } yield NamespaceBinding(null, ns, defaultNamespace)) getOrElse defaultNamespace

      scalaxb.toXML(x, Some(soapEnvelopeUri), "Envelope", namespace) match {
        case elem: scala.xml.Elem => elem
        case x => sys.error("unexpected non-elem: " + x.toString)
      }
    }
  }

  def simpleBody(x: DataRecord[Any]) = Body(Seq(x), Map())
}
