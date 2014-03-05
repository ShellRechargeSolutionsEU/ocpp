package com.thenewmotion.ocpp
package messages

/**
 * @author Yaroslav Klymko
 */
package object centralsystem {
  type Scope = com.thenewmotion.ocpp.messages.Scope
  val Scope = com.thenewmotion.ocpp.messages.Scope

  type ConnectorScope = com.thenewmotion.ocpp.messages.ConnectorScope
  val ConnectorScope = com.thenewmotion.ocpp.messages.ConnectorScope

  type ChargePointScope = com.thenewmotion.ocpp.messages.ChargePointScope.type
  val ChargePointScope = com.thenewmotion.ocpp.messages.ChargePointScope

  type IdTagInfo = com.thenewmotion.ocpp.messages.IdTagInfo
  val IdTagInfo = com.thenewmotion.ocpp.messages.IdTagInfo

  type AuthorizationStatus = com.thenewmotion.ocpp.messages.AuthorizationStatus.type
  val AuthorizationStatus = com.thenewmotion.ocpp.messages.AuthorizationStatus

  val DataTransferStatus = com.thenewmotion.ocpp.messages.DataTransferStatus
  type DataTransferStatus = com.thenewmotion.ocpp.messages.DataTransferStatus.type
}
