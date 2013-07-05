package com.thenewmotion.ocpp

import scala.language.implicitConversions
import scala.xml.{Elem, NodeSeq}
import scalaxb.{DataRecord, XMLFormat, fromXMLEither}
import soapenvelope12.Body
import com.thenewmotion.ocpp.Fault._


/**
 * A dispatcher takes a SOAP message body and performs the requested action by calling a method on the given object of
 * type T (the "service object").
 *
 * The task of the dispatcher is (1) finding out which method must be called on the service object and (2) deserializing
 * the data in the SOAP message so that it can be passed to one of the methods of the service object.
 *
 * @tparam T The type of the service object
 */
trait Dispatcher[T] {
  def dispatch(body: Body, service: => T): Body
}

abstract class AbstractDispatcher[T](log: LogFunc)(implicit evidence: OcppService[T]) extends Dispatcher[T] {
  implicit def faultToBody(x: soapenvelope12.Fault) = x.asBody
  def version: Version.Value

  val actions: ActionEnumeration
  import actions.RichValue

  import scalax.RichAny

  def dispatch(body: Body, service: => T): Body = {
    val data = for {
      dataRecord <- body.any
      elem <- dataRecord.value.asInstanceOfOpt[Elem]
      action <- actions.fromElem(elem)
    } yield action -> elem

    data.headOption match {
      case None if body.any.isEmpty => ProtocolError("Body is empty")
      case None => NotSupported("No supported action found")
      case Some((action, xml)) => dispatch(action, xml, service)
    }
  }

  protected def dispatch(action: actions.Value, xml: NodeSeq, service: => T): Body

  protected def reqRes: ReqRes = new ReqRes {
    def apply[REQ: XMLFormat, RES: XMLFormat](action: actions.Value, xml: NodeSeq)(f: REQ => RES) = fromXMLEither[REQ](xml) match {
      case Left(msg) => ProtocolError(msg)
      case Right(req) => try {
        log(req)
        val res = f(req)
        log(res)
        simpleBody(DataRecord(Some(evidence.namespace(version)), Some(action.responseLabel), res))
      } catch {
        case FaultException(fault) =>
          log(fault)
          fault
      }
    }
  }

  protected def fault(x: soapenvelope12.Fault): Nothing = throw new FaultException(x)

  trait ReqRes {
    def apply[REQ: XMLFormat, RES: XMLFormat](action: actions.Value, xml: NodeSeq)(f: REQ => RES): Body
  }
}

case class FaultException(fault: soapenvelope12.Fault) extends Exception(fault.toString)
