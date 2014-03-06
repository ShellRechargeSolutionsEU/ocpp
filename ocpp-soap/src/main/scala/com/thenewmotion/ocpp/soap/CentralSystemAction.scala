package com.thenewmotion.ocpp
package soap

/**
 * @author Yaroslav Klymko
 */
object CentralSystemAction extends ActionEnumeration {
  val Authorize,
  StartTransaction,
  StopTransaction,
  BootNotification,
  DiagnosticsStatusNotification,
  FirmwareStatusNotification,
  Heartbeat,
  MeterValues,
  StatusNotification,
  DataTransfer = Value
}
