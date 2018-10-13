package com.thenewmotion.ocpp
package json

import scala.language.higherKinds
import messages._
import messages.v1x._
import messages.v20.{Request => _, Response => _, _}

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
  type OcppJsonClient[
    F <: VersionFamily,
    OQ <: Request,
    IS <: Response,
    ORR[_ <: OQ, _ <: IS] <: ReqRes[_, _],
    IQ <: Request,
    OS <: Response,
    IRR[_ <: IQ, _ <: OS] <: ReqRes[_, _]
  ] = client.OcppJsonClient[F, OQ, IS, ORR, IQ, OS, IRR]
  type Ocpp1XJsonClient = client.Ocpp1XJsonClient
  type Ocpp20JsonClient = client.Ocpp20JsonClient

  val OcppJsonServer: server.OcppJsonServer.type = server.OcppJsonServer
  type OcppJsonServer[
    F <: VersionFamily,
    OQ <: Request,
    IS <: Response,
    ORR[_ <: OQ, _ <: IS] <: ReqRes[_, _],
    IQ <: Request,
    OS <: Response,
    IRR[_ <: IQ, _ <: OS] <: ReqRes[_, _]
  ] = server.OcppJsonServer[F, OQ, IS, ORR, IQ, OS, IRR]
  val Ocpp1XJsonServer: server.Ocpp1XJsonServer.type = server.Ocpp1XJsonServer
  type Ocpp1XJsonServer = server.Ocpp1XJsonServer
  val Ocpp20JsonServer: server.Ocpp20JsonServer.type = server.Ocpp20JsonServer
  type Ocpp20JsonServer = server.Ocpp20JsonServer
}
