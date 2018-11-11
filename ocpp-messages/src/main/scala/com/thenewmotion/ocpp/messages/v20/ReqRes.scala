package com.thenewmotion.ocpp
package messages
package v20

abstract class CsReqRes[REQ <: CsRequest, RES <: CsResponse] extends ReqRes[REQ, RES]

abstract class CsmsReqRes[REQ <: CsmsRequest, RES <: CsmsResponse] extends ReqRes[REQ, RES]

object CsReqRes {
  implicit object RequestStartTransactionReqRes extends CsReqRes[RequestStartTransactionRequest, RequestStartTransactionResponse]
}

object CsmsReqRes {
  implicit object BootNotificationReqRes   extends CsmsReqRes[BootNotificationRequest  , BootNotificationResponse  ]
  implicit object HeartbeatReqRes          extends CsmsReqRes[HeartbeatRequest         , HeartbeatResponse         ]
  implicit object TransactionEventReqRes   extends CsmsReqRes[TransactionEventRequest  , TransactionEventResponse  ]
  implicit object StatusNotificationReqRes extends CsmsReqRes[StatusNotificationRequest, StatusNotificationResponse]
}
