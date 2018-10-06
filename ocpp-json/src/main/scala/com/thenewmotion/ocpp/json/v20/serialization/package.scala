package com.thenewmotion.ocpp
package json
package v20

import messages.v20._

package object serialization {

  val ocppSerializers = Seq(
    new EnumerableNameSerializer(BootReason),
    InstantSerializer,
    new EnumerableNameSerializer(BootNotificationStatus)
  )
}
