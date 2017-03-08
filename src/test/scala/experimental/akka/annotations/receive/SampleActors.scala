package experimental.akka.annotations.receive

import java.util.concurrent.TimeUnit.SECONDS

import akka.actor.{Actor, ActorSystem, Props, ReceiveTimeout}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Await


/*
just some random code here to show some usages.
each usage here has its own main
 */

case class Strategy(rule: String)

trait BizContext {
  def pleaseThinkAbout(strategy: Strategy): Boolean
}

class BusinessLogic(context: BizContext) {

  val prefix = "Biz >>"

  @ReceiveMessage
  def doThis(x: Boolean) = println(s"$prefix do this: $x")


  @ReceiveMessage
  def doThat(x: Int) = println(s"$prefix do that: $x")

  @ReceiveMessage
  def implement(x: Strategy) = println(s"$prefix is $x really good? ${context.pleaseThinkAbout(x)}")

}

object Shutdown

object Sleep


object TechLogic {

  val prefix = "TEC >>"

  @ReceiveTrigger(Shutdown)
  def pleaseShutdown() = println(s"$prefix shutdown request received")

  @ReceiveTrigger(Sleep)
  def goodNight() = println(s"$prefix sleep request received")

  @ReceiveTrigger(ReceiveTimeout)
  def timeout() = println(s"$prefix I don't have any time for this nonsense")

}


//----------------------------------------------------------------------- manual internal wiring
class SampleActor(ctx: BizContext) extends Actor {

  override def receive: Receive = Dispatcher(new BusinessLogic(ctx), TechLogic).receive

}


object SampleActor {
  def main(args: Array[String]): Unit = {

    object dummyContext extends BizContext {
      override def pleaseThinkAbout(strategy: Strategy): Boolean = false
    }

    val system = ActorSystem("SampleSystem")
    val sampleActor = system.actorOf(Props(new SampleActor(dummyContext)), "actor-sample")

    sampleActor ! Strategy("copy & paste strategy")
    sampleActor ! Sleep
    sampleActor ! 6767
    sampleActor ! ReceiveTimeout
    sampleActor ! Shutdown
    //    sampleActor ! "oh dear this will crash, no string  message accepted"

  }
}

//-----------------------------------------------------------------------

// here receive func is injected
case class GenericActor(receive: Actor.Receive) extends Actor

object GenericActor {

  object dummyContext extends BizContext {
    override def pleaseThinkAbout(strategy: Strategy): Boolean = {
      println(s"this $strategy is garbage")
      false
    }
  }


  def main(args: Array[String]): Unit = {
    val system = ActorSystem("GenericActor")
    val dispatcher = Dispatcher(new BusinessLogic(dummyContext), TechLogic)
    val myActorX = system.actorOf(Props(new GenericActor(dispatcher.receive)), "myActorX")

    myActorX ! Strategy("cool strategy")
    myActorX ! "oh dear this will be ignored"

  }
}


//----------------------------------------------------------------------- mixed in
// --- framework: reusable self dispatching actor


// --- app
trait Biz extends Actor {
  @ReceiveMessage
  def biz(query: String) = {
    println(s"F A N C Y   B I Z   L O G I C: $query")
    sender() ! "N O  I D E A"
  }
}

// no an actor - not needed as no ref to sender/context etc.
trait Tech {
  @ReceiveTrigger(ReceiveTimeout)
  def timeout() = println("T I M E O U T")
}

class MyActor extends Xctor with Biz with Tech

object MyActor {
  def main(args: Array[String]): Unit = {
    implicit val timeout = Timeout(5, SECONDS)
    val system = ActorSystem("MyApp")
    val myApp = system.actorOf(Props[MyActor], "myapp")

    println(Await.result(myApp ? "what is the issue here?", timeout.duration))

    myApp ! ReceiveTimeout
  }
}

// not sure how useful as the traits have no state but anyway just for demonstration


//----------------------------------------------------------------------- most simple thing
// here access to sender/context etc. by extending Actor.
// for other approaches these need to be injected to receiver classes

class Actor77 extends Xctor {

  @ReceiveMessage
  def q(query: String) = sender() ! "N O  I D E A"

  @ReceiveTrigger(ReceiveTimeout)
  def timeout() = println("T I M E O U T")

}


object Actor77 {

  import akka.util.Timeout

  def main(args: Array[String]): Unit = {
    val system = ActorSystem("MyApp")
    val actor77 = system.actorOf(Props[Actor77], "myapp")
    implicit val timeout = Timeout(5, SECONDS)


    // await just for demo here
    println(Await.result(actor77 ? "how should this work?", timeout.duration))
    actor77 ! ReceiveTimeout


  }
}