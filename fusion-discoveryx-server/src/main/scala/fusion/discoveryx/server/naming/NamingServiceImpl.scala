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
import akka.grpc.scaladsl.Metadata
import akka.stream.scaladsl.{ Sink, Source }
import akka.util.Timeout
import fusion.discoveryx.common.Headers
import fusion.discoveryx.grpc.NamingServicePowerApi
import fusion.discoveryx.model._
import helloscala.common.IntStatus
import helloscala.common.util.StringUtils

import scala.concurrent.Future
import scala.concurrent.duration._

class NamingServiceImpl(namingProxy: ActorRef[Namings.Command])(implicit system: ActorSystem[_])
    extends NamingServicePowerApi {
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
    namingProxy.ask[InstanceReply](replyTo => Namings.RegisterInstance(in, replyTo)).recover {
      case _: TimeoutException => InstanceReply(IntStatus.GATEWAY_TIMEOUT)
    }
  }

  /**
   * 修改实例
   */
  override def modifyInstance(in: InstanceModify, metadata: Metadata): Future[InstanceReply] = {
    namingProxy.ask[InstanceReply](replyTo => Namings.ModifyInstance(in, replyTo)).recover {
      case _: TimeoutException => InstanceReply(IntStatus.GATEWAY_TIMEOUT)
    }
  }

  /**
   * 删除实例
   */
  override def removeInstance(in: InstanceRemove, metadata: Metadata): Future[InstanceReply] = {
    namingProxy.ask[InstanceReply](replyTo => Namings.RemoveInstance(in, replyTo)).recover {
      case _: TimeoutException => InstanceReply(IntStatus.GATEWAY_TIMEOUT)
    }
  }

  /**
   * 查询实例
   */
  override def queryInstance(in: InstanceQuery, metadata: Metadata): Future[InstanceReply] = {
    namingProxy.ask[InstanceReply](replyTo => Namings.QueryInstance(in, replyTo)).recover {
      case _: TimeoutException => InstanceReply(IntStatus.GATEWAY_TIMEOUT)
    }
  }

  override def heartbeat(
      in: Source[InstanceHeartbeat, NotUsed],
      metadata: Metadata): Source[ServerStatusBO, NotUsed] = {
    (for {
      namespace <- metadata.getText(Headers.NAMESPACE)
      serviceName <- metadata.getText(Headers.SERVICE_NAME)
    } yield {
      in.map { cmd =>
        namingProxy ! Namings.Heartbeat(cmd, namespace, serviceName)
        ServerStatusBO(IntStatus.OK)
      }
    }).getOrElse {
      in.runWith(Sink.ignore)
      Source.single(ServerStatusBO(IntStatus.BAD_REQUEST))
    }
  }

  @inline private def checkHeartbeat(v: InstanceHeartbeat): Boolean = {
    StringUtils.isNoneEmpty(v.instanceId)
  }
}
