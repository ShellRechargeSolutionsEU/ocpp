package com.thenewmotion.ocpp
package soap

import scala.language.implicitConversions
import xml.{Elem, NodeSeq}
import com.github.t3hnar.scalax.RichAny
import soapenvelope12.Body

/**
 * @author Yaroslav Klymko
 */
object VersionFromBody {

  import Version._

  private def fromNamespace(namespace: String): Option[Version.Value] = {
    val V12Regex = """^urn://Ocpp/C[sp]/2010/08/$""".r
    val V15Regex = """^urn://Ocpp/C[sp]/2012/06/$""".r


    namespace match {
      case V12Regex() => Some(V12)
      case V15Regex() => Some(V15)
      case _          => None
    }
  }

  def apply(body: NodeSeq): Option[Value] =
    for {
      n <- body.headOption
      e <- n.asInstanceOfOpt[Elem]
      v <- fromNamespace(e.namespace)
    } yield v

  def apply(body: Body): Option[Value] = (for {
    data <- body.any
    elem <- data.value.asInstanceOfOpt[Elem]
    v <- apply(elem)
  } yield v).headOption
}