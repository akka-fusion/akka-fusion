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
import akka.actor.typed.{ Behavior, PostStop, PreRestart, Terminated }
import akka.cluster.ClusterEvent.MemberEvent
import akka.cluster.Member
import akka.cluster.pubsub.{ DistributedPubSub, DistributedPubSubMediator }
import akka.http.scaladsl.model.StatusCodes
import fusion.schedulerx.protocol.{ Broker, JobInstanceDetail, Worker }
import fusion.schedulerx.server.model.JobConfigInfo
import fusion.schedulerx.server.protocol.{ BrokerInfo, BrokerReply, TriggerJob }
import fusion.schedulerx.server.repository.BrokerRepository
import fusion.schedulerx.{ Constants, SchedulerXSettings, Topics }
import helloscala.common.util.Utils

import scala.concurrent.duration._

object BrokerImpl {
  trait InternalCommand extends Broker.Command
  case class InitParameters(namespace: String, payload: BrokerInfo) extends Broker.Command
  private case class InternalClusterEvent(event: MemberEvent) extends InternalCommand
  private case class RemoveWorkerByAddress(address: Address) extends InternalCommand

  def apply(
      brokerId: String,
      brokerSettings: BrokerSettings,
      settings: SchedulerXSettings,
      brokerRepository: BrokerRepository): Behavior[Broker.Command] =
    Behaviors.setup(context =>
      Behaviors.withTimers { timers =>
        new BrokerImpl(brokerId, brokerSettings, settings, brokerRepository, timers, context).idle()
      })
}

import akka.actor.typed.scaladsl.adapter._
import fusion.schedulerx.server.BrokerImpl._
class BrokerImpl(
    brokerId: String,
    brokerSettings: BrokerSettings,
    settings: SchedulerXSettings,
    brokerRepository: BrokerRepository,
    timers: TimerScheduler[Broker.Command],
    context: ActorContext[Broker.Command]) {
  private var brokerInfo: BrokerInfo = _
  private val mediator = DistributedPubSub(context.system.toClassic).mediator
  private val workersData = new WorkersData(settings)

  mediator ! DistributedPubSubMediator.Subscribe(Topics.REGISTER_WORKER, context.self.toClassic)

  def idle(): Behavior[Broker.Command] =
    Behaviors.withStash(1024) { stash =>
      Behaviors.receiveMessage {
        case InitParameters(_, brokerInfo) =>
          this.brokerInfo = brokerInfo
          stash.unstashAll(receive())

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
          Behaviors.same

        case TriggerJob(maybeWorker, jobEntity, replyTo) =>
          workersData.findAvailableWorkers(maybeWorker) match {
            case Right(worker) =>
              val jobInstanceData = createJobInstanceDetail(jobEntity)
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
          brokerRepository.completeJobInstance(instanceId, result)
          Behaviors.same

        case Broker.RegistrationWorker(namespace, workerId, worker) =>
          if (brokerId == namespace) {
            context.log.info(s"Received worker re registration message, send ack to it. worker id is $workerId.")
            worker ! Worker.RegistrationWorkerAck(context.self)
            context.watch(worker)
          }
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

  private def createJobInstanceDetail(jobEntity: JobConfigInfo): JobInstanceDetail = {
    val schedulerTime = null
    JobInstanceDetail(
      jobEntity.jobId,
      Utils.timeBasedUuid().toString,
      jobEntity.name,
      jobEntity.jobType,
      schedulerTime,
      jobEntity.jarUrl,
      jobEntity.className,
      jobEntity.codeContent,
      jobEntity.timeout.map(_.seconds).getOrElse(Constants.DEFAULT_TIMEOUT),
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
