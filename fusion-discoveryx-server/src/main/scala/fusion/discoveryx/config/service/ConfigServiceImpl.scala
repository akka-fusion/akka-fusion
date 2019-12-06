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

package fusion.discoveryx.config.service

import akka.NotUsed
import akka.stream.scaladsl.Source
import fusion.discoveryx.grpc.ConfigService
import fusion.discoveryx.model.{
  ConfigChangeListen,
  ConfigChanged,
  ConfigPublish,
  ConfigQuery,
  ConfigRemove,
  ConfigReply,
  ServerStatusBO,
  ServerStatusQuery
}

import scala.concurrent.Future

class ConfigServiceImpl extends ConfigService {
  /**
   * 查询服务状态
   */
  override def serverStatus(in: ServerStatusQuery): Future[ServerStatusBO] = ???

  /**
   * 查询配置
   */
  override def queryConfig(in: ConfigQuery): Future[ConfigReply] = ???

  /**
   * 发布配置
   */
  override def publishConfig(in: ConfigPublish): Future[ConfigReply] = ???

  /**
   * 删除配置
   */
  override def removeConfig(in: ConfigRemove): Future[ConfigReply] = ???

  /**
   * 监听配置变化
   */
  override def listenerConfig(in: ConfigChangeListen): Source[ConfigChanged, NotUsed] = ???
}
