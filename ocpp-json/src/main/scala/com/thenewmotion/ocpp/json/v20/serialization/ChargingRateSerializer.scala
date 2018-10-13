package com.thenewmotion.ocpp
package json.v20.serialization

import messages.v20.ChargingRate
import org.json4s.CustomSerializer
import org.json4s.JsonAST.{JDecimal, JString}

object ChargingRateSerializer extends CustomSerializer[ChargingRate](_ => ({
  case JString(t) => ChargingRate(t.toDouble)
}, {
  case ChargingRate(rate) => JDecimal(BigDecimal(rate).setScale(1))
}))
