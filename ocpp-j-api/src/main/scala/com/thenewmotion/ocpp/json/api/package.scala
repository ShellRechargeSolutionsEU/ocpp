package com.thenewmotion.ocpp
package json

import messages.v1x._
import messages.v20._

package object api {

  type ChargePointRequestHandler =
    RequestHandler[ChargePointReq, ChargePointRes, ChargePointReqRes]

  type CentralSystemRequestHandler =
    RequestHandler[CentralSystemReq, CentralSystemRes, CentralSystemReqRes]

  type CsRequestHandler =
    RequestHandler[CsRequest, CsResponse, CsReqRes]

  type CsmsRequestHandler =
    RequestHandler[CsmsRequest, CsmsResponse, CsmsReqRes]

  val OcppJsonClient: client.OcppJsonClient.type = client.OcppJsonClient

  type OcppJsonServer = server.OcppJsonServer
  val OcppJsonServer: server.OcppJsonServer.type = server.OcppJsonServer
}
