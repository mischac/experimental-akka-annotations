package experimental.akka.annotations.receive

import org.scalatest._
import org.scalatest.prop.TableDrivenPropertyChecks._

case object Dummy

class ReceiveMessageSuite extends FunSuite {

  private val receive = Set("receive")

  private def messageSet(obj: Object) = {
    (Dispatcher(obj).methodMap map { case (_, m) => m.symbol.name.toString }).toSet
  }

  object TriggerWithArgFails {
    @ReceiveTrigger()
    def receive(a: Boolean): Unit = {}
  }

  object MessageWithoutArgFails {
    @ReceiveMessage()
    def receive(): Unit = {}
  }

  object MessageWithMoreThanOneArgsFails {
    @ReceiveMessage()
    def receive(a: Boolean, b: Boolean): Unit = {}
  }

  object TriggerWithNonObjectFails {
    @ReceiveTrigger(66767)
    def receive(): Unit = {}
  }

  object TriggerEmptyFails {
    @ReceiveTrigger()
    def receive(): Unit = {}
  }

  val failures = Table(
    "receiver",
    TriggerWithArgFails,
    MessageWithoutArgFails,
    MessageWithMoreThanOneArgsFails,
    TriggerWithNonObjectFails,
    TriggerEmptyFails
  )
  forAll(failures) { (obj) =>
    test(s"${obj.getClass.getSimpleName} fails") {
      assertThrows[RuntimeException] {
        Dispatcher(obj)
      }
    }
  }


  object MessageOk {
    @ReceiveMessage()
    def receive(a: Boolean): Unit = {}
  }


  object TriggerOk {
    @ReceiveTrigger(Dummy)
    def receive(): Unit = {}
  }

  val ok = Table(
    "receiver",
    MessageOk,
    TriggerOk
  )


  forAll(ok) { (obj) =>
    test(s"${obj.getClass.getSimpleName} does not fail") {
      assert(messageSet(obj) == receive)
    }
  }


}
