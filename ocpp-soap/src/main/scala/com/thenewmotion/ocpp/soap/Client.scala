package com.thenewmotion.ocpp
package soap

import com.typesafe.scalalogging.slf4j.Logging

/**
 * @author Yaroslav Klymko
 */
trait Client {
  def chargeBoxIdentity: String
  def version: Version.Value
}

private[ocpp] trait ScalaxbClient extends Logging {
  self: Client =>

  type Service

  def service: Service

  def ?[Req, Res](f: Service => (Req, String) => Either[scalaxb.Fault[Any], Res], req: Req): Res = {

    def logMsg(res: Any): String = s"$chargeBoxIdentity\n \t>> $req\n \t<< $res"

    f(service)(req, chargeBoxIdentity) match {
      case Left(fault) =>
        logger.warn(logMsg(fault.original))
        sys.error(fault.original.toString)
      case Right(res) =>
        logger.info(logMsg(res))
        res
    }
  }

  def notSupported(action: String): Nothing = throw new ActionNotSupportedException(version, action)

  def logNotSupported(name: String, value: Any) {
    logger.warn(s"$name is not supported in OCPP $version, value: $value")
  }
}