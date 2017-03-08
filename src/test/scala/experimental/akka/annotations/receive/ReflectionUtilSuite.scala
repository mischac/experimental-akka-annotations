package experimental.akka.annotations.receive

import experimental.akka.annotations.receive.outer.nested
import org.scalatest._

import scala.annotation.StaticAnnotation
import scala.reflect.runtime.universe._


object no1

case object no2

object outer {

  object nested

}


case class TestAnnotation() extends StaticAnnotation

case class TestObject() {

  @TestAnnotation
  def dummy() = {}

  @ReceiveTrigger(no1)
  def methodN01() = {}

  @ReceiveTrigger(nested)
  def methodNested() = {}

}


class ReflectionUtilSuite extends FunSuite {

  private val rtMirror = runtimeMirror(getClass.getClassLoader)

  private def method(instance: Any, methodName: String) = {
    val instanceMirror = runtimeMirror(getClass.getClassLoader).reflect(instance)
    instanceMirror.symbol.typeSignature.member(TermName(methodName)).asMethod
  }


  import ReflectionUtil._

  test("named object") {
    assert(singletonObject("experimental.akka.annotations.receive.no1") == no1)
  }

  test("named case object") {
    assert(singletonObject("experimental.akka.annotations.receive.no2") == no2)
  }

  test("named nested object") {
    assert(singletonObject("experimental.akka.annotations.receive.outer.nested") == nested)
  }

  test("annotation") {
    assert(annotation(method(new TestObject, "dummy"), classOf[TestAnnotation]).isDefined)
  }

  test("tree: object name to object") {
    val a = annotation(method(new TestObject, "methodN01"), classOf[ReceiveTrigger]).getOrElse(fail)
    assert(toSingletonObject(a.tree.children.tail.head) == no1)
  }

  test("tree: nested object name to object") {
    val a = annotation(method(new TestObject, "methodNested"), classOf[ReceiveTrigger]).getOrElse(fail)
    assert(toSingletonObject(a.tree.children.tail.head) == nested)
  }

}
