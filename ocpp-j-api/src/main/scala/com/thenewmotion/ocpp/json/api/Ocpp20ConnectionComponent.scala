package com.thenewmotion.ocpp
package json
package api

import scala.util.control.NonFatal
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.language.higherKinds
import messages.v20._
import json.v20._
import org.json4s.{JValue, MappingException}
import org.slf4j.{Logger, LoggerFactory}

/** One roles of the OCPP 2.0 communicatino protocol: Charging Station (CS) or
  * Charging Station Management System (CSMS)
  */
sealed trait Side
case object Cs extends Side
case object Csms extends Side

trait Ocpp20ConnectionComponent[
  OUTREQBOUND <: Request,
  INRESBOUND <: Response,
  OUTREQRES[_ <: OUTREQBOUND,_ <: INRESBOUND] <: ReqResV2[_, _],
  INREQBOUND <: Request,
  OUTRESBOUND <: Response,
  INREQRES[_ <: INREQBOUND, _ <: OUTRESBOUND] <: ReqResV2[_, _]
] extends OcppConnectionComponent[OUTREQBOUND, INRESBOUND, OUTREQRES, INREQBOUND, OUTRESBOUND, INREQRES] {

  this: SrpcComponent =>

  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  implicit val executionContext: ExecutionContext

  trait Ocpp20Connection extends OcppConnection {

    def incomingProcedures: Ocpp20Procedures[INREQBOUND, OUTRESBOUND, INREQRES]
    val outgoingProcedures: Ocpp20Procedures[OUTREQBOUND, INRESBOUND, OUTREQRES]

    def onSrpcCall(call: SrpcCall): Future[SrpcResponse] = {
      val ocppProc = incomingProcedures.procedureByName(call.procedureName)
      ocppProc match {
        case None =>
          // TODO distinguish NotSupported and NotImplemented
          Future.successful(SrpcCallError(PayloadErrorCode.NotImplemented,
                                          "This OCPP 2.0 procedure is not yet implemented"))
        case Some(procedure) =>
          // TODO scalafmt opzetten
          val jsonResponse: Future[JValue] = procedure.reqRes(call.payload) { (req, rr) =>
            Ocpp20ConnectionComponent.this.onRequest(req)(rr)
          }
          jsonResponse
            .map(SrpcCallResult)
            .recover[SrpcResponse] { case NonFatal(e) =>
            // TODO this bit copied from DefaultOcppConnection, should be shared
            logger.warn("Exception processing incoming OCPP request {}: {} {}",
                        call.procedureName, e.getClass.getSimpleName, e.getMessage)

            val ocppError = e match {
              case OcppException(err) =>
                err
              case MappingException(msg, _) =>
                OcppError(PayloadErrorCode.FormationViolation, msg)
              case _ =>
                OcppError(PayloadErrorCode.InternalError, "Unexpected error processing request")
            }

            SrpcCallError(
              ocppError.error,
              ocppError.description
            )
                                   }
      }
    }

    def sendRequest[REQ <: OUTREQBOUND, RES <: INRESBOUND](req: REQ)(implicit reqRes: OUTREQRES[REQ, RES]): Future[RES] = {
      val procedure = outgoingProcedures.procedureByReqRes(reqRes)
      procedure match {
        case None =>
          throw OcppException(PayloadErrorCode.NotSupported, "This OCPP procedure is not supported")
        case Some(proc) =>
          val srpcCall = SrpcCall(proc.name, proc.serializeReq(req))
          srpcConnection.sendCall(srpcCall) map {
            case SrpcCallResult(payload) =>
              proc.deserializeRes(payload)
            case SrpcCallError(code, description, details) =>
              throw OcppException(code, description)
          }
      }
    }
  }

  def ocppVersion: Version = Version.V20

  def ocppConnection: Ocpp20Connection

  override def onSrpcCall(msg: SrpcCall):  Future[SrpcResponse] = ocppConnection.onSrpcCall(msg)
}

trait CsOcpp20ConnectionComponent extends Ocpp20ConnectionComponent[
    CsmsRequest,
    CsmsResponse,
    CsmsReqRes,
    CsRequest,
    CsResponse,
    CsReqRes
  ] {
  self: SrpcComponent =>

  val ocppConnection = new Ocpp20Connection {

    val incomingProcedures: Ocpp20Procedures[CsRequest, CsResponse, CsReqRes] = CsOcpp20Procedures

    val outgoingProcedures: Ocpp20Procedures[CsmsRequest, CsmsResponse, CsmsReqRes] = CsmsOcpp20Procedures

    override def sendRequestUntyped(req: CsmsRequest): Future[CsmsResponse] = {
      req match {
        case r: BootNotificationRequest => sendRequest(r)
      }
    }
  }
}

trait CsmsOcpp20ConnectionComponent extends Ocpp20ConnectionComponent[
  CsRequest,
  CsResponse,
  CsReqRes,
  CsmsRequest,
  CsmsResponse,
  CsmsReqRes
  ] {
  self: SrpcComponent =>

  val ocppConnection = new Ocpp20Connection {

    val incomingProcedures: Ocpp20Procedures[CsmsRequest, CsmsResponse, CsmsReqRes] = CsmsOcpp20Procedures

    val outgoingProcedures: Ocpp20Procedures[CsRequest, CsResponse, CsReqRes] = CsOcpp20Procedures

    override def sendRequestUntyped(req: CsRequest): Future[CsResponse] = {
      req match {
        // TODO zo een request definiëren
        case _ => sys.error("Er zijn nog helemaal niet zulke requests :->")
      }
    }
  }
}
