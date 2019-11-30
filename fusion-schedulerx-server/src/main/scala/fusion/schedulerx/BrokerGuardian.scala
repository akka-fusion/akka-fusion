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

package fusion.schedulerx

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.ClusterEvent.{ ClusterDomainEvent, MemberEvent }
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.{ ClusterSharding, Entity }
import fusion.schedulerx.protocol.broker.{ BrokerCommand, BrokerCommandNS, BrokerProtocol }

object BrokerGuardian {
  private case class InternalClusterEvent(event: ClusterDomainEvent) extends BrokerCommand

  def apply(settings: SchedulerXSettings): Behavior[BrokerCommand] = Behaviors.setup { context =>
    var brokerIds = Set.empty[String]
    val sharding = ClusterSharding(context.system)

    val brokerRegion = sharding.init(Entity(BrokerProtocol.TypeKey)(entityContext =>
      Broker(entityContext.entityId, settings)).withRole(NodeRoles.BROKER))

    List("default").foreach(namespace => brokerRegion ! ShardingEnvelope(namespace, Broker.Tick(namespace)))

    Behaviors.receiveMessage[BrokerCommand] {
      case InternalClusterEvent(event: MemberEvent) =>
        val member = event.member
        context.log.info(s"Receive cluster member event: $member")
//        if (member.roles.contains(NodeRoles.WORKER) && member.status == MemberStatus.Up) {
//          workerRegion ! ShardingEnvelope(
//            SchedulerX.getWorkerId(member.address),
//            GetWorkerStatus(sharding.entityRefFor(BrokerProtocol.TypeKey, settings.namespace)))
//        }
        Behaviors.same

      case msg: BrokerCommandNS =>
        if (!brokerIds(msg.namespace)) {
          brokerIds += msg.namespace
        }
        brokerRegion ! ShardingEnvelope(msg.namespace, msg)
        Behaviors.same

      case other => // do nothing
        context.log.warn(s"Other message: $other")
        Behaviors.same
    }
  }
}
