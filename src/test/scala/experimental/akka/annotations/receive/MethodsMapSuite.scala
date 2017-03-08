package experimental.akka.annotations.receive

import java.lang.{Boolean, Double, Long}

import org.scalatest._

class Sample1() {
  @ReceiveMessage
  def m1(p: Int): Unit = {}

  @ReceiveMessage
  def m2(p: Boolean): Unit = {}

  @ReceiveMessage
  def m3(p: String): Unit = {}
}


class Sample2() {
  @ReceiveMessage
  def s1(p: Long): Unit = {}

  @ReceiveMessage
  def s2(p: Double): Unit = {}

  @ReceiveMessage
  def s3(p: Sample1): Unit = {}

  def ignored(i: Int): Unit = {}
}

class Sample3() {
  @ReceiveMessage
  def w3(p: String): Unit = {}
}

class MethodsMapSuite extends FunSuite {

  test("method map") {
    val actual = MethodMap(new Sample1, new Sample2) map { case (c, m) => (c, m.symbol.name.toString) }

    val expected = Map(
      classOf[Sample1] -> "s3",
      classOf[Integer] -> "m1",
      classOf[Long] -> "s1",
      classOf[Double] -> "s2",
      classOf[Boolean] -> "m2",
      classOf[String] -> "m3"
    )
    assert(actual.toSet == expected.toSet)
  }

  test("duplicated message") {
    assert(
      intercept[RuntimeException] {
        MethodMap(new Sample1, new Sample2, new Sample3)
      }.getMessage.startsWith("duplicated message class")
    )
  }


}
