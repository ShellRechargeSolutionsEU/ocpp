package com.thenewmotion.ocpp
package json.v20.serialization

import messages.v20.ChargingRate
import org.json4s.CustomSerializer
import org.json4s.JsonAST.JDecimal

object ChargingRateSerializer extends CustomSerializer[ChargingRate](_ => ({
  case JDecimal(r) => ChargingRate(r.toDouble)
}, {
  case ChargingRate(rate) => JDecimal(BigDecimal(rate).setScale(1))
}))
