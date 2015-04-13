package com.thenewmotion.ocpp
package soap

import com.github.t3hnar.scalax._
import soapenvelope12.Body
import scala.xml.Elem

/**
 * @author Yaroslav Klymko
 */
object SoapActionEnumeration {
  def fromHeader(e: Enumeration, header: String): Option[e.Value] =
    StringOption(header).flatMap {
      value =>
        val name = value.substring(1)
        e.values.find(_.toString equalsIgnoreCase name)
    }

  def fromBody(e: Enumeration, body: Body): Option[e.Value] = (for {
    data <- body.any
    elem <- data.value.asInstanceOfOpt[Elem]
    action <- fromElem(e, elem)
  } yield action).headOption

  def fromElem(e: Enumeration, body: Elem): Option[e.Value] = e.values.find(requestLabels(e)(_) equalsIgnoreCase body.label)

  private def labels(e: Enumeration, suffix: String): Map[e.Value, String] = e.values.map {
    value =>
      val (head :: tail) = value.toString.toList
      val label = (head.toLower :: tail).mkString + suffix
      value -> label
  }.toMap

  def requestLabels(e: Enumeration): Map[e.Value, String] = labels(e, "Request")
  def responseLabels(e: Enumeration): Map[e.Value, String] = labels(e, "Response")
}
