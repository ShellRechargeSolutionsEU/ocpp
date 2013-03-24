package com.thenewmotion.ocpp

import scalaxb.HttpClients
import dispatch._
import java.net.URI

/**
 * @author Yaroslav Klymko
 */
abstract class CustomDispatchHttpClients(http: Http) extends HttpClients {
  val httpClient = new HttpClient {
    def request(in: String, address: URI, headers: Map[String, String]) =
      http x (url(address.toString) << (in) <:< headers as_str)
  }
}