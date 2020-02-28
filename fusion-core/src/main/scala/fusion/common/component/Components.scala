/*
 * Copyright 2019 helloscala.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fusion.common.component

import akka.Done
import com.typesafe.scalalogging.StrictLogging
import helloscala.common.Configuration

import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration._

// #Components
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
    val isReplace = configuration.getOrElse(id + ".replace-exists", replaceExists)
    components.get(id).foreach {
      case c if isReplace =>
        try {
          Await.ready(componentClose(c), 30.seconds)
        } catch {
          case e: Throwable =>
            logger.error(s"registerComponent replace exists component 30s timeout error: ${e.toString}；id: $id", e)
        }
        components.remove(id)
      case _ =>
        throw new IllegalAccessException(s"id重复，$id")
    }
    components.put(id, other)
    other
  }

  def closeAsync()(implicit ec: ExecutionContext): Future[Done] = synchronized {
    Future.sequence(components.valuesIterator.map(componentClose).toList).map(_ => Done)
  }
}
// #Components
