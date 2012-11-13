package com.thenewmotion.chargenetwork.ocpp

import org.specs2.mutable.SpecificationWithJUnit
import OcppCs._
import com.thenewmotion.time.Imports._
import scalaxb.CanWriteXML

/**
 * @author Yaroslav Klymko
 */
class OcppCsSpec extends SpecificationWithJUnit {
  "Action" should {
    "unapply xml" >> {
      def unapply[T](t: T, label: String)(implicit format: CanWriteXML[T]) = Action.unapply(toXml(t, label))

      unapply(AuthorizeRequest("idTag"), "authorizeRequest") must beSome(Authorize)
      unapply(AuthorizeRequest("idTag"), "authorizerequest") must beSome(Authorize)
      unapply(AuthorizeRequest("idTag"), "AUTHORIZEREQUEST") must beSome(Authorize)
      unapply(StartTransactionRequest(1, "idTag", DateTime.now, 100), "startTransactionRequest") must beSome(StartTransaction)
      unapply(StopTransactionRequest(13, None, DateTime.now, 1), "stopTransactionRequest") must beSome(StopTransaction)
      unapply(BootNotificationRequest("vendor", "model"), "bootNotificationRequest") must beSome(BootNotification)
      unapply(DiagnosticsStatusNotificationRequest(Uploaded), "diagnosticsStatusNotificationRequest") must beSome(DiagnosticsStatusNotification)
      unapply(FirmwareStatusNotificationRequest(Downloaded), "firmwareStatusNotificationRequest") must beSome(FirmwareStatusNotification)
      unapply(HeartbeatRequest(), "heartbeatRequest") must beSome(Heartbeat)
      unapply(MeterValuesRequest(1), "meterValuesRequest") must beSome(MeterValues)
      unapply(StatusNotificationRequest(1, Available, NoError), "statusNotificationRequest") must beSome(StatusNotification)
    }
  }
}