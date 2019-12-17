/*
 * Copyright 2019 akka-fusion.com
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

package fusion.actuator

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.StrictLogging
import fusion.actuator.route.FusionActuatorRoute
import fusion.actuator.setting.ActuatorSetting
import fusion.common.extension.{ FusionExtension, FusionExtensionId }
import helloscala.common.Configuration

final class FusionActuator private (override val system: ActorSystem[_]) extends FusionExtension with StrictLogging {
  val actuatorSetting = ActuatorSetting(Configuration(system.settings.config))
  def route: Route = new FusionActuatorRoute(system, actuatorSetting).route
}

object FusionActuator extends FusionExtensionId[FusionActuator] {
  override def createExtension(system: ActorSystem[_]): FusionActuator = new FusionActuator(system)
}
