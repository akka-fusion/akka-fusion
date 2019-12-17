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

package fusion.discovery.server

import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.SupervisorStrategy
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.cluster.sharding.typed.scaladsl.Entity
import akka.grpc.scaladsl.ServiceHandler
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpResponse
import fusion.common.extension.{ FusionExtension, FusionExtensionId }
import fusion.discovery.grpc.ConfigServiceHandler
import fusion.discovery.grpc.NamingServiceHandler
import fusion.discovery.server.config.ConfigManager
import fusion.discovery.server.config.ConfigServiceImpl
import fusion.discovery.server.config.ConfigSetting
import fusion.discovery.server.naming.NamingProxy
import fusion.discovery.server.naming.NamingServiceImpl
import fusion.discovery.server.naming.NamingSetting
import fusion.discovery.server.naming.Namings

import scala.concurrent.Future
import scala.concurrent.duration._
import akka.actor.typed.scaladsl.adapter._
import fusion.core.extension.FusionCore
class DiscoveryServer private (override val system: ActorSystem[_]) extends FusionExtension {
  val configSetting = new ConfigSetting(configuration)
  val namingSetting = new NamingSetting(configuration)

  val grpcHandler: HttpRequest => Future[HttpResponse] = {
    implicit val classicSystem = system.toClassic
    val services = List(
      if (configSetting.enable) {
        val configManager: ActorRef[ConfigManager.Command] = FusionCore(system).spawnActorSync(
          Behaviors.supervise(ConfigManager()).onFailure(SupervisorStrategy.restart),
          ConfigManager.NAME,
          2.seconds)
        Some(ConfigServiceHandler.partial(new ConfigServiceImpl(configManager, system)))
      } else None,
      if (namingSetting.enable) {
        val shardRegion =
          ClusterSharding(system).init(Entity(Namings.TypeKey)(entityContext => Namings(entityContext.entityId)))
        val namingProxy: ActorRef[Namings.Command] = FusionCore(system).spawnActorSync(
          Behaviors.supervise(NamingProxy(shardRegion)).onFailure(SupervisorStrategy.restart),
          ConfigManager.NAME,
          2.seconds)
        Some(NamingServiceHandler.partial(new NamingServiceImpl(namingProxy, system)))
      } else None).flatten
    require(services.nonEmpty, "未找到任何GRPC服务")

    ServiceHandler.concatOrNotFound(services: _*)
  }
}

object DiscoveryServer extends FusionExtensionId[DiscoveryServer] {
  override def createExtension(system: ActorSystem[_]): DiscoveryServer = new DiscoveryServer(system)
}
