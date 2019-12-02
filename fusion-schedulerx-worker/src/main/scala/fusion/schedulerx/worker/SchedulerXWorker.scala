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

import java.util.concurrent.TimeoutException

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ ActorRef, ActorSystem, Props }
import akka.cluster.typed.Cluster
import akka.util.Timeout
import fusion.common.FusionProtocol
import fusion.schedulerx.protocol.Worker
import fusion.schedulerx.{ NodeRoles, SchedulerX, SchedulerXSettings }

import scala.concurrent.Await
import scala.concurrent.duration._

final class SchedulerXWorker private (schedulerX: SchedulerX) {
  implicit val system: ActorSystem[FusionProtocol.Command] = schedulerX.system
  private var _worker: ActorRef[Worker.Command] = _

  def worker: ActorRef[Worker.Command] = _worker

  def settings: SchedulerXSettings = schedulerX.schedulerXSettings

  @throws[TimeoutException]
  def start(): SchedulerXWorker = {
    implicit val timeout: Timeout = 10.seconds
    _worker = Await.result(
      system.ask[ActorRef[Worker.Command]](
        replyTo =>
          FusionProtocol.Spawn(
            WorkerImpl(SchedulerX.getWorkerId(Cluster(system).selfMember.address), settings),
            NodeRoles.WORKER,
            Props.empty,
            replyTo)),
      timeout.duration)
    this
  }
}

object SchedulerXWorker {
  def apply(schedulerX: SchedulerX): SchedulerXWorker = new SchedulerXWorker(schedulerX)
}
