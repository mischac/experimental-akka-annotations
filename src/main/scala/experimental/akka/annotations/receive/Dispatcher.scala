package experimental.akka.annotations.receive

import experimental.akka.annotations.receive.Dispatcher.NotFoundStrategy
import experimental.akka.annotations.receive.MethodMap.MethodMap

object Dispatcher {

  type NotFoundStrategy = Any => Unit

  private val throwException: NotFoundStrategy =
    m => throw new RuntimeException(s"$m: unknown message class ${m.getClass}")

  def apply(receivers: Any*): Dispatcher = Dispatcher(Some(throwException), MethodMap(receivers: _*))

  def relaxed(receivers: Any*): Dispatcher = Dispatcher(None, MethodMap(receivers: _*))

  def withHandler(notFoundHandler: NotFoundStrategy, receivers: Any*): Dispatcher =
    Dispatcher(Some(notFoundHandler), MethodMap(receivers: _*))

}

case class Dispatcher(notFoundStrategy: Option[NotFoundStrategy], methodMap: MethodMap) {

  def apply(message: Any): Unit = methodMap
    .get(message.getClass)
    .fold(notFoundStrategy.foreach(_ (message)))(_ (message))

  def receive: PartialFunction[Any, Unit] = {
    case message: Any => apply(message)
  }
}
