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

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorSystem, SupervisorStrategy }
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.{ ClusterSharding, Entity }
import fusion.common.FusionProtocol
import fusion.schedulerx._
import fusion.schedulerx.server.repository.BrokerRepository

class SchedulerXBroker(schedulerX: SchedulerX) {
  implicit val system: ActorSystem[FusionProtocol.Command] = schedulerX.system
  private var _guardianIds: Set[String] = Set()

  def brokerIds: Set[String] = _guardianIds

  @throws[TimeoutException]
  def start(): SchedulerXBroker = {
    val guardianRegion = ClusterSharding(system).init(
      Entity(BrokerGuardian.TypeKey)(entityContext =>
        Behaviors.supervise(BrokerGuardian(entityContext.entityId)).onFailure[Exception](SupervisorStrategy.resume))
        .withRole(NodeRoles.BROKER))
    _guardianIds = BrokerRepository(system)
      .listBroker()
      .map { brokerInfo =>
        guardianRegion ! ShardingEnvelope(
          brokerInfo.namespace,
          BrokerGuardian.BrokerCommand(BrokerImpl.InitParameters(brokerInfo.namespace, brokerInfo)))
        brokerInfo.namespace
      }
      .toSet
    SchedulerXBroker._instance = this
    this
  }
}

object SchedulerXBroker {
  private var _instance: SchedulerXBroker = _

  def instance: SchedulerXBroker = _instance
  def apply(schedulerX: SchedulerX): SchedulerXBroker = new SchedulerXBroker(schedulerX)
}
