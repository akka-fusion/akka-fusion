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
import com.typesafe.scalalogging.StrictLogging
import fusion.common.extension.FusionCoordinatedShutdown
import fusion.discoveryx.grpc.{ NamingService, NamingServiceClient }
import fusion.discoveryx.model._

import scala.concurrent.Future

object DiscoveryXNamingClient {
  def apply(system: ActorSystem[_]): DiscoveryXNamingClient = {
    implicit val classicSystem = system.toClassic
    import system.executionContext
    val settings = GrpcClientSettings.fromConfig(NamingService.name)
    apply(NamingServiceClient(settings))(system)
  }
  def apply(naming: NamingServiceClient)(implicit system: ActorSystem[_]): DiscoveryXNamingClient =
    new DiscoveryXNamingClient(naming)(system)
}

class DiscoveryXNamingClient private (val namingClient: NamingServiceClient)(implicit system: ActorSystem[_])
    extends StrictLogging {
  FusionCoordinatedShutdown(system).beforeServiceUnbind("DiscoveryXNamingService") { () =>
    namingClient.close()
  }

  /**
   * 查询服务状态
   */
  def serverStatus(in: ServerStatusQuery): Future[ServerStatusBO] = namingClient.serverStatus(in)

  /**
   * 添加实例
   */
  def registerInstance(in: InstanceRegister): Future[InstanceReply] = namingClient.registerInstance(in)

  /**
   * 修改实例
   */
  def modifyInstance(in: InstanceModify): Future[InstanceReply] = namingClient.modifyInstance(in)

  /**
   * 删除实例
   */
  def removeInstance(in: InstanceRemove): Future[InstanceReply] = namingClient.removeInstance(in)

  /**
   * 查询实例
   */
  def queryInstance(in: InstanceQuery): Future[InstanceReply] = namingClient.queryInstance(in)

  def heartbeat(in: Source[InstanceHeartbeat, NotUsed]): Source[ServerStatusBO, NotUsed] = namingClient.heartbeat(in)
}
