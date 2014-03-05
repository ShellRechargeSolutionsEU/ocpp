package com.thenewmotion.ocpp

import com.thenewmotion.ocpp.soap.Version

/**
 * @author Yaroslav Klymko
 */
case class ActionNotSupportedException(v: Version.Value, action: String)
  extends Exception(s"Action '$action' is not supported in ocpp $v")