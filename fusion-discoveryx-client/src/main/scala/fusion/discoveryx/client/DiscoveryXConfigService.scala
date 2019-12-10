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

package fusion.discoveryx.client

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter._
import akka.grpc.GrpcClientSettings
import akka.stream.scaladsl.Source
import fusion.common.extension.FusionCoordinatedShutdown
import fusion.discoveryx.grpc.{ ConfigService, ConfigServiceClient }
import fusion.discoveryx.model._

import scala.concurrent.Future

object DiscoveryXConfigService {
  def apply(system: ActorSystem[_]): DiscoveryXConfigService = {
    implicit val classicSystem = system.toClassic
    import system.executionContext
    apply(ConfigServiceClient(GrpcClientSettings.fromConfig(ConfigService.name)))(system)
  }
  def apply(config: ConfigServiceClient)(implicit system: ActorSystem[_]): DiscoveryXConfigService =
    new DiscoveryXConfigService(config)
}

class DiscoveryXConfigService private (val configClient: ConfigServiceClient)(implicit system: ActorSystem[_]) {
  FusionCoordinatedShutdown(system).beforeServiceUnbind("DiscoveryXNamingService") { () =>
    configClient.close()
  }

  /**
   * 查询服务状态
   */
  def serverStatus(in: ServerStatusQuery): Future[ServerStatusBO] = configClient.serverStatus(in)

  /**
   * 查询配置
   */
  def queryConfig(in: ConfigQuery): Future[ConfigReply] = configClient.queryConfig(in)

  /**
   * 发布配置
   */
  def publishConfig(in: ConfigPublish): Future[ConfigReply] = configClient.publishConfig(in)

  /**
   * 删除配置
   */
  def removeConfig(in: ConfigRemove): Future[ConfigReply] = configClient.removeConfig(in)

  /**
   * 监听配置变化
   */
  def listenerConfig(in: ConfigChangeListen): Source[ConfigChanged, NotUsed] = configClient.listenerConfig(in)
}
