package com.thenewmotion.ocpp
package soap

private[soap] object ConvertersV15 {

  import com.thenewmotion.ocpp
  import ocpp.v15._

  implicit class RichIdTagInfo(val self: messages.IdTagInfo) extends AnyVal{
    def toV15: IdTagInfoType = {
      val status: AuthorizationStatusType = {
        import messages.{AuthorizationStatus => ocpp}
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
        import messages.{AuthorizationStatus => ocpp}
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

  implicit class RichV15IdTagInfo(val self: IdTagInfo) extends AnyVal {
    def toOcpp: messages.IdTagInfo = {
      val status: messages.AuthorizationStatus = {
          import messages.{AuthorizationStatus => ocpp}
          self.status match {
            case AcceptedValue5 => ocpp.Accepted
            case Blocked        => ocpp.IdTagBlocked
            case Expired        => ocpp.IdTagExpired
            case Invalid        => ocpp.IdTagInvalid
            case ConcurrentTx   => ocpp.ConcurrentTx
        }
      }
      messages.IdTagInfo(status, self.expiryDate.map(_.toDateTime), self.parentIdTag)
    }
  }

  implicit class RichV15AuthorisationData(val self: AuthorisationData) extends AnyVal {
    def toOcpp: messages.AuthorisationData = messages.AuthorisationData(self.idTag, self.idTagInfo.map(_.toOcpp))
  }

  implicit class RichRemoteStartStopStatus(val self: RemoteStartStopStatus) extends AnyVal {
    def toOcpp: Boolean = self match {
      case AcceptedValue2 => true
      case RejectedValue2 => false
    }
  }

  implicit class RichUpdateStatus(val self: messages.UpdateStatus) extends AnyVal {
    def toV15: (UpdateStatus, Option[String]) = {
      import messages.{UpdateStatusWithoutHash, UpdateStatusWithHash}
      self match {
        case UpdateStatusWithHash.Accepted(h)        => (AcceptedValue10,                 h)
        case UpdateStatusWithoutHash.Failed          => (Failed,                       None)
        case UpdateStatusWithoutHash.HashError       => (ocpp.v15.HashError,           None)
        case UpdateStatusWithoutHash.NotSupported    => (ocpp.v15.NotSupportedValue,   None)
        case UpdateStatusWithoutHash.VersionMismatch => (ocpp.v15.VersionMismatch,     None)
      }
    }
  }
}
