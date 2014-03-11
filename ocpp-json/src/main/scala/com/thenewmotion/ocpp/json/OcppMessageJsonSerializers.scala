package com.thenewmotion.ocpp
package json

import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import net.liftweb.json.ext.EnumNameSerializer
import messages._
import messages.Meter._
import com.thenewmotion.time.Imports._
import org.joda.time.format.ISODateTimeFormat
import scala.concurrent.duration.FiniteDuration
import java.net.URI

object OcppMessageJsonSerializers {

  def apply(): Seq[Serializer[_]] = List(new BootNotificationResJsonFormat, new JodaDateTimeJsonFormat,
    new ScopeJsonFormat, new StartTransactionReqJsonFormat, new TransactionDataJsonFormat, new MeterJsonFormat,
    new MeterValueJsonFormat, new AuthorizationStatusJsonFormat, new FiniteDurationJsonFormat,
    new AuthorizeResJsonFormat, new StartTransactionResJsonFormat, new StopTransactionResJsonFormat,
    new UnlockConnectorReqJsonFormat, new UnlockConnectorResJsonFormat, new GetConfigurationReqJsonFormat,
    new ResetReqJsonFormat, new ResetResJsonFormat, new ChangeAvailabilityReqJsonFormat,
    new ChangeAvailabilityResJsonFormat, new ConnectorScopeJsonFormat, new StatusNotificationReqJsonFormat,
    new RemoteStartTransactionReqJsonFormat, new RemoteStartTransactionResJsonFormat,
    new RemoteStopTransactionResJsonFormat, new HeartbeatReqJsonFormat, new UpdateFirmwareReqJsonFormat,
    new UpdateFirmwareResJsonFormat, new FirmwareStatusNotificationReqJsonFormat, new URIJsonFormat,
    new GetDiagnosticsReqJsonFormat, new DiagnosticsStatusNotificationReqJsonFormat, new MeterValuesReqJsonFormat,
    new ChangeConfigurationResJsonFormat, new ClearCacheResJsonFormat, new GetConfigurationResJsonFormat,
    new GetLocalListVersionResJsonFormat, new SendLocalListReqJsonFormat, new SendLocalListResJsonFormat,
    new ReserveNowReqJsonFormat, new ReserveNowResJsonFormat, new CancelReservationResJsonFormat)

  class AuthorizeResJsonFormat extends CustomSerializerOnly[AuthorizeRes](format =>
    {
      case a: AuthorizeRes => JObject(List(
        JField("idTagInfo", Extraction.decompose(a.idTag)(format))))
    })

  class BootNotificationResJsonFormat extends CustomSerializerOnly[BootNotificationRes](format =>
    {
      case b: BootNotificationRes =>
        JObject(List(JField("status", JString(b.registrationAccepted.toStatusString)),
          JField("currentTime", Extraction.decompose(b.currentTime)(format)),
          JField("heartbeatInterval", Extraction.decompose(b.heartbeatInterval)(format))))
    })

  class StartTransactionReqJsonFormat extends CustomDeserializerOnly[StartTransactionReq](format =>
    {
      case x: JObject => {
        StartTransactionReq(connector = (x \ "connectorId").extract[ConnectorScope](format, manifest),
          idTag = (x \ "idTag").extract[String](format, manifest),
          timestamp = (x \ "timestamp").extract[DateTime](format, manifest),
          meterStart = (x \ "meterStart").extract[Int](format, manifest),
          reservationId = (x \ "reservationId").extract[Option[Int]](format, manifest))
      }
    })

  class StartTransactionResJsonFormat extends CustomSerializerOnly[StartTransactionRes](format =>
    {
      case str: StartTransactionRes => JObject(List(
        JField("transactionId", Extraction.decompose(str.transactionId)(format)),
        JField("idTagInfo", Extraction.decompose(str.idTag)(format))))
    })

