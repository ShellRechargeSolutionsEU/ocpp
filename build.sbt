val dispatchV = "0.11.3"
val json4sV = "3.2.10"
val sprayV = "1.3.3"
val specs2V = "3.9.1"
val slf4jV = "1.7.12"

val json4sNative = "org.json4s" %% "json4s-native" % json4sV
val json4sExt = "org.json4s" %% "json4s-ext" % json4sV
val javaWebSocket = "org.java-websocket" % "Java-WebSocket" % "1.3.0"
val slf4jApi = "org.slf4j" % "slf4j-api" % slf4jV
val slf4jSimpleLogger = "org.slf4j" % "slf4j-simple" % slf4jV
val dispatch = "net.databinder.dispatch" %% "dispatch-core" % dispatchV
val scalax = "com.github.t3hnar" %% "scalax" % "3.0"
val sprayHttp = "io.spray" %% "spray-http" % sprayV
val sprayHttpX = "io.spray" %% "spray-httpx" % sprayV
val akka = "com.typesafe.akka" %% "akka-actor" % "2.3.11" % "provided"
val specs2 = "org.specs2" %% "specs2-core" % specs2V % "test"
val specs2Mock = "org.specs2" %% "specs2-mock" % specs2V % "test"
val scalaXml = "org.scala-lang.modules" %% "scala-xml" % "1.0.4"
val scalaParser = "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4"
val commonsCodec = "commons-codec" % "commons-codec" % "1.10"

def module(name: String) = Project(name, file(name))
  .enablePlugins(OssLibPlugin)
  .settings(
    crossScalaVersions := Seq(tnm.ScalaVersion.prev),
    scalaVersion := tnm.ScalaVersion.prev,

    organization := "com.thenewmotion.ocpp",

    libraryDependencies += specs2)

def scalaxbModule(name: String, packageNameForGeneratedCode: String) =
  module(name)
   .enablePlugins(ScalaxbPlugin)
   .settings(
     libraryDependencies ++= Seq(
       scalaXml,
       scalaParser,
       dispatch
     ),
     scalaxbDispatchVersion in (Compile, scalaxb) := dispatchV,
     scalaxbPackageName in (Compile, scalaxb)     := packageNameForGeneratedCode,
     // please give us good old synchronous HTTP clients for now
     scalaxbAsync in scalaxb in Compile := false)


val messages = module("ocpp-messages")

val json = module("ocpp-json")
  .dependsOn(messages)
  .settings(
    libraryDependencies ++= Seq(
      json4sNative, json4sExt, slf4jApi
    )
  )

val ocpp12Soap = scalaxbModule("ocpp-12", "com.thenewmotion.ocpp.v12")
val ocpp15Soap = scalaxbModule("ocpp-15", "com.thenewmotion.ocpp.v15")

val ocppSoap = module("ocpp-soap")
  .dependsOn(messages, ocpp12Soap, ocpp15Soap)
  .settings(
    libraryDependencies ++= Seq(
      slf4jApi, scalax, specs2Mock))
val ocppSpray = module("ocpp-spray")
  .dependsOn(ocppSoap)
  .settings(
    libraryDependencies ++= Seq(
      sprayHttp, sprayHttpX, akka, specs2Mock))

val ocppJServerClientApi =
  module("ocpp-j-server-client-api")
    .dependsOn(messages, json)
    .settings(
      libraryDependencies ++= Seq(
        javaWebSocket, slf4jApi, commonsCodec, specs2Mock)
    )

val exampleJsonClient =
  module("example-json-client")
  .dependsOn(json, ocppJServerClientApi)
  .settings(
    libraryDependencies ++= Seq(slf4jApi, slf4jSimpleLogger),
    outputStrategy in run := Some(StdoutOutput),
    publish := {}
  )


enablePlugins(OssLibPlugin)

crossScalaVersions := Seq(tnm.ScalaVersion.prev)

scalaVersion := tnm.ScalaVersion.prev

publish := {}
