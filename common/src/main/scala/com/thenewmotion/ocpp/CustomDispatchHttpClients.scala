package com.thenewmotion.ocpp

import scalaxb.HttpClients
import dispatch._
import scala.concurrent.duration._
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * @author Yaroslav Klymko
 */
abstract class CustomDispatchHttpClients(http: Http) extends HttpClients {
  val httpClient = new HttpClient {

    def request(in: String, address: Uri, headers: Map[String, String]): String = {
      def request(headers: Map[String, String]): String = {
        httpLogger.debug(s">>\n\t${headers.toSeq.mkString("\n\t")}\n\t$in")
        val req = url(address.toString) << in <:< headers
        val out = Await.result(http(req > as.String), 3 seconds)
        httpLogger.debug(s"<<\n\t$out")
        out
      }
      request(SoapActionHeader(headers).fold(headers)(headers + _))
    }
  }
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