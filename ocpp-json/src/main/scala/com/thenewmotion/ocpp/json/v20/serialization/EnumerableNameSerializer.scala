package com.thenewmotion.ocpp.json.v20.serialization

import enums.reflection.EnumUtils.{Enumerable, Nameable}
import org.json4s.{CustomSerializer, JString}

class EnumerableNameSerializer[T <: Nameable : Manifest](e: Enumerable[T]) extends CustomSerializer[T](_ => ({
  case JString(n) if e.withName(n).isDefined => e.withName(n).getOrElse {
    sys.error("Enum value name recognized in isDefined but not in result!?")
  }
}, {
  {
    case n: T => JString(n.name)
  }
}))
