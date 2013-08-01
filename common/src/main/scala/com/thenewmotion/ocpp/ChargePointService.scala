package com.thenewmotion.ocpp

import org.joda.time.DateTime
import chargepoint._

/**
 * @author Yaroslav Klymko
 */
trait ChargePointService extends ChargePoint {
  type Accepted = Boolean
  type FileName = String

  def remoteStartTransaction(idTag: IdTag, connector: Option[ConnectorScope]): Accepted

  def remoteStopTransaction(transactionId: Int): Accepted

  def unlockConnector(connector: ConnectorScope): Accepted

  def getDiagnostics(location: Uri,
                     startTime: Option[DateTime],
                     stopTime: Option[DateTime],
                     retries: Retries): Option[FileName]

  def changeConfiguration(key: String, value: String): ConfigurationStatus.Value

  @throws[ActionNotSupportedException]
  def getConfiguration(keys: List[String]): GetConfigurationRes

  def changeAvailability(scope: Scope, availabilityType: AvailabilityType.Value): AvailabilityStatus.Value

  def clearCache: Boolean

  def reset(resetType: ResetType.Value): Accepted

  def updateFirmware(retrieveDate: DateTime,
                     location: Uri,
                     retries: Retries)

  @throws[ActionNotSupportedException]
  def sendLocalList(updateType: UpdateType.Value,
                    listVersion: AuthListSupported,
                    localAuthorisationList: List[AuthorisationData],
                    hash: Option[String]): UpdateStatus.Value

  @throws[ActionNotSupportedException]
  def getLocalListVersion: AuthListVersion

  @throws[ActionNotSupportedException]
  def dataTransfer(vendorId: String, messageId: Option[String], data: Option[String]): DataTransferRes

  @throws[ActionNotSupportedException]
  def reserveNow(connector: Scope,
                 expiryDate: DateTime,
                 idTag: IdTag,
                 parentIdTag: Option[String] = None,
                 reservationId: Int): Reservation.Value

  @throws[ActionNotSupportedException]
  def cancelReservation(reservationId: Int): Boolean

  def apply(req: Req) = req match {
    case x: RemoteStartTransactionReq => RemoteStartTransactionRes(remoteStartTransaction(
      idTag = x.idTag,
      connector = x.connector))

    case x: RemoteStopTransactionReq => RemoteStopTransactionRes(remoteStopTransaction(x.transactionId))

    case x: UnlockConnectorReq => UnlockConnectorRes(unlockConnector(x.connector))

    case x: GetDiagnosticsReq => GetDiagnosticsRes(getDiagnostics(
      location = x.location,
      startTime = x.startTime,
      stopTime = x.stopTime,
      retries = x.retries))

    case x: ChangeConfigurationReq => ChangeConfigurationRes(changeConfiguration(key = x.key, value = x.value))

    case x: GetConfigurationReq => getConfiguration(x.keys)

    case x: ChangeAvailabilityReq => ChangeAvailabilityRes(changeAvailability(
      scope = x.scope,
      availabilityType = x.availabilityType))

    case ClearCacheReq => ClearCacheRes(clearCache)

    case x: ResetReq => ResetRes(reset(x.resetType))

    case x: UpdateFirmwareReq =>
      updateFirmware(retrieveDate = x.retrieveDate, location = x.location, retries = x.retries)
      UpdateFirmwareRes

    case x: SendLocalListReq => SendLocalListRes(sendLocalList(
      updateType = x.updateType,
      listVersion = x.listVersion,
      localAuthorisationList = x.localAuthorisationList,
      hash = x.hash))

    case GetLocalListVersionReq => GetLocalListVersionRes(getLocalListVersion)

    case x: DataTransferReq => dataTransfer(
      vendorId = x.vendorId,
      messageId = x.messageId,
      data = x.data)

    case x: ReserveNowReq => ReserveNowRes(reserveNow(
      connector = x.connector,
      expiryDate = x.expiryDate,
      idTag = x.idTag,
      parentIdTag = x.parentIdTag,
      reservationId = x.reservationId))

    case x: CancelReservationReq => CancelReservationRes(cancelReservation(x.reservationId))
  }
}