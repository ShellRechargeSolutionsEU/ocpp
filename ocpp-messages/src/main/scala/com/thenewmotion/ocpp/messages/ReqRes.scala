package com.thenewmotion.ocpp.messages

/**
 * ReqRes works as a multi-parameter typeclass for associating response types to request types. That is, if an implicit
 * value with type ReqRes[REQ, RES] is in scope, it means that RES is the response type that belongs to a request of
 * type REQ.
 *
 * @tparam REQ
 * @tparam RES
 */
trait ReqRes[REQ <: Request, RES <: Response]

