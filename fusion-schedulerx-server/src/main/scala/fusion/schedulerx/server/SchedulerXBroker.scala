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

package fusion.schedulerx.server

import java.util.concurrent.TimeoutException

import akka.actor.typed.{ ActorSystem, SupervisorStrategy }
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.{ ClusterSharding, Entity }
import com.typesafe.config.{ Config, ConfigFactory }
import fusion.common.FusionProtocol
import fusion.schedulerx._
import fusion.schedulerx.protocol.Broker
import fusion.schedulerx.server.repository.BrokerRepository
import fusion.schedulerx.server.service.BrokerService

class SchedulerXBroker(schedulerX: SchedulerX) {
  implicit val system: ActorSystem[FusionProtocol.Command] = schedulerX.system
  private val sharding = ClusterSharding(system)
  val brokerSettings: BrokerSettings = BrokerSettings(settings, schedulerX.config)
  private val brokerRegion = sharding.init(
    Entity(Broker.TypeKey)(
      entityContext =>
        Behaviors
          .supervise(BrokerImpl(entityContext.entityId, brokerSettings, settings))
          .onFailure[Exception](SupervisorStrategy.resume)).withRole(NodeRoles.BROKER))
  private var _brokerIds: Set[String] = Set()

  val brokerService = new BrokerService(brokerRegion)

  def brokerIds: Set[String] = _brokerIds

  def settings: SchedulerXSettings = schedulerX.schedulerXSettings

  @throws[TimeoutException]
  def start(): SchedulerXBroker = {
    _brokerIds = BrokerRepository(system)
      .listBroker()
      .map { broker =>
        brokerRegion ! ShardingEnvelope(broker.namespace, BrokerImpl.InitParameters(broker.namespace, broker))
        broker.namespace
      }
      .toSet

    this
  }
}

object SchedulerXBroker {
  def apply(schedulerX: SchedulerX): SchedulerXBroker = new SchedulerXBroker(schedulerX)
}
