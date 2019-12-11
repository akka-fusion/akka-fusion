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
import akka.actor.Cancellable
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter._
import akka.grpc.GrpcClientSettings
import akka.stream.scaladsl.Source
import com.typesafe.scalalogging.StrictLogging
import fusion.common.extension.FusionCoordinatedShutdown
import fusion.discoveryx.common.{ Constants, Headers }
import fusion.discoveryx.grpc.{ NamingService, NamingServiceClient }
import fusion.discoveryx.model._
import helloscala.common.IntStatus

import scala.concurrent.Future
import scala.util.{ Failure, Success }

object DiscoveryXNamingClient {
  case class InstanceKey(namespace: String, serviceName: String, ip: String, port: Int)
  object InstanceKey {
    def apply(in: Instance): InstanceKey = InstanceKey(in.namespace, in.serviceName, in.ip, in.port)
    def apply(in: InstanceRemove): InstanceKey = InstanceKey(in.namespace, in.serviceName, in.ip, in.port)
  }

  def apply(system: ActorSystem[_]): DiscoveryXNamingClient = {
    implicit val classicSystem = system.toClassic
    import system.executionContext
    apply(NamingClientSettings(system), NamingServiceClient(GrpcClientSettings.fromConfig(NamingService.name)))(system)
  }

  def apply(settings: NamingClientSettings, naming: NamingServiceClient)(
      implicit system: ActorSystem[_]): DiscoveryXNamingClient =
    new DiscoveryXNamingClient(settings, naming)(system)
}

class DiscoveryXNamingClient private (val settings: NamingClientSettings, val namingClient: NamingServiceClient)(
    implicit system: ActorSystem[_])
    extends StrictLogging {
  import DiscoveryXNamingClient._
  private var heartbeatInstances = Map[InstanceKey, Cancellable]()
  logger.info(s"DiscoveryXNamingClient instanced: $settings")

  FusionCoordinatedShutdown(system).beforeServiceUnbind("DiscoveryXNamingService") { () =>
    for ((_, cancellable) <- heartbeatInstances if !cancellable.isCancelled) {
      cancellable.cancel
    }
    namingClient.close()
  }

  /**
   * 查询服务状态
   */
  def serverStatus(in: ServerStatusQuery): Future[ServerStatusBO] = namingClient.serverStatus(in)

  /**
   * 添加实例
   */
  def registerInstance(in: InstanceRegister): Future[InstanceReply] = {
    import system.executionContext
    namingClient.registerInstance(in).andThen {
      case Success(reply) if reply.status == IntStatus.OK && reply.data.isRegistered =>
        val inst = reply.data.registered.get
        val (cancellable, source) = Source
          .tick(settings.heartbeatInterval, settings.heartbeatInterval, InstanceHeartbeat(inst.instanceId))
          .preMaterialize()
        val key = InstanceKey(inst)
        heartbeatInstances.get(key).foreach(_.cancel())
        heartbeatInstances = heartbeatInstances.updated(key, cancellable)
        logger.info(s"注册实例成功，开始心跳调度。${settings.heartbeatInterval} | $inst")
        heartbeat(source, inst).runForeach(bo => logger.debug(s"Received: $bo"))
      case Success(reply) =>
        logger.warn(s"注册实例错误，返回：$reply")
      case Failure(exception) =>
        logger.error(s"注册实例失败：$exception")
    }
  }

  /**
   * 修改实例
   */
  def modifyInstance(in: InstanceModify): Future[InstanceReply] = namingClient.modifyInstance(in)

  /**
   * 删除实例
   */
  def removeInstance(in: InstanceRemove): Future[InstanceReply] = {
    val key = InstanceKey(in)
    heartbeatInstances.get(key).foreach(_.cancel())
    heartbeatInstances -= key
    namingClient.removeInstance(in)
  }

  /**
   * 查询实例
   */
  def queryInstance(in: InstanceQuery): Future[InstanceReply] = namingClient.queryInstance(in)

  private def heartbeat(in: Source[InstanceHeartbeat, NotUsed], inst: Instance): Source[ServerStatusBO, NotUsed] = {
    println("add header " + inst.serviceName)
    namingClient
      .heartbeat()
      .addHeader(Headers.NAMESPACE, inst.namespace)
      .addHeader(Headers.SERVICE_NAME, inst.serviceName)
      .addHeader(Headers.IP, inst.ip)
      .addHeader(Headers.PORT, inst.port.toString)
      .invoke(in)
  }
}
