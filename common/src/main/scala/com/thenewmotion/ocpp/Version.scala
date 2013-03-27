package com.thenewmotion.ocpp

/**
 * @author Yaroslav Klymko
 */
sealed trait Version
case object V12 extends Version {
  override def toString = "1.2"
}
case object V15 extends Version {
  override def toString = "1.5"
}