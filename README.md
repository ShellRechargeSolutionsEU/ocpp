# Open Charge Point Protocol for Scala [![Build Status](https://secure.travis-ci.org/thenewmotion/ocpp.png)](http://travis-ci.org/thenewmotion/ocpp)

You can find more details on the [official website](http://www.ocpp.nl/)

## Includes
* CentralSystemService.wsdl generated scala beans
* ChargePointService.wsdl generated scala beans
* CentralSystemClient for both 1.2 and 1.5 versions
* Helper methods to work with ocpp
* OCPP 1.2
* OCPP 1.5
* [Spray](http://spray.io) based service to handle OCPP requests
* Functionality to parse and create OCPP JSON messages

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
        <artifactId>ocpp_1.2_2.10</artifactId>
        <version>3.1-SNAPSHOT</version>
    </dependency>
```

OCPP 1.5
```xml
    <dependency>
        <groupId>com.thenewmotion.chargenetwork</groupId>
        <artifactId>ocpp_1.5_2.10</artifactId>
        <version>3.1-SNAPSHOT</version>
    </dependency>
```

Common
```xml
    <dependency>
        <groupId>com.thenewmotion.chargenetwork</groupId>
        <artifactId>ocpp_common_2.10</artifactId>
        <version>3.1-SNAPSHOT</version>
    </dependency>
```

Spray OCPP
```xml
    <dependency>
        <groupId>com.thenewmotion.chargenetwork</groupId>
        <artifactId>ocpp_spray_2.10</artifactId>
        <version>3.1-SNAPSHOT</version>
    </dependency>
```

## Acknowledgements

Most of the example messages in the ocpp-json unit tests were taken from [GIR ocppjs](http://www.gir.fr/ocppjs/)