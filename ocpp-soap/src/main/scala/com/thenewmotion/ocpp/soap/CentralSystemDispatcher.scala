package com.thenewmotion.ocpp
package soap

import scala.xml.NodeSeq
import scalaxb.XMLFormat
import scala.concurrent.{Future, ExecutionContext}
import com.thenewmotion.ocpp.{messages => ocpp}
import messages._

object CentralSystemDispatcher {
  def apply(version: Version): Dispatcher[CentralSystemReq, CentralSystemRes] = version match {
    case Version.V12 => CentralSystemDispatcherV12
    case Version.V15 => CentralSystemDispatcherV15
  }
}

object CentralSystemDispatcherV12 extends AbstractDispatcher[CentralSystemReq, CentralSystemRes] {
  import com.thenewmotion.ocpp.v12.{CentralSystemService => _, _}
  import ConvertersV12._

  def version = Version.V12

  type ActionType = CentralSystemAction
  override val actions = CentralSystemAction
  import actions._

  def dispatch(action: CentralSystemAction, xml: NodeSeq, service: CentralSystemReq => Future[CentralSystemRes])(implicit ec: ExecutionContext) = {
    def ?[XMLREQ: XMLFormat, XMLRES: XMLFormat](reqTrans: XMLREQ => CentralSystemReq)(resTrans: CentralSystemRes => XMLRES) =
      reqRes(action, xml, service)(reqTrans)(resTrans)

    action match {
      case Authorize => ?[AuthorizeRequest, AuthorizeResponse] {
        req => AuthorizeReq(req.idTag)
      } {
          case AuthorizeRes(idTag) => AuthorizeResponse(idTag.toV12)
      }

      case BootNotification => ?[BootNotificationRequest, BootNotificationResponse] {
        req =>
          BootNotificationReq(
            chargePointVendor = req.chargePointVendor,
            chargePointModel = req.chargePointModel,
            chargePointSerialNumber = stringOption(req.chargePointSerialNumber),
            chargeBoxSerialNumber = stringOption(req.chargeBoxSerialNumber),
            firmwareVersion = stringOption(req.firmwareVersion),
            iccid = stringOption(req.iccid),
            imsi = stringOption(req.imsi),
            meterType = stringOption(req.meterType),
            meterSerialNumber = stringOption(req.meterSerialNumber))
      } {
        case res: BootNotificationRes =>
          import res._

          val registrationStatus: RegistrationStatus = if (status == messages.RegistrationStatus.Accepted) AcceptedValue7 else RejectedValue6

          BootNotificationResponse(registrationStatus, Some(currentTime.toXMLCalendar), Some(interval.toSeconds.toInt))
      }

      case DiagnosticsStatusNotification =>
        ?[DiagnosticsStatusNotificationRequest, DiagnosticsStatusNotificationResponse] {
          req =>
            val uploaded = req.status match {
              case Uploaded => messages.DiagnosticsStatus.Uploaded
              case UploadFailed => messages.DiagnosticsStatus.UploadFailed
            }
            DiagnosticsStatusNotificationReq(uploaded)
        } { _ => DiagnosticsStatusNotificationResponse() }

      case StartTransaction => ?[StartTransactionRequest, StartTransactionResponse] {
        req =>
          import req._
           StartTransactionReq(
            ConnectorScope.fromOcpp(connectorId),
            idTag, timestamp.toDateTime, meterStart, None)
      } {
        case StartTransactionRes(transactionId, idTagInfo) =>
          StartTransactionResponse(transactionId, idTagInfo.toV12)
      }

      case StopTransaction => ?[StopTransactionRequest, StopTransactionResponse] {
        req =>
          import req._
           StopTransactionReq(transactionId, stringOption(idTag), timestamp.toDateTime, meterStop, messages.StopReason.Local, Nil)
      } {
        case StopTransactionRes(idTagInfo) =>
          StopTransactionResponse(idTagInfo.map(_.toV12))
      }

      case Heartbeat => ?[HeartbeatRequest, HeartbeatResponse] { _ => HeartbeatReq } {
        case HeartbeatRes(currentTime) => HeartbeatResponse(currentTime.toXMLCalendar)
      }

      case StatusNotification => ?[StatusNotificationRequest, StatusNotificationResponse] {
        req =>
          val status = req.status match {
            case Available => ocpp.ChargePointStatus.Available()
            case Occupied => ocpp.ChargePointStatus.Occupied(kind = None)
            case Unavailable => ocpp.ChargePointStatus.Unavailable()
            case Faulted =>
              val errorCode: Option[ocpp.ChargePointErrorCode] = {
                import ocpp.{ChargePointErrorCode => ocpp}
                req.errorCode match {
                  case ConnectorLockFailure => Some(ocpp.ConnectorLockFailure)
                  case HighTemperature => Some(ocpp.HighTemperature)
                  case Mode3Error => Some(ocpp.EVCommunicationError)
                  case NoError => None
                  case PowerMeterFailure => Some(ocpp.PowerMeterFailure)
                  case PowerSwitchFailure => Some(ocpp.PowerSwitchFailure)
                  case ReaderFailure => Some(ocpp.ReaderFailure)
                  case ResetFailure => Some(ocpp.ResetFailure)
                }
              }
              ocpp.ChargePointStatus.Faulted(errorCode, None, None)
          }
          StatusNotificationReq(Scope.fromOcpp(req.connectorId), status, None, None)
      } { _ => StatusNotificationResponse() }

      case FirmwareStatusNotification => ?[FirmwareStatusNotificationRequest, FirmwareStatusNotificationResponse] {
        req =>
          val status = {
            import ocpp.{FirmwareStatus => ocpp}
            req.status match {
              case Downloaded => ocpp.Downloaded
              case DownloadFailed => ocpp.DownloadFailed
              case InstallationFailed => ocpp.InstallationFailed
              case Installed => ocpp.Installed
            }
          }
          FirmwareStatusNotificationReq(status)
      } { _ => FirmwareStatusNotificationResponse() }

      case MeterValues => ?[MeterValuesRequest, MeterValuesResponse] {
        req =>
          def toMeter(x: MeterValue): meter.Meter =
            meter.Meter(x.timestamp.toDateTime, List(meter.DefaultValue(x.value)))

          MeterValuesReq(messages.Scope.fromOcpp(req.connectorId), None, req.values.map(toMeter).toList)
      } { _ => MeterValuesResponse() }

      case DataTransfer => throw new ActionNotSupportedException(version, "dataTransfer")
    }
  }
}

