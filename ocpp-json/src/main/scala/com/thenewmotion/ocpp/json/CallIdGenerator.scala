package com.thenewmotion.ocpp.json

import scala.util.Random

trait CallIdGenerator extends Iterator[String] {
  def hasNext = true
  def next(): String
}

object CallIdGenerator {
  def apply() = new DefaultCallIdGenerator
}

class DefaultCallIdGenerator extends CallIdGenerator {

  val callIdLength = 8

  val random = new Random

  private val idIterator = Stream.continually(random.nextPrintableChar()).grouped(callIdLength).map(_.mkString)


  // TODO get rid of synchronized
  def next() = synchronized { idIterator.next() }
}
