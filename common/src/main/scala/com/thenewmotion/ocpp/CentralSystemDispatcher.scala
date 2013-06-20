package com.thenewmotion.ocpp

import xml.Elem
import scalaxb.{Fault => _, _}
import soapenvelope12.Body
import scalax.RichAny
import Action._
import com.thenewmotion.ocpp
import com.thenewmotion.ocpp.Fault._
import ocpp.Meter.DefaultValue


/**
 * @author Yaroslav Klymko
 */
object CentralSystemDispatcher {
  def apply(body: Body, service: Version.Value => CentralSystemService, log: Any => Unit): Body = {

    implicit def faultToBody(x: soapenvelope12.Fault) = x.asBody

    val data = for {
      dataRecord <- body.any
      elem <- dataRecord.value.asInstanceOfOpt[Elem]
      action <- Action.fromElem(elem)
    } yield action -> elem

    data.headOption match {
      case None if body.any.isEmpty => ProtocolError("Body is empty")
      case None => NotSupported("No supported action found")
      case Some((action, xml)) => Version.fromBody(xml) match {
        case None => ProtocolError("Can't find an ocpp version")
        case Some(version) =>
          val reqRes = new ReqRes {
            def apply[REQ: XMLFormat, RES: XMLFormat](f: REQ => RES) = fromXMLEither[REQ](xml) match {
              case Left(msg) => ProtocolError(msg)
              case Right(req) => try {
                log(req)
                val res = f(req)
                log(res)
                simpleBody(DataRecord(Some(version.namespace), Some(action.responseLabel), res))
              } catch {
                case FaultException(fault) =>
                  log(fault)
                  fault
              }
            }
          }

          def dispatcher(service: => CentralSystemService) = version match {
            case Version.V12 => new CentralSystemDispatcherV12(action, reqRes, service)
            case Version.V15 => new CentralSystemDispatcherV15(action, reqRes, service)
          }
          dispatcher(service(version)).dispatch
      }
    }
  }
}

trait Dispatcher {
  def version: Version.Value
  def dispatch: Body
  def reqRes: ReqRes
  def ?[REQ: XMLFormat, RES: XMLFormat](f: REQ => RES): Body = reqRes(f)
  def fault(x: soapenvelope12.Fault): Nothing = throw new FaultException(x)
}

trait ReqRes {
  def apply[REQ: XMLFormat, RES: XMLFormat](f: REQ => RES): Body
}

