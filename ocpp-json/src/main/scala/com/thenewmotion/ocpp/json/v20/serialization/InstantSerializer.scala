package com.thenewmotion.ocpp.json.v20.serialization

import java.time.{Instant, ZoneId, ZonedDateTime}
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
import org.json4s.{CustomSerializer, JString}

object InstantSerializer extends CustomSerializer[Instant](_ => ({
  case JString(t) => ZonedDateTime.parse(t, ISO_OFFSET_DATE_TIME).toInstant
}, {
  case instant: Instant => JString(
    ZonedDateTime.ofInstant(instant, ZoneId.of("UTC")).format(ISO_OFFSET_DATE_TIME)
  )
}))
