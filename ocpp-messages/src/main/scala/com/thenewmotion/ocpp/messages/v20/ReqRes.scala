package com.thenewmotion.ocpp
package messages
package v20

abstract class CsReqRes  [REQ <: CsRequest  , RES <: CsResponse  ] extends ReqRes[REQ, RES]

abstract class CsmsReqRes[REQ <: CsmsRequest, RES <: CsmsResponse] extends ReqRes[REQ, RES]

object CsReqRes {
  implicit object GetBaseReportReqRes           extends CsReqRes[GetBaseReportRequest          , GetBaseReportResponse          ]
  implicit object GetTransactionStatusReqRes    extends CsReqRes[GetTransactionStatusRequest   , GetTransactionStatusResponse   ]
  implicit object GetVariablesReqRes            extends CsReqRes[GetVariablesRequest           , GetVariablesResponse           ]
  implicit object RequestStartTransactionReqRes extends CsReqRes[RequestStartTransactionRequest, RequestStartTransactionResponse]
  implicit object RequestStopTransactionReqRes  extends CsReqRes[RequestStopTransactionRequest , RequestStopTransactionResponse ]
  implicit object SendLocalListReqRes           extends CsReqRes[SendLocalListRequest          , SendLocalListResponse          ]
  implicit object SetVariablesReqRes            extends CsReqRes[SetVariablesRequest           , SetVariablesResponse           ]
}

object CsmsReqRes {
  implicit object AuthorizeReqRes          extends CsmsReqRes[AuthorizeRequest         , AuthorizeResponse         ]
  implicit object BootNotificationReqRes   extends CsmsReqRes[BootNotificationRequest  , BootNotificationResponse  ]
  implicit object HeartbeatReqRes          extends CsmsReqRes[HeartbeatRequest         , HeartbeatResponse         ]
  implicit object NotifyReportReqRes       extends CsmsReqRes[NotifyReportRequest      , NotifyReportResponse      ]
  implicit object StatusNotificationReqRes extends CsmsReqRes[StatusNotificationRequest, StatusNotificationResponse]
  implicit object TransactionEventReqRes   extends CsmsReqRes[TransactionEventRequest  , TransactionEventResponse  ]
}
