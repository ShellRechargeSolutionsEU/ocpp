package com.thenewmotion.ocpp

/**
 * @author Yaroslav Klymko
 */
private[ocpp] object ConvertersV15 {

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
      IdTagInfoType(status, self.expiryDate.map(_.toXMLCalendar), self.parentIdTag)
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
      IdTagInfo(status, self.expiryDate.map(_.toXMLCalendar), self.parentIdTag)
    }
  }

  implicit class RichRemoteStartStopStatus(val self: RemoteStartStopStatus) extends AnyVal {
    def toOcpp: Boolean = self match {
      case AcceptedValue2 => true
      case RejectedValue2 => false
    }
  }
}