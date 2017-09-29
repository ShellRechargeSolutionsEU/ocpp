package com.thenewmotion.ocpp
package soap

import com.github.t3hnar.scalax._
import enums.reflection.EnumUtils.{Enumerable, Nameable}
import soapenvelope12.Body

import scala.xml.Elem

/**
 * @author Yaroslav Klymko
 */
object SoapActionEnumeration {
  def fromHeader[T <: Nameable](e: Enumerable[T], header: String): Option[T] =
    StringOption(header).flatMap {
      value =>
        val name = value.substring(1)
        e.values.find(_.name equalsIgnoreCase name)
    }

  def fromBody[T <: Nameable](e: Enumerable[T], body: Body): Option[T] = (for {
    data <- body.any
    elem <- data.value.asInstanceOfOpt[Elem]
    action <- fromElem(e, elem)
  } yield action).headOption

  def fromElem[T <: Nameable](e: Enumerable[T], body: Elem): Option[T] = e.values.find(requestLabels(e)(_) equalsIgnoreCase body.label)

  private def labels[T <: Nameable](e: Enumerable[T], suffix: String): Map[T, String] = e.values.map {
    value =>
      val (head :: tail) = value.name.toList
      val label = (head.toLower :: tail).mkString + suffix
      value -> label
  }.toMap

  def requestLabels [T <: Nameable](e: Enumerable[T]): Map[T, String] = labels(e, "Request")
  def responseLabels[T <: Nameable](e: Enumerable[T]): Map[T, String] = labels(e, "Response")
}
