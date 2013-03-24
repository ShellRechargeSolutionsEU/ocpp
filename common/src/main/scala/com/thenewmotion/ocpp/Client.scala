package com.thenewmotion.ocpp

import javax.xml.datatype.XMLGregorianCalendar
import com.thenewmotion.time.Imports._
import scalaxb.Fault

/**
 * @author Yaroslav Klymko
 */
trait Client {
  protected def rightOrException[T](x: Either[Fault[Any], T]) = x match {
    case Left(fault) => sys.error(fault.toString) // TODO
    case Right(t) => t
  }

  protected implicit def fromOptionToOption[A, B](from: Option[A])(implicit conversion: A => B): Option[B] = from.map(conversion(_))
  protected implicit def dateTime(x: Option[XMLGregorianCalendar]): Option[DateTime] = fromOptionToOption(x)
  protected implicit def xmlGregorianCalendarOption(x: Option[DateTime]): Option[XMLGregorianCalendar] = fromOptionToOption(x)

  def chargeBoxIdentity: String

  protected def id = chargeBoxIdentity
}
