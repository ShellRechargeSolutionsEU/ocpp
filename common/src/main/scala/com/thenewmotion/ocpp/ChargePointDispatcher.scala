package com.thenewmotion.ocpp

import scala.xml.NodeSeq
import soapenvelope12.Body

/**
 * Can call the corresponding methods on a ChargePointService object when given a message containing a request sent to
 * a charge point.
 */

object ChargePointDispatcher {
  def apply(version: Version.Value): Dispatcher[ChargePointService] = version match {
    case Version.V12 => sys.error("Requests to the charge point are not yet supported with OCPP 1.2")
    case Version.V15 => new ChargePointDispatcherV15
  }
}

class ChargePointDispatcherV15 extends Dispatcher[ChargePointService] {
  def version: Version.Value = Version.V15

  val actions = ChargePointAction
  import actions._

  def dispatch(action: Value, xml: NodeSeq, service: => ChargePointService): Body = {
    import v15._

    action match {

      case CancelReservation => ?[CancelReservationRequest, CancelReservationResponse](action, xml) {
        req =>
          def booleanToCancelReservationStatus(s: Boolean) = if (s) CancelReservationStatus.fromString("Accepted")
                                                             else   CancelReservationStatus.fromString("Rejected")

          CancelReservationResponse(booleanToCancelReservationStatus(service.cancelReservation(req.reservationId)))
      }

      case GetLocalListVersion => ?[GetLocalListVersionRequest, GetLocalListVersionResponse](action, xml) {
        req =>
          def versionToInt(v: AuthListVersion): Int = v match {
            case AuthListNotSupported => -1
            case AuthListSupported(i) => i
          }
          GetLocalListVersionResponse(versionToInt(service.getLocalListVersion))
      }
    }
  }
}
