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

package fusion.discovery.server

import akka.actor.typed.ActorSystem
import akka.grpc.scaladsl.ServiceHandler
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpResponse
import fusion.core.extension.FusionCore
import fusion.core.extension.FusionExtension
import fusion.core.extension.FusionExtensionId
import fusion.discovery.grpc.ConfigServicePowerApiHandler
import fusion.discovery.grpc.NamingServicePowerApiHandler
import fusion.discovery.server.config.ConfigServiceImpl
import fusion.discovery.server.config.ConfigSetting
import fusion.discovery.server.naming.NamingServiceImpl
import fusion.discovery.server.naming.NamingSetting

import scala.concurrent.Future

class DiscoveryServer private (override val system: ActorSystem[_]) extends FusionExtension {
  val configSetting = new ConfigSetting(configuration)
  val namingSetting = new NamingSetting(configuration)

  def grpcHandler: HttpRequest => Future[HttpResponse] = {
    implicit val classicSystem = FusionCore(system).classicSystem

    val services = List(
      if (configSetting.enable) Some(ConfigServicePowerApiHandler.partial(new ConfigServiceImpl(this))) else None,
      if (namingSetting.enable) Some(NamingServicePowerApiHandler.partial(new NamingServiceImpl(this))) else None).flatten
    require(services.nonEmpty, "未找到任何GRPC服务")

    ServiceHandler.concatOrNotFound(services: _*)
  }
}

object DiscoveryServer extends FusionExtensionId[DiscoveryServer] {
  override def createExtension(system: ActorSystem[_]): DiscoveryServer = new DiscoveryServer(system)
}
