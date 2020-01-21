package com.thenewmotion.ocpp
package json
package api

import scala.language.higherKinds
import scala.util.control.NonFatal
import messages._
import org.slf4j.LoggerFactory

trait BaseOcppConnectionComponent[
  OUTREQBOUND <: Request,
  INRESBOUND <: Response,
  OUTREQRES[_ <: OUTREQBOUND, _ <: INRESBOUND],
  INREQBOUND <: Request,
  OUTRESBOUND <: Response,
  INREQRES[_ <: INREQBOUND, _  <: OUTRESBOUND]
] extends OcppConnectionComponent[
  OUTREQBOUND,
  INRESBOUND,
  OUTREQRES,
  INREQBOUND,
  OUTRESBOUND,
  INREQRES
] {

  self: SrpcComponent =>

  trait BaseOcppConnection extends OcppConnection {

    private val logger = LoggerFactory.getLogger(this.getClass)

    /**
      * OcppConnectionComponent implementations should use this to turn
      * exceptions thrown by the library user implemented `onRequest` methods
      * into call errors to be reported to the remote communication partner.
      *
      * @return The SrpcResponse reporting the error
      */
    protected def requestHandlerErrorToSrpcCallResult: PartialFunction[Throwable, SrpcResponse] = {
      case NonFatal(e) =>

        val ocppError = e match {
          case OcppException(err) => err
          case _ => OcppError(PayloadErrorCode.InternalError, "Unexpected error processing request")
        }

        SrpcCallError(
          ocppError.error,
          ocppError.description
        )
    }

    protected def logIncomingRequestHandlingError(call: SrpcCall): PartialFunction[Throwable, Throwable] = {
      case e: Throwable =>
        logger.warn(s"Exception processing OCPP request ${call.procedureName}:", e)
        e
    }
  }
}
