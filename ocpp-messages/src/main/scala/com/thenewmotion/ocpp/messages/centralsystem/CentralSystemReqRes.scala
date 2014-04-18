package com.thenewmotion.ocpp.messages
package centralsystem

/**
 * @author Yaroslav Klymko
 */
trait CentralSystemReqRes[REQ <: CentralSystemReq, RES <: CentralSystemRes] extends ReqRes[REQ, RES] {
  def apply(req: REQ)(implicit service: CentralSystem): RES
}

object CentralSystemReqRes {
  implicit object AuthorizeReqRes extends CentralSystemReqRes[AuthorizeReq, AuthorizeRes] {
    def apply(req: AuthorizeReq)(implicit service: CentralSystem) = service.authorize(req)
  }

  implicit object StartTransactionReqRes extends CentralSystemReqRes[StartTransactionReq, StartTransactionRes] {
    def apply(req: StartTransactionReq)(implicit service: CentralSystem) = service.startTransaction(req)
  }

  implicit object StopTransactionReqRes extends CentralSystemReqRes[StopTransactionReq, StopTransactionRes] {
    def apply(req: StopTransactionReq)(implicit service: CentralSystem) = service.stopTransaction(req)
  }

  implicit object HeartbeatReqRes extends CentralSystemReqRes[HeartbeatReq.type, HeartbeatRes] {
    def apply(req: HeartbeatReq.type)(implicit service: CentralSystem) = service.heartbeat
  }

  implicit object MeterValuesReqRes extends CentralSystemReqRes[MeterValuesReq, MeterValuesRes.type] {
    def apply(req: MeterValuesReq)(implicit service: CentralSystem) = {
      service.meterValues(req)
      MeterValuesRes
    }
  }

  implicit object BootNotificationReqRes extends CentralSystemReqRes[BootNotificationReq, BootNotificationRes] {
    def apply(req: BootNotificationReq)(implicit service: CentralSystem) = service.bootNotification(req)
  }

  implicit object StatusNotificationReqRes extends CentralSystemReqRes[StatusNotificationReq, StatusNotificationRes.type] {
    def apply(req: StatusNotificationReq)(implicit service: CentralSystem) = {
      service.statusNotification(req)
      StatusNotificationRes
    }
  }

  implicit object FirmwareStatusNotificationReqRes extends CentralSystemReqRes[FirmwareStatusNotificationReq, FirmwareStatusNotificationRes.type] {
    def apply(req: FirmwareStatusNotificationReq)(implicit service: CentralSystem) = {
      service.firmwareStatusNotification(req)
      FirmwareStatusNotificationRes
    }
  }

  implicit object DiagnosticsStatusNotificationReqRes extends CentralSystemReqRes[DiagnosticsStatusNotificationReq, DiagnosticsStatusNotificationRes.type] {
    def apply(req: DiagnosticsStatusNotificationReq)(implicit service: CentralSystem) = {
      service.diagnosticsStatusNotification(req)
      DiagnosticsStatusNotificationRes
    }
  }

  implicit object DataTransferReqRes extends CentralSystemReqRes[DataTransferReq, DataTransferRes] {
    def apply(req: DataTransferReq)(implicit service: CentralSystem) = service.dataTransfer(req)
  }

  implicit object ReqRes extends CentralSystemReqRes[CentralSystemReq, CentralSystemRes] {
    def apply(req: CentralSystemReq)(implicit service: CentralSystem) = req match {
      case x: AuthorizeReq => AuthorizeReqRes(x)
      case x: StartTransactionReq => StartTransactionReqRes(x)
      case x: StopTransactionReq => StopTransactionReqRes(x)
      case x: HeartbeatReq.type => HeartbeatReqRes(x)
      case x: MeterValuesReq => MeterValuesReqRes(x)
      case x: BootNotificationReq => BootNotificationReqRes(x)
      case x: StatusNotificationReq => StatusNotificationReqRes(x)
      case x: FirmwareStatusNotificationReq => FirmwareStatusNotificationReqRes(x)
      case x: DiagnosticsStatusNotificationReq => DiagnosticsStatusNotificationReqRes(x)
      case x: DataTransferReq => DataTransferReqRes(x)
    }
  }
}
