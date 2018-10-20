package com.thenewmotion.ocpp
package json

import java.net.URI
import scala.util.{Failure, Success, Try}
import enums.reflection.EnumUtils.{Enumerable, Nameable}
import org.json4s.MappingException

private[json] trait SerializationCommon {
  /**
   * Tries to get select the enumerable value whose name is equal to the given string. If no such enum value exists,
   * throws a net.liftweb.json.MappingException.
   */
  def enumerableFromJsonString[T <: Nameable](enum: Enumerable[T], s: String): T =
    enum.withName(s) match {
      case None =>
        throw new MappingException(s"Value $s is not valid for ${enum.getClass.getSimpleName}")
      case Some(v) => v
    }

  def noneIfDefault[T <: Nameable](enumerable: messages.v1x.EnumerableWithDefault[T], actual: T): Option[String] =
    if (actual == enumerable.default) None else Some(actual.name)

  def defaultIfNone[T <: Nameable](enumerable: messages.v1x.EnumerableWithDefault[T], str: Option[String]): T =
    str.map(enumerableFromJsonString(enumerable, _)).getOrElse(enumerable.default)

  def noneIfEmpty[T](l: List[T]): Option[List[T]] =
    if (l.isEmpty) None else Some(l)

  def emptyIfNone[T](o: Option[List[T]]): List[T] =
    o.getOrElse(List.empty[T])

  /**
   * Parses a URI and throws a lift-json MappingException if the syntax is wrong
   */
  def parseURI(s: String) = Try {
    new URI(s)
  } match {
    case Failure(e: Exception) => throw MappingException(s"Could not parse URL $s in OCPP-JSON message", e)
    case Failure(e)            => throw e
    case Success(u)            => u
  }
}
