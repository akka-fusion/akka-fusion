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

package fusion.discoveryx.name.service

import akka.NotUsed
import akka.stream.scaladsl.Source
import fusion.discoveryx.grpc.NamingService
import fusion.discoveryx.model.{
  InstanceHeartbeat,
  InstanceModify,
  InstanceQuery,
  InstanceRegister,
  InstanceRemove,
  InstanceReply,
  ServerStatusBO,
  ServerStatusQuery
}

import scala.concurrent.Future

class NamingServiceImpl extends NamingService {
  /**
   * 查询服务状态
   */
  override def serverStatus(in: ServerStatusQuery): Future[ServerStatusBO] = ???

  /**
   * 添加实例
   */
  override def registerInstance(in: InstanceRegister): Future[InstanceReply] = ???

  /**
   * 修改实例
   */
  override def modifyInstance(in: InstanceModify): Future[InstanceReply] = ???

  /**
   * 删除实例
   */
  override def removeInstance(in: InstanceRemove): Future[InstanceReply] = ???

  /**
   * 查询实例
   */
  override def queryInstance(in: InstanceQuery): Future[InstanceReply] = ???

  override def heartbeat(in: Source[InstanceHeartbeat, NotUsed]): Source[ServerStatusBO, NotUsed] = ???
}
