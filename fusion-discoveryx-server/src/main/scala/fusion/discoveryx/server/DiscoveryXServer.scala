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
import com.typesafe.scalalogging.StrictLogging
import fusion.core.extension.FusionCore
import fusion.discoveryx.DiscoveryX
import fusion.discoveryx.common.Constants
import fusion.discoveryx.server.config.ConfigSettings
import fusion.discoveryx.server.naming.NamingSettings
import fusion.discoveryx.server.route.Routes
import helloscala.common.Configuration
import helloscala.common.config.FusionConfigFactory

import scala.util.{ Failure, Success }

class DiscoveryXServer private (discoveryX: DiscoveryX) extends StrictLogging {
  FusionCore(discoveryX.system)
  val configSettings = new ConfigSettings(Configuration(discoveryX.config))
  val namingSettings = new NamingSettings(Configuration(discoveryX.config))

  def start(): Unit = {
    startHttp()(discoveryX.classicSystem)
  }

  private def startHttp()(implicit system: classic.ActorSystem): Unit = {
    val route = new Routes(discoveryX, configSettings, namingSettings).route
    val config = discoveryX.config
    Http()
      .bindAndHandleAsync(
        Route.asyncHandler(route),
        config.getString("fusion.http.default.server.host"),
        config.getInt("fusion.http.default.server.port"))
      .onComplete {
        case Success(value)     => logger.info(s"HTTP started, bind to $value")
        case Failure(exception) => logger.error(s"HTTP start failure. $exception")
      }(system.dispatcher)
  }
}

object DiscoveryXServer {
  def apply(discoveryX: DiscoveryX): DiscoveryXServer = new DiscoveryXServer(discoveryX)
  def apply(config: Config): DiscoveryXServer = apply(DiscoveryX.fromMergedConfig(config))
  def apply(): DiscoveryXServer =
    apply(FusionConfigFactory.arrangeConfig(ConfigFactory.load(), Constants.DISCOVERYX))
}
