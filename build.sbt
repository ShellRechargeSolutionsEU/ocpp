import ScalaxbKeys._

val dispatchV = "0.11.2"
val json4sV = "3.2.10"
val sprayV = "1.3.1"
val specs2V = "2.4.15"
val slf4jV = "1.7.12"

val time = "com.thenewmotion" %% "time" % "2.8"
val json4sNative = "org.json4s" %% "json4s-native" % json4sV
val json4sExt = "org.json4s" %% "json4s-ext" % json4sV
val javaWebSocket = "org.java-websocket" % "Java-WebSocket" % "1.3.0"
val slf4jApi = "org.slf4j" % "slf4j-api" % slf4jV
val slf4jSimpleLogger = "org.slf4j" % "slf4j-simple" % slf4jV
val dispatch = "net.databinder.dispatch" %% "dispatch-core" % dispatchV
val scalax = "com.github.t3hnar" %% "scalax" % "2.5"
val sprayHttp = "io.spray" %% "spray-http" % sprayV
val sprayHttpX = "io.spray" %% "spray-httpx" % sprayV
val sprayExt = "com.thenewmotion" %% "spray-ext" % "0.1.2"
val specs2 = "org.specs2" %% "specs2" % specs2V % "test"
val scalaXml = "org.scala-lang.modules" %% "scala-xml" % "1.0.2"
val scalaParser = "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.1"


def module(name: String) = Project(name, file(name))
  .enablePlugins(OssLibPlugin)
  .settings(
    // -Ywarn-unused-import is not supported in 2.10
    scalacOptions := scalacOptions.value.filterNot(_ == "-Ywarn-unused-import"),

    organization := "com.thenewmotion.ocpp",

    libraryDependencies += specs2)

def scalaxbModule(name: String, packageNameForGeneratedCode: String) =
  module(name)
   .settings(
     libraryDependencies ++= {
       if (scalaVersion.value startsWith "2.11") Seq(scalaXml, scalaParser)
       else Seq()
     },

     scalaxbSettings,

     libraryDependencies += dispatch,

     sourceGenerators in Compile += (scalaxb in Compile).taskValue,
     dispatchVersion in (Compile, scalaxb) := dispatchV,
     packageName in (Compile, scalaxb)     := packageNameForGeneratedCode)


val messages = module("ocpp-messages").settings(libraryDependencies += time)
val json = module("ocpp-json").dependsOn(messages)
             .settings(libraryDependencies ++= List(javaWebSocket, json4sNative, json4sExt, slf4jApi, time))
val ocpp12Soap = scalaxbModule("ocpp-12", "com.thenewmotion.ocpp.v12")
val ocpp15Soap = scalaxbModule("ocpp-15", "com.thenewmotion.ocpp.v15")
val ocppSoap = module("ocpp-soap")
                 .dependsOn(messages, ocpp12Soap, ocpp15Soap)
                 .settings(libraryDependencies ++= List(slf4jApi, scalax))
val ocppSpray = module("ocpp-spray")
                  .dependsOn(ocppSoap)
                  .settings(libraryDependencies ++= List(sprayHttp, sprayHttpX, sprayExt))
val exampleJsonClient = Project("example-json-client", file("example-json-client"))
  .dependsOn(json)
  .settings(
    libraryDependencies ++= List(slf4jApi, slf4jSimpleLogger),
    publish := {}
  )


enablePlugins(OssLibPlugin)

publish := {}
