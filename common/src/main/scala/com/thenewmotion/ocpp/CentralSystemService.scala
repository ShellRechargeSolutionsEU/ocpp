package com.thenewmotion.ocpp

import org.joda.time.DateTime
import centralsystem._

/**
 * @author Yaroslav Klymko
 */
trait CentralSystemService extends CentralSystem {

  type TransactionId = Int

  def authorize(idTag: String): IdTagInfo

  def startTransaction(connector: ConnectorScope,
                       idTag: IdTag,
                       timestamp: DateTime,
                       meterStart: Int,
                       reservationId: Option[Int]): (TransactionId, IdTagInfo)

  def stopTransaction(transactionId: TransactionId,
                      idTag: Option[IdTag],
                      timestamp: DateTime,
                      meterStop: Int,
                      transactionData: List[TransactionData]): Option[IdTagInfo]

  def heartbeat: DateTime

  def meterValues(scope: Scope, transactionId: Option[TransactionId], meters: List[Meter])

  def bootNotification(chargePointVendor: String,
                       chargePointModel: String,
                       chargePointSerialNumber: Option[String],
                       chargeBoxSerialNumber: Option[String],
                       firmwareVersion: Option[String],
                       iccid: Option[String],
                       imsi: Option[String],
                       meterType: Option[String],
                       meterSerialNumber: Option[String]): BootNotificationRes

  @throws[ActionNotSupportedException]
  def statusNotification(scope: Scope, status: ChargePointStatus, timestamp: Option[DateTime], vendorId: Option[String])

  def firmwareStatusNotification(status: FirmwareStatus.Value)

  def diagnosticsStatusNotification(uploaded: Boolean)

  @throws[ActionNotSupportedException]
  def dataTransfer(vendorId: String, messageId: Option[String], data: Option[String]): DataTransferRes


  def apply(req: Req): Res = req match {
    case x: AuthorizeReq => AuthorizeRes(authorize(x.idTag))

    case x: StartTransactionReq =>
      val (transactionId: TransactionId, idTag:IdTagInfo) = startTransaction(connector = x.connector,
        idTag = x.idTag,
        timestamp = x.timestamp,
        meterStart = x.meterStart,
        reservationId = x.reservationId)
      StartTransactionRes(transactionId = transactionId, idTag = idTag)

    case x: StopTransactionReq => StopTransactionRes(stopTransaction(
      transactionId = x.transactionId,
      idTag = x.idTag,
      timestamp = x.timestamp,
      meterStop = x.meterStop,
      transactionData = x.transactionData))

    case HeartbeatReq => HeartbeatRes(heartbeat)

    case x: MeterValuesReq =>
      meterValues(scope = x.scope, transactionId = x.transactionId, meters = x.meters)
      MeterValuesRes

    case x: BootNotificationReq => bootNotification(
      chargePointVendor = x.chargePointVendor,
      chargePointModel = x.chargePointModel,
      chargePointSerialNumber = x.chargePointSerialNumber,
      chargeBoxSerialNumber = x.chargeBoxSerialNumber,
      firmwareVersion = x.firmwareVersion,
      iccid = x.iccid,
      imsi = x.imsi,
      meterType = x.meterType,
      meterSerialNumber = x.meterSerialNumber)

    case x: StatusNotificationReq =>
      statusNotification(scope = x.scope, status = x.status, timestamp = x.timestamp, vendorId = x.vendorId)
      StatusNotificationRes

    case x: FirmwareStatusNotificationReq =>
      firmwareStatusNotification(status = x.status)
      FirmwareStatusNotificationRes

    case x: DiagnosticsStatusNotificationReq =>
      diagnosticsStatusNotification(uploaded = x.uploaded)
      DiagnosticsStatusNotificationRes

    case x: DataTransferReq => dataTransfer(vendorId = x.vendorId, messageId = x.messageId, data = x.data)
  }
}