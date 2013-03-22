package com.thenewmotion.chargenetwork.ocpp

import scalaxb.HttpClients

/**
 * @author Yaroslav Klymko
 */
trait FixedDispatchHttpClients extends HttpClients {
  val httpClient = new FixedDispatchHttpClient {}

  trait FixedDispatchHttpClient extends HttpClient {

    import dispatch._
    import com.thenewmotion.common.dispatch.Slf4jLogger

    val http = new Http with Slf4jLogger

    def request(in: String, address: java.net.URI, headers: Map[String, String]): String = {
      http x (url(address.toString) << (in) <:< headers as_str)
    }
  }
}