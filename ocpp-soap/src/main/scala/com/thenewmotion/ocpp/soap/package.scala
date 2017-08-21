package com.thenewmotion.ocpp

import soapenvelope12.{Fault, Envelope, Body}

import scalaxb.DataRecord
import scala.xml.{NamespaceBinding, NodeSeq}
import javax.xml.datatype.{XMLGregorianCalendar, DatatypeFactory}
import java.time.{ZonedDateTime, Instant, ZoneId}

import org.slf4j.LoggerFactory
import com.github.t3hnar.scalax.StringOption
import java.net.URI
import java.util.GregorianCalendar


/**
 * @author Yaroslav Klymko
 */
package object soap {
  type Uri = URI

  val soapEnvelopeUri = "http://www.w3.org/2003/05/soap-envelope"
  val defaultNamespace = scalaxb.toScope(
    Some("s") -> soapEnvelopeUri,
    Some("xs") -> "http://www.w3.org/2001/XMLSchema",
    Some("xsi") -> "http://www.w3.org/2001/XMLSchema-instance")

  val httpLogger = LoggerFactory.getLogger("com.thenewmotion.ocpp.http")

  implicit class ReachFault(val self: Fault) extends AnyVal {
    def asBody: Body = simpleBody(DataRecord(Some(soapEnvelopeUri), Some("Fault"), self))
  }

  implicit class ReachEnvelope(val self: Envelope) extends AnyVal {
    def toXml: NodeSeq = {
      val namespace = (for {
        body <- self.Body.any.headOption
        ns <- body.namespace
      } yield NamespaceBinding(null, ns, defaultNamespace)) getOrElse defaultNamespace

      scalaxb.toXML(self, Some(soapEnvelopeUri), "Envelope", namespace) match {
        case elem: scala.xml.Elem => elem
        case error => sys.error("unexpected non-elem: " + error.toString)
      }
    }
  }

  def simpleBody(x: DataRecord[Any]) = Body(Seq(x), Map())

  private val factory = DatatypeFactory.newInstance

  private val utc = ZoneId.of("UTC")

  implicit class RichXMLCalendar(val self: XMLGregorianCalendar) extends AnyVal {
    def toDateTime: ZonedDateTime =
      ZonedDateTime.ofInstant(Instant.ofEpochMilli(self.toGregorianCalendar.getTimeInMillis), utc)
  }

  implicit class RichZonedDateTime(val self: ZonedDateTime) extends AnyVal {
    def toXMLCalendar: XMLGregorianCalendar =
      factory.newXMLGregorianCalendar(GregorianCalendar.from(self.withZoneSameInstant(utc)))
  }

  def stringOption(x: Option[String]): Option[String] = x.flatMap(StringOption.apply)
}
