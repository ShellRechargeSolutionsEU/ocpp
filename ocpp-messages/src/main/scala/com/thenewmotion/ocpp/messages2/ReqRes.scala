package com.thenewmotion.ocpp.messages2

import scala.language.higherKinds
import com.thenewmotion.ocpp.messages.ReqRes

sealed trait ReqResV2[REQ <: Request, RES <: Response] extends ReqRes[REQ, RES]

abstract class CsReqRes[REQ <: CsRequest : Manifest, RES <: CsResponse : Manifest] extends ReqResV2[REQ, RES]

abstract class CsmsReqRes[REQ <: CsmsRequest : Manifest, RES <: CsmsResponse : Manifest] extends ReqResV2[REQ, RES]

object CsmsReqRes {
  implicit object BootNotificationReqRes extends CsmsReqRes[BootNotificationRequest, BootNotificationResponse]
}
