package experimental.akka.annotations.receive

import scala.reflect.runtime.universe.MethodMirror
import experimental.akka.annotations.receive.ReflectionUtil._
import experimental.akka.annotations.receive.ToMethod.toMethod

// actually MethodMirrorMap
object MethodMap {

  type MethodMap = Map[Class[_], MethodMirror]

  def apply(receivers: Any*): MethodMap = {

    val map = scala.collection.mutable.Map[Class[_], MethodMirror]()

    for (receiver <- receivers;
         mirror <- methodMirrors(receiver);
         messageClass <- ReceiveMessage(mirror) orElse ReceiveTrigger(mirror)
    ) map
      .put(messageClass, mirror)
      .flatMap(_ => throw new RuntimeException(s"duplicated message class $messageClass"))

    map.toMap
  }

}
