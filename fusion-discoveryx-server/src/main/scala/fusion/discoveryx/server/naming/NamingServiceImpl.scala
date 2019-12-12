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

package fusion.discoveryx.server.naming

import java.util.concurrent.TimeoutException

import akka.NotUsed
import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ ActorRef, ActorSystem }
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.grpc.scaladsl.Metadata
import akka.stream.scaladsl.Source
import akka.util.Timeout
import com.typesafe.scalalogging.StrictLogging
import fusion.discoveryx.common.Headers
import fusion.discoveryx.grpc.NamingServicePowerApi
import fusion.discoveryx.model._
import fusion.discoveryx.server.protocol._
import helloscala.common.IntStatus
import helloscala.common.exception.HSBadRequestException

import scala.concurrent.Future
import scala.concurrent.duration._

class NamingServiceImpl(namingProxy: ActorRef[ShardingEnvelope[Namings.Command]])(implicit system: ActorSystem[_])
    extends NamingServicePowerApi
    with StrictLogging {
  import system.executionContext
  implicit private val timeout: Timeout = 5.seconds

  /**
   * 查询服务状态
   */
  override def serverStatus(in: ServerStatusQuery, metadata: Metadata): Future[ServerStatusBO] = {
    Future.successful(ServerStatusBO(IntStatus.OK))
  }

  /**
   * 添加实例
   */
  override def registerInstance(in: InstanceRegister, metadata: Metadata): Future[InstanceReply] = {
    namingProxy.ask[InstanceReply](replyTo => ShardingEnvelope(in.namespace, RegisterInstance(in, replyTo))).recover {
      case _: TimeoutException => InstanceReply(IntStatus.GATEWAY_TIMEOUT)
    }
  }

  /**
   * 修改实例
   */
  override def modifyInstance(in: InstanceModify, metadata: Metadata): Future[InstanceReply] = {
    namingProxy.ask[InstanceReply](replyTo => ShardingEnvelope(in.namespace, ModifyInstance(in, replyTo))).recover {
      case _: TimeoutException => InstanceReply(IntStatus.GATEWAY_TIMEOUT)
    }
  }

  /**
   * 删除实例
   */
  override def removeInstance(in: InstanceRemove, metadata: Metadata): Future[InstanceReply] = {
    namingProxy.ask[InstanceReply](replyTo => ShardingEnvelope(in.namespace, RemoveInstance(in, replyTo))).recover {
      case _: TimeoutException => InstanceReply(IntStatus.GATEWAY_TIMEOUT)
    }
  }

  /**
   * 查询实例
   */
  override def queryInstance(in: InstanceQuery, metadata: Metadata): Future[InstanceReply] = {
    namingProxy.ask[InstanceReply](replyTo => ShardingEnvelope(in.namespace, QueryInstance(in, replyTo))).recover {
      case _: TimeoutException => InstanceReply(IntStatus.GATEWAY_TIMEOUT)
    }
  }

  override def heartbeat(
      in: Source[InstanceHeartbeat, NotUsed],
      metadata: Metadata): Source[ServerStatusBO, NotUsed] = {
    try {
      val namespace = metadata
        .getText(Headers.NAMESPACE)
        .getOrElse(throw HSBadRequestException(s"Request header missing, need '${Headers.NAMESPACE}'."))
      val serviceName = metadata
        .getText(Headers.SERVICE_NAME)
        .getOrElse(throw HSBadRequestException(s"Request header missing, need '${Headers.SERVICE_NAME}'."))
      in.map { cmd =>
        namingProxy ! ShardingEnvelope(namespace, Heartbeat(cmd, namespace, serviceName))
        ServerStatusBO(IntStatus.OK)
      }
    } catch {
      case e: Exception =>
        logger.warn(s"Receive heartbeat message error: $e")
        Source.single(ServerStatusBO(IntStatus.BAD_REQUEST))
    }
  }
}
