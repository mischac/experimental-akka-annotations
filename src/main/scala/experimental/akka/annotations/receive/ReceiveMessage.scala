package experimental.akka.annotations.receive

import ReflectionUtil._

import scala.annotation.StaticAnnotation
import scala.reflect.runtime.universe._

object ToMethod {
  implicit def toMethod(methodMirror: MethodMirror): MethodSymbol = methodMirror.symbol.asMethod
}

object ReceiveMessage {

  // gets the message class from the method parameter (i.e. the single param of the annotated method)
  def apply(method: MethodSymbol): Option[(Class[_])] =
    annotationWithArgsCountCheck(method, classOf[ReceiveMessage], 1)
      .map(_ => getMethodParam(method))
}

case class ReceiveMessage() extends StaticAnnotation

object ReceiveTrigger {

  // gets the message class from the messageObject (param of ReceiveTrigger) which needs to be a singleton object
  def apply(method: MethodSymbol): Option[(Class[_])] =
    annotationWithArgsCountCheck(method, classOf[ReceiveTrigger], 0)
      .map(_.tree.children.tail.head)
      .map(toSingletonObject)
      .map(_.getClass)
}

case class ReceiveTrigger(messageObject: Any) extends StaticAnnotation
