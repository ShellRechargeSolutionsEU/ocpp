package com.thenewmotion.ocpp

import scalaxb.{HttpClients, SoapClients}
import scala.xml.{NamespaceBinding, NodeSeq}
import java.util.UUID

/**
 * @author Yaroslav Klymko
 */
trait WsaAddressingSoapClients extends SoapClients {
  this: HttpClients =>

  override lazy val soapClient = new WsaAddressingClient {}

  def endpoint: Option[Uri]

  trait WsaAddressingClient extends SoapClient {
    override def requestResponse(body: NodeSeq,
                                 headers: NodeSeq,
                                 scope: NamespaceBinding,
                                 address: Uri,
                                 webMethod: String,
                                 action: Option[Uri]) = WsaAddressing(endpoint, action, headers, scope) {
      (headers, scope) => super.requestResponse(body, headers, scope, address, webMethod, action)
    }
  }

}

object WsaAddressing {
  val Uri = "http://www.w3.org/2005/08/addressing"
  val AnonymousUri = Uri + "/anonymous"

  def scope(parent: NamespaceBinding): NamespaceBinding =
    NamespaceBinding("wsa", Uri, parent)

  def apply[T](endpoint: Option[Uri],
               action: Option[Uri],
               headers: NodeSeq,
               scope: NamespaceBinding)(f: (NodeSeq, NamespaceBinding) => T): T = {
    val withAddressing = for {
      a <- action
      e <- endpoint
    } yield f(this.headers(action = a, endpoint = e) ++ headers, this.scope(scope))

    withAddressing getOrElse f(headers, scope)
  }

  def headers(action: Uri, endpoint: Uri): NodeSeq = Seq(
    <wsa:MessageID>{UUID.randomUUID().toString}</wsa:MessageID>,
    <wsa:Action>{action}</wsa:Action>,
    <wsa:ReplyTo>
      <wsa:Address>{endpoint}</wsa:Address>
    </wsa:ReplyTo>,
    <wsa:From>
      <wsa:Address>{endpoint}</wsa:Address>
    </wsa:From>
  )
}

