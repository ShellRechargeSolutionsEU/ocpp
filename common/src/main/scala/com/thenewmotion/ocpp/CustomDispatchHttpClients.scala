package com.thenewmotion.ocpp

import scalaxb.HttpClients
import dispatch._

/**
 * @author Yaroslav Klymko
 */
abstract class CustomDispatchHttpClients(http: Http) extends HttpClients {
  val httpClient = new HttpClient {

    def request(in: String, address: Uri, headers: Map[String, String]): String = {
      val soapAction = SoapActionHeader(headers)
      val req = url(address.toString) << in <:< soapAction.fold(headers)(headers + _)
      http(req > as.String)()
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