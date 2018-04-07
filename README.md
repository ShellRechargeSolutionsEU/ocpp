# Open Charge Point Protocol for Scala [![Build Status](https://secure.travis-ci.org/NewMotion/ocpp.png)](http://travis-ci.org/NewMotion/ocpp) [![Coverage Status](https://coveralls.io/repos/github/NewMotion/ocpp/badge.svg?branch=master)](https://coveralls.io/github/NewMotion/ocpp?branch=master)

The Open Charge Point Protocol (OCPP) is a network protocol for communication
between electric vehicle chargers and a central backoffice system. It is
developed by the Open Charge Alliance (OCA). You can find more details on the
[official website of the OCA](http://openchargealliance.org/).

This library is the implementation of OCPP developed and used by NewMotion, one
of Europe's largest Electric Vehicle Charge Point Operators.

The library is designed with versatility in mind. OCPP comes in 3 versions (1.2,
1.5 and 1.6), two transport variants (SOAP/XML aka OCPP-S and WebSocket/JSON aka
OCPP-J), and two roles ("Charge Point" and "Central System"). This library will
help you with almost any combination of those. Only version 1.2 with
WebSocket/JSON and version 1.6 with SOAP/XML are not possible.

Also, you will probably want to use different WebSocket libraries for
different scenarios: a production back-office server with tens of thousands of
concurrent connections, a client in a load testing tool, or a simple one-off
script to test a certain behavior. This library uses the
[cake pattern](http://www.cakesolutions.net/teamblogs/2011/12/19/cake-pattern-in-depth)
to make it easy to swap out the underlying WebSocket implementation while still
using the same concise high-level API.

## How to use

### Setup

The library is divided into a bunch of separate modules so applications using it
won't get too many dependencies dragged in. Those are:

  * `ocpp-j-api`: high-level interface to OCPP-J connections
  * `ocpp-json`: serialization of OCPP messages to/from JSON
  * `ocpp-messages`: A model of OCPP messaging that is independent of protocol
                     version and transport variant

  * `ocpp-soap`: A version-independent interface to OCPP-SOAP
  * `ocpp-spray`: A library to help handling OCPP SOAP messages with Akka and
                  the Spray HTTP library
  * `ocpp-12` and `ocpp-15`: WSDL files and generated code for the OCPP 1.2 and
                             1.5 SOAP services

So if you want to use the high-level OCPP-J connection interface, and you're
using SBT, you can declare the dependency by adding this to your `plugins.sbt`:

```
resolvers += "TNM" at "http://nexus.thenewmotion.com/content/groups/public"

```

and this to your `build.sbt`:

```
libraryDependencies += "com.thenewmotion" %% "ocpp-j-api" % "6.0.0"
```

With Maven, you'd set up the repository in your pom.xml:
```xml
    <repository>
        <id>thenewmotion</id>
        <name>The New Motion Repository</name>
        <url>http://nexus.thenewmotion.com/content/repositories/releases-public</url>
    </repository>
```

and add this to your dependencies:

```xml
    <dependency>
        <groupId>com.thenewmotion.ocpp</groupId>
        <artifactId>ocpp-j-api_2.11</artifactId>
        <version>6.0.0</version>
    </dependency>
```

### Using the simple client API

An example OCPP-J client application included. You can run it like this:

    sbt "project example-json-client" "run 01234567 ws://localhost:8017/ocppws 1.5,1.6"

This means: connect to the Central System running at
`ws://localhost:8017/ocppws`, as a charge point with ID 01234567, using OCPP
version 1.5 and if that is not supported try 1.6 instead. If you don't specify
a version, 1.6 is used by default.

If you look at the code of the example by clicking [here](example-json-client/src/main/scala/com/thenewmotion/ocpp/json/example/JsonClientTestApp.scala),
you can see how the client API is used:

 * A connection is established by creating an instance of `OcppJsonClient`. The
   server endpoint URI, charge point ID and OCPP version to use are passed
   to the constructor.

 * To specify how your application handles incoming messages, you override the
   `requestHandler` and `onError` members.

 * To send OCPP messages to the Central System, you call the `send` method on
   the `OcppJsonClient` instance. You will get a `Future` back that will be
   completed with the Central System's response. If the Central System fails
   to respond to your request, the `Future` will fail.

 * `OcppJsonClient` is an instance of the [`OcppEndpoint`](ocpp-j-api/src/main/scala/com/thenewmotion/ocpp/json/api/OcppEndpoint.scala)
   trait. This trait defines this interface.

#### Handling requests

To specify the request handler, we use a [_magnet pattern_](http://spray.io/blog/2012-12-13-the-magnet-pattern/).
You can specify the request handler in different ways. After the
`val requestHandler: ChargePointRequestHandler =`, you see a
[`ChargePoint`](ocpp-messages/src/main/scala/com/thenewmotion/ocpp/messages/ChargePoint.scala)
instance in the example program. But you can also specify the request handler
as a function from `ChargePointReq` to `Future[ChargePointRes]`:

```scala
    val requestHandler: ChargePointRequestHandler = { (req: ChargePointReq) =>
      req match {
        case GetConfigurationReq(keys) =>
          System.out.println(s"Received GetConfiguration for $keys")
          Future.successful(GetConfigurationRes(
            values = List(),
            unknownKeys = keys
          ))
        case x =>
          val opName = x.getClass.getSimpleName
          Future.failed(OcppException(
            PayloadErrorCode.NotSupported,
            s"Demo app doesn't support $opName"
          ))
      }
    }
```

This behavior of this request handler is more or less equivalent to that of the
one in the example app. It is shorter at the price of being less type-safe:
this code does not check if you generate the right response type for the
request, so if you generate a GetConfigurationRes in response to a
GetConfigurationReq for instance.

#### Sending requests

Sending requests is simple, as explained. You call the `send` method of your
endpoint and off you go, like this:

```scala
    connection.send(HeartbeatReq)
```

The set of messages you can send is defined in [ocpp-messages](ocpp-messages/src/main/scala/com/thenewmotion/ocpp/messages/Message.scala).
For every request type, you represent requests as instances of a case class
named `<Operation Name>Req`, e.g. `StatusNotificationReq`, `HeartbeatReq`.

These case classes in `ocpp-messages` are designed according to two principles:
 * They are independent of OCPP version, so you have one interface to charging
   stations that use different versions
 * They sometimes group and rearrange fields to make it impossible to specify
   nonsense messages (e.g., no `vendorErrorCode` in status notifications that
   are not about errors). This makes it easier to write the code dealing with
   those requests, which does not have to validate things first.

This does mean that sometimes the way these case classes are defined may be a
bit surprising to people familiar with the OCPP specification. It be so. Use
the link to the file above, or use ⌘P in IntelliJ IDEA, to see how to give these
case classes the right parameters to formulate the request you want to send.

This also means that it is possible to send requests that cannot be represented
in the OCPP version that is used for the connection you send them over. In that
case `send` will return a failed future with an `OcppError` with error code
`NotSupported`.

The result of the `send` method is a `Future[RES]`, where `RES` is the type
of the response that belongs to the request you sent. So the type of this
expression:

```scala
     connection.send(AuthorizeReq(idTag = "12345678"))
```

is `Future[AuthorizeRes]`.

And if you want to do something with the result, the code could look like this:

```scala
     connection.send(AuthorizeReq(idTag = "12345678")).map { res =>
       if (res.idTag.status == AuthorizationStatus.Accepted)
         System.out.println("12345678 is authorized.")
       else
         System.out.println("12345678 has been rejected. No power to you!")
     }
```

Note that the library does not by itself
enforce the OCPP requirement that you wait for the response before sending the
next request. A simple way to obey it is chaining the send operations in a `for`
comprehension, as shown in the example app.

### Using the cake pattern directly

If you want to build an OCPP-J client using a different WebSocket
implementation, or an OCPP-J server, you'll have to use the
[cake](http://www.cakesolutions.net/teamblogs/2011/12/19/cake-pattern-in-depth)
layers directly.

The OCPP cake has three layers:

 * [`OcppConnectionComponent`](ocpp-j-api/src/main/scala/com/thenewmotion/ocpp/json/api/OcppConnection.scala): matches requests to responses, and handles serialization and deserialization between OCPP request/response objects and SRPC messages
 * [`SrpcComponent`](ocpp-j-api/src/main/scala/com/thenewmotion/ocpp/json/api/SrpcConnection.scala): serializes and deserializes SRPC messages to JSON
 * [`WebSocketComponent`](ocpp-j-api/src/main/scala/com/thenewmotion/ocpp/json/api/WebSocketConnection.scala): reads and writes JSON messages from and to a WebSocket connection

There are default implementations for the `OcppConnectionComponent` and
`SrpcComponent`. The `WebSocketComponent` you will have to create yourself to
integrate with your WebSocket implementation of choice.

To put it in a diagram:

```
    +--------------V--------------------^--------------+
    |     com.thenewmotion.ocpp.messages.{Req, Res}    |
    |                                                  |
    |         OcppConnectionComponent layer            |
    |                                                  |
    +--------------V--------------------^--------------+
    |    com.thenewmotion.ocpp.json.TransportMessage   |
    |                                                  |
    |              SrpcComponent layer                 |
    |                                                  |
    +--------------V--------------------^--------------+
    |                org.json4s.JValue                 |
    |                                                  |
    |           WebSocketComponent layer               |
    |                                                  |
    +--------------V--------------------^--------------+
               (WebSocket lib specific types)
```

So the `OcppConnectionComponent` layer exchanges OCPP requests and responses
with your app. It exchanges SRPC messages, represented as
[TransportMessage](ocpp-json/src/main/scala/com/thenewmotion/ocpp/json/TransportMessageProtocol.scala)
objects, with the `SrpcComponent` layer. The `SrpcComponent` layer exchanges
JSON messages, represented as `org.json4s.JValue` objects, with the WebSocket
layer. The WebSocket layer then speaks to the WebSocket library, using whatever
types that library uses, usually just `String`s.

Now this is not the whole picture yet: besides just OCPP messages, the layers
also exchange connection commands and events, like "close this connection!" or
"an error occurred sending that request". The traits also define methods to
exchange those, and so the total amount of methods in your
`OcppConnectionComponent with SrpcComponent with WebSocketComponent` instance
may be intimidating at first. Let's make a version of the above diagram that
shows the methods defined by each layer:

```
                                                                     OcppConnectionComponent.onRequest (override)
       OcppConnectionComponent.ocppConnection.sendRequest (call)     OcppConnectionComponent.onOcppError (override)
    +------------------V-----------------------------------------------^--------------------------------------------------+
    |                                                                                                                     |
    |                                    OcppConnectionComponent layer                                                    |
    |                                                                                                                     |
    |                                                                                                                     |
    | SrpcConnectionComponent.srpcConnection.sendRequest (call)      SrpcConnectionComponent.onSrpcRequest (override)     |
    +------------------V-----------------------------------------------^--------------------------------------------------+
    |                                                                                                                     |
    |                                          SrpcComponent layer                                                        |
    |                                                                                                                     |
    |                                                                WebSocketConnectionComponent.onMessage (override)    |
    | WebSocketConnectionComponent.webSocketConnection.send (call)   WebSocketConnectionComponent.onDisconnect (override) |
    | WebSocketConnectionComponent.webSocketConnection.close (call)  WebSocketConnectionComponent.onError (override)      |
    +------------------V-----------------------------------------------^--------------------------------------------------+
    |                                                                                                                     |
    |                                       WebSocketComponent layer                                                      |
    |                                                                                                                     |
    | (WebSocket library dependent)                                  (WebSocket library dependent)                        |
    +------------------V-----------------------------------------------^--------------------------------------------------+
```

So each layer defines the interface that the higher layers can use to
communicate with it. For every layer, you see on the left how the higher layer
can give it information, and on the right how the higher layer gets information
from it.

For instance, on top you see that the user of the whole cake can give
information to the OCPP layer by calling the `ocppConnection.sendRequest`
method. And on the lower middle right you see that the SRPC cake layer
can get information from the WebSocket layer by overriding the
`onMessage`, `onDisconnect` and `onError` methods of the
`WebSocketComponent`.

There has to be one instance of the whole cake for every open WebSocket
connection in the system. A server would typically maintain a mapping of
WebSocket connection IDs from the underlying library to
`OcppConnectionComponent with SrpcComponent with WebSocketComponent` instances
for them. When it receives an incoming WebSocket message, it will look up the
cake for that connection, and pass the message to the WebSocket layer of that
cake.

So now with this background information, the steps to constructing your cake would be:

 * Determine the kind of interface you want to the logic in the rest of your app

 * Create a trait extending `WebSocketComponent` that uses your WebSocket
   implementation of choice

 * Create the cake:

      * Do either `new CentralSystemOcppConnectionComponent with DefaultSrpcComponent with MyWebSocketComponent { ... }`
        or `new ChargePointCentralSystemOcppConnectionComponent with DefaultSrpcComponent with MyWebSocketComponent { ... }`

      * Define in it a `val webSocketConnection`, `val srpcConnection` and
        `val ocppConnection`. For `ocppConnection`, use one of the
        `defaultChargePointOcppConnection` and
        `defaultCentralSystemOcppConnection` methods defined by the
        DefaultOcppConnectionComponent traits. For `val srpcConnection`, use
        `new DefaultSrpcConnection`.

 * Define all the "(override)" methods shown at the top of the cake to connect
   your app's request and response processing to the OCPP cake

 * Make the WebSocket layer call your WebSocket library to send messages over
   the socket, and make your WebSocket library call the cake for it to receive messages

#### Putting this together for a server

Because that bit about the cake pattern is still quite abstract, let's
look at how we can implement an OCPP server using the cake pattern.

According to the list of steps above, we first have to determine the
interface that we want or OCPP server component to have.

The interface I am thinking of here is something like this:

```
abstract class OcppJsonServer(listenPort: Int, ocppVersion: Version) {

  type OutgoingEndpoint = OutgoingOcppEndpoint[ChargePointReq, ChargePointRes, ChargePointReqRes]

  type IncomingEndpoint = IncomingOcppEndpoint[CentralSystemReq, CentralSystemRes, CentralSystemReqRes]

  def handleConnection: OutgoingEndpoint => IncomingEndpoint
}
```

So if someone writes a back-office system using this server component,
she has to provide three things:

 * the TCP port to listen on, as a constructor argument

 * The OCPP version to use, as a constructor argument (I'm too lazy to worry
   about version negotiation right now)

 * Her own logic for how to handle and send OCPP messages over the
   connections to this server. She should specify this as a method
   `handleConnection` that gets an endpoint for sending outgoing
   requests as an argument, and returns to the server component an
   endpoint for handling incoming requests and close or error events.

So now we have decided on an interface and we move to step two: create
a trait extending `WebSocketComponent` that uses our WebSocket
implementation of choice. Here we are using java-websocket, so we'll
end up using WebSocketServer.

If we look at the WebSocketServer API, we see that it is based on
instantiating a `WebSocketServer` object with overridden methods to
handle incoming messages. The interface looks like this:

```java
public abstract class WebSocketServer extends AbstractWebSocket implements Runnable {

  // These methods are provided by the implementation...

  public WebSocketServer(InetSocketAddress address) { ... }

  public void start() { ... }

  public void stop() { ... }

  // ...and these are to be overridden by the user
  public abstract void onStart();

  public abstract void onOpen( WebSocket conn, ClientHandshake handshake );

  public abstract void onClose( WebSocket conn, int code, String reason, boolean remote );

  public abstract void onMessage( WebSocket conn, String message );

  public abstract void onError( WebSocket conn, Exception ex );
}
```

So for each connection, `WebSocketServer` creates a `WebSocket` object,
and then passes that into the callback. We will create our
`WebSocketComponent` instance so that it calls the `send` method on such
a `WebSocket` to send outgoing messages. That means a first stab at our
`WebSocketComponent` looks like this:

```scala
import org.java_websocket.WebSocket
import org.json4s.JValue
import org.json4s.native.JsonMethods.{compact, render}

trait SimpleServerWebSocketComponent extends WebSocketComponent {

  trait SimpleServerWebSocketConnection extends WebSocketConnection {

    def webSocket: WebSocket

    def send(msg: JValue): Unit = webSocket.send(compact(render(msg)))

    def close(): Unit = webSocket.close()
  }
}
```

That brings us to step 3: creating the cake. Now we want to create a
cake for every incoming WebSocket connection to the server, so that
means we also have to create a WebSocket server that will create a cake
in its `onOpen` method:

```scala
import java.net.InetSocketAddress
import scala.concurrent.ExecutionContext
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import messages._

abstract class OcppJsonServer(listenPort: Int, ocppVersion: Version)
  extends WebSocketServer(new InetSocketAddress(listenPort)) {

  type OutgoingEndpoint = OutgoingOcppEndpoint[ChargePointReq, ChargePointRes, ChargePointReqRes]

  type IncomingEndpoint = IncomingOcppEndpoint[CentralSystemReq, CentralSystemRes, CentralSystemReqRes]

  def handleConnection: OutgoingEndpoint => IncomingEndpoint

  override def onStart(): Unit = {}

  override def onOpen(conn: WebSocket, hndshk: ClientHandshake): Unit = {

    val ocppConnection = new CentralSystemOcppConnectionComponent  with DefaultSrpcComponent with SimpleServerWebSocketComponent {
      override val ocppConnection: DefaultOcppConnection = defaultCentralSystemOcppConnection

      override val srpcConnection: DefaultSrpcConnection = new DefaultSrpcConnection()

      override val webSocketConnection: SimpleServerWebSocketConnection = new SimpleServerWebSocketConnection {
        val webSocket: WebSocket = conn
      }

      def onRequest[REQ <: CentralSystemReq, RES <: CentralSystemRes](req: REQ)(implicit reqRes: CentralSystemReqRes[REQ, RES]) = ???

      def onOcppError(error: OcppError): Unit = ???

      def onDisconnect() = ???

      implicit val executionContext: ExecutionContext = ???

      def ocppVersion: Version = ???
    }
  }

  override def onClose(
                        conn: WebSocket,
                        code: Int,
                        reason: IdTag,
                        remote: Boolean
                      ): Unit = ???

  override def onMessage(conn: WebSocket, message: IdTag): Unit = ???

  override def onError(conn: WebSocket, ex: Exception): Unit = ???
}
```

Ouch, that's a big load of code there. Still, it came about after a few
simple steps:

  1. Take the interface template for OcppJsonServer that we started this
     example with
  2. Make it extend WebSocketServer, and add `???` implementations of
     its abstract methods
  3. In the `onOpen` method from the `WebSocketServer` abstract class,
     create a cake with the three layers: `CentralSystemOcppConnectionComponent with DefaultSrpcComponent with SimpleServerWebSOcketComponent`
  4. Add `???` implementations of all the abstract methods in the cake
  5. Add actual definitions of the `ocppConnection`, `srpcConnection`
     and `webSocketConnection` members of the three cake layers.

Note that at step 5, we have passed the `WebSocket` argument to the
`onOpen` method on into the `SimpleServerWebSocketConnection` so that
the cake can later use this `WebSocket` to send OCPP messages over.

So by now we're at the fourth point of the five-step cake plan: connect our
app's logic to the OCPP cake. The app's logic, in our case, is the
`handleConnection` that the library user specifies. And then, _inside_
the OCPP cake definition in `onOpen`, we create the incoming endpoint,
and call the user-defined `handleConnection` on it so an endpoint for
the incoming messages is created:

```scala
private val outgoingEndpoint = new OutgoingEndpoint {
  def send[REQ <: ChargePointReq, RES <: ChargePointRes](req: REQ)(implicit reqRes: ChargePointReqRes[REQ, RES]): Future[RES] =
    ocppConnection.sendRequest(req)

  def close(): Unit = webSocketConnection.close()
}

private val incomingEndpoint = handleConnection(outgoingEndpoint)
```

Now that we have the incoming endpoint, we can also fill in definitions for the
`onRequest`, `onError` and `onDisconnect` handlers in the OCPP cake:

```scala
def onRequest[REQ <: CentralSystemReq, RES <: CentralSystemRes](req: REQ)(implicit reqRes: CentralSystemReqRes[REQ, RES]) =
  incomingEndpoint.requestHandler(req)

def onOcppError(error: OcppError): Unit =
  incomingEndpoint.onError(error)

def onDisconnect() =
  incomingEndpoint.onDisconnect()
```

And then, there is the `ocppVersion` method in `OcppConnectionComponent`
that will tell it which OCPP version to use to serialize and deserialize
OCPP messages to JSON. We have to fill in the OCPP version that was
passed in to the `OcppJsonServer` constructor. To avoid name clashes,
we rename the argument to `OcppJsonServer`:

```scala
abstract class OcppJsonServer(listenPort: Int, requestedOcppVersion: Version)
```

and then fill in this `requestedOcppVersion` argument in the cake's
`ocppVersion` method:

```scala
def ocppVersion: Version = requestedOcppVersion
```

By now, the OCPP cake definition body is free of any reference to "???". The
cake is fully linked to the app's business logic.

That means we're at the fifth and last step of the five-step plan: We have to
make sure that when a new connection is opened, we will call this function to
create an `IncomingEndpoint`, and that we later call this `IncomingEndpoint`
whenever a message comes in on the connection. To make this happen, we will need
a map from `WebSocket` instances that the server gets in its `onMessage` method,
to the OCPP cake instance that will process messages for that connection.

So let's add this to the `OcppJsonServer` class: link the WebSocket library
we're using to our OCPP cake.

```scala
  private type OcppCake = CentralSystemOcppConnectionComponent with DefaultSrpcComponent with SimpleServerWebSocketComponent

  private val ocppConnections: mutable.Map[WebSocket, OcppCake] = mutable.HashMap()
```

and to fill the map, we add this line at the bottom of `onOpen` in our
`OcppJsonServer`:

```scala
ocppConnections.put(conn, ocppConnection)
```

and as responsible professionals, let's also remove the entry from the map again
when a connection is closed, by changing the definition of `onClose` to this:

```scala
override def onClose(
  conn: WebSocket,
  code: Int,
  reason: IdTag,
  remote: Boolean
): Unit = ocppConnections.remove(conn)
```

Now the last thing to be done on the way to a working server, is making the
`onMessage`, `onDisconnect` and `onError` callbacks of `WebSocketServer`
actually call into the connection's OCPP cake to let it process the message. It
turns out that to do this, we have to add two methods to the
`SimpleWebSocketServerComponent`:

```scala
def feedIncomingMessage(msg: String) = self.onMessage(org.json4s.native.JsonMethods.parse(msg))

def feedIncomingDisconnect(): Unit = self.onDisconnect()

def feedIncomingError(err: Exception) = self.onError(err)
```

to make that work, we also have to make SimpleServerWebSocketComponent aware
that it is to be mixed into something that is also an SrpcComponent, by adding
a self-type:

```scala
trait SimpleServerWebSocketComponent extends WebSocketComponent {

  self: SrpcComponent =>

  ...
```

and back in `OcppJsonServer`, we change the implementation of `onMessage`,
`onClose` and `onError` to feed those events into the cake for the right
connection:

```scala
override def onClose(
  conn: WebSocket,
  code: Int,
  reason: IdTag,
  remote: Boolean
): Unit = {
  ocppConnections.remove(conn) foreach { c =>
    c.feedIncomingDisconnect()
  }
}

override def onMessage(conn: WebSocket, message: String): Unit =
  ocppConnections.get(conn) foreach { c =>
    c.feedIncomingMessage(message)
  }

override def onError(conn: WebSocket, ex: Exception): Unit =
  ocppConnections.get(conn) foreach { c =>
    c.feedIncomingError(ex)
  }
```

That's it, it should work now!

In fact, upon testing it, I realize that with this interface, the server-side
code doesn't know the ChargePointIdentity of the client. That's not very
helpful; an OCPP back-office system will probably want to know which charge
points are connected to it. So let's change the definition of `handleConnection`
in `OcppJsonServer` to this:

```scala
def handleConnection(clientChargePointIdentity: String, remote: OutgoingEndpoint): IncomingEndpoint
```

and let's pass the ChargePointIdentity into it by changing `onOpen` to this:

```scala
  override def onOpen(conn: WebSocket, hndshk: ClientHandshake): Unit = {

    val uri = hndshk.getResourceDescriptor
    uri.split("/").lastOption match {

      case None =>
        conn.close(1003, "No ChargePointIdentity in path")

      case Some(chargePointIdentity) =>
        onOpenWithCPIdentity(conn, chargePointIdentity)
    }
  }

  private def onOpenWithCPIdentity(conn : WebSocket, chargePointIdentity: String): Unit = {
    val ocppConnection = new CentralSystemOcppConnectionComponent  with DefaultSrpcComponent with SimpleServerWebSocketComponent {
    ... // continues as in earlier definition of onOpen
 ```

So there we check if the URL includes the charge point identity. If it doesn't,
we immediately close the WebSocket connection with an error message. If we do
have a ChargePointIdentity, we proceed to handle the open as we did before. And
now it should really be done.

In order to save you the typing and bugfixing, an actual tested version
of the server developed while writing this [is included](ocpp-j-api/src/main/scala/com/thenewmotion/ocpp/json/api/server/OcppJsonServer.scala).

And there is also a small
[example app](example-json-server/src/main/scala/com/thenewmotion/ocpp/json/example/ExampleServerTestApp.scala)
that shows how to use the `OcppJsonServer` interface. It will listen on port
2345, and return `NotImplemented` to any request except BootNotification. In
response to a BootNotification, it will send a response and also send a
GetConfiguration request back to the client. To run it, you do:

```
$ sbt "project example-json-server" run
```

### Just serializing

If you do not need the connection management provided by the high-level API,
you can still use the `ocpp-json` module for serializing and deserializing OCPP
messages that you will send or receive using other libraries.

To do so, call the methods in the [OcppJ](ocpp-json/src/main/scala/com/thenewmotion/ocpp/json/OcppJ.scala)
object after importing either
`com.thenewmotion.ocpp.json.v15.SerializationV15._`
or `com.thenewmotion.ocpp.json.v16.SerializationV16._` to select which OCPP
version to use:

```scala

    import com.thenewmotion.ocpp.json.OcppJ
    import com.thenewmotion.ocpp.messages
    import com.thenewmotion.ocpp.Version
    import com.thenewmotion.ocpp.json.v16.SerializationV16._

    OcppJ.write(AuthorizeReq(idTag = "ABCDEF012"))
    // this results in:
    // res6: String = {"idTag":"ABCDEF012"}

    OcppJ.read[AuthorizeReq, Version.V16.type]("""{"idTag":"ABCDEF012"}""")
    // this results in:
    // res10: com.thenewmotion.ocpp.messages.AuthorizeReq = AuthorizeReq(ABCDEF012)
```

There are also `serialize` and `deserialize` methods on the `OcppJ` object that
use json4s `JValue`s as the representation of JSON instead of raw `String`s.
You can use those to build the [SRPC](http://www.gir.fr/ocppjs/ocpp_srpc_spec.shtml)
messages that are sent over the WebSocket. See [TransportMessage](ocpp-json/src/main/scala/com/thenewmotion/ocpp/json/TransportMessageProtocol.scala)
and [TransportMessageJsonSerializers](ocpp-json/src/main/scala/com/thenewmotion/ocpp/json/TransportMessageJsonSerializers.scala)
for how to work with those.

### SOAP

OCPP-S is what this library started with, but by now it is undocumented legacy.
It may be split off to enjoy retirement in its own little project on the next
major version.

## Changelog

### Changes in 6.0.3

 - Support DataTransfer messages from Charge Point to Central System also over SOAP

### Changes in 6.0.2

 - Support DataTransfer messages from Charge Point to Central System

### Changes in 6.0.1

 - Throw more meaningful exceptions instead of always `VersionMismatch` when an `OcppJsonClient` fails to connect

### Changes in 6.0.0 compared to version 4.x

This library had been stable for a few years between 2014 and 2017, with 4.x.x
version numbers, supporting OCPP-S 1.2 and 1.5, and OCPP-J 1.5, but not 1.6. Now
that 1.6 support has been added with version 6.0.0, many wildly incompatible
changes to the library interface were made while we were at it. The most
important ones to be aware of when porting older code:

 - The `CentralSystem` and `ChargePoint` traits were renamed to
   `SyncCentralSystem` and `SyncChargePoint`. The names `CentralSystem` and
   `ChargePoint` are now used for asynchronous versions of these traits that
   return `Future`s.
 - In the high-level JSON API, request-response-handling has become more
   type-safe. Your request handler is no longer just a function from requests
   to responses, but now a `RequestHandler` which will also verify that you
   produce the right response type for the given request.
 - The library now uses [enum-utils](https://github.com/NewMotion/enum-utils)
   instead of Scala's `Enumeration`s
 - The library now uses Java 8's `java.time` for date and time handling instead
   of `com.thenewmotion.time`.
 - `JsonDeserializable` was renamed to `JsonOperation` and now handles not only
   deserialization but also serialization of OCPP messages for OCPP-J.
 - `OcppJsonClient` now takes a version parameter

## TODO

 - Building for Scala 2.12. May require dropping the SOAP and/or switching JSON
   libraries.

## Licensing and acknowledgements

The contents of this repository are © 2012 - 2017 The New Motion B.V., licensed under the [GPL version 3](LICENSE), except:

 * [The example messages for OCPP 1.5](ocpp-json/src/test/resources/com/thenewmotion/ocpp/json/ocpp15/without_srpc) in the ocpp-json unit tests, which were taken from [GIR ocppjs](http://www.gir.fr/ocppjs/).

 * The [JSON schema files](ocpp-json/src/test/resources/schemas/) for OCPP 1.6 are part of the OCPP 1.6 Specification, distributed under the following conditions:

      ```
      Copyright © 2010 – 2015 Open Charge Alliance. All rights reserved.
      This document is made available under the *Creative Commons Attribution- NoDerivatives 4.0 International Public License* (https://creativecommons.org/licenses/by-nd/4.0/legalcode).
      ```
