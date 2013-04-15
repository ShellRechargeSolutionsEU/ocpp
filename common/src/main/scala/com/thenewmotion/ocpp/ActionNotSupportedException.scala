package com.thenewmotion.ocpp

/**
 * @author Yaroslav Klymko
 */
case class ActionNotSupportedException(v: Version.Value, action: String)
  extends Exception("Action '%s' is not supported in ocpp %s".format(action, v))