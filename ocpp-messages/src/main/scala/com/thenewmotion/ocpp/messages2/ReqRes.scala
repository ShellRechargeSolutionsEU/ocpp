package com.thenewmotion.ocpp.messages2

import com.thenewmotion.ocpp.messages.ReqRes

sealed trait CsReqRes[REQ <: CsRequest, RES <: CsResponse] extends ReqRes[REQ, RES]

sealed trait CsmsReqRes[REQ <: CsmsRequest, RES <: CsmsResponse] extends ReqRes[REQ, RES]