object CentralSystemDispatcherV15 extends AbstractDispatcher[CentralSystemReq, CentralSystemRes] {
  import com.thenewmotion.ocpp.v15.{CentralSystemService => _, _}
  import ConvertersV15._

  def version = Version.V15

  type ActionType = CentralSystemAction
  val actions = CentralSystemAction
  import actions._


  def dispatch(action: ActionType, xml: NodeSeq, service: CentralSystemReq => Future[CentralSystemRes])(implicit ec: ExecutionContext) = {
    def ?[XMLREQ: XMLFormat, XMLRES: XMLFormat](reqTrans: XMLREQ => CentralSystemReq)(resTrans: CentralSystemRes => XMLRES) =
      reqRes(action, xml, service)(reqTrans)(resTrans)

    action match {
      case Authorize => ?[AuthorizeRequest, AuthorizeResponse] { req => AuthorizeReq(req.idTag) } {
        case AuthorizeRes(idTag) => AuthorizeResponse(idTag.toV15)
      }

      case BootNotification => ?[BootNotificationRequest, BootNotificationResponse] { req =>
        BootNotificationReq(
          chargePointVendor = req.chargePointVendor,
          chargePointModel = req.chargePointModel,
          chargePointSerialNumber = stringOption(req.chargePointSerialNumber),
          chargeBoxSerialNumber = stringOption(req.chargeBoxSerialNumber),
          firmwareVersion = stringOption(req.firmwareVersion),
          iccid = stringOption(req.iccid),
          imsi = stringOption(req.imsi),
          meterType = stringOption(req.meterType),
          meterSerialNumber = stringOption(req.meterSerialNumber))
      } {
        case BootNotificationRes(registrationAccepted, currentTime, heartbeatInterval) =>
          val registrationStatus: RegistrationStatus = if (registrationAccepted == messages.RegistrationStatus.Accepted) AcceptedValue11 else RejectedValue9

          BootNotificationResponse(registrationStatus, currentTime.toXMLCalendar, heartbeatInterval.toSeconds.toInt)
      }

      case DiagnosticsStatusNotification =>
        ?[DiagnosticsStatusNotificationRequest, DiagnosticsStatusNotificationResponse] {
          req =>
            val uploaded = req.status match {
              case Uploaded => messages.DiagnosticsStatus.Uploaded
              case UploadFailed => messages.DiagnosticsStatus.UploadFailed
            }
            DiagnosticsStatusNotificationReq(uploaded)
        } { _ => DiagnosticsStatusNotificationResponse() }

      case StartTransaction => ?[StartTransactionRequest, StartTransactionResponse] { req =>
        import req._

        StartTransactionReq(
          messages.ConnectorScope.fromOcpp(connectorId),
          idTag, timestamp.toDateTime, meterStart, None)
      } {
        case StartTransactionRes(transactionId, idTagInfo) =>
          StartTransactionResponse(transactionId, idTagInfo.toV15)
      }

      case StopTransaction => ?[StopTransactionRequest, StopTransactionResponse] { req =>
        import req._
        def toMeter(x: MeterValue) = messages.meter.Meter(x.timestamp.toDateTime, x.value.map(toValue).toList)

        StopTransactionReq(
          transactionId,
          stringOption(idTag),
          timestamp.toDateTime,
          meterStop,
          messages.StopReason.Local,
          transactionData.flatMap(_.values.map(toMeter)).toList
        )
      } {
        case StopTransactionRes(idTagInfo) =>
          StopTransactionResponse(idTagInfo.map(_.toV15))
      }

      case Heartbeat => ?[HeartbeatRequest, HeartbeatResponse] { _ => HeartbeatReq } {
        case HeartbeatRes(currentTime) => HeartbeatResponse(currentTime.toXMLCalendar)
      }

      case StatusNotification => ?[StatusNotificationRequest, StatusNotificationResponse] { req =>
        val status = req.status match {
          case Available => ocpp.ChargePointStatus.Available()
          case OccupiedValue => ocpp.ChargePointStatus.Occupied(kind = None)
          case UnavailableValue => ocpp.ChargePointStatus.Unavailable()
          case FaultedValue =>
            val errorCode = {
              import ocpp.{ChargePointErrorCode => ocpp}
              req.errorCode match {
                case ConnectorLockFailure => Some(ocpp.ConnectorLockFailure)
                case HighTemperature => Some(ocpp.HighTemperature)
                case Mode3Error => Some(ocpp.EVCommunicationError)
                case NoError => None
                case PowerMeterFailure => Some(ocpp.PowerMeterFailure)
                case PowerSwitchFailure => Some(ocpp.PowerSwitchFailure)
                case ReaderFailure => Some(ocpp.ReaderFailure)
                case ResetFailure => Some(ocpp.ResetFailure)
                case GroundFailure => Some(ocpp.GroundFailure)
                case OverCurrentFailure => Some(ocpp.OverCurrentFailure)
                case UnderVoltage => Some(ocpp.UnderVoltage)
                case WeakSignal => Some(ocpp.WeakSignal)
                case OtherError => Some(ocpp.OtherError)
              }
            }
            ocpp.ChargePointStatus.Faulted(errorCode, req.info, req.vendorErrorCode)
          case Reserved => ocpp.ChargePointStatus.Reserved()
        }
        StatusNotificationReq(
          messages.Scope.fromOcpp(req.connectorId),
          status,
          req.timestamp.map(_.toDateTime),
          stringOption(req.vendorId))
      } (_ => StatusNotificationResponse())

      case FirmwareStatusNotification => ?[FirmwareStatusNotificationRequest, FirmwareStatusNotificationResponse] {
        req =>
          val status = {
            import ocpp.{FirmwareStatus => ocpp}
            req.status match {
              case Downloaded => ocpp.Downloaded
              case DownloadFailed => ocpp.DownloadFailed
              case InstallationFailed => ocpp.InstallationFailed
              case Installed => ocpp.Installed
            }
          }
          FirmwareStatusNotificationReq(status)
      } (_ => FirmwareStatusNotificationResponse())

      case MeterValues => ?[MeterValuesRequest, MeterValuesResponse] {
        req =>
          def toMeter(x: MeterValue): meter.Meter =
            meter.Meter(x.timestamp.toDateTime, x.value.map(toValue).toList)

          MeterValuesReq(messages.Scope.fromOcpp(req.connectorId), req.transactionId, req.values.map(toMeter).toList)
      }  (_ => MeterValuesResponse())

      case DataTransfer => ?[DataTransferRequestType, DataTransferResponseType] {
        req =>
          CentralSystemDataTransferReq(
            req.vendorId,
            stringOption(req.messageId),
            stringOption(req.data))
      } {
        case res: CentralSystemDataTransferRes =>
          val status: DataTransferStatusType = {
            import messages.{DataTransferStatus => ocpp}
            res.status match {
              case ocpp.Accepted => AcceptedValue13
              case ocpp.Rejected => RejectedValue10
              case ocpp.UnknownMessageId => UnknownMessageIdValue
              case ocpp.UnknownVendorId => UnknownVendorIdValue
            }
          }
          DataTransferResponseType(status, res.data)
      }
    }
  }