class CentralSystemDispatcherV12(val action: Value,
                                 val reqRes: ReqRes,
                                 service: => CentralSystemService) extends Dispatcher {
  import v12._
  import ConvertersV12._

  def version = Version.V12

  def dispatch = action match {
    case Authorize => ?[AuthorizeRequest, AuthorizeResponse] {
      req => AuthorizeResponse(service.authorize(req.idTag).toV12)
    }

    case BootNotification => ?[BootNotificationRequest, BootNotificationResponse] {
      req =>
        import req._
        val ocpp.BootNotificationResponse(registrationAccepted, currentTime, heartbeatInterval) =
          service.bootNotification(
            chargePointVendor,
            chargePointModel,
            chargePointSerialNumber,
            chargeBoxSerialNumber,
            firmwareVersion,
            iccid,
            imsi,
            meterType,
            meterSerialNumber)

        val registrationStatus: RegistrationStatus = if (registrationAccepted) AcceptedValue7 else RejectedValue6

        BootNotificationResponse(registrationStatus, Some(currentTime.toXMLCalendar), Some(heartbeatInterval.toSeconds.toInt))
    }

    case DiagnosticsStatusNotification =>
      ?[DiagnosticsStatusNotificationRequest, DiagnosticsStatusNotificationResponse] {
        req =>
          val uploaded = req.status match {
            case Uploaded => true
            case UploadFailed => false
          }
          service.diagnosticsStatusNotification(uploaded)
          DiagnosticsStatusNotificationResponse()
      }

    case StartTransaction => ?[StartTransactionRequest, StartTransactionResponse] {
      req =>
        import req._
        val (transactionId, idTagInfo) = service.startTransaction(
          ocpp.ConnectorScope.fromOcpp(connectorId),
          idTag, timestamp.toDateTime, meterStart, None)
        StartTransactionResponse(transactionId, idTagInfo.toV12)
    }

    case StopTransaction => ?[StopTransactionRequest, StopTransactionResponse] {
      req =>
        import req._
        val idTagInfo = service.stopTransaction(transactionId, idTag, timestamp.toDateTime, meterStop, Nil)
        StopTransactionResponse(idTagInfo.map(_.toV12))
    }

    case Heartbeat => ?[HeartbeatRequest, HeartbeatResponse] {
      _ => HeartbeatResponse(service.heartbeat.toXMLCalendar)
    }

    case StatusNotification => ?[StatusNotificationRequest, StatusNotificationResponse] {
      req =>
        val status = req.status match {
          case Available => ocpp.Available
          case Occupied => ocpp.Occupied
          case Unavailable => ocpp.Unavailable
          case Faulted =>
            val errorCode: Option[ocpp.ChargePointErrorCode.Value] = {
              import ocpp.{ChargePointErrorCode => ocpp}
              req.errorCode match {
                case ConnectorLockFailure => Some(ocpp.ConnectorLockFailure)
                case HighTemperature => Some(ocpp.HighTemperature)
                case Mode3Error => Some(ocpp.Mode3Error)
                case NoError => None
                case PowerMeterFailure => Some(ocpp.PowerMeterFailure)
                case PowerSwitchFailure => Some(ocpp.PowerSwitchFailure)
                case ReaderFailure => Some(ocpp.ReaderFailure)
                case ResetFailure => Some(ocpp.ResetFailure)
              }
            }
            ocpp.Faulted(errorCode, None, None)
        }
        service.statusNotification(ocpp.Scope.fromOcpp(req.connectorId), status, None, None)
        StatusNotificationResponse()
    }

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
          service.firmwareStatusNotification(status)
          FirmwareStatusNotificationResponse()
      }

    case MeterValues => ?[MeterValuesRequest, MeterValuesResponse] {
      req =>
        def toMeter(x: MeterValue): Meter = Meter(x.timestamp.toDateTime, List(Meter.DefaultValue(x.value)))
        service.meterValues(ocpp.Scope.fromOcpp(req.connectorId), None, req.values.map(toMeter).toList)
        MeterValuesResponse()
    }
    case DataTransfer => throw new ActionNotSupportedException(version, "dataTransfer")
  }
}

