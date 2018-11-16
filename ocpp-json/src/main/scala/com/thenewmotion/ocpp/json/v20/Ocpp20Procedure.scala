package com.thenewmotion.ocpp
package json
package v20

import messages.ReqRes
import messages.v20._
import messages.v20.CsmsReqRes._
import messages.v20.CsReqRes._
import org.json4s.JValue

import scala.concurrent.{ExecutionContext, Future}
import scala.language.higherKinds

abstract class Ocpp20Procedure[
  REQBOUND <: Request,
  RESBOUND <: Response,
  REQ <: REQBOUND : Manifest,
  RES <: RESBOUND : Manifest,
  REQRES[_ <: REQBOUND, _ <: RESBOUND] <: ReqRes[_, _]
] {

  private val requestManifest: Manifest[REQ] = manifest[REQ]

  private val responseManifest: Manifest[RES] = manifest[RES]

  val name: String =
    requestManifest.runtimeClass.getSimpleName.replaceAll("Request\\$?$", "")

  val reqRes: REQRES[REQ, RES]

  def serializeReq(req: REQ): JValue =
    Serialization.serialize(req)

  def deserializeReq(reqJson: JValue): REQ =
    Serialization.deserialize[REQ](reqJson)(requestManifest)

  def serializeRes(res: RES): JValue =
    Serialization.serialize(res)

  def deserializeRes(resJson: JValue): RES =
    Serialization.deserialize[RES](resJson)(responseManifest)

  def reqRes(reqJson: JValue)(f: (REQ, REQRES[REQ, RES]) => Future[RES])(implicit ec: ExecutionContext): Future[JValue] =
    f(deserializeReq(reqJson), reqRes).map(serializeRes)
}

object Ocpp20Procedure {
  def apply[
    REQBOUND <: Request,
    RESBOUND <: Response,
    REQ <: REQBOUND : Manifest,
    RES <: RESBOUND : Manifest,
    REQRES[_ <: REQBOUND, _ <: RESBOUND] <: ReqRes[_, _]
  ](implicit rr: REQRES[REQ, RES]): Ocpp20Procedure[REQBOUND, RESBOUND, REQ, RES, REQRES] =
    new Ocpp20Procedure[REQBOUND, RESBOUND, REQ, RES, REQRES] {
      val reqRes: REQRES[REQ, RES] = rr
    }
}

object CsOcpp20Procedure {
  def apply[REQ <: CsRequest : Manifest,
            RES <: CsResponse : Manifest,
            REQRES[_ <: CsRequest, _ <: CsResponse] <: CsReqRes[_, _]](
    implicit reqRes: REQRES[REQ, RES]
  ):
    Ocpp20Procedure[CsRequest, CsResponse, REQ, RES, REQRES] =
      Ocpp20Procedure[CsRequest, CsResponse, REQ, RES, REQRES]
}

object CsmsOcpp20Procedure {
  def apply[REQ <: CsmsRequest : Manifest,
  RES <: CsmsResponse : Manifest,
  REQRES[_ <: CsmsRequest, _ <: CsmsResponse] <: CsmsReqRes[_, _]](
    implicit reqRes: REQRES[REQ, RES]
  ):
  Ocpp20Procedure[CsmsRequest, CsmsResponse, REQ, RES, REQRES] =
    Ocpp20Procedure[CsmsRequest, CsmsResponse, REQ, RES, REQRES]
}

trait Ocpp20Procedures[REQBOUND <: Request, RESBOUND <: Response, REQRES[_ <: REQBOUND, _ <: RESBOUND] <: ReqRes[_, _]] {

  type MyProcedure[REQ <: REQBOUND, RES <: RESBOUND] = Ocpp20Procedure[REQBOUND, RESBOUND, REQ, RES, REQRES]

  val procedures: List[MyProcedure[_ <: REQBOUND, _ <: RESBOUND]]

  def procedureByName(name: String): Option[MyProcedure[_ <: REQBOUND, _ <: RESBOUND]] =
    procedures.find(_.name == name)

  def procedureByReqRes[REQ <: REQBOUND, RES <: RESBOUND](reqRes: REQRES[REQ, RES]): Option[MyProcedure[REQ, RES]] =
    procedures.find(_.reqRes == reqRes).map(_.asInstanceOf[MyProcedure[REQ, RES]])
}

object CsOcpp20Procedures extends Ocpp20Procedures[
  CsRequest,
  CsResponse,
  CsReqRes
  ] {
  val procedures = List(
    CsOcpp20Procedure[GetBaseReportRequest          , GetBaseReportResponse          , CsReqRes],
    CsOcpp20Procedure[GetTransactionStatusRequest   , GetTransactionStatusResponse   , CsReqRes],
    CsOcpp20Procedure[GetVariablesRequest           , GetVariablesResponse           , CsReqRes],
    CsOcpp20Procedure[RequestStartTransactionRequest, RequestStartTransactionResponse, CsReqRes],
    CsOcpp20Procedure[RequestStopTransactionRequest , RequestStopTransactionResponse , CsReqRes],
    CsOcpp20Procedure[SendLocalListRequest          , SendLocalListResponse          , CsReqRes],
    CsOcpp20Procedure[SetVariablesRequest           , SetVariablesResponse           , CsReqRes]
  )
}

object CsmsOcpp20Procedures extends Ocpp20Procedures[
  CsmsRequest,
  CsmsResponse,
  CsmsReqRes
] {
  val procedures = List(
    CsmsOcpp20Procedure[AuthorizeRequest         , AuthorizeResponse         , CsmsReqRes],
    CsmsOcpp20Procedure[BootNotificationRequest  , BootNotificationResponse  , CsmsReqRes],
    CsmsOcpp20Procedure[HeartbeatRequest         , HeartbeatResponse         , CsmsReqRes],
    CsmsOcpp20Procedure[NotifyReportRequest      , NotifyReportResponse      , CsmsReqRes],
    CsmsOcpp20Procedure[StatusNotificationRequest, StatusNotificationResponse, CsmsReqRes],
    CsmsOcpp20Procedure[TransactionEventRequest  , TransactionEventResponse  , CsmsReqRes]
  )
}