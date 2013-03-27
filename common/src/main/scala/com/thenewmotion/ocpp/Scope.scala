package com.thenewmotion.ocpp

/**
 * @author Yaroslav Klymko
 */
sealed trait Scope
case object ChargePointScope extends Scope
case class ConnectorScope(id: Int) extends Scope

object Scope {
  def fromOcpp(connectorId: Int): Scope =
    if (connectorId <= 0) ChargePointScope else ConnectorScope.fromOcpp(connectorId)

  private[ocpp] implicit def reachScope(self: Scope) = new {
    def toOcpp: Int = self match {
      case ChargePointScope => 0
      case ConnectorScope(id) => id + 1
    }
  }
}

object ConnectorScope {
  def fromOcpp(connectorId: Int): ConnectorScope = ConnectorScope(connectorId - 1)
}