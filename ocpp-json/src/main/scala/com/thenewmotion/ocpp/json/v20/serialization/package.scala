package com.thenewmotion.ocpp
package json
package v20

import messages.v20._

package object serialization {

  val ocppSerializers = Seq(
    new EnumerableNameSerializer(BootReason),
    InstantSerializer,
    ChargingRateSerializer,
    new EnumerableNameSerializer(BootNotificationStatus),
    new EnumerableNameSerializer(IdTokenType),
    new EnumerableNameSerializer(ChargingProfilePurpose),
    new EnumerableNameSerializer(ChargingProfileKind),
    new EnumerableNameSerializer(RecurrencyKind),
    new EnumerableNameSerializer(ChargingRateUnit),
    new EnumerableNameSerializer(RequestStartStopStatus)
  )
}
