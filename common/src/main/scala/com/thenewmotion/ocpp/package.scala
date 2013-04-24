package com.thenewmotion

import soapenvelope12.{Body, Fault, Envelope}
import scalaxb.DataRecord
import scala.xml.{NamespaceBinding, NodeSeq}
import javax.xml.datatype.{XMLGregorianCalendar, DatatypeFactory}
import org.joda.time.{DateTimeZone, DateTime}


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
        case error => sys.error("unexpected non-elem: " + error.toString)
      }
    }
  }

  def simpleBody(x: DataRecord[Any]) = Body(Seq(x), Map())

  private val factory = DatatypeFactory.newInstance

  implicit class RichXMLCalendar(val self: XMLGregorianCalendar) {
    def toDateTime: DateTime = new DateTime(self.toGregorianCalendar.getTimeInMillis)
  }

  implicit class RichDateTime(val self: DateTime) extends AnyVal {
    def toXMLCalendar: XMLGregorianCalendar =
      factory.newXMLGregorianCalendar(self.toDateTime(DateTimeZone.UTC).toGregorianCalendar)
  }
}
