package experimental.akka.annotations.receive

import org.scalatest._
import org.scalatest.prop.TableDrivenPropertyChecks._

import scala.collection.mutable.Buffer

object trigger

case class Hello(a: Int, flag: Boolean)

class DispatcherSuite extends FunSuite {

  case class App1(args: Buffer[Any]) {

    @ReceiveMessage
    def app1(x: Int) = args += x

    @ReceiveMessage
    def app1(x: Boolean) = args += x

    @ReceiveMessage
    def app1(x: String) = args += x


    @ReceiveMessage
    def app1(x: Hello) = args += x


    @ReceiveTrigger(trigger)
    def app1() = args += trigger

  }


  test("standard dispatcher") {

    val expectations = Buffer.empty[Any]

    val app1 = new App1(Buffer.empty)
    val dispatcher = Dispatcher(app1)

    def invoke(arg: Any) = {
      // invokes and stores the expectation
      dispatcher(arg)
      expectations += arg
    }

    invoke(88)
    invoke(true)
    invoke("hello")
    invoke(Hello(22, false))
    invoke(trigger)

    assert(expectations == app1.args)
  }


  test("standard dispatcher exception on unknown double arg") {

    val dispatcher = Dispatcher(new App1(Buffer.empty))

    assert(
      intercept[RuntimeException] {
        dispatcher(23.4)
      }.getMessage.contains("unknown message class")
    )

  }

  test("relaxed dispatcher does not throw an exception on unknown double arg") {

    Dispatcher.relaxed(new App1(Buffer.empty))(23.4)

  }

  trait App2 {

    @ReceiveMessage
    def anotherMethod(x: Double) = {}
  }

  class SuperApp extends App1(Buffer.empty) with App2

  test("find inherited methods") {
    Dispatcher(new SuperApp)(23.4)
  }

  class a {
    @ReceiveMessage
    def a() = {}
  }

  trait b {
    @ReceiveMessage
    def b() = {}
  }

  class c extends a with b {
    @ReceiveMessage
    def c1 = {}

    @ReceiveMessage
    def c2 = {}

    @ReceiveMessage
    def c3 = {}
  }


  object LongReceive {
    @ReceiveMessage
    def receive(p: Long): Unit = {}
  }

  object IntReceive {
    @ReceiveMessage
    def receive(p: Int): Unit = {}

  }

  object BooleanReceive {
    @ReceiveMessage
    def receive(p: Boolean): Unit = {}
  }

  object ByteReceive {
    @ReceiveMessage
    def receive(p: Byte): Unit = {}

  }

  object DoubleReceive {
    @ReceiveMessage
    def receive(p: Double): Unit = {}

  }

  object CharReceive {
    @ReceiveMessage
    def receive(p: Char): Unit = {}
  }

  object FloatReceive {
    @ReceiveMessage
    def receive(p: Float): Unit = {}
  }


  object ShortReceive {
    @ReceiveMessage
    def receive(p: Short): Unit = {}
  }

  val primitives = Table(
    ("receiver", "message"),
    (LongReceive, 1l),
    (IntReceive, 22),
    (DoubleReceive, 22.89),
    (FloatReceive, 22.89f),
    (BooleanReceive, true),
    (ByteReceive, 1.byteValue()),
    (ShortReceive, 1.shortValue()),
    (CharReceive, 'c')
  )

  forAll(primitives) { (obj, message) =>
    test(s"primitive dispatch ${obj.getClass.getSimpleName} does not fail") {
      val dispatcher = Dispatcher(obj)
      // assert method mp
      val values = dispatcher.methodMap.mapValues(m => m.symbol.name.toString)
      assert(
        values == Map(message.getClass -> "receive")
      )
      // check invoke: this will fail if msg class is not found
      dispatcher.receive(message)
    }
  }


}
