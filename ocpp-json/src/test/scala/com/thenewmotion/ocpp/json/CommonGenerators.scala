package com.thenewmotion.ocpp.json

import org.scalacheck.Gen
import Gen._
import enums.reflection.EnumUtils.{Enumerable, Nameable}

object CommonGenerators {

  def enumerableGen[T <: Nameable](e: Enumerable[T]): Gen[T]  =
    oneOf(e.values.toList)

  def enumerableNameGen[T <: Nameable](e: Enumerable[T]): Gen[String] = enumerableGen(e).map(_.name)
}
