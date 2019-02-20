package fusion.core.util

import scala.collection.mutable

trait Components[T] extends AutoCloseable {
  protected val components = mutable.Map.empty[String, T]
  val component: T

  final def lookup(id: String): T = synchronized(lookupComponent(id))

  protected def lookupComponent(id: String): T

  final def register(id: String, other: T, replaceExists: Boolean = false): T =
    synchronized(registerComponent(id, other, replaceExists))

  protected def registerComponent(id: String, other: T, replaceExists: Boolean): T
}
