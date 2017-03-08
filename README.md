### annotations for akka methods
usually an actor's `receive` has some pattern matching against the incoming message to invoke some logic against message.
the approach here implemented allows to annotated methods (per message type) and have on runtime the methods automatically invoked from the `receive` method.


#### annotations
there are two annotations, both for methods.

##### ReceiveMessage

Example:

```
   @ReceiveMessage
   def replicate(data: Data) = .....
```

The annotated method needs to have exactly one parameter.
In example above any message of type `Data` will cause an invocation of the `replicate` method with the data message.

##### ReceiveTrigger

Example:

```
import akka.util.Timeout
....

  @ReceiveTrigger(ReceiveTimeout)
  def timeout() = ...

```

This is used for messages which are singleton objects. 
the annotated method should not have any parameters.

#### dispatcher & actor code
to use the annotation have in you actor code something like this:

```  override def receive: Receive = Dispatcher(receiver1, receiver2).receive```
with some objects receiver1, receiver2 which have annotated messages.

alternatively extend [`Xctor`](src/main/scala/experimental/akka/annotations/receive/Actors.scala).

see also [`SampleActors`](src/test/scala/experimental/akka/annotations/receive/SampleActors.scala) for usage examples.

the dispatcher essentially maintains a map `message class -> (receiver, receiver method)` and on runtime will invoke for a given message the corresponding method. behaviour for missing mappings can be implemented - see `NotFoundStrategy` in the `Dispatcher`.

### remarks
#### typed
this will not at all make the akka experience more typed as it focuses only message-to-method.
inside the method impl there might be still a lot of messaging to other actors rather than invoking methods etc
(this might be encapsulated via a trait though).

for more general approaches see deprecated [`TypedActor`](http://doc.akka.io/docs/akka/current/scala/typed-actors.html) or experimental [`Akka Typed`](http://doc.akka.io/docs/akka/current/scala/typed.html).

This here were rather experiments to play with scala annotations.
 
 
