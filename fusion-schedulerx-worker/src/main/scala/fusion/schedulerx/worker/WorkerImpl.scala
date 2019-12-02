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

import java.time.OffsetDateTime

import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.adapter._
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors, TimerScheduler }
import akka.actor.typed.{ ActorRef, ActorRefResolver, Behavior, Terminated }
import akka.cluster.UniqueAddress
import akka.cluster.pubsub.{ DistributedPubSub, DistributedPubSubMediator }
import akka.http.scaladsl.model.StatusCodes
import fusion.json.jackson.CborSerializable
import fusion.schedulerx.job.ProcessResult
import fusion.schedulerx.protocol.Broker.WorkerStatus
import fusion.schedulerx.protocol._
import fusion.schedulerx.worker.job.JobInstance
import fusion.schedulerx.worker.job.JobInstance.JobCommand
import fusion.schedulerx.{ SchedulerX, SchedulerXSettings, Topics }

import scala.concurrent.duration._

object WorkerImpl {
  final case class AnsweredAddress(workerId: String, address: UniqueAddress) extends CborSerializable
  final private case class BrokerListing(listing: Receptionist.Listing) extends Worker.Command
  final case object ReportSystemStatus extends Worker.Command
  final case object RegisterToBrokerTimeout extends Worker.Command
  final case class JobInstanceResult(instanceId: String, result: ProcessResult, jobInstance: ActorRef[JobCommand])
      extends Worker.Command

  def apply(workerId: String, settings: SchedulerXSettings): Behavior[Worker.Command] =
    Behaviors.setup(context =>
      Behaviors.withTimers(timers => new WorkerImpl(workerId, settings, timers, context).init(1, Duration.Zero)))
}

import fusion.schedulerx.worker.WorkerImpl._
class WorkerImpl private (
    workerId: String,
    settings: SchedulerXSettings,
    timers: TimerScheduler[Worker.Command],
    context: ActorContext[Worker.Command]) {
  private val mediator = DistributedPubSub(context.system.toClassic).mediator

  context.log.info(
    s"Worker: $workerId, format: ${ActorRefResolver(context.system).toSerializationFormat(context.self)} startup.")

  def init(registerCount: Int, registerDelay: FiniteDuration): Behavior[Worker.Command] = Behaviors.receiveMessage {
    case RegisterToBrokerTimeout =>
      val message = Broker.RegisterWorker(registerCount, settings.namespace, workerId, context.self)
      mediator ! DistributedPubSubMediator.Publish(Topics.REGISTER_WORKER, message)
      val delay = settings.worker.computeRegisterDelay(registerDelay)
      timers.startSingleTimer(RegisterToBrokerTimeout, RegisterToBrokerTimeout, delay)
      init(registerCount + 1, delay)

    case Worker.RegisterToBrokerAck(broker) =>
      broker ! WorkerStatus(SchedulerX.counter(), getWorkerStatus(Nil))
      context.watch(broker)
      timers.startTimerWithFixedDelay(ReportSystemStatus, ReportSystemStatus, settings.worker.healthInterval)
      receive(broker, Nil)

    case other =>
      context.log.warn(s"Invalid message: $other")
      Behaviors.same
  }

  def receive(
      broker: ActorRef[Broker.Command],
      runningJobs: List[(ActorRef[JobCommand], JobInstanceData)]): Behavior[Worker.Command] =
    Behaviors
      .receiveMessage[Worker.Command] {
        case ReportSystemStatus =>
          broker ! WorkerStatus(SchedulerX.counter(), getWorkerStatus(runningJobs))
          Behaviors.same

        case Worker.StartJob(jobInfo) =>
          if (runningJobs.size < settings.worker.jobMaxConcurrent) {
            val jobInst = context.spawn(JobInstance(context.self, jobInfo), jobInfo.instanceId)
            val startTime = OffsetDateTime.now()
            val runnings = (jobInst -> jobInfo.copy(startTime = Some(startTime))) :: runningJobs
            broker ! Broker.TriggerJobReply(
              StatusCodes.Created.intValue,
              jobInfo.instanceId,
              Some(startTime),
              getWorkerStatus(runnings))
            receive(broker, runnings)
          } else {
            broker ! Broker.TriggerJobReply(
              StatusCodes.TooManyRequests.intValue,
              jobInfo.instanceId,
              None,
              getWorkerStatus(runningJobs))
            Behaviors.same
          }

        case WorkerImpl.JobInstanceResult(instanceId, result, ref) =>
          // TODO 确保 broker 肯定能收到此消息
          broker ! Broker.JobInstanceResult(instanceId, result, getWorkerStatus(runningJobs))
          receive(broker, runningJobs.filterNot(_._1 == ref))
      }
      .receiveSignal {
        case (_, Terminated(`broker`)) => init(1, Duration.Zero)
        case (_, Terminated(ref))      => receive(broker, runningJobs.filterNot(_._1 == ref))
      }

  private def getWorkerStatus(runningJobs: List[(ActorRef[JobCommand], JobInstanceData)]): WorkerServiceStatus = {
    val runnings = runningJobs.map { case (_, info) => RunningJob(info.instanceId, info.startTime.get) }
    WorkerServiceStatus(
      context.self,
      workerId,
      OffsetDateTime.now(),
      runnings,
      settings.worker.jobMaxConcurrent,
      SchedulerX.serverStatus())
  }
}
