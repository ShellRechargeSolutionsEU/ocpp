package com.thenewmotion.ocpp.soap

import scalaxb.{HttpClients, SoapClients}
import scala.xml.NodeSeq
import com.thenewmotion.ocpp._
import scala.xml.NamespaceBinding
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

object WsaAddressing extends WsaAddressing(() => UUID.randomUUID().toString)

class WsaAddressing(messageId: () => String) {
  val Uri = "http://www.w3.org/2005/08/addressing"
  val AnonymousUri = Uri + "/anonymous"

  def scope(parent: NamespaceBinding): NamespaceBinding =
    NamespaceBinding("wsa", Uri, parent)

  def apply[T](endpoint: Option[Uri],
               action: Option[Uri],
               headers: NodeSeq,
               scope: NamespaceBinding)(f: (NodeSeq, NamespaceBinding) => T): T = {

    val wsaAddressing = this.headers(endpoint, action)

    if (wsaAddressing.isEmpty) f(headers, scope)
    else f(wsaAddressing ++ headers, this.scope(scope))
  }

  def headers(endpoint: Option[Uri], action: Option[Uri]) = {
    val wsaMessageId = if (action.isDefined || endpoint.isDefined)
      <wsa:MessageID>uuid:{messageId()}</wsa:MessageID>
    else NodeSeq.Empty

    val wsaAction = action.map(x => <wsa:Action>{x}</wsa:Action>) getOrElse NodeSeq.Empty
    val wsaEndpoint = endpoint.map(x => <wsa:From><wsa:Address>{x}</wsa:Address></wsa:From>) getOrElse NodeSeq.Empty
    wsaMessageId ++ wsaAction ++ wsaEndpoint
  }
}

