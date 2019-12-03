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

import akka.actor.typed.scaladsl.{ ActorContext, Behaviors, TimerScheduler }
import akka.actor.typed.{ Behavior, PostStop, SupervisorStrategy }
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
import fusion.schedulerx.protocol.Broker
import fusion.schedulerx.server.model.JobConfigInfo
import fusion.schedulerx.server.repository.BrokerRepository
import fusion.schedulerx.{ NodeRoles, SchedulerXSettings }
import org.quartz.{ JobDetail, Trigger }
import org.quartz.impl.StdSchedulerFactory
import org.quartz.impl.matchers.GroupMatcher

object BrokerGuardian {
  trait Command
  case class BrokerCommand(cmd: Broker.Command) extends Command
  case class RequestCommand(cmd: BrokerScheduler.Request) extends Command

  val TypeKey: EntityTypeKey[Command] = EntityTypeKey("Broker")

  def apply(namespace: String): Behavior[Command] =
    Behaviors.setup(context => Behaviors.withTimers(timers => new BrokerGuardian(namespace, timers, context).init()))
}

import BrokerGuardian._
class BrokerGuardian private (namespace: String, timers: TimerScheduler[Command], context: ActorContext[Command]) {
  private val settings = SchedulerXSettings(context.system.settings.config)
  private val brokerSettings: BrokerSettings = BrokerSettings(settings, context.system.settings.config)
  private val brokerRepository = BrokerRepository(context.system)
  private val scheduler = StdSchedulerFactory.getDefaultScheduler
  scheduler.start()

  private val brokerRef = context.spawn(
    Behaviors
      .supervise(BrokerImpl(namespace, brokerSettings, settings, brokerRepository))
      .onFailure[Exception](SupervisorStrategy.resume),
    NodeRoles.BROKER)
  private val schedulerRef = context.spawn(
    Behaviors
      .supervise(BrokerScheduler(namespace, brokerSettings, settings, brokerRepository, brokerRef, scheduler))
      .onFailure[Exception](SupervisorStrategy.resume),
    "scheduler")

  def init(): Behavior[Command] = Behaviors.receiveMessage[Command] { msg =>
    Behaviors.same
  }

  def receive(): Behavior[Command] =
    Behaviors
      .receiveMessage[Command] {
        case BrokerCommand(cmd) =>
          brokerRef ! cmd
          Behaviors.same
        case RequestCommand(cmd) =>
          schedulerRef ! cmd
          Behaviors.same
      }
      .receiveSignal {
        case (_, PostStop) =>
          storageSchedulerState()
          scheduler.shutdown(true)
          Behaviors.same
      }

  def storageSchedulerState(): Unit = {
    import scala.jdk.CollectionConverters._
    scheduler.getJobKeys(GroupMatcher.anyGroup()).asScala.map { jobKey =>
      val detail = scheduler.getJobDetail(jobKey)
      val trigger = scheduler.getTriggersOfJob(jobKey).asScala.head
    }
  }

  def generateJobConfigInfo(detail: JobDetail, trigger: Trigger): JobConfigInfo = {
    val key = detail.getKey
    //JobConfigInfo(namespace, key.getName, key.getGroup, null, null, detail.getDescription)
    ???
  }
}