  class StopTransactionResJsonFormat extends CustomSerializerOnly[StopTransactionRes](format =>
    {
      case str: StopTransactionRes => JObject(List(
        JField("idTagInfo", Extraction.decompose(str.idTag)(format))))
    })

  class UnlockConnectorReqJsonFormat extends CustomSerializerOnly[UnlockConnectorReq](format =>
    {
      case ucr: UnlockConnectorReq => JObject(List(
        JField("connectorId", Extraction.decompose(ucr.connector)(format))))
    })

  class UnlockConnectorResJsonFormat extends CustomDeserializerOnly[UnlockConnectorRes](format =>
    {
      case ucr: JObject => UnlockConnectorRes(parseStatusField(ucr))
    })

  class GetConfigurationReqJsonFormat extends CustomSerializerOnly[GetConfigurationReq](format =>
    {
      case gcr: GetConfigurationReq => JObject(List(
        JField("key", Extraction.decompose(gcr.keys)(format))))
    })

  class ResetReqJsonFormat extends CustomSerializerOnly[ResetReq](format =>
    {
      case rr: ResetReq => "type" -> rr.resetType.toString
    })

  class ResetResJsonFormat extends CustomDeserializerOnly[ResetRes](format =>
    {
      case jval: JObject => ResetRes(parseStatusField(jval))
    })

  class ChangeAvailabilityReqJsonFormat extends CustomSerializerOnly[ChangeAvailabilityReq](format =>
    {
      case car: ChangeAvailabilityReq =>
        ("connectorId" -> Extraction.decompose(car.scope)(format)) ~
          ("type" -> car.availabilityType.toString)
    })

  class ChangeAvailabilityResJsonFormat extends CustomDeserializerOnly[ChangeAvailabilityRes](format =>
    {
      case jval: JObject =>
        val status = (jval \ "status").extract[AvailabilityStatus.Value](
          format + new EnumNameSerializer(AvailabilityStatus), manifest)
        ChangeAvailabilityRes(status)
    })

  class StatusNotificationReqJsonFormat extends CustomDeserializerOnly[StatusNotificationReq](format =>
    {
      case jval: JObject =>
        val scope = (jval \ "connectorId").extract[Scope](format, manifest)
        val jsonStatus = (jval \ "status").extract[String](format, manifest)
        val status = jsonStatus match {
          case "Available"   => Available
          case "Occupied"    => Occupied
          case "Unavailable" => Unavailable
          case "Reserved"    => Reserved
          case "Faulted" =>
            val errorCode = (jval \ "errorCode").extractOpt[ChargePointErrorCode.Value](format + new EnumNameSerializer(ChargePointErrorCode), manifest)
            val info = (jval \ "info").extractOpt[String](format, manifest)
            val vendorErrorCode = (jval \ "vendorErrorCode").extractOpt[String](format, manifest)
            Faulted(errorCode, info, vendorErrorCode)
        }
        val timestamp = (jval \ "timestamp").extractOpt[DateTime](format, manifest)
        val vendorId = (jval \ "vendorId").extractOpt[String](format, manifest)
        StatusNotificationReq(scope, status, timestamp, vendorId)
    })

  class RemoteStartTransactionReqJsonFormat extends CustomSerializerOnly[RemoteStartTransactionReq](format =>
    {
      case rstr: RemoteStartTransactionReq =>
        ("connectorId" -> Extraction.decompose(rstr.connector)(format)) ~
          ("idTag" -> rstr.idTag)
    })

  class RemoteStartTransactionResJsonFormat extends CustomDeserializerOnly[RemoteStartTransactionRes](format =>
    {
      case jval: JObject => RemoteStartTransactionRes(parseStatusField(jval))
    })

  class RemoteStopTransactionResJsonFormat extends CustomDeserializerOnly[RemoteStopTransactionRes](format =>
    {
      case jval: JObject => RemoteStopTransactionRes(parseStatusField(jval))
    })

