package com.thenewmotion.ocpp
package messages
package v20

import enums.reflection.EnumUtils.{Enumerable, Nameable}

case class AuthorizeRequest(
  evseId: Option[List[Int]],
  idToken: IdToken,
  `15118CertificateHashData`: Option[List[OCSPRequestData]]
) extends CsmsRequest

case class OCSPRequestData(
  hashAlgorithm: HashAlgorithm,
  issuerNameHash: String,
  issuerKeyHash: String,
  serialNumber: String,
  responderURL: Option[String]
)

sealed trait HashAlgorithm extends Nameable
object HashAlgorithm extends Enumerable[HashAlgorithm] {
  case object SHA256 extends HashAlgorithm
  case object SHA384 extends HashAlgorithm
  case object SHA512 extends HashAlgorithm

  val values = List(SHA256, SHA384, SHA512)
}

case class AuthorizeResponse(
  certificateStatus: Option[CertificateStatus],
  evseId: Option[List[Int]],
  idTokenInfo: IdTokenInfo
) extends CsmsResponse

sealed trait CertificateStatus extends Nameable
object CertificateStatus extends Enumerable[CertificateStatus] {
  case object Accepted               extends CertificateStatus
  case object SignatureError         extends CertificateStatus
  case object CertificateExpired     extends CertificateStatus
  case object CertificateRevoked     extends CertificateStatus
  case object NoCertificateAvailable extends CertificateStatus
  case object CertChainError         extends CertificateStatus
  case object ContractCancelled      extends CertificateStatus

  val values = List(
    Accepted,
    SignatureError,
    CertificateExpired,
    CertificateRevoked,
    NoCertificateAvailable,
    CertChainError,
    ContractCancelled
  )
}
