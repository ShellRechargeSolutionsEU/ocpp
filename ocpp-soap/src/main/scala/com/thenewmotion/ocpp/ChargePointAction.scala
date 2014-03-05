package com.thenewmotion.ocpp

/**
 * @author Yaroslav Klymko
 */
object ChargePointAction extends ActionEnumeration {
  val CancelReservation,
  ChangeAvailability,
  ChangeConfiguration,
  ClearCache,
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