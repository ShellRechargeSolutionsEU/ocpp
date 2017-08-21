package com.thenewmotion.ocpp.json

import java.time.{ZonedDateTime, ZoneId}

import org.json4s.JsonAST._
import org.json4s.CustomSerializer
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME

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

