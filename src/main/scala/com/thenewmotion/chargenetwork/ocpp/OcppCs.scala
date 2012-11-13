package com.thenewmotion.chargenetwork.ocpp

import xml.{Elem, NodeSeq}
import scalaxb.CanWriteXML
import soapenvelope12.Body

/**
 * @author Yaroslav Klymko
 */
object OcppCs {

  object Action {

    def unapply(action: String): Option[Action] = PartialFunction.condOpt(action.toLowerCase) {
      case "/authorize"                     ⇒ Authorize
      case "/starttransaction"              ⇒ StartTransaction
      case "/stoptransaction"               ⇒ StopTransaction
      case "/bootnotification"              ⇒ BootNotification
      case "/diagnosticsstatusnotification" ⇒ DiagnosticsStatusNotification
      case "/firmwarestatusnotification"    ⇒ FirmwareStatusNotification
      case "/heartbeat"                     ⇒ Heartbeat
      case "/metervalues"                   ⇒ MeterValues
      case "/statusnotification"            ⇒ StatusNotification
    }

    def unapply(body: NodeSeq): Option[Action] = {
      body.headOption.flatMap(node ⇒ PartialFunction.condOpt(node.label.toLowerCase) {
        case "authorizerequest"                     ⇒ Authorize
        case "starttransactionrequest"              ⇒ StartTransaction
        case "stoptransactionrequest"               ⇒ StopTransaction
        case "bootnotificationrequest"              ⇒ BootNotification
        case "diagnosticsstatusnotificationrequest" ⇒ DiagnosticsStatusNotification
        case "firmwarestatusnotificationrequest"    ⇒ FirmwareStatusNotification
        case "heartbeatrequest"                     ⇒ Heartbeat
        case "metervaluesrequest"                   ⇒ MeterValues
        case "statusnotificationrequest"            ⇒ StatusNotification
      })
    }

    def unapply(body: Body): Option[Action] = {
      body.any.headOption.flatMap(dr ⇒ PartialFunction.condOpt(dr.value) {
        case elem: Elem ⇒ elem
      }).flatMap(unapply)
    }
  }

  sealed abstract class Action(label: String) {
    override val toString = label
  }

  case object Authorize                     extends Action("Authorize")
  case object StartTransaction              extends Action("StartTransaction")
  case object StopTransaction               extends Action("StopTransaction")
  case object BootNotification              extends Action("BootNotification")
  case object DiagnosticsStatusNotification extends Action("DiagnosticsStatusNotification")
  case object FirmwareStatusNotification    extends Action("FirmwareStatusNotification")
  case object Heartbeat                     extends Action("Heartbeat")
  case object MeterValues                   extends Action("MeterValues")
  case object StatusNotification            extends Action("StatusNotification")

  def toXml[T](obj: T, label: String)(implicit format: CanWriteXML[T]) = {
    scalaxb.toXML(obj, Some("urn://Ocpp/Cs/2010/08/"), label, defaultScope)
  }
}
