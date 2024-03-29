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
import akka.http.scaladsl.server.Route
import fusion.http.server.AbstractRoute

trait ActuatorRoute extends AbstractRoute {
  val system: ExtendedActorSystem
  val name: String
  def isTemplated: Boolean = false

  final def aroundRoute: Route = pathPrefix(name) { route }
}
