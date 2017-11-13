val dispatchV = "0.11.3"
val json4sV = "3.5.3"
val sprayV = "1.3.3"
val specs2V = "3.9.1"
val slf4jV = "1.7.25"

val json4sNative = "org.json4s" %% "json4s-native" % json4sV
val javaWebSocket = "org.java-websocket" % "Java-WebSocket" % "1.3.5"
val slf4jApi = "org.slf4j" % "slf4j-api" % slf4jV
val slf4jSimple = "org.slf4j" % "slf4j-simple" % slf4jV
val dispatch = "net.databinder.dispatch" %% "dispatch-core" % dispatchV
val scalax = "com.github.t3hnar" %% "scalax" % "3.0"
val sprayHttp = "io.spray" %% "spray-http" % sprayV
val sprayHttpX = "io.spray" %% "spray-httpx" % sprayV
val akka = "com.typesafe.akka" %% "akka-actor" % "2.3.11" % "provided"
val specs2 = "org.specs2" %% "specs2-core" % specs2V % "it,test"
val specs2Mock = "org.specs2" %% "specs2-mock" % specs2V % "test"
val scalaXml = "org.scala-lang.modules" %% "scala-xml" % "1.0.4"
val scalaParser = "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4"
val enumUtils = "com.thenewmotion" %% "enum-utils" % "0.2.1"
val scalaCheck = "org.scalacheck" %% "scalacheck" % "1.13.4" % "test"
val specs2ScalaCheck = "org.specs2" %% "specs2-scalacheck" % specs2V % "test"
val json4sjackson = "org.json4s" %% "json4s-jackson" % json4sV % "test"
val jsonSchemaValidator = "com.github.java-json-tools" % "json-schema-validator" % "2.2.8" % "test"
val mockServer = "org.mock-server" % "mockserver-client-java" % "3.10.4" % "test"
val mockServerNetty = "org.mock-server" % "mockserver-netty" % "3.10.4"  % "test"

def module(name: String) = Project(name, file(name))
  .enablePlugins(OssLibPlugin)
  .configs(IntegrationTest)
  .settings(
    crossScalaVersions := Seq(tnm.ScalaVersion.prev),
    scalaVersion := tnm.ScalaVersion.prev,
    organization := "com.thenewmotion.ocpp",
    libraryDependencies += specs2,
    Defaults.itSettings
  )

def scalaxbModule(name: String, packageNameForGeneratedCode: String) =
  module(name)
   .enablePlugins(ScalaxbPlugin)
   .settings(
     scalacOptions -="-Ywarn-unused-import",
     libraryDependencies ++= Seq(
       scalaXml,
       scalaParser,
       dispatch
     ),

     scalaxbDispatchVersion in (Compile, scalaxb) := dispatchV,
     scalaxbPackageName in (Compile, scalaxb)     := packageNameForGeneratedCode,
     // please give us good old synchronous HTTP clients for now
     scalaxbAsync in scalaxb in Compile := false,

     coverageExcludedPackages := ".*"
   )

val messages = module("ocpp-messages")
  .settings(libraryDependencies += enumUtils)

val json = module("ocpp-json")
  .dependsOn(messages)
  .settings(
    libraryDependencies ++= Seq(
      json4sNative, slf4jApi, scalaCheck, specs2ScalaCheck, jsonSchemaValidator, json4sjackson
    )
  )

val ocpp12Soap = scalaxbModule("ocpp-12", "com.thenewmotion.ocpp.v12")
val ocpp15Soap = scalaxbModule("ocpp-15", "com.thenewmotion.ocpp.v15")

val ocppSoap = module("ocpp-soap")
  .dependsOn(messages, ocpp12Soap, ocpp15Soap)
  .settings(
    scalacOptions -= "-Ywarn-value-discard",
    libraryDependencies ++= Seq(
      slf4jApi, scalax, specs2Mock))

val ocppSpray = module("ocpp-spray")
  .dependsOn(ocppSoap)
  .settings(
    libraryDependencies ++= Seq(
      sprayHttp, sprayHttpX, akka, specs2Mock))

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

enablePlugins(OssLibPlugin)

crossScalaVersions := Seq(tnm.ScalaVersion.prev)

scalaVersion := tnm.ScalaVersion.prev

publish := {}
