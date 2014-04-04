package com.thenewmotion.ocpp.messages.chargepoint

/**
 * @author Yaroslav Klymko
 */
object ChargePointAction extends Enumeration {
  val CancelReservation,
  ChangeAvailability,
  ChangeConfiguration,
  ClearCache,
  DataTransfer,
  GetConfiguration,
  GetDiagnostics,
  GetLocalListVersion,
  RemoteStartTransaction,
  RemoteStopTransaction,
  ReserveNow,
  Reset,
  SendLocalList,
  UnlockConnector,
  UpdateFirmware = Value
}