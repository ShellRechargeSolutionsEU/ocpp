val json4sV = "3.6.7"
val specs2V = "4.8.1"
val slf4jV = "1.7.30"

val json4sNative = "org.json4s" %% "json4s-native" % json4sV
val javaWebSocket = "org.java-websocket" % "Java-WebSocket" % "1.4.0"
val slf4jApi = "org.slf4j" % "slf4j-api" % slf4jV
val slf4jSimple = "org.slf4j" % "slf4j-simple" % slf4jV
val specs2 = "org.specs2" %% "specs2-core" % specs2V % "it,test"
val specs2Mock = "org.specs2" %% "specs2-mock" % specs2V % "test"
val scalaCheck = "org.scalacheck" %% "scalacheck" % "1.14.3" % "test"
val specs2ScalaCheck = "org.specs2" %% "specs2-scalacheck" % specs2V % "test"
val json4sjackson = "org.json4s" %% "json4s-jackson" % json4sV % "test"
val jsonSchemaValidator = "com.github.java-json-tools" % "json-schema-validator" % "2.2.11" % "test"
val mockServer = "org.mock-server" % "mockserver-client-java" % "5.8.0" % "test"
val mockServerNetty = "org.mock-server" % "mockserver-netty" % "5.8.0"  % "test"

def module(name: String) = Project(name, file(name))
  .configs(IntegrationTest)
  .settings(
    organization := "com.thenewmotion.ocpp",
    libraryDependencies += specs2,
    Defaults.itSettings
  )

val messages = module("ocpp-messages")

val json = module("ocpp-json")
  .dependsOn(messages)
  .settings(
    libraryDependencies ++= Seq(
      json4sNative, slf4jApi, scalaCheck, specs2ScalaCheck, jsonSchemaValidator, json4sjackson
    )
  )

val ocppJApi =
  module("ocpp-j-api")
    .dependsOn(messages, json)
    .settings(
      libraryDependencies ++= Seq(
        javaWebSocket, slf4jApi, specs2Mock, mockServer, mockServerNetty),
      (fork in IntegrationTest) := true
    )

val exampleJsonClient =
  module("example-json-client")
  .dependsOn(json, ocppJApi)
  .settings(
    libraryDependencies += slf4jSimple,
    outputStrategy in run := Some(StdoutOutput),
    coverageExcludedPackages := ".*",
    publish := {}
  )

val exampleJsonServer =
  module("example-json-server")
    .dependsOn(json, ocppJApi)
    .settings(
      libraryDependencies += slf4jSimple,
      outputStrategy in run := Some(StdoutOutput),
      connectInput in run := true,
      coverageExcludedPackages := ".*",
      publish := {}
    )

val exampleJsonServer20 =
  module("example-json-server-20")
    .dependsOn(json, ocppJApi)
    .settings(
      libraryDependencies += slf4jSimple,
      outputStrategy in run := Some(StdoutOutput),
      connectInput in run := true,
      coverageExcludedPackages := ".*",
      publish := {}
    )

crossScalaVersions := Seq("2.13.1", "2.12.10", "2.11.12")

publish := {}
