package com.thenewmotion.ocpp
package soap

/**
 * @author Yaroslav Klymko
 */
private[soap] object ConvertersV12 {

  import com.thenewmotion.ocpp
  import ocpp.messages.enums
  import ocpp.v12._

  implicit class RichRemoteStartStopStatus(val self: RemoteStartStopStatus) extends AnyVal {
    def toOcpp: Boolean = self match {
      case AcceptedValue5 => true
      case RejectedValue5 => false
    }
  }

  implicit class RichIdTagInfo(val self: IdTagInfo) extends AnyVal {
    def toOcpp: enums.IdTagInfo = {
      val status = {
        import enums.{AuthorizationStatus => ocpp}
        self.status match {
          case AcceptedValue6 => ocpp.Accepted
          case Blocked => ocpp.IdTagBlocked
          case Expired => ocpp.IdTagExpired
          case Invalid => ocpp.IdTagInvalid
          case ConcurrentTx => ocpp.ConcurrentTx
        }
      }
      enums.IdTagInfo(status, self.expiryDate.map(_.toDateTime), self.parentIdTag)
    }
  }

  implicit class RichOcppIdTagInfo(val self: enums.IdTagInfo) extends AnyVal {
    def toV12: IdTagInfo = {
      val status: AuthorizationStatus = {
        import enums.{AuthorizationStatus => ocpp}
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
