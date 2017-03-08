package experimental.akka.annotations.receive

import akka.actor.Actor

/**
  * extend this one to benefit from the annotations
  */
class Xctor extends Actor {
  override def receive: Receive = Dispatcher(this).receive
}
