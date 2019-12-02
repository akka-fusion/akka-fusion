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
import akka.actor.typed.{ ActorRef, Behavior, PostStop, PreRestart, Terminated }
import akka.cluster.ClusterEvent.MemberEvent
import akka.cluster.pubsub.{ DistributedPubSub, DistributedPubSubMediator }
import akka.cluster.typed.{ Cluster, Subscribe }
import akka.cluster.{ Member, MemberStatus }
import akka.http.scaladsl.model.StatusCodes
import akka.util.Timeout
import fusion.json.jackson.CborSerializable
import fusion.schedulerx.protocol.{ Broker, JobInstanceData, Worker }
import fusion.schedulerx.server.model.JobEntity
import fusion.schedulerx.server.protocol.{ BrokerInfo, BrokerReply, TriggerJob }
import fusion.schedulerx.server.repository.BrokerRepository
import fusion.schedulerx.{ NodeRoles, SchedulerXSettings, Topics }
import helloscala.common.util.Utils

import scala.concurrent.duration._

object BrokerImpl {
  trait InternalCommand extends Broker.Command
  case class InitParameters(namespace: String, payload: BrokerInfo) extends Broker.CommandNS
  private final case object InitSuccess extends InternalCommand
  private final case class InitFailure(e: Throwable) extends InternalCommand
  private case class InternalClusterEvent(event: MemberEvent) extends InternalCommand
  private case class RemoveWorkerByAddress(address: Address) extends InternalCommand

  def apply(brokerId: String, brokerSettings: BrokerSettings, settings: SchedulerXSettings): Behavior[Broker.Command] =
    Behaviors.setup(context =>
      Behaviors.withTimers { timers =>
        new BrokerImpl(brokerId, brokerSettings, settings, timers, context).idle()
      })
}

import akka.actor.typed.scaladsl.adapter._
import fusion.schedulerx.server.BrokerImpl._
class BrokerImpl(
    brokerId: String,
    brokerSettings: BrokerSettings,
    settings: SchedulerXSettings,
    timers: TimerScheduler[Broker.Command],
    context: ActorContext[Broker.Command]) {
  private val cluster = Cluster(context.system)
  private val clusterAdapter = context.messageAdapter(msg => InternalClusterEvent(msg))
  private val mediator = DistributedPubSub(context.system.toClassic).mediator
  private val workersData = new WorkersData(settings)
  private val brokerRepository = BrokerRepository(context.system)

  mediator ! DistributedPubSubMediator.Subscribe(Topics.REGISTER_WORKER, context.self.toClassic)
  cluster.subscriptions ! Subscribe(clusterAdapter, classOf[MemberEvent])
  for (member <- cluster.state.members if member.status == MemberStatus.Up) {
    if (member.roles(NodeRoles.WORKER)) {
      context.self ! InitSuccess
    }
  }

  def idle(): Behavior[Broker.Command] =
    Behaviors.withStash(1024) { stash =>
      Behaviors.receiveMessage {
        case InitSuccess =>
          context.log.info(s"Init success, become to receive().")
          stash.unstashAll(receive())

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

  def receive(): Behavior[Broker.Command] =
    Behaviors
      .receiveMessage[Broker.Command] {
        case message: Broker.WorkerStatus =>
          workersData.update(message.status.workerId, message.status)
          context.log.info(s"workers size: ${workersData.size} $message")
          receive()

        case TriggerJob(maybeWorker, jobEntity, replyTo) =>
          workersData.findAvailableWorkers(maybeWorker) match {
            case Right(worker) =>
              val jobInstanceData = createJobInstanceData(jobEntity)
              brokerRepository.saveJobInstance(jobInstanceData)
              worker ! Worker.StartJob(jobInstanceData)
              replyTo ! BrokerReply(StatusCodes.Accepted.intValue, "")
            case Left(msg) =>
              replyTo ! BrokerReply(StatusCodes.TooManyRequests.intValue, msg)
          }
          Behaviors.same

        case Broker.TriggerJobReply(status, instanceId, startTimeOption, serviceStatus) =>
          workersData.update(serviceStatus.workerId, serviceStatus)
          brokerRepository.updateJobInstance(instanceId, status, startTimeOption)
          Behaviors.same

        case Broker.JobInstanceResult(instanceId, result, serverStatus) =>
          workersData.update(serverStatus.workerId, serverStatus)
          brokerRepository.updateJobInstance(instanceId, result)
          Behaviors.same

        case Broker.RegisterWorker(counter, `brokerId`, workerId, worker) =>
          context.log.info(s"The ${counter}th worker registration, worker id is $workerId.")
          worker ! Worker.RegisterToBrokerAck(context.self)
          context.watch(worker)
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
        case (_, Terminated(ref)) =>
          workersData.remove(ref.path.name)
          Behaviors.same
      }

  private def createJobInstanceData(jobEntity: JobEntity): JobInstanceData = {
    val schedulerTime = null
    JobInstanceData(
      jobEntity.id,
      Utils.timeBasedUuid().toString,
      jobEntity.name,
      jobEntity.`type`,
      schedulerTime,
      jobEntity.jarUrl,
      jobEntity.mainClass,
      jobEntity.codeContent,
      jobEntity.timeout,
      None,
      None)
  }

  private def removeWorker(member: Member): Unit = {
    context.self ! RemoveWorkerByAddress(member.uniqueAddress.address)
  }

  private def cleanup(): Unit = {}

  private def postStop(): Unit = {
    cleanup()
  }
}
