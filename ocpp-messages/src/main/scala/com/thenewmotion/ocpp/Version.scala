package com.thenewmotion.ocpp

import enums.reflection.EnumUtils.{Enumerable, Nameable}

trait Version extends Nameable

object Version extends Enumerable[Version] {
  case object V12 extends Version
  case object V15 extends Version
  case object V16 extends Version

  val values = Set(V12, V15, V16)
}
