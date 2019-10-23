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

package fusion.discovery.server.config

import akka.NotUsed
import akka.grpc.scaladsl.Metadata
import akka.stream.scaladsl.Source
import fusion.discovery.grpc.ConfigServicePowerApi
import fusion.discovery.model._
import fusion.discovery.server.DiscoveryServer

import scala.concurrent.Future

class ConfigServiceImpl(server: DiscoveryServer) extends ConfigServicePowerApi {
  override def serverStatus(in: ServerStatusQuery, metadata: Metadata): Future[ServerStatusBO] = ???

  override def queryConfig(in: ConfigQuery, metadata: Metadata): Future[ConfigBO] = ???

  override def addConfig(in: ConfigAdd, metadata: Metadata): Future[ConfigAdded] = ???

  override def removeConfig(in: ConfigRemove, metadata: Metadata): Future[ConfigRemoved] = ???

  override def listenerConfig(in: ConfigChangeListen, metadata: Metadata): Source[ConfigChanged, NotUsed] = ???
}