  def toValue(x: com.thenewmotion.ocpp.v15.Value): meter.Value = {
    import meter.{ReadingContext => ocpp}
    def toReadingContext(x: ReadingContext): meter.ReadingContext = {
      x match {
        case Interruptionu46Begin => ocpp.InterruptionBegin
        case Interruptionu46End => ocpp.InterruptionEnd
        case Sampleu46Clock => ocpp.SampleClock
        case Sampleu46Periodic => ocpp.SamplePeriodic
        case Transactionu46Begin => ocpp.TransactionBegin
        case Transactionu46End => ocpp.TransactionEnd
      }
    }

    def toValueFormat(x: ValueFormat): meter.ValueFormat = {
      import meter.{ValueFormat => ocpp}
      x match {
        case Raw => ocpp.Raw
        case SignedData => ocpp.SignedData
      }
    }

    def toMeasurand(x: Measurand): meter.Measurand = {
      import meter.{Measurand => ocpp}
      x match {
        case Energyu46Activeu46Exportu46Register => ocpp.EnergyActiveExportRegister
        case Energyu46Activeu46Importu46Register => ocpp.EnergyActiveImportRegister
        case Energyu46Reactiveu46Exportu46Register => ocpp.EnergyReactiveExportRegister
        case Energyu46Reactiveu46Importu46Register => ocpp.EnergyReactiveImportRegister
        case Energyu46Activeu46Exportu46Interval => ocpp.EnergyActiveExportInterval
        case Energyu46Activeu46Importu46Interval => ocpp.EnergyActiveImportInterval
        case Energyu46Reactiveu46Exportu46Interval => ocpp.EnergyReactiveExportInterval
        case Energyu46Reactiveu46Importu46Interval => ocpp.EnergyReactiveImportInterval
        case Poweru46Activeu46Export => ocpp.PowerActiveExport
        case Poweru46Activeu46Import => ocpp.PowerActiveImport
        case Poweru46Reactiveu46Export => ocpp.PowerReactiveExport
        case Poweru46Reactiveu46Import => ocpp.PowerReactiveImport
        case Currentu46Export => ocpp.CurrentExport
        case Currentu46Import => ocpp.CurrentImport
        case Voltage => ocpp.Voltage
        case Temperature => ocpp.Temperature
      }
    }

    def toLocation(x: Location): meter.Location = {
      import com.thenewmotion.ocpp.v15._
      import meter.{Location => ocpp}
      x match {
        case Inlet => ocpp.Inlet
        case Outlet => ocpp.Outlet
        case Body => ocpp.Body
      }
    }

    def toUnit(x: UnitOfMeasure): meter.UnitOfMeasure = {
      import meter.{UnitOfMeasure => ocpp}
      x match {
        case Wh => ocpp.Wh
        case KWh => ocpp.Kwh
        case Varh => ocpp.Varh
        case Kvarh => ocpp.Kvarh
        case W => ocpp.W
        case KW => ocpp.Kw
        case Var => ocpp.Var
        case Kvar => ocpp.Kvar
        case Amp => ocpp.Amp
        case Volt => ocpp.Volt
        case Celsius => ocpp.Celsius
      }
    }

    meter.Value(
      x.value,
      x.context.map(toReadingContext) getOrElse meter.DefaultValue.readingContext,
      x.format.map(toValueFormat) getOrElse meter.DefaultValue.format,
      x.measurand.map(toMeasurand) getOrElse meter.DefaultValue.measurand,
      phase = None,
      x.location.map(toLocation) getOrElse meter.DefaultValue.location,
      x.unit.map(toUnit) getOrElse meter.DefaultValue.unitOfMeasure)
  }
}
