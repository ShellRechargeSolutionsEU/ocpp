package com.thenewmotion.ocpp.json.v1x

import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
import java.time.{ZoneId, ZonedDateTime}

import org.json4s.CustomSerializer
import org.json4s.JsonAST._

class ZonedDateTimeJsonFormat extends CustomSerializer[ZonedDateTime](format => (
  {
    case JString(formattedDate) =>
      ZonedDateTime
        .parse(formattedDate, ISO_OFFSET_DATE_TIME)
        .withZoneSameInstant(ZoneId.of("UTC"))
  },
  {
    case x: ZonedDateTime => JString(x.withZoneSameInstant(ZoneId.of("UTC")).format(ISO_OFFSET_DATE_TIME))
  }))

