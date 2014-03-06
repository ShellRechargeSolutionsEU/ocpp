package com.thenewmotion.ocpp
package soap

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