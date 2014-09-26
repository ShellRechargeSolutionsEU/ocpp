package com.thenewmotion.ocpp.json

trait CallIdGenerator extends Iterator[String] {
  def hasNext = true
  def next(): String
}

object CallIdGenerator {
  def apply() = new DefaultCallIdGenerator
}

class DefaultCallIdGenerator extends CallIdGenerator {

  private val idIterator: Iterator[String] = Stream.iterate(0)(_+1).map(_.toHexString).toIterator

  def next() = synchronized { idIterator.next() }
}
