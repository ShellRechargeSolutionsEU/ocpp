package com.thenewmotion.ocpp
package soap

import scala.language.implicitConversions
import scala.xml.{Elem, NodeSeq}
import scalaxb.{DataRecord, XMLFormat, fromXMLEither}
import soapenvelope12.Body
import Fault._
import scala.concurrent.{ExecutionContext, Future}


/**
 * A dispatcher takes a SOAP message body and performs the requested action by calling the given function, and returns
 * the result as a SOAP message body again.
 *
 * Thus the task of the dispatcher is deserializing the data in the SOAP message so that it can be passed to one of the
 * methods of the service object.
 *
 * @tparam REQ The type of request that the processing function takes
 * @tparam RES The type of response that the processing function produces
 */
trait Dispatcher[REQ, RES] {
  def dispatch(body: Body, f: REQ => Future[RES])(implicit ec: ExecutionContext): Future[Body]
}

abstract class AbstractDispatcher[REQ, RES](implicit evidence: OcppService[REQ, RES]) extends Dispatcher[REQ, RES] {
  implicit def faultToBody(x: soapenvelope12.Fault) = x.asBody
  def version: Version.Value

  val actions: Enumeration

  import com.github.t3hnar.scalax.RichAny

  def dispatch(body: Body, f: REQ => Future[RES])
              (implicit ec: ExecutionContext): Future[Body] = {
    val data = for {
      dataRecord <- body.any
      elem <- dataRecord.value.asInstanceOfOpt[Elem]
      action <- SoapActionEnumeration.fromElem(actions, elem)
    } yield action -> elem

    data.headOption match {
      case None if body.any.isEmpty => Future.successful(ProtocolError("Body is empty"))
      case None => Future.successful(NotSupported("No supported action found"))
      case Some((action, xml)) => dispatch(action, xml, f)
    }
  }

  protected def dispatch(action: actions.Value, xml: NodeSeq, f: REQ => Future[RES])
                        (implicit ec: ExecutionContext): Future[Body]

  protected def reqRes: ReqRes = new ReqRes {
    def apply[XMLREQ, XMLRES](action: actions.Value, xml: NodeSeq, f: REQ => Future[RES])
                                                   (reqTrans: XMLREQ => REQ)
                                                   (resTrans: RES => XMLRES)
                                                   (implicit ec: ExecutionContext,
                                                    reqEv: XMLFormat[XMLREQ],
                                                    resEv: XMLFormat[XMLRES]) =
      fromXMLEither[XMLREQ](xml) match {
        case Left(msg) => Future.successful(ProtocolError(msg))
        case Right(xmlReq) => try {
          val req = reqTrans(xmlReq)
          f(req) map { res =>
            val xmlRes = resTrans(res)
            simpleBody(DataRecord(Some(evidence.namespace(version)), Some(SoapActionEnumeration.responseLabels(actions)(action)), xmlRes))
          } recover {
            case FaultException(fault) => fault
          }
        }
      }
  }

  protected def fault(x: soapenvelope12.Fault): Nothing = throw new FaultException(x)

  trait ReqRes {
    def apply[XMLREQ, XMLRES](action: actions.Value, xml: NodeSeq, f: REQ => Future[RES])
                                                   (reqTrans: XMLREQ => REQ)
                                                   (resTrans: RES => XMLRES)
                                                   (implicit ec: ExecutionContext,
                                                    reqEv: XMLFormat[XMLREQ],
                                                    resEv: XMLFormat[XMLRES]): Future[Body]
  }
}

case class FaultException(fault: soapenvelope12.Fault) extends Exception(fault.toString)
