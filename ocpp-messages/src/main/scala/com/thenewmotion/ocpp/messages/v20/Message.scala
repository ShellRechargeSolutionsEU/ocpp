package com.thenewmotion.ocpp
package messages
package v20

trait Message extends messages.Message

trait Request extends Message with messages.Request
trait Response extends Message with messages.Response

trait CsRequest extends Request
trait CsResponse extends Response
trait CsmsRequest extends Request
trait CsmsResponse extends Response

