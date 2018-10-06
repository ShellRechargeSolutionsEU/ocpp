package com.thenewmotion.ocpp

import enums.EnumUtils.{Enumerable, Nameable}

sealed trait Version extends Nameable {
  def family: VersionFamily
}

sealed trait Version1X extends Version {
  override val family = VersionFamily.V1X
}

object Version extends Enumerable[Version] {
  case object V12 extends Version1X { override val name = "1.2" }
  case object V15 extends Version1X { override val name = "1.5" }
  case object V16 extends Version1X { override val name = "1.6" }
  case object V20 extends Version {
    override val name = "2.0"
    override val family = VersionFamily.V20
  }

  val values = Set(V12, V15, V16, V20)
}
