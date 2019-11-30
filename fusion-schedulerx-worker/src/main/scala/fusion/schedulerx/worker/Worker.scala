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

import akka.actor.Cancellable
import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.adapter._
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors, TimerScheduler }
import akka.actor.typed.{ ActorRef, Behavior, Terminated }
import akka.cluster.UniqueAddress
import akka.cluster.pubsub.{ DistributedPubSub, DistributedPubSubMediator }
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
import akka.cluster.typed.Cluster
import fusion.json.jackson.CborSerializable
import fusion.schedulerx.protocol.broker.{ BrokerCommand, RegisterWorker, WorkerServiceStatus, WorkerStatus }
import fusion.schedulerx.protocol.worker.{ GetWorkerStatus, WorkerCommand }
import fusion.schedulerx.{ SchedulerX, SchedulerXSettings, Topics }

import scala.concurrent.duration._

object Worker {
  final case class AnsweredAddress(workerId: String, address: UniqueAddress) extends CborSerializable
  final private case class BrokerListing(listing: Receptionist.Listing) extends WorkerCommand
  final case object ReportSystemStatus extends WorkerCommand

  sealed trait JobCommand extends WorkerCommand

  val TypeKey: EntityTypeKey[WorkerCommand] = EntityTypeKey[WorkerCommand]("Worker")
  def apply(workerId: String, settings: SchedulerXSettings): Behavior[WorkerCommand] =
    Behaviors.setup(context => Behaviors.withTimers(timers => new Worker(workerId, settings, timers, context).init()))
}

import fusion.schedulerx.worker.Worker._
class Worker private (
    workerId: String,
    settings: SchedulerXSettings,
    timers: TimerScheduler[WorkerCommand],
    context: ActorContext[WorkerCommand]) {
  import context.executionContext
  private val cluster = Cluster(context.system)

  context.log.info(s"Worker: $workerId, startup.")

  def init(): Behavior[WorkerCommand] = {
    val mediator = DistributedPubSub(context.system.toClassic).mediator
    val cancellable = context.system.scheduler.scheduleAtFixedRate(0.second, 1.second) { () =>
      val message = RegisterWorker(SchedulerX.counter(), settings.namespace, workerId, context.self)
      mediator ! DistributedPubSubMediator.Publish(Topics.WORKER_STARTUP, message)
    }
    idle(cancellable)
  }

  def idle(cancellable: Cancellable): Behavior[WorkerCommand] = Behaviors.receiveMessage {
    case GetWorkerStatus(broker) =>
      broker ! getWorkerStatus()
      cancellable.cancel()
      context.watch(broker)
      timers.startTimerWithFixedDelay(ReportSystemStatus, ReportSystemStatus, settings.worker.healthInterval)
      receive(broker)
    case ReportSystemStatus =>
      Behaviors.same
    case other =>
      context.log.warn(s"Invalid message: $other")
      Behaviors.same
  }

  def receive(broker: ActorRef[BrokerCommand]): Behavior[WorkerCommand] =
    Behaviors
      .receiveMessage[WorkerCommand] {
        case ReportSystemStatus =>
          broker ! getWorkerStatus()
          Behaviors.same
        case GetWorkerStatus(broker) =>
          broker ! getWorkerStatus()
          receive(broker)
      }
      .receiveSignal {
        case (_, Terminated(`broker`)) =>
          init()
        case (_, Terminated(ref)) =>
          context.log.info(s"Watched actor terminated, it is $ref.")
          Behaviors.same
      }

  private def getWorkerStatus(): WorkerStatus = {
    WorkerStatus(
      SchedulerX.counter(),
      settings.namespace,
      WorkerServiceStatus(settings.namespace, workerId, cluster.selfMember.address),
      context.self)
  }
}
