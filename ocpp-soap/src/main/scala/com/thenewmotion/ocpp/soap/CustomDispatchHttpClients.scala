package com.thenewmotion.ocpp
package soap

import scalaxb.HttpClients
import dispatch._
import scala.concurrent.duration._
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

abstract class CustomDispatchHttpClients(http: Http) extends HttpClients {
  trait CustomDispatchHttpClient extends HttpClient {
    def request(in: String, address: Uri, headers: Map[String, String]): String = {
      def request(headers: Map[String, String]): String =
        requestWithLogging(in, address, headers)

      request(SoapActionHeader(headers).fold(headers)(headers + _))
    }

    protected def requestWithLogging(in: String, address: Uri, headers: Map[String, String]): String = {
      httpLogger.debug(s">>\n\t${headers.toSeq.mkString("\n\t")}\n\t$in")
      val out = requestIO(in, address, headers)
      httpLogger.debug(s"<<\n\t$out")
      out
    }

    protected def requestIO(in: String, address: Uri, headers: Map[String, String]): String = {
      val req = url(address.toString) << in <:< headers
      Await.result(http(req > as.String), CustomDispatchHttpClients.httpRequestTimeout)
    }
  }

  val httpClient = new CustomDispatchHttpClient {}
}

object CustomDispatchHttpClients {
  val httpRequestTimeout = 45.seconds
}

object SoapActionHeader {
  type Header = (String, String)
  private val headerName = "SOAPAction"

  def apply(headers: Map[String, String]): Option[Header] = for {
    contentType <- headers.collectFirst {
      case (key, value) if key equalsIgnoreCase "content-type" => value
    }
    action <- contentType.split(";").map(_.trim).find(_.toLowerCase.startsWith("action="))
    value <- action.split("=").toList match {
      case _ :: x :: Nil => Some(x)
      case _ => None
    }
  } yield headerName -> value
}
