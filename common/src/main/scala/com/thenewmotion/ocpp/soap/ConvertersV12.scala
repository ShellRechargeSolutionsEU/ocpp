package com.thenewmotion.ocpp
package soap

import com.thenewmotion.ocpp.v12

/**
 * @author Yaroslav Klymko
 */
object ConvertersV12 {

  import com.thenewmotion.ocpp
  import v12._

  implicit class RichRemoteStartStopStatus(val self: RemoteStartStopStatus) extends AnyVal {
    def toOcpp: Boolean = self match {
      case AcceptedValue5 => true
      case RejectedValue5 => false
    }
  }

  implicit class RichIdTagInfo(val self: IdTagInfo) extends AnyVal {
    def toOcpp: ocpp.IdTagInfo = {
      val status = {
        import ocpp.{AuthorizationStatus => ocpp}
        self.status match {
          case AcceptedValue6 => ocpp.Accepted
          case Blocked => ocpp.IdTagBlocked
          case Expired => ocpp.IdTagExpired
          case Invalid => ocpp.IdTagInvalid
          case ConcurrentTx => ocpp.ConcurrentTx
        }
      }
      ocpp.IdTagInfo(status, self.expiryDate.map(_.toDateTime), self.parentIdTag)
    }
  }

  implicit class RichOcppIdTagInfo(val self: ocpp.IdTagInfo) extends AnyVal {
    def toV12: IdTagInfo = {
      val status: AuthorizationStatus = {
        import ocpp.{AuthorizationStatus => ocpp}
        self.status match {
          case ocpp.Accepted => AcceptedValue6
          case ocpp.IdTagBlocked => Blocked
          case ocpp.IdTagExpired => Expired
          case ocpp.IdTagInvalid => Invalid
          case ocpp.ConcurrentTx => ConcurrentTx
        }
      }
      IdTagInfo(status, self.expiryDate.map(_.toXMLCalendar), self.parentIdTag)
    }
  }

}
