package com.thenewmotion.ocpp.messages.v20

import enums.reflection.EnumUtils.{Enumerable, Nameable}

case class SendLocalListRequest(
  versionNumber: Int,
  updateType: Update,
  localAuthorizationData: AuthorizationData
) extends CsRequest

sealed trait Update extends Nameable
object Update extends Enumerable[Update] {
  case object Differential extends Update
  case object Full         extends Update

  val values = List(Differential, Full)
}

case class AuthorizationData(
  idTokenInfo: IdTokenInfo,
  idToken: IdToken
)

case class SendLocalListResponse(
  status: UpdateStatus
) extends CsResponse

sealed trait UpdateStatus extends Nameable
object UpdateStatus extends Enumerable[UpdateStatus] {
  case object Accepted extends        UpdateStatus
  case object Failed   extends        UpdateStatus
  case object VersionMismatch extends UpdateStatus

  val values = List(Accepted, Failed, VersionMismatch)
}