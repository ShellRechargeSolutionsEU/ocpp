package com.thenewmotion.ocpp.messages

// TODO rename this to Message, and all v1-specific shit should go to a v1 package
trait MessageV1orV2

trait RequestV1orV2 extends MessageV1orV2
trait ResponseV1orV2 extends MessageV1orV2
