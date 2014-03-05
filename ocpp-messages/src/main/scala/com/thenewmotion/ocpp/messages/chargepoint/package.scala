package com.thenewmotion.ocpp
package messages

/**
 * @author Yaroslav Klymko
 */
package object chargepoint {
  type Scope = com.thenewmotion.ocpp.messages.Scope
  val Scope = com.thenewmotion.ocpp.messages.Scope

  type ConnectorScope = com.thenewmotion.ocpp.messages.ConnectorScope
  val ConnectorScope = com.thenewmotion.ocpp.messages.ConnectorScope

  type ChargePointScope = com.thenewmotion.ocpp.messages.ChargePointScope.type
  val ChargePointScope = com.thenewmotion.ocpp.messages.ChargePointScope

  val IdTagInfo = com.thenewmotion.ocpp.messages.IdTagInfo
  type IdTagInfo = com.thenewmotion.ocpp.messages.IdTagInfo

  val AuthorizationStatus = com.thenewmotion.ocpp.messages.AuthorizationStatus
  type AuthorizationStatus = com.thenewmotion.ocpp.messages.AuthorizationStatus.type

  val DataTransferStatus = com.thenewmotion.ocpp.messages.DataTransferStatus
  type DataTransferStatus = com.thenewmotion.ocpp.messages.DataTransferStatus.type
}
