package com.thenewmotion.ocpp

import xml.{Elem, NodeSeq}
import scalax.richAny
import soapenvelope12.Body

/**
 * @author Yaroslav Klymko
 */
object Version extends Enumeration {
  val V12 = Value("1.2")
  val V15 = Value("1.5")

  implicit def richVersion(x: Value): RichVersion = new RichVersion(x)

  class RichVersion(self: Value) {
    def namespace: String = self match {
      case V12 => "urn://Ocpp/Cs/2010/08/"
      case V15 => "urn://Ocpp/Cs/2012/06/"
    }
  }

  def fromBody(body: NodeSeq): Option[Value] = for {
    n <- body.headOption
    e <- n.asInstanceOfOpt[Elem]
    v <- values.find(_.namespace == e.namespace)
  } yield v

  def fromBody(body: Body): Option[Value] = (for {
    data <- body.any
    elem <- data.value.asInstanceOfOpt[Elem]
    v <- fromBody(elem)
  } yield v).headOption
}