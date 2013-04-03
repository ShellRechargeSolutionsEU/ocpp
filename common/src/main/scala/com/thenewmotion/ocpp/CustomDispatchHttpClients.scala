package com.thenewmotion.ocpp

import scalaxb.HttpClients
import dispatch._
import java.net.URI

/**
 * @author Yaroslav Klymko
 */
abstract class CustomDispatchHttpClients(http: Http) extends HttpClients {
  val httpClient = new HttpClient {

    def request(in: String, address: java.net.URI, headers: Map[String, String]): String = {
      val req = url(address.toString) << in <:< headers
      http(req > as.String)()
    }
  }
}