  class HeartbeatReqJsonFormat extends CustomDeserializerOnly[HeartbeatReq.type](format =>
    {
      case jval: JObject => HeartbeatReq
    })

  class UpdateFirmwareReqJsonFormat extends CustomSerializerOnly[UpdateFirmwareReq](format =>
    {
      case ufr: UpdateFirmwareReq =>
        ("retrieveDate" -> Extraction.decompose(ufr.retrieveDate)(format)) ~
          ("location" -> Extraction.decompose(ufr.location)(format)) ~
          ("retries" -> Extraction.decompose(ufr.retries.numberOfRetries)(format)) ~
          ("retryInterval" -> Extraction.decompose(ufr.retries.intervalInSeconds)(format))
    })

  class UpdateFirmwareResJsonFormat extends CustomDeserializerOnly[UpdateFirmwareRes.type](format =>
    {
      case jval: JObject => UpdateFirmwareRes
    })

  class FirmwareStatusNotificationReqJsonFormat extends CustomDeserializerOnly[FirmwareStatusNotificationReq](format =>
    {
      case jval: JObject =>
        val formats = format + new EnumNameSerializer(FirmwareStatus)
        val firmwareStatus = (jval \ "status").extract[FirmwareStatus.Value](formats, manifest)
        FirmwareStatusNotificationReq(status = firmwareStatus)
    })

  class GetDiagnosticsReqJsonFormat extends CustomSerializerOnly[GetDiagnosticsReq](format =>
    {
      case gdr: GetDiagnosticsReq =>
        ("location" -> Extraction.decompose(gdr.location)(format)) ~
          ("startTime" -> Extraction.decompose(gdr.startTime)(format)) ~
          ("stopTime" -> Extraction.decompose(gdr.stopTime)(format)) ~
          ("retries" -> Extraction.decompose(gdr.retries.numberOfRetries)(format)) ~
          ("retryInterval" -> Extraction.decompose(gdr.retries.intervalInSeconds)(format))
    })

  class DiagnosticsStatusNotificationReqJsonFormat extends CustomDeserializerOnly[DiagnosticsStatusNotificationReq](format =>
    {
      case jval: JObject =>
        val status = jval \ "status"
        val uploadSucceeded = status match {
          case JString("Uploaded")     => true
          case JString("UploadFailed") => false
          case _                       => throw new MappingException(s"Unrecognized diagnostics status $status")
        }
        DiagnosticsStatusNotificationReq(uploaded = uploadSucceeded)
    })

  class MeterValuesReqJsonFormat extends CustomDeserializerOnly[MeterValuesReq](format =>
    {
      case jval: JObject =>
        val scope = (jval \ "connectorId").extract[Scope](format, manifest)
        val transactionId = (jval \ "transactionId").extract[Option[Int]](format, manifest)
        val meters = (jval \ "values").extract[List[Meter]](format, manifest)
        MeterValuesReq(scope, transactionId, meters)
    })

  class ChangeConfigurationResJsonFormat extends CustomDeserializerOnly[ChangeConfigurationRes](format =>
    {
      case jval: JObject =>
        val formats = format + new EnumNameSerializer(ConfigurationStatus)
        val status = (jval \ "status").extract[ConfigurationStatus.Value](formats, manifest)
        ChangeConfigurationRes(status)
    })

  class ClearCacheResJsonFormat extends CustomDeserializerOnly[ClearCacheRes](format =>
    {
      case jval: JObject => ClearCacheRes(parseStatusField(jval))
    })

  class GetConfigurationResJsonFormat extends CustomDeserializerOnly[GetConfigurationRes](format =>
    {
      case jval: JObject =>
        val values = (jval \ "configurationKey").extract[List[KeyValue]](format, manifest)
        val unknownKeys = (jval \ "unknownKey").extract[List[String]](format, manifest)
        GetConfigurationRes(values, unknownKeys)
    })

