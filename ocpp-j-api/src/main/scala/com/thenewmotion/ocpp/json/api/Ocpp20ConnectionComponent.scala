package com.thenewmotion.ocpp
package json
package api

import com.thenewmotion.ocpp.messages.ReqRes

import scala.concurrent.{ExecutionContext, Future}
import scala.language.higherKinds
import scala.util.{Failure, Success, Try}
import messages.v20._
import json.v20._
import org.json4s.JValue

/** One roles of the OCPP 2.0 communication protocol: Charging Station (CS) or
  * Charging Station Management System (CSMS)
  */
trait Ocpp20ConnectionComponent[
  OUTREQBOUND <: Request,
  INRESBOUND <: Response,
  OUTREQRES[_ <: OUTREQBOUND,_ <: INRESBOUND] <: ReqRes[_, _],
  INREQBOUND <: Request,
  OUTRESBOUND <: Response,
  INREQRES[_ <: INREQBOUND, _ <: OUTRESBOUND] <: ReqRes[_, _]
] extends BaseOcppConnectionComponent[OUTREQBOUND, INRESBOUND, OUTREQRES, INREQBOUND, OUTRESBOUND, INREQRES] {

  this: SrpcComponent =>

  protected implicit val executionContext: ExecutionContext

  trait Ocpp20Connection extends BaseOcppConnection {

    def incomingProcedures: Ocpp20Procedures[INREQBOUND, OUTRESBOUND, INREQRES]

    val outgoingProcedures: Ocpp20Procedures[OUTREQBOUND, INRESBOUND, OUTREQRES]

    def onSrpcCall(call: SrpcCall): Future[SrpcResponse] = {
      val ocppProc = incomingProcedures.procedureByName(call.procedureName)
      ocppProc match {
        case None =>
          Future.successful(
            SrpcCallError(
              PayloadErrorCode.NotImplemented,
              "This OCPP 2.0 procedure is not yet implemented"
            )
          )
        case Some(procedure) =>
          val jsonResponse: Future[JValue] = procedure.reqRes(call.payload) { (req, rr) =>
            Ocpp20ConnectionComponent.this.onRequest(req)(rr)
                                                                            }
          jsonResponse
            .map(SrpcCallResult)
            .recover(logIncomingRequestHandlingError(call) andThen requestHandlerErrorToSrpcCallResult)
      }
    }

    def sendRequest[REQ <: OUTREQBOUND, RES <: INRESBOUND](req: REQ)
      (implicit reqRes: OUTREQRES[REQ, RES]): Future[RES] = {
      val procedure = outgoingProcedures.procedureByReqRes(reqRes)
      procedure match {
        case None =>
          Future.failed(OcppException(PayloadErrorCode.NotSupported, "This OCPP procedure is not supported"))
        case Some(proc) =>
          safeSrpcCall(proc.name, proc.serializeReq(req)) map {
            case SrpcCallResult(payload) =>
              proc.deserializeRes(payload)
            case SrpcCallError(code, description, details) =>
              throw OcppException(code, description)
          }
      }
    }

    private def safeSrpcCall(operationName: String, payload: JValue): Future[SrpcResponse] = {
      val srpcCall = SrpcCall(operationName, payload)
      Try(srpcConnection.sendCall(srpcCall)) match {
        case Success(futureResponse) =>
          futureResponse
        case Failure(e) =>
          Future.failed(OcppException(PayloadErrorCode.GenericError, "Failed to obtain response from SRPC layer"))
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

  lazy val ocppConnection = new Ocpp20Connection {

    val incomingProcedures: Ocpp20Procedures[CsRequest, CsResponse, CsReqRes] = CsOcpp20Procedures

    val outgoingProcedures: Ocpp20Procedures[CsmsRequest, CsmsResponse, CsmsReqRes] = CsmsOcpp20Procedures

    override def sendRequestUntyped(req: CsmsRequest): Future[CsmsResponse] =
      req match {
        case r: AuthorizeRequest          => sendRequest(r)
        case r: BootNotificationRequest   => sendRequest(r)
        case r: HeartbeatRequest          => sendRequest(r)
        case r: NotifyReportRequest       => sendRequest(r)
        case r: StatusNotificationRequest => sendRequest(r)
        case r: TransactionEventRequest   => sendRequest(r)
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

  lazy val ocppConnection = new Ocpp20Connection {

    val incomingProcedures: Ocpp20Procedures[CsmsRequest, CsmsResponse, CsmsReqRes] = CsmsOcpp20Procedures

    val outgoingProcedures: Ocpp20Procedures[CsRequest, CsResponse, CsReqRes] = CsOcpp20Procedures

    override def sendRequestUntyped(req: CsRequest): Future[CsResponse] =
      req match {
        case r: GetBaseReportRequest           => sendRequest(r)
        case r: GetTransactionStatusRequest    => sendRequest(r)
        case r: GetVariablesRequest            => sendRequest(r)
        case r: RequestStartTransactionRequest => sendRequest(r)
        case r: RequestStopTransactionRequest  => sendRequest(r)
        case r: SendLocalListRequest           => sendRequest(r)
        case r: SetVariablesRequest            => sendRequest(r)
      }
  }
}
