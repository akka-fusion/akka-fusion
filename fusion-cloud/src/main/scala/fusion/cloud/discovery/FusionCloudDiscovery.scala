/*
 * Copyright 2019-2021 helloscala.com
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

package fusion.cloud.discovery

import akka.actor.typed.{ ActorRef, Extension }
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
import akka.pattern.StatusReply

/**
 * @author Yang Jing <a href="mailto:yang.xunjing@qq.com">yangbajing</a>
 * @since 2020-12-02 12:08:17
 */
trait FusionCloudDiscovery extends Extension {
  def register(servInst: ServiceInstance): ServiceInstance

  def configureServiceInstance(instance: Option[ServiceInstance]): ServiceInstance
}

object FusionCloudDiscovery {
  trait Command
  val TypeKey: EntityTypeKey[Command] = EntityTypeKey("FusionCloudDiscovery")

  case class GetRegistration(replyTo: ActorRef[StatusReply[Registration]]) extends Command

  case class AddRegistration(registration: Registration) extends Command
}

abstract class Registration {}
