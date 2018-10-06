package com.thenewmotion.ocpp.messages.v1x

/**
 * @author Yaroslav Klymko
 */
sealed trait Scope
case object ChargePointScope extends Scope
case class ConnectorScope(id: Int) extends Scope

object Scope {
  private[ocpp] def fromOcpp(connectorId: Int): Scope =
    if (connectorId <= 0) ChargePointScope else ConnectorScope.fromOcpp(connectorId)

  private[ocpp] implicit class RichScope(val self: Scope) extends AnyVal {
    def toOcpp: Int = self match {
      case ChargePointScope => 0
      case ConnectorScope(id) => id + 1
    }
  }
}

object ConnectorScope {
  private[ocpp] def fromOcpp(connectorId: Int): ConnectorScope = ConnectorScope(connectorId - 1)
}
