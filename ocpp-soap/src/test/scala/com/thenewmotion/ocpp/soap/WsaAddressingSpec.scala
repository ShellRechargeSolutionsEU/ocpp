package com.thenewmotion.ocpp
package soap

import org.specs2.mutable.Specification
import scala.xml.{NamespaceBinding, TopScope, NodeSeq}
import org.specs2.mock.Mockito

/**
 * @author Yaroslav Klymko
 */
class WsaAddressingSpec extends Specification with Mockito {

  "WsaAddressing.apply" should {

    "add wsa:addressing header if action and replyTo defined" in {
      val scope = TopScope
      val headers = NodeSeq.Empty
      val wsaAddressing = new WsaAddressing(() => "aaa-bbb-ccc-ddd")

      val values = for {
        endpoint <- List(Some(new Uri("http://address.com")), None)
        action <- List(Some(new Uri("/action")), None)
      } yield endpoint -> action


      foreach(values) {
        case (endpoint, action) =>
          val f = mock[(NodeSeq, NamespaceBinding) => Unit]
          wsaAddressing(endpoint, action, headers, scope)(f)

          (endpoint, action) match {
            case (None, None) => there was one(f).apply(headers, scope)
            case (e, a) => there was one(f).apply(wsaAddressing.headers(e, a), wsaAddressing.scope(scope))
          }
      }
    }
  }
}
