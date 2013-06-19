package com.thenewmotion.ocpp.spray

import xml.{XML, NodeSeq}
import com.thenewmotion.ocpp.Fault._
import com.typesafe.scalalogging.slf4j.Logging
import soapenvelope12.Body
import soapenvelope12.Fault
import soapenvelope12.Envelope
import java.net.URI
import com.thenewmotion.ocpp._
import _root_.spray.http.{StatusCodes, HttpResponse, HttpRequest}
import StatusCodes._
import java.io.ByteArrayInputStream


object OcppProcessing extends Logging {

  type Response = HttpResponse
  type ChargerResponse = () => Response
  type ChargerId = String
  type Result = Either[Response, (ChargerId, ChargerResponse)]
  /**
   * Function supplied by the user of this class. The function provides us the user's implementation of
   * CentralSystemService, which we can call methods on to perform actions according to the OCPP requests we receive.
   */
  type ServiceFunction = Option[Version.Value] => Option[URI] => ChargerId => CentralSystemService

  def apply(req: HttpRequest, serviceFunction: ServiceFunction): Result = safe {
    for {
      post <- soapPost(req).right
      xml <- toXml(post).right
      env <- envelope(xml).right
      chargerId <- chargerId(env).right
    } yield {
      val version = Version.fromBody(env.Body)
      val chargerUrl = ChargeBoxAddress.unapply(env)
      val chargerResponse = () => safe(OcppResponse(responseBody(chargerId, version, serviceFunction(version)(chargerUrl), env.Body))).merge
      chargerId -> chargerResponse
    }
  }.joinRight

  private def safe[T](func: ⇒ T): Either[Response, T] =
    try Right(func) catch {
      case e: ChargeBoxIdentityException =>
        val msg = s"Charge box ${e.chargerId} not found"
        logger.warn(msg)
        IdentityMismatch(msg)
      case e: Exception =>
        if (logger.underlying.isDebugEnabled) logger.error(e.getMessage, e)
        else logger.error(e.getMessage)
        InternalError(e.getMessage)
    }

  private def soapPost(req: HttpRequest): Either[Response, HttpRequest] =
    SoapPost.unapply(req) match {
      case None => Left(HttpResponse(NotFound))
      case _ => Right(req)
    }

  private def toXml(req: HttpRequest): Either[Response, NodeSeq] = {
    try XML.load(new ByteArrayInputStream(req.entity.buffer)) match {
      case NodeSeq.Empty => ProtocolError("Body is empty")
      case xml =>
        logger.debug(">> " + xml.toString())
        Right(xml)
    } catch {
      case e: Exception =>
        logger.error(e.getMessage)
        ProtocolError(e.getMessage)
    }
  }

  private def envelope(xml: NodeSeq): Either[Response, Envelope] =
    scalaxb.fromXMLEither[Envelope](xml).left.map(x => OcppResponse(ProtocolError(x)))

  private def chargerId(env: Envelope): Either[Response, ChargerId] =
    ChargeBoxIdentity.unapply(env).toRight {
      val msg = "Failed to parse 'chargeBoxIdentity' value"
      logger.warn(msg)
      OcppResponse(ProtocolError(msg))
    }

  private def responseBody(chargerId: String, protocol: Option[Version.Value],
                           serviceFunction: ChargerId => CentralSystemService, body: Body): Body = {
    def log(x: Any) {}

    CentralSystemDispatcher(body, _ => serviceFunction(chargerId), log)
  }

  implicit def errorToEither[T](x: Fault): Either[Response, T] = Left(OcppResponse(x))
}

/**
 * Exception to be thrown from the service function if no charge box can be found with the charge box ID given in the
 * request
 */
class ChargeBoxIdentityException(val chargerId: String) extends Exception
