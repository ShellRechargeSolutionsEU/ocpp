package com.thenewmotion.ocpp
package messages

/**
 * @author Yaroslav Klymko
 */
package object chargepoint {
  type Scope = com.thenewmotion.ocpp.Scope
  val Scope = com.thenewmotion.ocpp.Scope

  type ConnectorScope = com.thenewmotion.ocpp.ConnectorScope
  val ConnectorScope = com.thenewmotion.ocpp.ConnectorScope

  type ChargePointScope = com.thenewmotion.ocpp.ChargePointScope.type
  val ChargePointScope = com.thenewmotion.ocpp.ChargePointScope

  val IdTagInfo = com.thenewmotion.ocpp.IdTagInfo
  type IdTagInfo = com.thenewmotion.ocpp.IdTagInfo

  val AuthorizationStatus = com.thenewmotion.ocpp.AuthorizationStatus
  type AuthorizationStatus = com.thenewmotion.ocpp.AuthorizationStatus.type

  val DataTransferStatus = com.thenewmotion.ocpp.DataTransferStatus
  type DataTransferStatus = com.thenewmotion.ocpp.DataTransferStatus.type
}
