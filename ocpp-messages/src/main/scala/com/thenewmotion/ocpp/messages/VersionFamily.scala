package com.thenewmotion.ocpp
package messages

import v1x.{CentralSystemReq, CentralSystemRes, ChargePointReq, ChargePointRes}
import v20.{CsRequest, CsResponse, CsmsRequest, CsmsResponse}

/**
  * A VersionFamily is a set of versions that use the same set of messages.
  * Currently the options are V1X (for OCPP 1.2, 1.5 and 1.6) and V20 (for OCPP
  * 2.0).
  *
  * OCPP 2.0 and the 1.x versions share the general RPC mechanism and
  * JSON serialization technique, but they have completely different
  * sets of messages.
  */
sealed trait VersionFamily
case object V1X extends VersionFamily
case object V20 extends VersionFamily

object VersionFamily {
  sealed trait MessageTypeForVersionFamily[VFam <: VersionFamily, MessageType <: Message]
  implicit object V1XChargePointRequest extends MessageTypeForVersionFamily[V1X.type, ChargePointReq]
  implicit object V1XChargePointResponse extends MessageTypeForVersionFamily[V1X.type, ChargePointRes]
  implicit object V1XCentralSystemRequest extends MessageTypeForVersionFamily[V1X.type, CentralSystemReq]
  implicit object V1XCentralSystemResponse extends MessageTypeForVersionFamily[V1X.type, CentralSystemRes]
  implicit object V20CsRequest extends MessageTypeForVersionFamily[V20.type, CsRequest]
  implicit object V20CsResponse extends MessageTypeForVersionFamily[V20.type, CsResponse]
  implicit object V20CsmsRequest extends MessageTypeForVersionFamily[V20.type, CsmsRequest]
  implicit object V20CsmsResponse extends MessageTypeForVersionFamily[V20.type, CsmsResponse]
}
