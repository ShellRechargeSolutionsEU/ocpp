# Open Charge Point Protocol for Scala [![Build Status](https://secure.travis-ci.org/NewMotion/ocpp.png)](http://travis-ci.org/NewMotion/ocpp) [![Coverage Status](https://coveralls.io/repos/github/NewMotion/ocpp/badge.svg?branch=master)](https://coveralls.io/github/NewMotion/ocpp?branch=master) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/eab36abac62e4e33845a10a1485f35c6)](https://www.codacy.com/app/reinierl/ocpp)

The Open Charge Point Protocol (OCPP) is a network protocol for communication
between electric vehicle chargers and a central backoffice system. It is
developed by the Open Charge Alliance (OCA). You can find more details on the
[official website of the OCA](http://openchargealliance.org/).

## Functionality

This library is the implementation of OCPP developed and used by NewMotion, one
of Europe's largest Electric Vehicle Charge Point Operators.

This library only implements the network protocol. That is, it provides data
types for the OCPP messages, remote procedure call using those request and
response messages, and error reporting about those remote procedure calls. It
does _not_ provide any actual handling of the message contents. For an actual
app speaking OCPP using this library, see
[docile-charge-point](https://github.com/NewMotion/docile-charge-point).

The library is designed with versatility in mind. OCPP comes in 4 versions (1.2,
1.5, 1.6 and 2.0), two transport variants (SOAP/XML aka OCPP-S and WebSocket/JSON aka
OCPP-J), and two roles ("Charge Point" and "Central System"). This library will
help you with 1.5 and 1.6 over JSON. For 1.2 and 1.5 over SOAP, there is [a
separate library by NewMotion](https://github.com/NewMotion/ocpp-soap) that
depends on this one. Some OCPP 2.0 support is present, but not a full
implementation yet. The main body of this README will be writing about the OCPP
1.5 and 1.6 support; for OCPP 2.0 see [here](#ocpp2).

Version 2.0 with SOAP/XML is not possible. Version 1.2 with
WebSocket/JSON and version 1.6 with SOAP/XML are not supported by this
library.

Users of this library probably want to use different WebSocket libraries for
different scenarios: a production back-office server with tens of thousands of
concurrent connections, a client in a load testing tool, or a simple one-off
script to test a certain behavior. This library uses the
[cake pattern](http://www.cakesolutions.net/teamblogs/2011/12/19/cake-pattern-in-depth)
to make it easy to swap out the underlying WebSocket implementation while still
using the same concise high-level API.

## How to use

### Setup

The library is divided into three separate modules so applications using it
won't get too many dependencies dragged in. Those are:

  * `ocpp-j-api`: high-level interface to OCPP-J connections
  * `ocpp-json`: serialization of OCPP messages to/from JSON
  * `ocpp-messages`: The definitions of OCPP messages, independent from
                     the transport variant used

So if you want to use the high-level OCPP-J connection interface, and you're
using SBT, you can declare the dependency by adding this this to your `build.sbt` after publishing the library:

```
libraryDependencies += "com.thenewmotion.ocpp" %% "ocpp-j-api" % "9.2.2"
```

With Maven, add this to your dependencies:

```xml
    <dependency>
        <groupId>com.thenewmotion.ocpp</groupId>
        <artifactId>ocpp-j-api_2.11</artifactId>
        <version>9.2.2</version>
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

 * A connection is established by creating an instance of `OcppJsonClient`
   using the `OcppJsonClient.forVersion1x` factory method.
   The server endpoint URI, charge point ID and OCPP version to use are passed
   to the  method, followed by a handler for incoming OCPP requests in a
   second parameter list.

 * To send OCPP messages to the Central System, you call the `send` method on
   the `OcppJsonClient` instance. You will get a `Future` back that will be
   completed with the Central System's response. If the Central System fails
   to respond to your request, the `Future` will fail.

 * `OcppJsonClient` is an instance of the [`OutgoingOcppEndpoint`](ocpp-j-api/src/main/scala/com/thenewmotion/ocpp/json/api/OutgoingOcppEndpoint.scala)
   trait. This trait defines this interface.

#### Handling requests

To specify the request handler, we use a [_magnet pattern_](http://spray.io/blog/2012-12-13-the-magnet-pattern/).
You can specify the request handler in different ways. After the
`val requestHandler: ChargePointRequestHandler =`, you see a
[`ChargePoint`](ocpp-messages/src/main/scala/com/thenewmotion/ocpp/messages/ChargePoint.scala)
instance in the example program. But you can also specify the request handler
as a function from `ChargePointReq` to `Future[ChargePointRes]`:

```scala

 val ocppJsonClient = OcppJsonClient.forVersion1x(chargerId, new URI(centralSystemUri), versions) {
   (req: ChargePointReq) =>
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

The set of messages you can send with OCPP 1.x connections is defined in
[ocpp-messages](ocpp-messages/src/main/scala/com/thenewmotion/ocpp/messages/v1x/Message.scala).
For every request type, you represent requests as instances of a case class
named `<Operation Name>Req`, e.g. `StatusNotificationReq`, `HeartbeatReq`.

For OCPP 1.x, these case classes in `ocpp-messages` are designed
according to two principles:
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

#### Error handling

If the remote side responds to your OCPP requests with a `CALLERROR` message
indicating a failure to process your request, the future returned from `.send`
will be failed. The exception in there will be an `OcppException` object, which
contains an `OcppError` object, which contains the error code and description
sent from the other side.

It works the same way in your own request handlers. You can return a failed
future with an `OcppException`, and the library will turn this into a
`CALLERROR` message and sends it back to the remote side.

### Using the cake pattern directly

If you want to build an OCPP-J client using a different WebSocket
implementation, or an OCPP-J server, you'll have to use the
[cake](http://www.cakesolutions.net/teamblogs/2011/12/19/cake-pattern-in-depth)
layers directly.

The OCPP cake has three layers:

 * [`OcppConnectionComponent`](ocpp-j-api/src/main/scala/com/thenewmotion/ocpp/json/api/OcppConnection.scala): handles serialization and deserialization between OCPP request/response objects and SRPC messages
 * [`SrpcComponent`](ocpp-j-api/src/main/scala/com/thenewmotion/ocpp/json/api/SrpcConnection.scala): matches requests to responses, and serializes and deserializes SRPC messages to JSON
 * [`WebSocketComponent`](ocpp-j-api/src/main/scala/com/thenewmotion/ocpp/json/api/WebSocketConnection.scala): reads and writes JSON messages from and to a WebSocket connection

There are default implementations for the `OcppConnectionComponent` and
`SrpcComponent`. The `WebSocketComponent` you will have to create yourself to
integrate with your WebSocket implementation of choice.

To put it in a diagram:

```
                  (your application logic)

    +--------------V--------------------^--------------+
    |   com.thenewmotion.ocpp.messages.v1x.{Req, Res}  |
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
       OcppConnectionComponent.ocppConnection.sendRequest (call)     OcppConnectionComponent.onRequest (override)
    +------------------V-----------------------------------------------^-----------------------------------------------------------+
    |                                                                                                                              |
    |                                    OcppConnectionComponent layer                                                             |
    |                                                                                                                              |
    | SrpcConnectionComponent.srpcConnection.sendCall (call)                                                                       |
    | SrpcConnectionComponent.srpcConnection.close (call)                                                                          |
    | SrpcConnectionComponent.srpcConnection.forceClose (call)                                                                     |
    | SrpcConnectionComponent.srpcConnection.onClose (call)          SrpcConnectionComponent.onSrpcCall (override)                 |
    +------------------V-----------------------------------------------^-----------------------------------------------------------+
    |                                                                                                                              |
    |                                          SrpcComponent layer                                                                 |
    |                                                                                                                              |
    |                                                                WebSocketConnectionComponent.onMessage (override)             |
    | WebSocketConnectionComponent.webSocketConnection.send (call)   WebSocketConnectionComponent.onWebSocketDisconnect (override) |
    | WebSocketConnectionComponent.webSocketConnection.close (call)  WebSocketConnectionComponent.onError (override)               |
    +------------------V-----------------------------------------------^-----------------------------------------------------------+
    |                                                                                                                              |
    |                                       WebSocketComponent layer                                                               |
    |                                                                                                                              |
    | (WebSocket library dependent)                                  (WebSocket library dependent)                                 |
    +------------------V-----------------------------------------------^-----------------------------------------------------------+
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

      * Do either `new CentralSystemOcpp1XConnectionComponent with DefaultSrpcComponent with MyWebSocketComponent { ... }`
        or `new ChargePointOcpp1XConnectionComponent with DefaultSrpcComponent with MyWebSocketComponent { ... }`

      * Define in it a `val webSocketConnection`, `val srpcConnection` and
        `val ocppConnection`. For `ocppConnection`, use one of the
        `defaultChargePointOcppConnection` and
        `defaultCentralSystemOcppConnection` methods defined by the
        `*Ocpp1XConnectionComponent` traits. For `val srpcConnection`, use
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

  def handleConnection: OutgoingEndpoint => CentralSystemRequestHandler
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
   requests as an argument, and returns to the server component a
   request handler for handling incoming requests

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
import messages.v1x._

abstract class OcppJsonServer(listenPort: Int, ocppVersion: Version)
  extends WebSocketServer(new InetSocketAddress(listenPort)) {

  type OutgoingEndpoint = OutgoingOcppEndpoint[ChargePointReq, ChargePointRes, ChargePointReqRes]

  def handleConnection: OutgoingEndpoint => CentralSystemRequestHandler

  override def onStart(): Unit = {}

  override def onOpen(conn: WebSocket, hndshk: ClientHandshake): Unit = {

    val ocppConnection = new CentralSystemOcpp1XConnectionComponent  with DefaultSrpcComponent with SimpleServerWebSocketComponent {
      override val ocppConnection: DefaultOcppConnection = defaultCentralSystemOcppConnection

      override val srpcConnection: DefaultSrpcConnection = new DefaultSrpcConnection()

      override val webSocketConnection: SimpleServerWebSocketConnection = new SimpleServerWebSocketConnection {
        val webSocket: WebSocket = conn
      }

      def onRequest[REQ <: CentralSystemReq, RES <: CentralSystemRes](req: REQ)(implicit reqRes: CentralSystemReqRes[REQ, RES]) = ???

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

  override def onMessage(conn: WebSocket, message: String): Unit = ???

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
     create a cake with the three layers: `CentralSystemOcpp1XConnectionComponent with DefaultSrpcComponent with SimpleServerWebSOcketComponent`
  4. Add `???` implementations of all the abstract methods in the cake
  5. Add actual definitions of the `ocppConnection`, `srpcConnection`
     and `webSocketConnection` members of the three cake layers.

Note that at step 5, we have passed the `WebSocket` argument to the
`onOpen` method on into the `SimpleServerWebSocketConnection` so that
the cake can later use this `WebSocket` to send OCPP messages over.

So by now we're at the fourth point of the five-step cake plan: connect our
app's logic to the OCPP cake. The app's logic, in our case, is the
`handleConnection` that the library user specifies. And then, _inside_
the OCPP cake definition in `onOpen`, we create the outgoing endpoint,
and call the user-defined `handleConnection` on it so a request handler
for incoming messages is created:

```scala
private val outgoingEndpoint = new OutgoingEndpoint {
  def send[REQ <: ChargePointReq, RES <: ChargePointRes](req: REQ)(implicit reqRes: ChargePointReqRes[REQ, RES]): Future[RES] =
    ocppConnection.sendRequest(req)

  def close(): Future[Unit] = srpcConnection.close()
}

private val requestHandler = handleConnection(outgoingEndpoint)
```

Now that we have the incoming request handler, we can also fill
in definitions for the `onRequest` handler in the OCPP cake:

```scala
def onRequest[REQ <: CentralSystemReq, RES <: CentralSystemRes](req: REQ)(implicit reqRes: CentralSystemReqRes[REQ, RES]) =
  requestHandler(req)
```

And then, there is the `ocppVersion` method in `OcppConnectionComponent`
that will tell it which OCPP version to use to serialize and deserialize
OCPP messages to JSON. We have to fill in the OCPP version that was
passed in to the `OcppJsonServer` constructor. To avoid name clashes,
we rename the argument to `requestedOcppVersion`:

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
create a `CentralSystemRequestHandler`, and that we later call this `CentralSystemRequestHandler`
whenever a message comes in on the connection. To make this happen, we will need
a map from `WebSocket` instances that the server gets in its `onMessage` method,
to the OCPP cake instance that will process messages for that connection.

So let's add this to the `OcppJsonServer` class: link the WebSocket connections
we're using to their OCPP cakes.

```scala
  private type OcppCake = CentralSystemOcppConnectionComponent with DefaultSrpcComponent with SimpleServerWebSocketComponent

  object connectionMap {
    private val ocppConnections: mutable.Map[WebSocket, BaseConnectionCake] =
      mutable.HashMap[WebSocket, BaseConnectionCake]()

    def put(conn: WebSocket, cake: BaseConnectionCake): Unit = connectionMap.synchronized {
      ocppConnections.put(conn, cake)
      ()
    }

    def remove(conn: WebSocket): Option[BaseConnectionCake] = connectionMap.synchronized {
      ocppConnections.remove(conn)
    }

    def get(conn: WebSocket): Option[BaseConnectionCake] = connectionMap.synchronized {
      ocppConnections.get(conn)
    }
  }
```

We use a mutable map, wrapped in its own little `object connectionMap`
that assures thread-safety.

To fill the map, we add this line at the bottom of `onOpen` in our
`OcppJsonServer`:

```scala
connectionMap.put(conn, ocppConnection)
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
`onMessage`, `onClose` and `onError` callbacks of `WebSocketServer`
actually call into the connection's OCPP cake to let it process the message. It
turns out that to do this, we have to add three methods to the
`SimpleWebSocketServerComponent`:

```scala
def feedIncomingMessage(msg: String) = self.onMessage(org.json4s.native.JsonMethods.parse(msg))

def feedIncomingDisconnect(): Unit = self.onWebSocketDisconnect()

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
  connectionMap.remove(conn) foreach { c =>
    c.feedIncomingDisconnect()
  }
}

override def onMessage(conn: WebSocket, message: String): Unit =
  connectionMap.get(conn) foreach { c =>
    c.feedIncomingMessage(message)
  }

override def onError(conn: WebSocket, ex: Exception): Unit =
  connectionMap.get(conn) foreach { c =>
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
def handleConnection(clientChargePointIdentity: String, remote: OutgoingEndpoint): CentralSystemRequestHandler
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

In order to save you the typing and bugfixing, an actual tested version of the
server developed while writing this [is
included](ocpp-j-api/src/main/scala/com/thenewmotion/ocpp/json/api/server/OcppJsonServer.scala).
With the OCPP 2.0 work though, it has become a bit more involved because it is
now an abstract class that is extended by 1.x and 2.0 specific classes.

And there is also a small
[example app](example-json-server/src/main/scala/com/thenewmotion/ocpp/json/example/ExampleServerTestApp.scala)
that shows how to use the `OcppJsonServer` interface. It will listen on port
2345, and return `NotImplemented` to any request except BootNotification. In
response to a BootNotification, it will send a response and also send a
GetConfiguration request back to the client. To run it, you do:

```
$ sbt "project example-json-server" run
```

There is also an [OCPP 2.0 version of this server](example-json-server-20/src/main/scala/com/thenewmotion/ocpp/json/example/ExampleServerTestApp.scala).
You can run it with:

```
$ sbt "project example-json-server-20" run
```

### Just serializing

If you do not need the connection management provided by the high-level API,
you can still use the `ocpp-json` module for serializing and deserializing OCPP
messages that you will send or receive using other libraries.

To do so, call the methods in the [Serialization](ocpp-json/src/main/scala/com/thenewmotion/ocpp/json/v1x/Serialization.scala)
object after importing either
`com.thenewmotion.ocpp.json.v1x.v15.SerializationV15._`
or `com.thenewmotion.ocpp.json.v1x.v16.SerializationV16._` to select which OCPP
version to use:

```scala

    import com.thenewmotion.ocpp.json.OcppJ
    import com.thenewmotion.ocpp.messages.{v1x => messages}
    import com.thenewmotion.ocpp.Version
    import com.thenewmotion.ocpp.json.v16.SerializationV16._

    OcppJ.write(messages.AuthorizeReq(idTag = "ABCDEF012"))
    // this results in:
    // res6: String = {"idTag":"ABCDEF012"}

    OcppJ.read[messages.AuthorizeReq, Version.V16.type]("""{"idTag":"ABCDEF012"}""")
    // this results in:
    // res10: com.thenewmotion.ocpp.messages.AuthorizeReq = AuthorizeReq(ABCDEF012)
```

There are also `serialize` and `deserialize` methods on the `Serialization` object that
use json4s `JValue`s as the representation of JSON instead of raw `String`s.
You can use those to build the [SRPC](http://www.gir.fr/ocppjs/ocpp_srpc_spec.shtml)
messages that are sent over the WebSocket. See [TransportMessage](ocpp-json/src/main/scala/com/thenewmotion/ocpp/json/TransportMessageProtocol.scala)
and [TransportMessageJsonSerializers](ocpp-json/src/main/scala/com/thenewmotion/ocpp/json/TransportMessageJsonSerializers.scala)
for how to work with those.

<a name="ocpp2"></a>
## OCPP 2.0 support

OCPP 2.0 presents a major revision of the OCPP protocol, vastly improving
authentication and encryption of the connection, and bringing new, more refined
models of the charging station's configuration and the lifecycle of a charge
transaction.

This means that it is not possible to write code that will transparently work
with OCPP 2.0 or other OCPP versions, as is possible with this library when
using the 1.x versions of OCPP.

You can use this library already to exchange OCPP 2.0 messages, but not all
message types, error codes, security mechanisms etc added by 2.0 are supported
yet. We're "glass half full" people, so we'll first explain what works, and then
move on to explain what still remains to be done.

### What this library offers

#### Creating an OCPP 2.0 connection

Using the `OcppJsonClient.forversion20`, you can create an instance of
`OcppJsonClient`, more specifically `Ocpp20JsonClient`, that will let you send
and receive OCPP 2.0 messages.

#### OCPP 2.0 messages

For OCPP 2.0, the message case classes are named `<Operation Name>Request` and
`<Operation Name>Response`, e.g. `BootNotificationRequest` and
`BootNotificationResponse`. They live in the
`com.thenewmotion.ocpp.messages.v20` package. They extend the
`com.thenewmotion.ocpp.messages.Request` and
`com.thenewmotion.ocpp.messages.Response` types depending on whether they are
requests or responses.  They also extend the `CsRequest`, `CsResponse`,
`CsmsRequest` and `CsmsResponse` types defined in the
`com.thenewmotion.ocpp.messages.v20` package, depending on which side is
executing the request. `CsRequest` and `CsResponse` are for operations executed
by the "Cs" (Charging Station, equivalent to the "Charge Point" of OCPP 1.x);
`CsmsRequest` and `CsmsResponse` are for operations executed by the "Csms"
(Charging Station Management System, equivalent to the "Central System" of OCPP
1.X).

Unlike the 1.5/1.6 message case classes, the OCPP 2.0 case classes for requests
and responses directly reflect the message structures defined in the OCPP 2.0
specification.  This is done to reduce the cognitive load when comparing
message definitions in Scala code to the OCPP specification, and because OCPP
2.0 as of now has no protocol versions that are similar enough to write code
that can transparently deal with multiple protocol versions.

The price of giving up the version-independent message case classes is
that some counterintuitive idiosyncracies of the OCPP specification, like
starting to count at 1 instead of 0 and using 0 as a sentinel, are now
inflicted upon unsuspecting Scala developers.

#### Supported operations

Currently the library has message case classes in place for the following
operations:

Charging Station operations:

 * GetBaseReport
 * GetTransactionStatus
 * GetVariables
 * RequestStartTransaction
 * RequestStopTransaction
 * SendLocalList
 * SetVariables

Charging Station Management System operations:

 * Authorize
 * BootNotification
 * Heartbeat
 * StatusNotification
 * TransactionEvent

### What remains to be done

   * support new RPC-level error codes

     * Although this is actually about SRPC, perhaps the simplest implementation
       would be in the OCPP layer, where we already distinguish the versions.
       The Ocpp1XConnectionComponent could filter error codes sent and received
       and map the ones unsupported by OCPP 1.X to the closest alternative that
       is supported. That would save us the complexity of multiple SrpcComponent
       implementations and choosing between them.

   * Message case classes and serializers for all operations

     * To add a message, you should:
       * Add Request and Response case classes [here](ocpp-messages/src/main/scala/com/thenewmotion/ocpp/messages/v20/Message.scala)
       * Add a ReqRes instance [here](ocpp-messages/src/main/scala/com/thenewmotion/ocpp/messages/v20/ReqRes.scala)
       * Add a Ocpp20Procedure [here](ocpp-json/src/main/scala/com/thenewmotion/ocpp/json/v20/Ocpp20Procedure.scala)
       * Add necessary JSON serializers (e.g. for new `Enumerable`s) [here](ocpp-json/src/main/scala/com/thenewmotion/ocpp/json/v20/serialization)
       * Add a JSON schema validation test [here](ocpp-json/src/test/scala/com/thenewmotion/ocpp/json/v20/JsonSchemaValidationSpec.scala)
       * Add a JSON serialization/deserialization round-trip test [here](ocpp-json/src/test/scala/com/thenewmotion/ocpp/json/v20/SerializationSpec.scala)

     * Maybe we can find a way to make it easier to add this messages, or at
       least make it straightforward so that you can just follow the compiler
       errors until it compiles and then it also works.

   * Factory method on OcppJsonClient to give caller a 1.x or 2.0 client
     depending on negotiation with server

   * Similarly on `OcppJsonServer`, create a factory method that lets people
     create a server with two request handlers, one for 1.x and for 2.0, and
     let the server negotiate the OCPP version to use with the client.

   * Add a mechanism for the library to report standardized security events
     about the connection

   * Add support for the 3 security profiles for authenticated encryption

   * Perhaps then add a note that the ready-made server class and app in this
     library are not intended for production use, do not support the
     authenticated encryption of the OCPP channel, and are as such insecure.

   * Support for JSON web signatures in the RPC-level encoding

## Changelog

### Changes in 9.2.2

 - JsonOperations.reqRes: handle serialization and deserialisation both within Future context

### Changes in 9.2.1

 - Remove a Scala 2.13 build until dependencies are fixed

### Changes in 9.2.0

 - Added support for `v1x.ChargePointDataTransferReq` and `v1x.ChargePointDataTransferRes` to
  `com.thenewmotion.ocpp.json.v1x.v15.SerializationV15` and `com.thenewmotion.ocpp.json.v1x.v16.SerializationV16`

 - Add a Scala 2.13 build

 - Add OpenJDK 8 and 11 builds

### Changes in 9.1.0

 - Added more OCPP 2.0 messages

 - Added an OCPP 2.0 example server app

### Changes in 9.0.1

 - Renamed `com.thenewmotion.ocpp.VersionFamily.V1XCentralSystemRequest` to
   `com.thenewmotion.ocpp.VersionFamily.V1XCentralSystemMessages` as it should
   have been named all along.

### Changes in 9.0.0

 - A start was made with support for OCPP 2.0

 - `OcppJsonServer` has now been split into two classes, depending on
   whether you want to serve OCPP 2.0 or OCPP 1.5/1.6:
   `Ocpp1XJsonServer` and `Ocpp20JsonServer`

 - `OcppJsonClient` has similarly been split into `Ocpp1XJsonClient` and
   `Ocpp20JsonClient`. Factory methods `OcppJsonClient.forVersion1x`
   and `OcppJsonClient.forVersion20` are available for easier
   instantiation of `OcppJsonClient` instances.

 - The `connection` member of `OcppJsonClient` was made private. To
   see the version of OCPP used by an `OcppJsonClient`, you can now use
   the `ocppVersion` method on the `OcppJsonClient` class.

 - The interfaces of the `ocpp-messages` and `ocpp-json` projects dealing
   with OCPP 1.x messages have moved to the `com.thenewmotion.ocpp.messages.v1x`
   and `com.thenewmotion.ocpp.json.v1x` packages, respectively.

   So code that used `com.thenewmotion.ocpp.messages.AuthorizationReq`
   must be changed to use `com.thenewmotion.ocpp.messages.v1x.AuthorizationReq`,
   and code that used `com.thenewmotion.ocpp.json.JsonOperation` must be changed
   to use `com.thenewmotion.ocpp.json.v1x.JsonOperation`.

  - `com.thenewmotion.ocpp.json.OcppJ` was renamed to
    `com.thenemwotion.ocpp.json.v1x.Serialization`

### Changes in 8.0.0

 - Move the code for handling OCPP over SOAP to another project

 - Add a Scala 2.12 build

### Changes in 7.0.0

 - Wait for pending incoming requests to be answered before closing a WebSocket
   connection

 - `OcppJsonClient.close` is now asynchronous; it returns a future that is
   completed once the connection is closed.

 - `OcppJsonClient` now has an apply method to construct
   `OcppJsonClient` instances without overriding any members

 - The `IncomingOcppEndpoint` trait is gone because the `onError` and
   `onDisconnect` methods were removed, so that only the
   `requestHandler` member was left and can simply take a
    `RequestHandler` in every place where the library previously
   expected an `IncomingOcppEndpoint`.

 - The rudimentary `onError` method is removed from `OcppJsonClient`.
   All OCPP errors are reported as failed futures returned from
   `OcppJsonClient.send`.

 - As part of the same dead code removal, the `onOcppError` method in
   `OcppComponent` is also gone

 - The `onDisconnect` method is removed from `OcppJsonClient` because
   closes are now signaled via an `onClose` member which returns a
   future which is completed once the connection is closed.

 - Because it is no longer needed to implement
   `IncomingOcppEndpoint.onDisconnect`, the
   `SrpcComponent.onSrpcDisconnect` method is removed.

 - The more robust closing involved changing some method names in the `SrpcConnectionComponent` and `WebSocketComponent`:

     * `SrpcComponent#SrpcConnection.send` is now `SrpcComponent#SrpcConnection.sendCall`
     * `SrpcComponent#SrpcConnection.close` now works asynchronously and returns a `Future[Unit]`
     * `SrpcComponent#SrpcConnection.forceClose` was added and works like the old `.close`, immediately closing the underlying WebSocket without waiting for processing to complete
     * `SrpcComponent.onSrpcRequest` is now `SrpcComponent.onSrpcCall`
     * `WebSocketComponent.onDisconnect` was renamed to `WebSocketComponent.onWebSocketDisconnect`

 - The case classes for SRPC messages were renamed to reflect the names used in the specification

 - Fixed a bug in the serialization of SendLocalListReq, where a member was
   called "localAuthorisationList" instead of "localAuthorizationList"

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

## Licensing and acknowledgements

The contents of this repository are © 2012 - 2018 The New Motion B.V., licensed under the [GPL version 3](LICENSE), except:

 * [The example messages for OCPP 1.5](ocpp-json/src/test/resources/com/thenewmotion/ocpp/json/v1x/ocpp15/without_srpc) in the ocpp-json unit tests, which were taken from [GIR ocppjs](http://www.gir.fr/ocppjs/).

 * The [JSON schema files](ocpp-json/src/test/resources/com/thenewmotion/ocpp/json/v1x/v16/schemas/) for OCPP 1.6 are part of the OCPP 1.6 Specification, distributed under the following conditions:

      ```
      Copyright © 2010 – 2015 Open Charge Alliance. All rights reserved.
      This document is made available under the *Creative Commons Attribution- NoDerivatives 4.0 International Public License* (https://creativecommons.org/licenses/by-nd/4.0/legalcode).
      ```

 * The [JSON schema files](ocpp-json/src/test/resources/com/thenewmotion/ocpp/json/v20/schemas/) for OCPP 2.0 are part of the OCPP 2.0 Specification, distributed under the following conditions:

      ```
      Copyright © 2010 – 2018 Open Charge Alliance. All rights reserved.
      This document is made available under the *Creative Commons Attribution-NoDerivatives 4.0 International Public License*
      (https://creativecommons.org/licenses/by-nd/4.0/legalcode).
      ```


