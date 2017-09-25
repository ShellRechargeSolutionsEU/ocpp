package com.thenewmotion.ocpp
package json
package v15

import java.net.URI
import java.net.URISyntaxException

import enums.reflection.EnumUtils.Enumerable
import enums.reflection.EnumUtils.Nameable
import org.json4s.MappingException

import scala.concurrent.duration._

object ConvertersV15 {

  private def unexpectedMessage(msg: Any) =
    throw new Exception(s"Couldn't convert unexpected OCPP message $msg")

  private implicit class RichIdTagInfo(idTagInfo: messages.IdTagInfo) {
    def toV15: IdTagInfo = IdTagInfo(
      status = idTagInfo.status.name,
      expiryDate = idTagInfo.expiryDate,
      parentIdTag = idTagInfo.parentIdTag
    )
  }

  private implicit class RichV15IdTagInfo(self: IdTagInfo) {
    def fromV15: messages.IdTagInfo = messages.IdTagInfo(
      status = enumerableFromJsonString(messages.AuthorizationStatus, self.status),
      expiryDate = self.expiryDate,
      parentIdTag = self.parentIdTag
    )
  }

  private object RichChargePointStatus {
    val defaultErrorCode = "NoError"
  }

  private implicit class RichChargePointStatus(self: messages.ChargePointStatus) {

    import RichChargePointStatus.defaultErrorCode

    def toV15Fields: (String, String, Option[String], Option[String]) = {
      def simpleStatus(name: String) = (name, defaultErrorCode, self.info, None)

      import messages.ChargePointStatus
      self match {
        case ChargePointStatus.Available(_) => simpleStatus("Available")
        case ChargePointStatus.Occupied(_, _) => simpleStatus("Occupied")
        case ChargePointStatus.Unavailable(_) => simpleStatus("Unavailable")
        case ChargePointStatus.Reserved(_) => simpleStatus("Reserved")
        case ChargePointStatus.Faulted(errCode, inf, vendorErrCode) =>
          ("Faulted", errCode.map(_.name).getOrElse(defaultErrorCode), inf, vendorErrCode)
      }
    }
  }

  private def statusFieldsToOcppStatus(status: String, errorCode: String, info: Option[String],
    vendorErrorCode: Option[String]): messages.ChargePointStatus = {
    import messages.ChargePointStatus

    import RichChargePointStatus.defaultErrorCode
    status match {
      case "Available" => ChargePointStatus.Available(info)
      case "Occupied" => ChargePointStatus.Occupied(kind = None, info)
      case "Unavailable" => ChargePointStatus.Unavailable(info)
      case "Reserved" => ChargePointStatus.Reserved(info)
      case "Faulted" =>
        val errorCodeString =
          if (errorCode == defaultErrorCode)
            None
          else
            Some(enumerableFromJsonString(messages.ChargePointErrorCode, errorCode))
        ChargePointStatus.Faulted(errorCodeString, info, vendorErrorCode)
    }
  }

  private implicit class RichMeter(self: messages.meter.Meter) {
    def toV15: Meter = Meter(
      timestamp = self.timestamp,
      values = self.values.map(valueToV15)
    )

    def valueToV15(v: messages.meter.Value): MeterValue = {
      import messages.meter._
      MeterValue(
        value = v.value,
        measurand = noneIfDefault(Measurand, v.measurand),
        context = noneIfDefault(ReadingContext, v.context),
        format = noneIfDefault(ValueFormat, v.format),
        location = noneIfDefault(Location, v.location),
        unit = noneIfDefault(UnitOfMeasure, v.unit)
      )
    }
  }

  private def transactionDataFromV15(v15td: Option[List[TransactionData]]): List[messages.meter.Meter] =
    v15td.fold(List.empty[messages.meter.Meter])(metersFromV15)

  private def metersFromV15(v15mv: List[TransactionData]): List[messages.meter.Meter] =
    v15mv.flatMap(_.values.fold(List.empty[messages.meter.Meter])(_.map(meterFromV15)))

  private def meterFromV15(v15m: Meter): messages.meter.Meter = {
    messages.meter.Meter(v15m.timestamp, v15m.values.map(meterValueFromV15))
  }

