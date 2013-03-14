package com.thenewmotion.chargenetwork.ocpp.v12

import scalaxb.DataRecord
import xml.{NamespaceBinding, NodeSeq}
import soapenvelope12.{Fault, Body, Header}

/**
 * @author Yaroslav Klymko
 */
object XmlToEnvelope {
  def apply(body: NodeSeq, headers: NodeSeq): soapenvelope12.Envelope = {
    val bodyRecords = body.toSeq map {
      DataRecord(None, None, _)
    }
    val headerOption = headers.toSeq.headOption map {
      _ =>
        Header(headers.toSeq map {
          DataRecord(None, None, _)
        }, Map())
    }
    soapenvelope12.Envelope(headerOption, Body(bodyRecords, Map()), Map())
  }
}

object EnvelopeToXml {
  def apply(env: soapenvelope12.Envelope, namespace: NamespaceBinding = soapenvelope12.defaultScope): NodeSeq = {
    val merged = scalaxb.toScope(((Some("soap12") -> "http://www.w3.org/2003/05/soap-envelope") ::
      scalaxb.fromScope(namespace)).distinct: _*)
    scalaxb.toXML(env, Some("http://www.w3.org/2003/05/soap-envelope"), Some("Envelope"), merged) match {
      case elem: scala.xml.Elem => elem
      case x => sys.error("unexpected non-elem: " + x.toString)
    }
  }
}


object FaultToXml {
  def apply(fault: Fault) =
    scalaxb.toXML(fault, Some("http://www.w3.org/2003/05/soap-envelope"), Some("Fault"), soapenvelope12.defaultScope)
}