  class GetLocalListVersionResJsonFormat extends CustomDeserializerOnly[GetLocalListVersionRes](format =>
    {
      case jval: JObject =>
        val version = (jval \ "listVersion").extract[Int](format, manifest)
        GetLocalListVersionRes(AuthListVersion(version))
    })

  class SendLocalListReqJsonFormat extends CustomSerializerOnly[SendLocalListReq](format =>
    {
      case sllr: SendLocalListReq =>
        ("updateType" -> Extraction.decompose(sllr.updateType)(format + new EnumNameSerializer(UpdateType))) ~
          ("listVersion" -> Extraction.decompose(sllr.listVersion.version)(format)) ~
          ("localAuthorisationList" -> Extraction.decompose(sllr.localAuthorisationList)(format)) ~
          ("hash" -> Extraction.decompose(sllr.hash)(format))
    })

  class SendLocalListResJsonFormat extends CustomDeserializerOnly[SendLocalListRes](format =>
    {
      case jval: JObject =>
        val statusString = (jval \ "status").extract[String](format, manifest)
        val status = statusString match {
          case "Failed"          => UpdateStatus.UpdateFailed
          case "HashError"       => UpdateStatus.HashError
          case "NotSupported"    => UpdateStatus.NotSupportedValue
          case "VersionMismatch" => UpdateStatus.VersionMismatch
          case "Accepted" =>
            val hash = (jval \ "hash").extractOpt[String](format, manifest)
            UpdateStatus.UpdateAccepted(hash)
          case _ => throw new MappingException(s"No usable value for update status in $jval")
        }
        SendLocalListRes(status)
    })

  class ReserveNowReqJsonFormat extends CustomSerializerOnly[ReserveNowReq](format =>
    {
      case rnr: ReserveNowReq =>
        ("connectorId" -> Extraction.decompose(rnr.connector)(format)) ~
          ("expiryDate" -> Extraction.decompose(rnr.expiryDate)(format)) ~
          ("idTag" -> Extraction.decompose(rnr.idTag)(format)) ~
          ("parentIdTag" -> Extraction.decompose(rnr.parentIdTag)(format)) ~
          ("reservationId" -> Extraction.decompose(rnr.reservationId)(format))
    })

  class ReserveNowResJsonFormat extends CustomDeserializerOnly[ReserveNowRes](format =>
    {
      case jval: JObject =>
        val status = (jval \ "status").extract[Reservation.Value](format + new EnumNameSerializer(Reservation), manifest)
        ReserveNowRes(status)
    })

  class CancelReservationResJsonFormat extends CustomDeserializerOnly[CancelReservationRes](format =>
    {
      case jval: JObject => CancelReservationRes(parseStatusField(jval))
    })

  class ScopeJsonFormat extends CustomSerializer[Scope](format => (
    {
      case JInt(ocppConnectorId) => Scope.fromOcpp(ocppConnectorId.toInt)
    },
    {
      case x: Scope => JInt(x.toOcpp)
    }))

  class ConnectorScopeJsonFormat extends CustomSerializer[ConnectorScope](format => (
    {
      case JInt(ocppConnectorId) if ocppConnectorId >= 1 => ConnectorScope.fromOcpp(ocppConnectorId.toInt)
    },
    {
      case x: ConnectorScope => JInt(x.toOcpp)
    }))

  class AuthorizationStatusJsonFormat extends Serializer[AuthorizationStatus.Value] {
    import AuthorizationStatusJsonFormat._

    val ValueClass = classOf[AuthorizationStatus.Value]

    def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
      case as: AuthorizationStatus.Value if enumToJson.isDefinedAt(as.toString) => JString(enumToJson(as.toString))
    }

