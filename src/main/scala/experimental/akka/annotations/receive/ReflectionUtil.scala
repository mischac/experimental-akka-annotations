package experimental.akka.annotations.receive

import scala.reflect.runtime.universe._

object ReflectionUtil {

  private val rtMirror = runtimeMirror(getClass.getClassLoader)

  // todo find way to get correct class via reflection or add all primitives
  // this is needed currently as method reflection returns the scala type and on runtime the class might be a java primitive
  // possibly both keys should be in the map
  private val primitives = Map(
    "scala.Boolean" -> "java.lang.Boolean",
    "scala.Long" -> "java.lang.Long",
    "scala.Double" -> "java.lang.Double",
    "scala.Float" -> "java.lang.Float",
    "scala.Short" -> "java.lang.Short",
    "scala.Int" -> "java.lang.Integer",
    "scala.Byte" -> "java.lang.Byte",
    "scala.Char" -> "java.lang.Character"
  )


  def annotationWithArgsCountCheck(method: MethodSymbol, clazz: Class[_], argCount: Int): Option[Annotation] = {
    val maybeAnnotation = annotation(method, clazz)
    maybeAnnotation.foreach(
      a => require(method.paramLists.head.size == argCount, s"$method must have exactly $argCount arg(s) for @$a")
    )
    maybeAnnotation
  }


  def annotation(method: MethodSymbol, clazz: Class[_]): Option[Annotation] =
    method.annotations.find(_.tree.tpe.typeSymbol.fullName == clazz.getName)

  // obviously this can fail
  // todo object within a class??
  def singletonObject(name: String): Any = rtMirror.reflectModule(rtMirror.staticModule(name)).instance

  def methodMirrors(receiver: Any): Iterable[MethodMirror] = {

    def relevant(s: Symbol) = s.isMethod && s.isPublic && !s.isConstructor && s.annotations.nonEmpty

    val mirror = rtMirror.reflect(receiver)
    mirror.symbol.typeSignature.members.filter(relevant)
      .map(_.asMethod)
      .map(mirror.reflectMethod)
  }

  def getMethodParam(method: MethodSymbol): Class[_] = {
    val symbols = method.paramLists.head
    val className = symbols.head.typeSignature.typeSymbol.fullName
    Class.forName(primitives.getOrElse(className, className))
  }

  def objectName(tree: Tree): String = {
    var name: Option[String] = None

    new Traverser {
      // this is result of trial & error - looks brittle todo check that
      // different cases below relate to nested/non nested object definitions
      override def traverse(t: Tree): Unit = t match {
        case s: Select => name = Some(s.symbol.fullName)
        case ident: Ident =>
          if (!ident.symbol.isPackage)
            name = Some(ident.tpe.typeSymbol.fullName)
          else
            super.traverse(ident)
        case _ => super.traverse(t)
      }
    }.traverse(tree)

    name.getOrElse(throw new RuntimeException(s"no named object found in $tree"))
  }

  def toSingletonObject(tree: Tree): Any = singletonObject(objectName(tree))

}