class CentralSystemDispatcherV15(val action: Value,
                                 val reqRes: ReqRes,
                                 service: => CentralSystemService) extends Dispatcher {
  import v15._
  import ConvertersV15._

  def version = Version.V15
  def dispatch = action match {
    case Authorize => ?[AuthorizeRequest, AuthorizeResponse] {
      req => AuthorizeResponse(service.authorize(req.idTag).toV15)
    }

    case BootNotification => ?[BootNotificationRequest, BootNotificationResponse] {
      req =>
        import req._
        val ocpp.BootNotificationResponse(registrationAccepted, currentTime, heartbeatInterval) =
          service.bootNotification(
            chargePointVendor,
            chargePointModel,
            chargePointSerialNumber,
            chargeBoxSerialNumber,
            firmwareVersion,
            iccid,
            imsi,
            meterType,
            meterSerialNumber)

        val registrationStatus: RegistrationStatus = if (registrationAccepted) AcceptedValue11 else RejectedValue9

        BootNotificationResponse(registrationStatus, currentTime.toXMLCalendar, heartbeatInterval.toSeconds.toInt)
    }

    case DiagnosticsStatusNotification =>
      ?[DiagnosticsStatusNotificationRequest, DiagnosticsStatusNotificationResponse] {
        req =>
          val uploaded = req.status match {
            case Uploaded => true
            case UploadFailed => false
          }
          service.diagnosticsStatusNotification(uploaded)
          DiagnosticsStatusNotificationResponse()
      }

    case StartTransaction => ?[StartTransactionRequest, StartTransactionResponse] {
      req =>
        import req._
        val (transactionId, idTagInfo) = service.startTransaction(
          ocpp.ConnectorScope.fromOcpp(connectorId),
          idTag, timestamp.toDateTime, meterStart, None)
        StartTransactionResponse(transactionId, idTagInfo.toV15)
    }

    case StopTransaction => ?[StopTransactionRequest, StopTransactionResponse] {
      req =>
        import req._
        def toMeter(x: MeterValue) = Meter(x.timestamp.toDateTime, x.value.map(toValue).toList)
        def toTransactionData(x: TransactionData) = ocpp.TransactionData(x.values.map(toMeter).toList)

        val idTagInfo = service.stopTransaction(
          transactionId,
          idTag,
          timestamp.toDateTime,
          meterStop,
          transactionData.map(toTransactionData).toList)
        StopTransactionResponse(idTagInfo.map(_.toV15))
    }

    case Heartbeat => ?[HeartbeatRequest, HeartbeatResponse] {
      _ => HeartbeatResponse(service.heartbeat.toXMLCalendar)
    }

    case StatusNotification => ?[StatusNotificationRequest, StatusNotificationResponse] {
      req =>
        val status = req.status match {
          case Available => ocpp.Available
          case OccupiedValue => ocpp.Occupied
          case UnavailableValue => ocpp.Unavailable
          case FaultedValue =>
            val errorCode = {
              import ocpp.{ChargePointErrorCode => ocpp}
              req.errorCode match {
                case ConnectorLockFailure => Some(ocpp.ConnectorLockFailure)
                case HighTemperature => Some(ocpp.HighTemperature)
                case Mode3Error => Some(ocpp.Mode3Error)
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
            ocpp.Faulted(errorCode, req.info, req.vendorErrorCode)
          case Reserved => ocpp.Reserved
        }
        service.statusNotification(
          ocpp.Scope.fromOcpp(req.connectorId),
          status,
          req.timestamp.map(_.toDateTime),
          req.vendorId)
        StatusNotificationResponse()
    }

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
        service.firmwareStatusNotification(status)
        FirmwareStatusNotificationResponse()
    }

    case MeterValues => ?[MeterValuesRequest, MeterValuesResponse] {
      req =>
        def toMeter(x: MeterValue): Meter = Meter(x.timestamp.toDateTime, x.value.map(toValue).toList)
        service.meterValues(ocpp.Scope.fromOcpp(req.connectorId), req.transactionId, req.values.map(toMeter).toList)
        MeterValuesResponse()
    }

    case DataTransfer => ?[DataTransferRequest, DataTransferResponse] {
      req =>
        val res = service.dataTransfer(req.vendorId, req.messageId, req.data)
        val status: DataTransferStatus = {
          import ocpp.{DataTransferStatus => ocpp}
          res.status match {
            case ocpp.Accepted => AcceptedValue
            case ocpp.Rejected => RejectedValue
            case ocpp.UnknownMessageId => UnknownMessageId
            case ocpp.UnknownVendorId => UnknownVendorId
          }
        }
        DataTransferResponse(status, res.data)
    }
  }

  def toValue(x: Value): Meter.Value = {
    def toReadingContext(x: ReadingContext): Meter.ReadingContext.Value = {
      import Meter.{ReadingContext => ocpp}
      x match {
        case Interruptionu46Begin => ocpp.InterruptionBegin
        case Interruptionu46End => ocpp.InterruptionEnd
        case Sampleu46Clock => ocpp.SampleClock
        case Sampleu46Periodic => ocpp.SamplePeriodic
        case Transactionu46Begin => ocpp.TransactionBegin
        case Transactionu46End => ocpp.TransactionEnd
      }
    }

    def toValueFormat(x: ValueFormat): Meter.ValueFormat.Value = {
      import Meter.{ValueFormat => ocpp}
      x match {
        case Raw => ocpp.Raw
        case SignedData => ocpp.Signed
      }
    }

    def toMeasurand(x: Measurand): Meter.Measurand.Value = {
      import Meter.{Measurand => ocpp}
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

    def toLocation(x: Location): Meter.Location.Value = {
      import Meter.{Location => ocpp}
      x match {
        case Inlet => ocpp.Inlet
        case Outlet => ocpp.Outlet
        case v15.Body => ocpp.Body
      }
    }

    def toUnit(x: UnitOfMeasure): Meter.UnitOfMeasure.Value = {
      import Meter.{UnitOfMeasure => ocpp}
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

    Meter.Value(
      x.value,
      x.context.map(toReadingContext) getOrElse DefaultValue.readingContext,
      x.format.map(toValueFormat) getOrElse DefaultValue.format,
      x.measurand.map(toMeasurand) getOrElse DefaultValue.measurand,
      x.location.map(toLocation) getOrElse DefaultValue.location,
      x.unit.map(toUnit) getOrElse DefaultValue.unitOfMeasure)
  }
}

case class FaultException(fault: soapenvelope12.Fault) extends Exception(fault.toString)