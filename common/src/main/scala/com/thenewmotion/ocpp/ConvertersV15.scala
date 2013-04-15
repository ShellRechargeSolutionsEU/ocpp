package com.thenewmotion.ocpp

import com.thenewmotion.time.Imports._
import javax.xml.datatype.XMLGregorianCalendar

/**
 * @author Yaroslav Klymko
 */
object ConvertersV15 {

  import com.thenewmotion.ocpp
  import v15._

  implicit class RichIdTagInfo(self: ocpp.IdTagInfo) {
    def toV15: IdTagInfoType = {
      val status: AuthorizationStatusType = {
        import ocpp.{AuthorizationStatus => ocpp}
        self.status match {
          case ocpp.Accepted => AcceptedValue12
          case ocpp.IdTagBlocked => BlockedValue
          case ocpp.IdTagExpired => ExpiredValue
          case ocpp.IdTagInvalid => InvalidValue
          case ocpp.ConcurrentTx => ConcurrentTxValue
        }
      }
      IdTagInfoType(status, self.expiryDate.map(implicitly[XMLGregorianCalendar](_)), self.parentIdTag)
    }

    def toIdTagInfo: IdTagInfo = {
      val status: AuthorizationStatus = {
        import ocpp.{AuthorizationStatus => ocpp}
        self.status match {
          case ocpp.Accepted => AcceptedValue5
          case ocpp.IdTagBlocked => Blocked
          case ocpp.IdTagExpired => Expired
          case ocpp.IdTagInvalid => Invalid
          case ocpp.ConcurrentTx => ConcurrentTx
        }
      }
      IdTagInfo(status, self.expiryDate.map(implicitly[XMLGregorianCalendar](_)), self.parentIdTag)
    }
  }

}