  private def meterValueFromV15(v15m: MeterValue): messages.meter.Value = {
    import messages.meter._
    import v15m._

    Value(
      value = value,
      measurand = defaultIfNone(Measurand, measurand),
      phase = None,
      context = defaultIfNone(ReadingContext, context),
      format = defaultIfNone(ValueFormat, format),
      location = defaultIfNone(Location, location),
      unit = unit.fold[UnitOfMeasure](UnitOfMeasure.Wh) { unitString =>
        UnitOfMeasure.withName(unitString) match {
          case Some(unitOfMeasure) => unitOfMeasure
          case None => unitString match {
            case "Amp" => UnitOfMeasure.Amp
            case "Volt" => UnitOfMeasure.Volt
            case _ => throw new MappingException(s"Value $unitString is not valid for UnitOfMeasure")
          }
        }
      }
    )
  }

  private implicit class BooleanToStatusString(val b: Boolean) extends AnyVal {
    def toStatusString = if (b) "Accepted" else "Rejected"
  }

  private def statusStringToBoolean(statusString: String) = statusString match {
    case "Accepted" => true
    case "Rejected" => false
    case _ => throw new MappingException(
      s"Did not recognize status $statusString (expected 'Accepted' or 'Rejected')"
    )
  }

  private implicit class RichKeyValue(val self: messages.KeyValue) {

    import self._

    def toV15: ConfigurationEntry = ConfigurationEntry(key, readonly, value)
  }

  private implicit class RichConfigurationEntry(self: ConfigurationEntry) {

    import self._

    def fromV15: messages.KeyValue = messages.KeyValue(key, readonly, value)
  }

  private implicit class RichAuthListVersion(self: messages.AuthListVersion) {
    def toV15: Int = self match {
      case messages.AuthListNotSupported => -1
      case messages.AuthListSupported(i) => i
    }
  }

  private implicit class RichAuthorisationData(self: messages.AuthorisationData) {
    def toV15: AuthorisationData = {
      val v15IdTagInfo = self match {
        case messages.AuthorisationAdd(_, idTagInfo) => Some(idTagInfo.toV15)
        case messages.AuthorisationRemove(_) => None
      }

      AuthorisationData(self.idTag, v15IdTagInfo)
    }
  }

  private implicit class RichV15AuthorisationData(self: AuthorisationData) {
    def fromV15: messages.AuthorisationData = messages.AuthorisationData(
      self.idTag, self.idTagInfo.map(_.fromV15)
    )
  }

  private implicit class RichUpdateStatus(self: messages.UpdateStatus) {
    def toV15Fields: (String, Option[String]) = self match {
      case updateStatus: messages.UpdateStatusWithoutHash => (updateStatus.name, None)
      case messages.UpdateStatusWithHash.Accepted(hash) => ("Accepted", hash)
    }
  }

  private def updateStatusFromV15(status: String, hash: Option[String]): messages.UpdateStatus = {
    messages.UpdateStatusWithoutHash.withName(status) match {
      case Some(updateStatus) => updateStatus
      case None => status match {
        case "Accepted" => messages.UpdateStatusWithHash.Accepted(hash)
        case _ => throw new MappingException(s"Value $status is not valid for UpdateStatus")
      }
    }
  }

  /**
   * Tries to get select the enumerable value whose name is equal to the given string. If no such enum value exists,
   * throws a net.liftweb.json.MappingException.
   */
  private def enumerableFromJsonString[T <: Nameable](enum: Enumerable[T], s: String): T =
    enum.withName(s) match {
      case None =>
        throw new MappingException(s"Value $s is not valid for ${enum.getClass.getSimpleName}")
      case Some(v) => v
    }

  private def noneIfDefault[T <: Nameable](enumerable: messages.EnumerableWithDefault[T], actual: T): Option[String] =
    if (actual == enumerable.default) None else Some(actual.name)

  private def defaultIfNone[T <: Nameable](enumerable: messages.EnumerableWithDefault[T], str: Option[String]): T =
    str.map(enumerableFromJsonString(enumerable, _)).getOrElse(enumerable.default)

  private def noneIfEmpty[T](l: List[T]): Option[List[T]] =
    if (l.isEmpty) None else Some(l)

  /**
   * Parses a URI and throws a lift-json MappingException if the syntax is wrong
   */
  private def parseURI(s: String) = try {
    new URI(s)
  } catch {
    case e: URISyntaxException => throw MappingException(s"Invalid URL $s in OCPP-JSON message", e)
  }
}
