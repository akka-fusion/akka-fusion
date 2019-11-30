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

import java.util.concurrent.TimeoutException

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ ActorRef, ActorSystem, Props }
import akka.util.Timeout
import com.typesafe.config.{ Config, ConfigFactory }
import fusion.schedulerx.protocol.broker.BrokerCommand

import scala.concurrent.Await
import scala.concurrent.duration._

class SchedulerXBroker(schedulerX: SchedulerX) {
  implicit val system: ActorSystem[SchedulerXGuardian.Command] = schedulerX.system
  private var _broker: ActorRef[BrokerCommand] = _

  def broker: ActorRef[BrokerCommand] = _broker

  def settings: SchedulerXSettings = schedulerX.schedulerXSettings

  @throws[TimeoutException]
  def start(): SchedulerXBroker = {
    implicit val timeout: Timeout = 10.seconds
    _broker = Await.result(
      system.ask[ActorRef[BrokerCommand]](replyTo =>
        SchedulerXGuardian.Spawn(BrokerGuardian(settings), "worker", Props.empty, replyTo)),
      timeout.duration)
    this
  }
}

object SchedulerXBroker {
  def apply(schedulerX: SchedulerX): SchedulerXBroker = new SchedulerXBroker(schedulerX)
  def apply(config: Config): SchedulerXBroker = apply(SchedulerX(config))
  def apply(): SchedulerXBroker = apply(ConfigFactory.load())
}
