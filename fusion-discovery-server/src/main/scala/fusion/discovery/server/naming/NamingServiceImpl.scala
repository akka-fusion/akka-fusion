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

package fusion.discovery.server.naming

import akka.grpc.scaladsl.Metadata
import fusion.discovery.grpc.NamingServicePowerApi
import fusion.discovery.model._
import fusion.discovery.server.DiscoveryServer

import scala.concurrent.Future

class NamingServiceImpl(server: DiscoveryServer) extends NamingServicePowerApi {
  override def serverStatus(in: ServerStatusQuery, metadata: Metadata): Future[ServerStatusBO] = ???

  override def addInstance(in: InstanceAdd, metadata: Metadata): Future[InstanceAdded] = ???

  override def removeInstance(in: InstanceRemove, metadata: Metadata): Future[InstanceRemoved] = ???

  override def queryInstance(in: InstanceQuery, metadata: Metadata): Future[InstanceQueried] = ???
}
