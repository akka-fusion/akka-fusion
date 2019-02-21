package fusion.core.util

import com.typesafe.config.Config

import scala.collection.mutable

abstract class Components[T](DEFAULT_ID: String) extends AutoCloseable {
  protected val components = mutable.Map.empty[String, T]

  def config: Config

  protected def createComponent(id: String): T
  protected def componentClose(c: T): Unit

  val component: T = createComponent(DEFAULT_ID)

  final def lookup(id: String): T = synchronized(lookupComponent(id))

  protected def lookupComponent(id: String): T = id match {
    case DEFAULT_ID => component
    case _          => components.getOrElseUpdate(id, createComponent(id))
  }

  final def register(id: String, other: T, replaceExists: Boolean = false): T =
    synchronized(registerComponent(id, other, replaceExists))

  protected def registerComponent(id: String, other: T, replaceExists: Boolean): T = {
    require(id != DEFAULT_ID, s"id不能为默认配置ID，$id == $DEFAULT_ID")
    val beReplace =
      if (config.hasPath(id + ".replace-exists")) config.getBoolean(id + ".replace-exists") else replaceExists
    components.get(id).foreach {
      case c if beReplace =>
        componentClose(c)
        components.remove(id)
      case _ =>
        throw new IllegalAccessException(s"id重复，$id == $DEFAULT_ID")
    }
    val client = createComponent(id)
    components.put(id, client)
    client
  }

  override def close(): Unit = {
    componentClose(component)
    components.valuesIterator.foreach(componentClose)
  }

}
