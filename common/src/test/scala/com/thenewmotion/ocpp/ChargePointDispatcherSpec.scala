package com.thenewmotion.ocpp

import org.specs2.mock.Mockito
import org.specs2.mutable.SpecificationWithJUnit
import ChargePointAction._

class ChargePointDispatcherSpec extends SpecificationWithJUnit with Mockito {

  "ChargePointDispatcherV15" should {
    import v15.{ChargePointService => _, _}

    "call cancelReservation and return its false result" in {
      val cpService = mock[ChargePointService]
      cpService.cancelReservation(13) returns false

      val xml = <CancelReservation><reservationId xmlns="urn://Ocpp/Cp/2012/06/">13</reservationId></CancelReservation>
      val body = (new ChargePointDispatcherV15).dispatch(CancelReservation, xml, cpService)

      body.any(0).value mustEqual CancelReservationResponse(CancelReservationStatus.fromString("Rejected"))
    }

    "call cancelReservation and return its true result" in {
      val cpService = mock[ChargePointService]
      cpService.cancelReservation(4) returns true

      val xml = <CancelReservation><reservationId xmlns="urn://Ocpp/Cp/2012/06/">4</reservationId></CancelReservation>
      val body = (new ChargePointDispatcherV15).dispatch(CancelReservation, xml, cpService)

      body.any(0).value mustEqual CancelReservationResponse(CancelReservationStatus.fromString("Accepted"))
    }

    "return auth list version reported by charge point service to client" in {
      val cpService = mock[ChargePointService]
      cpService.getLocalListVersion returns AuthListSupported(37)

      val body = (new ChargePointDispatcherV15).dispatch(GetLocalListVersion, <GetLocalListVersion/>, cpService)
      body.any(0).value mustEqual GetLocalListVersionResponse(37)
    }

    "return auth list version -1 to client if service says local auth list is not supported" in {
      val cpService = mock[ChargePointService]
      cpService.getLocalListVersion returns AuthListNotSupported

      val dispatcher = new ChargePointDispatcherV15

      val body = dispatcher.dispatch(GetLocalListVersion, <GetLocalListVersion/>, cpService)
      body.any(0).value mustEqual GetLocalListVersionResponse(-1)
    }
  }
}
