package com.thenewmotion.ocpp.json

import org.json4s.JsonAST._
import org.json4s.CustomSerializer
import com.thenewmotion.time.Imports._
import org.joda.time.format.ISODateTimeFormat

class JodaDateTimeJsonFormat extends CustomSerializer[DateTime](format => (
  {
    case JString(formattedDate) => ISODateTimeFormat.dateOptionalTimeParser().parseDateTime(formattedDate)
  },
  {
    case x: DateTime => JString(x.toJsonString)
  }))

