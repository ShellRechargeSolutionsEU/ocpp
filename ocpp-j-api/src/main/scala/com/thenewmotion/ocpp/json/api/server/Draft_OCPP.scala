/*
 * Adapted from the "Draft_OCPP16" example on
 * https://github.com/TooTallNate/Java-WebSocket/wiki/Implementing-your-own-Sec-WebSocket-Protocol
 */
package com.thenewmotion.ocpp
package json.api
package server

import org.java_websocket.drafts.Draft.HandshakeState
import org.java_websocket.drafts.{Draft, Draft_6455}
import org.java_websocket.handshake._

class Draft_OCPP(version: Version) extends Draft_6455 {

  private val protosForVersions = Map[Version, String](
    Version.V15 -> "ocpp1.5",
    Version.V16 -> "ocpp1.6"
  )

  private val protoHeader = protosForVersions.getOrElse(version, {
    sys.error(s"Version $version is not supported with OCPP-J")
  })

  override def acceptHandshakeAsServer(handshakedata: ClientHandshake): HandshakeState = {
    if (super.acceptHandshakeAsServer(handshakedata) == HandshakeState.NOT_MATCHED)
      HandshakeState.NOT_MATCHED
    else clientHandshakeMatchesOurOcppVersion(handshakedata)
  }

  override def acceptHandshakeAsClient(request: ClientHandshake, response: ServerHandshake): HandshakeState = {
    if (super.acceptHandshakeAsClient(request, response) == HandshakeState.NOT_MATCHED)
      HandshakeState.NOT_MATCHED
    else clientHandshakeMatchesOurOcppVersion(request)
  }

  private def clientHandshakeMatchesOurOcppVersion(req: ClientHandshake): HandshakeState = {
    Option(req.getFieldValue("Sec-WebSocket-Protocol")) match {
      case Some(requestedProto) if requestedProto.equalsIgnoreCase(protoHeader) => HandshakeState.MATCHED
      case _ => HandshakeState.NOT_MATCHED
    }
  }

  override def postProcessHandshakeRequestAsClient(request: ClientHandshakeBuilder): ClientHandshakeBuilder = {
    super.postProcessHandshakeRequestAsClient(request)
    request.put("Sec-WebSocket-Protocol", protoHeader)
    request
  }

  override def postProcessHandshakeResponseAsServer(
    request: ClientHandshake,
    response: ServerHandshakeBuilder
  ): HandshakeBuilder = {
    val result: HandshakeBuilder = super.postProcessHandshakeResponseAsServer(request, response)
    result.put("Sec-WebSocket-Protocol", protoHeader)
    result
  }

  override def copyInstance(): Draft = new Draft_OCPP(version)
}
