package com.thenewmotion.ocpp

/**
 * @author Yaroslav Klymko
 */
package object centralsystem {
  trait CentralSystem extends (Req => Res)

  type Scope = com.thenewmotion.ocpp.Scope
  val Scope = com.thenewmotion.ocpp.Scope

  type ConnectorScope = com.thenewmotion.ocpp.ConnectorScope
  val ConnectorScope = com.thenewmotion.ocpp.ConnectorScope

  type ChargePointScope = com.thenewmotion.ocpp.ChargePointScope.type
  val ChargePointScope = com.thenewmotion.ocpp.ChargePointScope

  type IdTagInfo = com.thenewmotion.ocpp.IdTagInfo
  val IdTagInfo = com.thenewmotion.ocpp.IdTagInfo

  type AuthorizationStatus = com.thenewmotion.ocpp.AuthorizationStatus.type
  val AuthorizationStatus = com.thenewmotion.ocpp.AuthorizationStatus

  val DataTransferStatus = com.thenewmotion.ocpp.DataTransferStatus
  type DataTransferStatus = com.thenewmotion.ocpp.DataTransferStatus.type
}
