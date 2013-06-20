package com.thenewmotion.ocpp

import org.specs2.mutable.SpecificationWithJUnit
import scala.xml.{NamespaceBinding, TopScope, NodeSeq}
import org.specs2.mock.Mockito

/**
 * @author Yaroslav Klymko
 */
class WsaAddressingSpec extends SpecificationWithJUnit with Mockito {
  "WsaAddressing.apply" should {
    "add wsa:addressing header if action and replyTo defined" in {
      val scope = TopScope
      val headers = NodeSeq.Empty
      val wsaAddressing = new WsaAddressing(() => "")

      for {
        action <- List(Some(new Uri("/action")), None)
        endpoint <- List(Some(new Uri("http://address.com")), None)
      } {
        val f = mock[(NodeSeq, NamespaceBinding) => Unit]
        wsaAddressing(endpoint, action, headers, scope)(f)

        (action, endpoint) match {
          case (Some(a), Some(e)) => there was one(f).apply(wsaAddressing.headers(a, e), WsaAddressing.scope(scope))
          case _ => there was one(f).apply(headers, scope)
        }
      }
    }
  }
}
