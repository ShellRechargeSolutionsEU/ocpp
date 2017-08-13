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
        <groupId>com.thenewmotion.ocpp</groupId>
        <artifactId>ocpp-1.2_2.10</artifactId>
        <version>4.3.0</version>
    </dependency>
    ```

    OCPP 1.5
    ```xml
    <dependency>
        <groupId>com.thenewmotion.ocpp</groupId>
        <artifactId>ocpp-1.5_2.10</artifactId>
        <version>4.3.0</version>
    </dependency>
    ```

    Spray OCPP
    ```xml
    <dependency>
        <groupId>com.thenewmotion.ocpp</groupId>
        <artifactId>ocpp-spray_2.10</artifactId>
        <version>4.3.0</version>
    </dependency>
    ```

    OCPP 1.5-JSON
    ```xml
    <dependency>
        <groupId>com.thenewmotion.ocpp</groupId>
        <artifactId>ocpp-json_2.10</artifactId>
        <version>4.3.0</version>
    </dependency>
    ```

## Usage

There is an example OCPP 1.5-JSON client application in example-json-client/src/main/scala/com/thenewmotion/ocpp/json. You can run it like this:

    sbt "project example-json-client" "run 01234567 ws://localhost:8017/ocppws"

or with TLS encryption and Basic Auth according to the OCPP 1.6 JSON Implementation Specification:

    sbt "project example-json-client" "run 01234567 wss://test-cn-node-internet.thenewmotion.com/ocppws abcdef1234abcdef1234abcdef1234abcdef1234"

## Acknowledgements

Most of the example messages in the ocpp-json unit tests were taken from [GIR ocppjs](http://www.gir.fr/ocppjs/)
