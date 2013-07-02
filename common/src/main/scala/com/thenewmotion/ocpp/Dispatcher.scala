package com.thenewmotion.ocpp

import scala.xml.NodeSeq
import scalaxb.{DataRecord, XMLFormat, fromXMLEither}
import soapenvelope12.Body
import com.thenewmotion.ocpp.Fault._
import Action._


trait Dispatcher[T] {
  implicit def faultToBody(x: soapenvelope12.Fault) = x.asBody
  def version: Version.Value
  def dispatch(action: Value, xml: NodeSeq, service: => T): Body

  protected def reqRes: ReqRes = new ReqRes {
    def apply[REQ: XMLFormat, RES: XMLFormat](action: Value, xml: NodeSeq)(f: REQ => RES) = fromXMLEither[REQ](xml) match {
      case Left(msg) => ProtocolError(msg)
      case Right(req) => try {
        val res = f(req)
        simpleBody(DataRecord(Some(version.namespace), Some(action.responseLabel), res))
      } catch {
        case FaultException(fault) =>
          fault
      }
    }
  }

  protected def ?[REQ: XMLFormat, RES: XMLFormat](action: Value, xml: NodeSeq)(f: REQ => RES): Body = reqRes(action, xml)(f)
  protected def fault(x: soapenvelope12.Fault): Nothing = throw new FaultException(x)
}

trait ReqRes {
  def apply[REQ: XMLFormat, RES: XMLFormat](action: Value, xml: NodeSeq)(f: REQ => RES): Body
}

case class FaultException(fault: soapenvelope12.Fault) extends Exception(fault.toString)
