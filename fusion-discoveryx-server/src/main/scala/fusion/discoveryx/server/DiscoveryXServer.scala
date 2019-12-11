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

import akka.actor.typed.{ ActorRef, SupervisorStrategy }
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.sharding.typed.scaladsl.{ ClusterSharding, Entity }
import akka.grpc.scaladsl.ServiceHandler
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ HttpRequest, HttpResponse }
import akka.http.scaladsl.server.Route
import akka.stream.SystemMaterializer
import akka.{ actor => classic }
import com.typesafe.config.{ Config, ConfigFactory }
import com.typesafe.scalalogging.StrictLogging
import fusion.core.extension.FusionCore
import fusion.discoveryx.DiscoveryX
import fusion.discoveryx.grpc.{
  ConfigServiceHandler,
  ConfigServicePowerApiHandler,
  NamingServiceHandler,
  NamingServicePowerApiHandler
}
import fusion.discoveryx.common.Constants
import fusion.discoveryx.server.config.{ ConfigManager, ConfigServiceImpl, ConfigSetting }
import fusion.discoveryx.server.naming.{ NamingProxy, NamingServiceImpl, NamingSettings, Namings }
import fusion.discoveryx.server.route.Routes
import helloscala.common.Configuration
import helloscala.common.config.FusionConfigFactory

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{ Failure, Success }

class DiscoveryXServer private (discoveryX: DiscoveryX) extends StrictLogging {
  FusionCore(discoveryX.system)
  val configSetting = new ConfigSetting(Configuration(discoveryX.config))
  val namingSetting = new NamingSettings(Configuration(discoveryX.config))
  val grpcHandler: HttpRequest => Future[HttpResponse] = {
    implicit val system = discoveryX.system
    implicit val mat = SystemMaterializer(system).materializer
    implicit val classicSystem = discoveryX.classicSystem
    val services = List(
      if (configSetting.enable) {
        val configManager: ActorRef[ConfigManager.Command] = discoveryX.spawnActorSync(
          Behaviors.supervise(ConfigManager()).onFailure(SupervisorStrategy.restart),
          ConfigManager.NAME,
          2.seconds)
        Some(ConfigServiceHandler.partial(new ConfigServiceImpl(configManager)))
      } else None,
      if (namingSetting.enable) {
        val namingProxy = NamingProxy.create(discoveryX)
        Some(NamingServicePowerApiHandler.partial(new NamingServiceImpl(namingProxy)))
      } else None).flatten
    require(services.nonEmpty, "未找到任何 gRPC 服务")

    ServiceHandler.concatOrNotFound(services: _*)
  }

  def start(): Unit = {
    startHttp()(discoveryX.classicSystem)
  }

  private def startHttp()(implicit system: classic.ActorSystem): Unit = {
    val route = new Routes(grpcHandler).route
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
