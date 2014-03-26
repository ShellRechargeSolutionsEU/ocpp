package com.thenewmotion.ocpp.json

import net.liftweb.json.JsonAST._
import net.liftweb.json.CustomSerializer
import com.thenewmotion.time.Imports._
import org.joda.time.format.ISODateTimeFormat

class JodaDateTimeJsonFormat extends CustomSerializer[DateTime](format => (
  {
    case JString(formattedDate) => ISODateTimeFormat.dateOptionalTimeParser().parseDateTime(formattedDate)
  },
  {
    case x: DateTime => JString(x.toJsonString)
  }))

