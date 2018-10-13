package com.thenewmotion.ocpp
package json.v20.scalacheck

import org.scalacheck.Gen
import Gen._
import messages.v20._

object Helpers {

  /**
    * According to the OCPP 2.0 spec, the only restriction on "string" is that
    * it's Unicode. Be careful what you wish for people, but here you go.
    *
    * @param maxSize
    * @return
    */
  def ocppString(maxSize: Int): Gen[String] =
    resize(maxSize, listOf(chooseNum(0, 0x10FFFF).map(Character.toChars)))
      .map(_.flatten.mkString)

  def ocpp2IdentifierString(maxSize: Int): Gen[String] = resize(
    maxSize,
    listOf(oneOf(
      alphaNumChar,
      oneOf('*', '-', '_', '=', ':', '+', '|', '@', '.')
    )).map(_.mkString)
  )

  def modem: Gen[Modem] =
    for {
      iccid <- option(ocpp2IdentifierString(20))
      imsi <- option(ocpp2IdentifierString(20))
    } yield Modem(iccid, imsi)
}
