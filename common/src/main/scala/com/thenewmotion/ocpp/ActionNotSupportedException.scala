package com.thenewmotion.ocpp

/**
 * @author Yaroslav Klymko
 */
case class ActionNotSupportedException(v: Version.Value, action: String)
  extends Exception(s"Action '$action' is not supported in ocpp $v")