package fusion.core.util

import akka.Done
import com.typesafe.scalalogging.StrictLogging
import helloscala.common.Configuration

import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration._

abstract class Components[T](DEFAULT_ID: String) extends StrictLogging {
  protected val components = mutable.Map.empty[String, T]

  def configuration: Configuration

  protected def createComponent(id: String): T
  protected def componentClose(c: T): Future[Done]

  def component: T = lookup(DEFAULT_ID)

  final def lookup(id: String): T = synchronized(lookupComponent(id))

  protected def lookupComponent(id: String): T = components.getOrElseUpdate(id, createComponent(id))

  final def register(id: String, other: T, replaceExists: Boolean = false): T =
    synchronized(registerComponent(id, other, replaceExists))

  protected def registerComponent(id: String, other: T, replaceExists: Boolean): T = {
    require(id != DEFAULT_ID, s"id不能为默认配置ID，$id == $DEFAULT_ID")
    val beReplace =
      if (configuration.hasPath(id + ".replace-exists")) configuration.getBoolean(id + ".replace-exists")
      else replaceExists
    components.get(id).foreach {
      case c if beReplace =>
        try {
          Await.ready(componentClose(c), 30.seconds)
        } catch {
          case e: Throwable =>
            logger.error(s"registerComponent replace exists component 30s timeout error: ${e.toString}；id: $id", e)
        }
        components.remove(id)
      case _ =>
        throw new IllegalAccessException(s"id重复，$id == $DEFAULT_ID")
    }
    components.put(id, other)
    other
  }

  def closeAsync()(implicit ec: ExecutionContext): Future[Done] = synchronized {
    Future.sequence(components.valuesIterator.map(componentClose).toList).map(_ => Done)
  }

}
