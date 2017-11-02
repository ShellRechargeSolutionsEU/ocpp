package com.thenewmotion.ocpp
package json

import messages._

package object api {

  type ChargePointRequestHandler =
    RequestHandler[ChargePointReq, ChargePointRes, ChargePointReqRes]

  type CentralSystemRequestHandler =
    RequestHandler[CentralSystemReq, CentralSystemRes, CentralSystemReqRes]

  type OcppJsonClient = client.OcppJsonClient
  val OcppJsonClient: client.OcppJsonClient.type = client.OcppJsonClient

  type OcppJsonServer = server.OcppJsonServer
  val OcppJsonServer: server.OcppJsonServer.type = server.OcppJsonServer

}
