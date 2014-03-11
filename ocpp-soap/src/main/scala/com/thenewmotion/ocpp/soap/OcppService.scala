package com.thenewmotion.ocpp
package soap

import messages.{ChargePointReq => CpReq, ChargePointRes => CpRes, CentralSystemReq => CsReq, CentralSystemRes => CsRes}


/**
 * Type class for OCPP services that can be called via SOAP messages
 */
trait OcppService[REQ, RES] {
  def apply(version: Version.Value): Dispatcher[REQ, RES]
  def namespace(version: Version.Value): String
}

object OcppService {
  implicit val centralSystemOcppService: OcppService[CsReq, CsRes] = new OcppService[CsReq, CsRes] {
    def apply(version: Version.Value) = CentralSystemDispatcher(version)

    def namespace(version: Version.Value) = version match {
      case Version.V12 => "urn://Ocpp/Cs/2010/08/"
      case Version.V15 => "urn://Ocpp/Cs/2012/06/"
    }
  }

  implicit val chargePointOcppService: OcppService[CpReq, CpRes] = new OcppService[CpReq, CpRes] {
    def apply(version: Version.Value) = ChargePointDispatcher(version)

    def namespace(version: Version.Value) = version match {
      case Version.V12 => "urn://Ocpp/Cp/2010/08/"
      case Version.V15 => "urn://Ocpp/Cp/2012/06/"
    }
  }
}