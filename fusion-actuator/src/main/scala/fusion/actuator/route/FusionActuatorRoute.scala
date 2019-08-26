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
import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.StrictLogging
import fusion.actuator.setting.ActuatorSetting
import fusion.json.jackson.Jackson
import helloscala.common.util.Utils

import scala.collection.JavaConverters._

case class Item(href: String, templated: Boolean)

class FusionActuatorRoute(system: ExtendedActorSystem, actuatorSetting: ActuatorSetting) extends StrictLogging {
  private val components = system.settings.config.getStringList("fusion.actuator.routes").asScala.flatMap { fqcn =>
    Utils.try2option(
      system.dynamicAccess.createInstanceFor[ActuatorRoute](fqcn, List(classOf[ExtendedActorSystem] -> system)),
      e => logger.error(s"创建实例失败，fqcn: $fqcn", e))
  }
  private val routes = components.map(_.aroundRoute)
  private def links(request: HttpRequest): Map[String, Item] =
    components.map { comp =>
      val href = request.uri
        .copy(path = Uri.Path(s"/${actuatorSetting.contextPath}/${comp.name}"), rawQueryString = None, fragment = None)
        .toString()
      comp.name -> Item(href, comp.isTemplated)
    }.toMap

  def route: Route =
    pathPrefix(actuatorSetting.contextPath) {
      pathEndOrSingleSlash {
        extractRequest { request =>
          complete {
            HttpEntity(ContentTypes.`application/json`, Jackson.stringify(Map("_links" -> links(request))))
          }
        }
      } ~
      concat(routes: _*)
    }
}
