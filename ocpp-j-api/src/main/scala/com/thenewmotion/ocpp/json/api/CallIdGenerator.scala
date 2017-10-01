package com.thenewmotion.ocpp.json.api

trait CallIdGenerator extends Iterator[String] {
  def hasNext = true
  def next(): String
}

object CallIdGenerator {
  def apply() = new AtomicCallIdGenerator
}

class AtomicCallIdGenerator extends CallIdGenerator {
  private val id = new java.util.concurrent.atomic.AtomicLong(-1)

  def next() = id.incrementAndGet.toHexString
}
