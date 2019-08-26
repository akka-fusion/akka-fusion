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

package fusion.actuator.route

import akka.actor.ExtendedActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.StrictLogging
import fusion.core.model.Health
import fusion.core.model.HealthComponent
import fusion.json.jackson.http.JacksonHttpUtils
import helloscala.common.util.Utils

import scala.collection.JavaConverters._

final class HealthRoute(val system: ExtendedActorSystem) extends ActuatorRoute with StrictLogging {
  override val name = "health"

  private val healths = system.settings.config
    .getStringList("fusion.actuator.health.components")
    .iterator()
    .asScala
    .flatMap { fqcn =>
      Utils.try2option(
        system.dynamicAccess.getObjectFor[HealthComponent](fqcn),
        e => logger.error(s"获取object失败，fqcn: $fqcn", e))
    }
    .map(comp => comp.name -> comp)
    .toMap

  def route: Route =
    pathEndOrSingleSlash {
      complete(JacksonHttpUtils.httpEntity(Health.up(healths.mapValues(_.health))))
    } ~
    pathPrefix(Segment) { comp =>
      pathEndOrSingleSlash {
        healths.get(comp) match {
          case Some(health) => complete(JacksonHttpUtils.httpEntity(health))
          case _            => complete(StatusCodes.NotFound)
        }
      } ~
      path(Segment) { instance =>
        val maybe = healths.get(comp) match {
          case Some(health: HealthComponent) => health.health.details.get(instance)
          case _                             => None
        }
        maybe match {
          case Some(v) => complete(JacksonHttpUtils.httpEntity(v))
          case _       => complete(StatusCodes.NotFound)
        }
      }
    }

}
