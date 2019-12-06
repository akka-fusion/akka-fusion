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

package fusion.discoveryx.server

import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.{ actor => classic }
import com.typesafe.config.{ Config, ConfigFactory }
import fusion.discoveryx.DiscoveryX
import fusion.discoveryx.common.Constants
import fusion.discoveryx.server.route.Routes
import helloscala.common.config.FusionConfigFactory

class DiscoveryXServer private (discoveryX: DiscoveryX) {
  def start() = {
    startHttp()(discoveryX.classicSystem)
  }

  private def startHttp()(implicit system: classic.ActorSystem): Unit = {
    val route: Route = new Routes(discoveryX).route
    val config = discoveryX.config
    Http().bindAndHandle(
      route,
      config.getString("fusion.http.default.server.host"),
      config.getInt("fusion.http.default.server.port"))
  }
}

object DiscoveryXServer {
  def apply(discoveryX: DiscoveryX): DiscoveryXServer = new DiscoveryXServer(discoveryX)
  def apply(config: Config): DiscoveryXServer = apply(DiscoveryX.fromMergedConfig(config))
  def apply(): DiscoveryXServer =
    apply(FusionConfigFactory.arrangeConfig(ConfigFactory.load(), Constants.DISCOVERYX))
}
