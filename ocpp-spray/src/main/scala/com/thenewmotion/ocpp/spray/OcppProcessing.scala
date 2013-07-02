package com.thenewmotion.ocpp.spray

import xml.{XML, NodeSeq, Elem}
import com.thenewmotion.ocpp.Fault._
import com.typesafe.scalalogging.slf4j.Logging
import soapenvelope12.Body
import soapenvelope12.Fault
import soapenvelope12.Envelope
import com.thenewmotion.ocpp._
import _root_.spray.http.{StatusCodes, HttpResponse, HttpRequest}
import StatusCodes._
import java.io.ByteArrayInputStream
import scalax.RichAny

/**
 * The information about the charge point available in an incoming request
 */
case class ChargerInfo(ocppVersion: Option[Version.Value],
                       endpointUrl: Option[Uri],
                       chargerId: String)

object OcppProcessing extends Logging {

  type Response = HttpResponse
  type ChargerResponse = () => Response
  type ChargerId = String
  type Result = Either[Response, (ChargerId, ChargerResponse)]
  /**
   * Function supplied by the user of this class. The function provides us the user's implementation of
   * an OCPP service, which we can call methods on to perform actions according to the OCPP requests we receive.
   */
  type ServiceFunction[ServiceType] = ChargerInfo => ServiceType

  def apply[ServiceType : OcppService](req: HttpRequest, serviceFunction: ServiceFunction[ServiceType]): Result = safe {
      parseRequest(req).right map { case (chargerInfo, body) =>
        lazy val responseBody = dispatch(chargerInfo.ocppVersion, body, serviceFunction(chargerInfo))
        val chargerResponse: ChargerResponse = () => safe(OcppResponse(responseBody)).merge
        chargerInfo.chargerId -> chargerResponse
      }
  }.joinRight

  private def parseRequest(req: HttpRequest): Either[HttpResponse, (ChargerInfo, Body)] = {
    for {
      post <- soapPost(req).right
      xml <- toXml(post).right
      env <- envelope(xml).right
      chargerId <- chargerId(env).right
    } yield {
      val version = Version.fromBody(env.Body)
      val chargerUrl = ChargeBoxAddress.unapply(env)
      val chargerInfo = ChargerInfo(version, chargerUrl, chargerId)
      (chargerInfo, env.Body)
    }
  }

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

  private[spray] def dispatch[T](version: Option[Version.Value], body: Body, service: => T)
                                (implicit dispatcher: Version.Value => Dispatcher[T]): Body = {

    implicit def faultToBody(x: soapenvelope12.Fault) = x.asBody

    val data = for {
      dataRecord <- body.any
      elem <- dataRecord.value.asInstanceOfOpt[Elem]
      action <- CentralSystemAction.fromElem(elem) // TODO @Reinier, please fix this
    } yield action -> elem

    data.headOption match {
      case None if body.any.isEmpty => ProtocolError("Body is empty")
      case None => NotSupported("No supported action found")
      case Some((action, xml)) => version match {
        case None => ProtocolError("Can't find an ocpp version")
        case Some(v) =>
          dispatcher(v).dispatch(action, xml, service)
      }
    }
  }

  implicit def errorToEither[T](x: Fault): Either[Response, T] = Left(OcppResponse(x))
}

/**
 * Exception to be thrown from the service function if no charge box can be found with the charge box ID given in the
 * request
 */
class ChargeBoxIdentityException(val chargerId: String) extends Exception

/**
 * Type class for OCPP services that can be called via SOAP messages
 */
trait OcppService[T] {
  def apply(version: Version.Value): Dispatcher[T]
}

object OcppService {
  implicit val centralSystemOcppService: OcppService[CentralSystemService] = new OcppService[CentralSystemService] {
    def apply(version: Version.Value): Dispatcher[CentralSystemService] = CentralSystemDispatcher(version)
  }

  implicit val chargePointOcppService: OcppService[ChargePointService] = new OcppService[ChargePointService] {
    def apply(version: Version.Value): Dispatcher[ChargePointService] = ChargePointDispatcher(version)
  }
}

