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

package fusion.discoveryx.server.route

import akka.actor
import akka.actor.typed.{ ActorRef, ActorSystem, SupervisorStrategy }
import akka.actor.typed.scaladsl.Behaviors
import akka.grpc.scaladsl.ServiceHandler
import akka.http.scaladsl.model.{ HttpRequest, HttpResponse }
import akka.http.scaladsl.server.Directives.{ complete, extractRequest, onSuccess }
import akka.http.scaladsl.server.Route
import akka.stream.{ Materializer, SystemMaterializer }
import fusion.common.FusionProtocol
import fusion.discoveryx.DiscoveryX
import fusion.discoveryx.grpc.{ ConfigServiceHandler, NamingServicePowerApiHandler }
import fusion.discoveryx.server.config.{ ConfigManager, ConfigServiceImpl, ConfigSettings }
import fusion.discoveryx.server.naming.{ NamingManager, NamingServiceImpl, NamingSettings }

import scala.concurrent.Future
import scala.concurrent.duration._

class GrpcRoute(discoveryX: DiscoveryX, configSettings: ConfigSettings, namingSettings: NamingSettings) {
  def route: Route = extractRequest { request =>
    onSuccess(grpcHandler(request)) { response =>
      complete(response)
    }
  }

  private val grpcHandler: HttpRequest => Future[HttpResponse] = {
    implicit val system: ActorSystem[FusionProtocol.Command] = discoveryX.system
    implicit val mat: Materializer = SystemMaterializer(system).materializer
    implicit val classicSystem: actor.ActorSystem = discoveryX.classicSystem
    val services = List(
      if (configSettings.enable) {
        val configManager: ActorRef[ConfigManager.Command] = discoveryX.spawnActorSync(
          Behaviors.supervise(ConfigManager()).onFailure(SupervisorStrategy.restart),
          ConfigManager.NAME,
          2.seconds)
        Some(ConfigServiceHandler.partial(new ConfigServiceImpl(configManager)))
      } else None,
      if (namingSettings.enable) {
        val namingProxy = NamingManager.init(discoveryX.system)
        Some(NamingServicePowerApiHandler.partial(new NamingServiceImpl(namingProxy)))
      } else None).flatten
    require(services.nonEmpty, "未找到任何 gRPC 服务")

    ServiceHandler.concatOrNotFound(services: _*)
  }
}
