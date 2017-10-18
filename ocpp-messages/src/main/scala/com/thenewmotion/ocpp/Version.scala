package com.thenewmotion.ocpp

import enums.EnumUtils.{Enumerable, Nameable}

sealed trait Version extends Nameable

object Version extends Enumerable[Version] {
  case object V12 extends Version { override def name = "1.2" }
  case object V15 extends Version { override def name = "1.5" }
  case object V16 extends Version { override def name = "1.6" }

  val values = Set(V12, V15, V16)
}
