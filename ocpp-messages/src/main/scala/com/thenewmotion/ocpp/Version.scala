package com.thenewmotion.ocpp

import enums.EnumUtils.{Enumerable, Nameable}

sealed trait Version extends Nameable
sealed trait Version1X extends Version

object Version extends Enumerable[Version] {
  case object V12 extends Version1X { override def name = "1.2" }
  case object V15 extends Version1X { override def name = "1.5" }
  case object V16 extends Version1X { override def name = "1.6" }
  case object V20 extends Version { override def name = "2.0" }

  val values = Set(V12, V15, V16)
}
