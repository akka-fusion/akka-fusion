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

package fusion.schedulerx.worker

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors, TimerScheduler }
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.{ ClusterSharding, Entity }
import akka.cluster.typed.Cluster
import fusion.schedulerx.protocol.worker.WorkerProtocol
import fusion.schedulerx.{ NodeRoles, SchedulerX, SchedulerXSettings }

object WorkerGuardian {
  sealed trait Command

//  private final case class BrokerListing(listing: Receptionist.Listing) extends Command

  def apply(settings: SchedulerXSettings): Behavior[Command] =
    Behaviors.setup(context =>
      Behaviors.withTimers { timers =>
        new WorkerGuardian(settings, timers, context).init()
      })
}

import fusion.schedulerx.worker.WorkerGuardian._
class WorkerGuardian private (
    settings: SchedulerXSettings,
    timers: TimerScheduler[Command],
    context: ActorContext[Command]) {
  private val sharding = ClusterSharding(context.system)
  private val workerRegion = sharding.init(Entity(WorkerProtocol.TypeKey)(entityContext =>
    Worker(entityContext.entityId, settings)).withRole(NodeRoles.WORKER))

  workerRegion ! ShardingEnvelope(
    SchedulerX.getWorkerId(Cluster(context.system).selfMember.address),
    Worker.ReportSystemStatus)

  def init(): Behavior[Command] = Behaviors.receiveMessagePartial[Command] { msg =>
    context.log.info(s"Receive message: $msg")
    Behaviors.same
  }
}
