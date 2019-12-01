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

import akka.actor.Address
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors, TimerScheduler }
import akka.actor.typed.{ ActorRef, Behavior, PostStop, PreRestart }
import akka.cluster.ClusterEvent.MemberEvent
import akka.cluster.pubsub.{ DistributedPubSub, DistributedPubSubMediator }
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.{ ClusterSharding, Entity }
import akka.cluster.typed.{ Cluster, Subscribe }
import akka.cluster.{ Member, MemberStatus }
import fusion.schedulerx.protocol.broker.{ BrokerCommand, BrokerCommandNS, RegisterWorker, WorkerStatus }
import fusion.schedulerx.protocol.worker.{ RegisterToBrokerAck, WorkerCommand, WorkerProtocol }
import fusion.schedulerx.server.protocol.BrokerInfo
import fusion.schedulerx.worker.Worker
import fusion.schedulerx.{ NodeRoles, SchedulerXSettings, Topics }

object Broker {
  trait InternalCommand extends BrokerCommand
  private final case object InitSuccess extends InternalCommand
  private final case class InitFailure(e: Throwable) extends InternalCommand
  private case class InternalClusterEvent(event: MemberEvent) extends InternalCommand
  private case class RemoveWorkerByAddress(address: Address) extends InternalCommand
  case class InitParameters(namespace: String, payload: BrokerInfo) extends BrokerCommandNS

  def apply(brokerId: String, brokerSettings: BrokerSettings, settings: SchedulerXSettings): Behavior[BrokerCommand] =
    Behaviors.setup(context =>
      Behaviors.withTimers { timers =>
        new Broker(brokerId, brokerSettings, settings, timers, context).idle()
      })
}

import akka.actor.typed.scaladsl.adapter._
import fusion.schedulerx.server.Broker._
class Broker(
    brokerId: String,
    brokerSettings: BrokerSettings,
    settings: SchedulerXSettings,
    timers: TimerScheduler[BrokerCommand],
    context: ActorContext[BrokerCommand]) {
  private val cluster = Cluster(context.system)
  private val clusterAdapter = context.messageAdapter(msg => InternalClusterEvent(msg))
  private val sharding = ClusterSharding(context.system)
  private val mediator = DistributedPubSub(context.system.toClassic).mediator

  mediator ! DistributedPubSubMediator.Subscribe(Topics.REGISTER_WORKER, context.self.toClassic)
  cluster.subscriptions ! Subscribe(clusterAdapter, classOf[MemberEvent])
  for (member <- cluster.state.members if member.status == MemberStatus.Up) {
    if (member.roles(NodeRoles.WORKER)) {
      context.self ! InitSuccess
    }
  }

  def receive(
      workers: Map[String, WorkerStatus],
      workerRegion: ActorRef[ShardingEnvelope[WorkerCommand]]): Behavior[BrokerCommand] =
    Behaviors
      .receiveMessage[BrokerCommand] {
        case worker: WorkerStatus if worker.namespace == brokerId =>
          val items = workers.updated(worker.status.workerId, worker)
          context.log.info(s"workers size: ${items.size} $worker")
          receive(items, workerRegion)

        case RemoveWorkerByAddress(address) =>
          receive(workers.filterNot { case (_, worker) => worker.worker.path.address == address }, workerRegion)

        case RegisterWorker(counter, `brokerId`, workerId, worker) =>
          context.log.info(s"The ${counter}th worker registration, worker id is $workerId.")
          worker ! RegisterToBrokerAck(context.self)
          Behaviors.same

        case other =>
          context.log.debug(s"Invalid message: $other")
          Behaviors.same
      }
      .receiveSignal {
        case (_, PreRestart) =>
          cleanup()
          Behaviors.same
        case (_, PostStop) =>
          postStop()
          Behaviors.same
      }

  def idle(): Behavior[BrokerCommand] =
    Behaviors.withStash(1024) { stash =>
      Behaviors.receiveMessage {
        case InitSuccess =>
          val workerRegion = sharding.init(Entity(WorkerProtocol.TypeKey)(entityContext =>
            Worker(entityContext.entityId, settings)).withRole(NodeRoles.WORKER))
          context.log.info(s"Init success, become to receive(Map(), $workerRegion).")
          stash.unstashAll(receive(Map(), workerRegion))

        case worker: WorkerStatus =>
          context.log.info(s"worker status: $worker")
          if (worker.namespace == brokerId) {
            val workers = Map(worker.status.workerId -> worker)
            context.log.info(s"WorkerStatus, become to receive($workers).")
            receive(
              workers,
              sharding.init(
                Entity(WorkerProtocol.TypeKey)(entityContext => Worker(entityContext.entityId, settings))
                  .withRole(NodeRoles.WORKER)))
          } else
            Behaviors.same

        case InitFailure(e) =>
          context.log.error(s"Init failure: $e")
          Behaviors.stopped

        case InternalClusterEvent(event) =>
          val member = event.member
          if (member.status == MemberStatus.Up && member.roles(NodeRoles.WORKER)) {
            context.self ! InitSuccess
          }
          Behaviors.same

        case msg =>
          stash.stash(msg)
          Behaviors.same
      }
    }

  private def removeWorker(member: Member): Unit = {
    context.self ! RemoveWorkerByAddress(member.uniqueAddress.address)
  }

  private def cleanup(): Unit = {}

  private def postStop(): Unit = {
    cleanup()
  }
}
