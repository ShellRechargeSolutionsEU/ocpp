package com.thenewmotion.ocpp
package json

import messages._

package object api {

  type ChargePointRequestHandler =
    RequestHandler[ChargePointReq, ChargePointRes, ChargePointReqRes]

  type CentralSystemRequestHandler =
    RequestHandler[CentralSystemReq, CentralSystemRes, CentralSystemReqRes]

}
