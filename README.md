# Open Charge Point Protocol for Scala [![Build Status](https://secure.travis-ci.org/thenewmotion/ocpp.png)](http://travis-ci.org/thenewmotion/ocpp)

The Open Charge Point Protocol (OCPP) is a network protocol for communication between electric vehicle chargers and a central backoffice system. It is developed by the Open Charge Alliance (OCA). You can find more details on the [official website of the OCA](http://openchargealliance.org/)

## Includes
* CentralSystemService.wsdl generated scala beans
* ChargePointService.wsdl generated scala beans
* CentralSystemClient for both 1.2 and 1.5 versions
* Helper methods to work with ocpp
* OCPP 1.2
* OCPP 1.5
* [Spray](http://spray.io) based service to handle OCPP requests
* Functionality to parse and create OCPP JSON messages
* A high-level interface for an OCPP 1.5-JSON client

## Setup

1. Add this repository to your pom.xml:
    ```xml
    <repository>
        <id>thenewmotion</id>
        <name>The New Motion Repository</name>
        <url>http://nexus.thenewmotion.com/content/repositories/releases-public</url>
    </repository>
    ```

2. Add dependency to your pom.xml:

    OCPP 1.2
    ```xml
    <dependency>
        <groupId>com.thenewmotion.chargenetwork</groupId>
        <artifactId>ocpp-1.2_2.10</artifactId>
        <version>4.2.3</version>
    </dependency>
    ```

    OCPP 1.5
    ```xml
    <dependency>
        <groupId>com.thenewmotion.chargenetwork</groupId>
        <artifactId>ocpp-1.5_2.10</artifactId>
        <version>4.2.3</version>
    </dependency>
    ```

    Spray OCPP
    ```xml
    <dependency>
        <groupId>com.thenewmotion.chargenetwork</groupId>
        <artifactId>ocpp-spray_2.10</artifactId>
        <version>4.2.3</version>
    </dependency>
    ```

    OCPP 1.5-JSON
    ```xml
    <dependency>
        <groupId>com.thenewmotion.chargenetwork</groupId>
        <artifactId>ocpp-json_2.10</artifactId>
        <version>4.2.3</version>
    </dependency>
    ```

## Usage

There is an example OCPP 1.5-JSON client application in ocpp-json/src/test/scala/com/thenewmotion/example, reproduced
here for your convenience:

```scala
package com.thenewmotion.example

import java.net.URI
import com.typesafe.scalalogging.slf4j.Logging
import com.thenewmotion.ocpp.messages._
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import com.thenewmotion.ocpp.json.{OcppError, PayloadErrorCode, OcppException, OcppJsonClient}

object JsonClientTestApp extends App {
  val connection = new OcppJsonClient("Test Charger", new URI("http://localhost:8080/ocppws")) with Logging {

    def onRequest(req: ChargePointReq): Future[ChargePointRes] = Future {
      req match {
        case GetLocalListVersionReq =>
          GetLocalListVersionRes(AuthListNotSupported)
        case _ =>
          throw OcppException(PayloadErrorCode.NotSupported, "Demo app doesn't support that")
      }
    }

    def onError(err: OcppError) = logger.warn(s"OCPP error: ${err.error} ${err.description}")

    def onDisconnect = logger.warn("WebSocket disconnect")
  }

  connection.send(BootNotificationReq(
    chargePointVendor = "The New Motion",
    chargePointModel = "Lolo 47.6",
    chargePointSerialNumber = Some("123456"),
    chargeBoxSerialNumber = None,
    firmwareVersion = None,
    iccid = None,
    imsi = None,
    meterType = None,
    meterSerialNumber = None))

  Thread.sleep(7000)

  connection.close()
}
```

## Acknowledgements

Most of the example messages in the ocpp-json unit tests were taken from [GIR ocppjs](http://www.gir.fr/ocppjs/)