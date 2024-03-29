/*
 * Copyright 2019-2021 helloscala.com
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

package fusion.actuator.route

import akka.actor.ExtendedActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.StrictLogging
import fusion.core.model.{ Health, HealthComponent }
import fusion.http.server.JacksonDirectives
import fusion.json.jackson.http.{ DefaultJacksonSupport, JacksonSupport }
import helloscala.common.util.Utils

import scala.jdk.CollectionConverters._

final class HealthRoute(val system: ExtendedActorSystem)
    extends ActuatorRoute
    with JacksonDirectives
    with StrictLogging {
  override val jacksonSupport: JacksonSupport = DefaultJacksonSupport
  override val name = "health"

  private val healths: Map[String, HealthComponent] = system.settings.config
    .getStringList("fusion.actuator.health.components")
    .asScala
    .view
    .flatMap { fqcn =>
      Utils.try2option(
        system.dynamicAccess.getObjectFor[HealthComponent](fqcn),
        e => logger.error(s"获取object失败，fqcn: $fqcn", e))
    }
    .map(comp => comp.name -> comp)
    .toMap

  def route: Route =
    pathEndOrSingleSlash {
      objectComplete(Health.up(healths.map { case (k, v) => k -> v.health }))
    } ~
    pathPrefix(Segment) { comp =>
      pathEndOrSingleSlash {
        healths.get(comp) match {
          case Some(health) => objectComplete(health)
          case _            => complete(StatusCodes.NotFound)
        }
      } ~
      path(Segment) { instance =>
        val maybe = healths.get(comp) match {
          case Some(health: HealthComponent) => health.health.details.get(instance)
          case _                             => None
        }
        maybe match {
          case Some(v) => objectComplete(v)
          case _       => complete(StatusCodes.NotFound)
        }
      }
    }
}
