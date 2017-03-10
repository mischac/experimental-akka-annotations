### annotations for akka methods
usually an actor's `receive` has some pattern matching against the incoming message to invoke some logic against message.
the approach here implemented allows to annotate methods (per message type) and have on runtime the methods automatically invoked from the `receive` method.


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

#### dispatcher
##### the dispatcher
the dispatcher essentially maintains a map `message class -> (receiver, receiver method)` and will on runtime for a given message invoke 
the corresponding `receiver.method`. 

behaviour for missing mappings can be implemented - see `NotFoundStrategy` in the `Dispatcher`, the default is a runtime exception.

#### usage samples
##### by overriding `receive`
usage (from an actor) example:

```override def receive: Receive = Dispatcher(obj1, obj2).receive```

will dispatch messages to objects obj1, obj2 according their annotated methods.


##### by extending `Xctor`
alternatively extend [`Xctor`](src/main/scala/experimental/akka/annotations/receive/Actors.scala) (with/without mixins). for example

```class MyActor extends Xctor with Biz with Tech```

will take all annotated methods in `MyActor`,`Biz` and `Tech` into account.

see also [`SampleActors`](src/test/scala/experimental/akka/annotations/receive/SampleActors.scala) for usage examples.


### remarks
#### typed
this will not at all make the akka experience more typed as it focuses only message-to-method.
inside the method impl there might be still a lot of messaging to other actors rather than invoking methods etc
(this might be encapsulated via a trait though).

for more general approaches see deprecated [`TypedActor`](http://doc.akka.io/docs/akka/current/scala/typed-actors.html) or experimental [`Akka Typed`](http://doc.akka.io/docs/akka/current/scala/typed.html).

so not sure how applicable this all is for larger akka implementations - for now this is an experiment with scala annotations.
 
 
