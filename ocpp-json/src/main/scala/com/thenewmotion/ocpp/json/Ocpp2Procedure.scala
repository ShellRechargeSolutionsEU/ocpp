package com.thenewmotion.ocpp
package json

import scala.language.higherKinds
import scala.concurrent.{ExecutionContext, Future}
import v20._
import CsmsReqRes._
import org.json4s.{DefaultFormats, Extraction, Formats, JValue}

abstract class Ocpp2Procedure[
REQBOUND <: Request,
RESBOUND <: Response,
REQ <: REQBOUND : Manifest,
RES <: RESBOUND : Manifest,
REQRES[_ <: REQBOUND, _ <: RESBOUND] <: ReqResV2[_, _]
] {

  implicit val formats: Formats = DefaultFormats

  private val requestManifest: Manifest[REQ] = manifest[REQ]

  private val responseManifest: Manifest[RES] = manifest[RES]

  val name: String =
    requestManifest.runtimeClass.getName.replaceAll("Request\\$?$", "")

  val reqRes: REQRES[REQ, RES]

  def serializeReq(req: REQ): JValue =
    Extraction.decompose(req)

  def deserializeReq(reqJson: JValue): REQ =
    Extraction.extract[REQ](reqJson)(formats, requestManifest)

  def serializeRes(res: RES): JValue =
    Extraction.decompose(res)

  def deserializeRes(resJson: JValue): RES =
    Extraction.extract[RES](resJson)(formats, responseManifest)

  def reqRes(reqJson: JValue)(f: (REQ, REQRES[REQ, RES]) => Future[RES])(implicit ec: ExecutionContext): Future[JValue] =
    f(deserializeReq(reqJson), reqRes).map(serializeRes)
}

object Ocpp2Procedure {
  def apply[
    REQBOUND <: Request,
    RESBOUND <: Response,
    REQ <: REQBOUND : Manifest,
    RES <: RESBOUND : Manifest,
    REQRES[_ <: REQBOUND, _ <: RESBOUND] <: ReqResV2[_, _]
  ](_reqRes: REQRES[REQ, RES]): Ocpp2Procedure[REQBOUND, RESBOUND, REQ, RES, REQRES] =
    new Ocpp2Procedure[REQBOUND, RESBOUND, REQ, RES, REQRES] {
      val reqRes: REQRES[REQ, RES] = _reqRes
    }
}

object CsOcpp2Procedure {
  def apply[REQ <: CsRequest : Manifest,
            RES <: CsResponse : Manifest,
            REQRES[_ <: CsRequest, _ <: CsResponse] <: CsReqRes[_, _]](
    implicit reqRes: REQRES[REQ, RES]
  ):
    Ocpp2Procedure[CsRequest, CsResponse, REQ, RES, REQRES] =
      Ocpp2Procedure[CsRequest, CsResponse, REQ, RES, REQRES](reqRes)
}

object CsmsOcpp2Procedure {
  def apply[REQ <: CsmsRequest : Manifest,
  RES <: CsmsResponse : Manifest,
  REQRES[_ <: CsmsRequest, _ <: CsmsResponse] <: CsmsReqRes[_, _]](
    implicit reqRes: REQRES[REQ, RES]
  ):
  Ocpp2Procedure[CsmsRequest, CsmsResponse, REQ, RES, REQRES] =
    Ocpp2Procedure[CsmsRequest, CsmsResponse, REQ, RES, REQRES](reqRes)
}

trait Ocpp2Procedures[REQBOUND <: Request, RESBOUND <: Response, REQRES[_ <: REQBOUND, _ <: RESBOUND] <: ReqResV2[_, _]] {

  type MyProcedure[REQ <: REQBOUND, RES <: RESBOUND] = Ocpp2Procedure[REQBOUND, RESBOUND, REQ, RES, REQRES]

  val procedures: List[MyProcedure[_ <: REQBOUND, _ <: RESBOUND]]

  def procedureByName(name: String): Option[MyProcedure[_ <: REQBOUND, _ <: RESBOUND]] =
    procedures.find(_.name == name)

  def procedureByReqRes[REQ <: REQBOUND, RES <: RESBOUND](reqRes: REQRES[REQ, RES]): Option[MyProcedure[_ <: REQBOUND, _ <: RESBOUND]] =
    procedures.find(_.reqRes == reqRes)
}

object CsOcpp2Procedures extends Ocpp2Procedures[
  CsRequest,
  CsResponse,
  CsReqRes
  ] {
  val procedures = List(
  )
}

object CsmsOcpp2Procedures extends Ocpp2Procedures[
  CsmsRequest,
  CsmsResponse,
  CsmsReqRes
] {
  val procedures = List(
    CsmsOcpp2Procedure[BootNotificationRequest, BootNotificationResponse, CsmsReqRes](
      implicitly[Manifest[BootNotificationRequest]],
      implicitly[Manifest[BootNotificationResponse]],
      BootNotificationReqRes
    )
  )
}