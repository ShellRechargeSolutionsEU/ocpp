package com.thenewmotion.ocpp.messages.centralsystem

/**
 * @author Yaroslav Klymko
 */
object CentralSystemAction extends Enumeration {
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
