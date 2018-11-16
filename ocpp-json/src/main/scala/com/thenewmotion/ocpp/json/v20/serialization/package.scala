package com.thenewmotion.ocpp
package json
package v20

import messages.v20._

package object serialization {

  val ocppSerializers = Seq(
    new EnumerableNameSerializer(BootReason),
    InstantSerializer,
    ChargingRateSerializer,
    new EnumerableNameSerializer(BootNotificationStatus),
    new EnumerableNameSerializer(IdTokenType),
    new EnumerableNameSerializer(ChargingProfilePurpose),
    new EnumerableNameSerializer(ChargingProfileKind),
    new EnumerableNameSerializer(RecurrencyKind),
    new EnumerableNameSerializer(ChargingRateUnit),
    new EnumerableNameSerializer(RequestStartStopStatus),
    new EnumerableNameSerializer(Reason),
    new EnumerableNameSerializer(TriggerReason),
    new EnumerableNameSerializer(TransactionEvent),
    new EnumerableNameSerializer(ReadingContext),
    new EnumerableNameSerializer(Measurand),
    new EnumerableNameSerializer(Phase),
    new EnumerableNameSerializer(Location),
    new EnumerableNameSerializer(SignatureMethod),
    new EnumerableNameSerializer(EncodingMethod),
    new EnumerableNameSerializer(ChargingState),
    new EnumerableNameSerializer(AuthorizationStatus),
    new EnumerableNameSerializer(MessageFormat),
    new EnumerableNameSerializer(ConnectorStatus),
    new EnumerableNameSerializer(HashAlgorithm),
    new EnumerableNameSerializer(CertificateStatus),
    new EnumerableNameSerializer(Attribute),
    new EnumerableNameSerializer(GetVariableStatus),
    new EnumerableNameSerializer(SetVariableStatus),
    new EnumerableNameSerializer(Update),
    new EnumerableNameSerializer(UpdateStatus),
    new EnumerableNameSerializer(ReportBase),
    new EnumerableNameSerializer(GenericDeviceModelStatus),
    new EnumerableNameSerializer(Mutability),
    new EnumerableNameSerializer(Data)
  )
}
