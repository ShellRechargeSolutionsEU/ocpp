package com.thenewmotion.ocpp

/**
 * Type class for OCPP services that can be called via SOAP messages
 */
trait OcppService[T] {
  def apply(version: Version.Value): Dispatcher[T]
  def namespace(version: Version.Value): String
}

object OcppService {
  implicit val centralSystemOcppService: OcppService[CentralSystemService] = new OcppService[CentralSystemService] {
    def apply(version: Version.Value) = CentralSystemDispatcher(version)

    def namespace(version: Version.Value) = version match {
      case Version.V12 => "urn://Ocpp/Cs/2010/08/"
      case Version.V15 => "urn://Ocpp/Cs/2012/06/"
    }
  }

  implicit val chargePointOcppService: OcppService[ChargePointService] = new OcppService[ChargePointService] {
    def apply(version: Version.Value) = ChargePointDispatcher(version)

    def namespace(version: Version.Value) = version match {
      case Version.V12 => "urn://Ocpp/Cp/2010/08/"
      case Version.V15 => "urn://Ocpp/Cp/2012/06/"
    }
  }
}