    def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), AuthorizationStatus.Value] =
      {
        case (TypeInfo(ValueClass, _), json) => json match {
          case JString(value) if jsonToEnum.isDefinedAt(value) => AuthorizationStatus.withName(jsonToEnum(value))
          case v => throw new MappingException(s"Can't convert $v to AuthorizationStatus#Value")
        }
      }
  }

  object AuthorizationStatusJsonFormat {
    val names = List(("Accepted", "Accepted"), ("IdTagBlocked", "Blocked"), ("IdTagExpired", "Expired"),
      ("IdTagInvalid", "Invalid"), ("ConcurrentTx", "ConcurrentTx"))
    val jsonToEnum = Map(names.map(_.swap): _*)
    val enumToJson = Map(names: _*)
  }

  class TransactionDataJsonFormat extends CustomDeserializerOnly[TransactionData](format =>
    {
      case jval: JObject => TransactionData((jval \ "values").extract[List[Meter]](format, manifest))
    })

  class MeterJsonFormat extends CustomDeserializerOnly[Meter](format =>
    {
      case jval: JObject =>
        Meter(timestamp = (jval \ "timestamp").extract[DateTime](format, manifest),
          values = (jval \ "values").extract[List[Meter.Value]](format, manifest))
    })

  class MeterValueJsonFormat extends CustomDeserializerOnly[Meter.Value](format => {
    case jval: JObject => {
      Meter.Value(value = (jval \ "value").extract[String](format, manifest),
        measurand = getMeterValueProperty(jval \ "measurand", Measurand.EnergyActiveImportRegister, new EnumNameSerializer(Measurand)),
        context = getMeterValueProperty(jval \ "context", ReadingContext.SamplePeriodic, new EnumNameSerializer(ReadingContext)),
        format = getMeterValueProperty(jval \ "format", ValueFormat.Raw, new EnumNameSerializer(ValueFormat)),
        location = getMeterValueProperty(jval \ "location", Location.Outlet, new EnumNameSerializer(Location)),
        unit = getMeterValueProperty(jval \ "unit", UnitOfMeasure.Wh, new EnumNameSerializer(UnitOfMeasure)))
    }
  })

  def getMeterValueProperty[T <: Enumeration: Manifest](json: JValue, default: T#Value,
                                                        serializer: EnumNameSerializer[T]): T#Value = {
    json match {
      case JNothing => default
      case j        => j.extract[T#Value](DefaultFormats + serializer, manifest[T#Value])
    }
  }

  class JodaDateTimeJsonFormat extends CustomSerializer[DateTime](format => (
    {
      case JString(formattedDate) => ISODateTimeFormat.dateOptionalTimeParser().parseDateTime(formattedDate)
    },
    {
      case x: DateTime => JString(x.toJsonString)
    }))

  class FiniteDurationJsonFormat extends CustomSerializerOnly[FiniteDuration](format =>
    {
      case d: FiniteDuration => JInt(d.toSeconds)
    })

  class URIJsonFormat extends CustomSerializerOnly[URI](format =>
    {
      case u: URI => JString(u.toASCIIString)
    })

  class CustomSerializerOnly[T: Manifest](ser: Formats => PartialFunction[Any, JValue])
    extends CustomSerializer[T](format => (PartialFunction.empty, ser(format)))

  class CustomDeserializerOnly[T: Manifest](deser: Formats => PartialFunction[JValue, T])
    extends CustomSerializer[T](format => (deser(format), PartialFunction.empty))

  private implicit class BooleanToStatusString(val b: Boolean) extends AnyVal {
    def toStatusString = if (b) "Accepted" else "Rejected"
  }

  private def parseStatusField(jval: JObject): Boolean = {
    implicit val formats = DefaultFormats
    val status = (jval \ "status").extract[String]
    statusStringToBoolean(status)
  }

  private def statusStringToBoolean(statusString: String) = statusString match {
    case "Accepted" => true
    case "Rejected" => false
    case _          => throw new MappingException(s"Did not recognize status $statusString (expected 'Accepted' or 'Rejected')")
  }
}
