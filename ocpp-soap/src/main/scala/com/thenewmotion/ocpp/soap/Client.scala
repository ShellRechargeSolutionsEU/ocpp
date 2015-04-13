package com.thenewmotion.ocpp
package soap

import org.slf4j.LoggerFactory

/**
 * @author Yaroslav Klymko
 */
trait Client {
  def chargeBoxIdentity: String
  def version: Version.Value
}

private[ocpp] trait ScalaxbClient {
  self: Client =>

  private val logger = LoggerFactory.getLogger(ScalaxbClient.this.getClass)

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