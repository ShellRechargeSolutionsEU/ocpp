package com.thenewmotion.ocpp

import messages.{Request, Response, ReqRes}
import messages.v1x.{CentralSystemReq, CentralSystemReqRes, CentralSystemRes, ChargePointReq, ChargePointReqRes, ChargePointRes}
import messages.v20.{CsRequest, CsResponse, CsReqRes, CsmsRequest, CsmsResponse, CsmsReqRes}

import scala.language.higherKinds

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

object VersionFamily {

  case object V1X extends VersionFamily
  case object V20 extends VersionFamily


  /** A type class relating Message types to the version family that they're
    * used with
    *
    * E.g. relating ChangeConfigurationReq to V1X and TransactionEventRequest to V20
    *
    * @tparam VFam
    * @tparam MessageType
    */
  sealed trait MessageTypesForVersionFamily[
    VFam <: VersionFamily,
    ReqMessageType <: Request,
    ResMessageType <: Response,
    ReqResMessageType[Req <: ReqMessageType, Res <: ResMessageType] <: ReqRes[_,_]
  ]

  /** A type class that extends MessageTypeForVersionFamily to not only link a
    * message type to its version family, but also mark it as a message that is
    * sent to send or reply to a Charging Station/Charge Point request
    *
    * @tparam VFam
    * @tparam MessageType
    */
  sealed trait CsMessageTypesForVersionFamily[
    VFam <: VersionFamily,
    ReqMessageType <: Request,
    ResMessageType <: Response,
    ReqResMessageType[Req <: ReqMessageType, Res <: ResMessageType] <: ReqRes[_,_]
  ]

  /** A type class that extends MessageTypeForVersionFamily to not only link a
    * message type to its version family, but also mark it as a message that is
    * sent to send or reply to a CSMS/Central System request
    *
    * @tparam VFam
    * @tparam MessageType
    */
  sealed trait CsmsMessageTypesForVersionFamily[
    VFam <: VersionFamily,
    ReqMessageType <: Request,
    ResMessageType <: Response,
    ReqResMessageType[Req <: ReqMessageType, Res <: ResMessageType] <: ReqRes[_,_]
  ]

  implicit object V1XChargePointMessages extends CsMessageTypesForVersionFamily[
    V1X.type,
    ChargePointReq,
    ChargePointRes,
    ChargePointReqRes
  ]

  implicit object V1XCentralSystemMessages extends CsmsMessageTypesForVersionFamily[
    V1X.type,
    CentralSystemReq,
    CentralSystemRes,
    CentralSystemReqRes
  ]

  implicit object V20CsMessages extends CsMessageTypesForVersionFamily[
    V20.type,
    CsRequest,
    CsResponse,
    CsReqRes
  ]

  implicit object V20CsmsMessages extends CsmsMessageTypesForVersionFamily[
    V20.type,
    CsmsRequest,
    CsmsResponse,
    CsmsReqRes
  ]
}
