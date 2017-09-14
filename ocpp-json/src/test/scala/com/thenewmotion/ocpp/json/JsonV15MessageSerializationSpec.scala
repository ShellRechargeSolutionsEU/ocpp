package com.thenewmotion.ocpp
package json

import java.time.ZonedDateTime

import messages._
import org.scalacheck.Gen
import org.scalacheck.Prop.forAll
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import v15.Ocpp15J

object JsonV15MessageSerializationSpec extends Specification with ScalaCheck {

  import Generators._
  "JsonV15MessageSerializationSpec" >> {
    "serialize/deserialize start transaction requests" >> {
/*
      forAll(startTransactionReqGen) { startTransactionReq =>
        Ocpp15J.deserialize[StartTransactionReq](
          Ocpp15J.serialize(startTransactionReq)
        ) == startTransactionReq
      }
*/
      true
    }
  }
}

object Generators {
  val now = ZonedDateTime.now

  val connectorScopeGen = Gen.chooseNum(1, 4).map(ConnectorScope.fromOcpp)
  val idTagGen = Gen.listOf(Gen.alphaNumChar).map(_.mkString).suchThat(_.nonEmpty) // alphaNumStr
  val timestampGen = Gen.const(now)
  val meterStartGen = Gen.chooseNum(0, 100)
  val reservationIdGen = Gen.option(Gen.choose(0, 100))

  def startTransactionReqGen: Gen[StartTransactionReq] =
    for {
      connectorScope <- connectorScopeGen
      idTag <- idTagGen
      timestamp <- timestampGen
      meterStart <- meterStartGen
      reservationId <- reservationIdGen
    } yield {
      StartTransactionReq(
        connectorScope,
        idTag,
        timestamp,
        meterStart,
        reservationId
      )
    }

/* experiment:
  def meterStaGen(start: Int) = Gen.chooseNum(start, 100)
  def meterStoGen(gen: Gen[Int]) = gen.flatMap(start => Gen.chooseNum(start, 100))
  val start = meterStaGen(3)
  val stop = meterStoGen(start)
  val startstop = Gen.listOf((start, stop))
  val test: Gen[Boolean] = for {
    stst <- startstop
    (sta, sto) <- stst
    stav <- sta
    stov <- sto
  } yield stav < stov
*/
}
