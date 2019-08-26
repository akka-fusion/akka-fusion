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

package fusion.core.extension

import akka.Done
import akka.actor.CoordinatedShutdown.UnknownReason
import akka.actor.ActorSystem
import akka.actor.Cancellable
import akka.actor.CoordinatedShutdown

import scala.concurrent.Future

class FusionCoordinatedShutdown(system: ActorSystem) {
  def run(): Future[Done] = CoordinatedShutdown(system).run(UnknownReason)

  def addJvmShutdownHook[T](hook: => T): Cancellable =
    CoordinatedShutdown(system).addCancellableJvmShutdownHook(hook)

  def addTask(phase: String, taskName: String)(task: () => Future[Done]): Unit =
    CoordinatedShutdown(system).addTask(phase, taskName)(task)

  def beforeServiceUnbind(taskName: String)(task: () => Future[Done]): Unit =
    CoordinatedShutdown(system).addTask(CoordinatedShutdown.PhaseBeforeServiceUnbind, taskName)(task)

  def serviceUnbind(taskName: String)(task: () => Future[Done]): Unit =
    CoordinatedShutdown(system).addTask(CoordinatedShutdown.PhaseServiceUnbind, taskName)(task)

  def serviceRequestsDone(taskName: String)(task: () => Future[Done]): Unit =
    CoordinatedShutdown(system).addTask(CoordinatedShutdown.PhaseServiceRequestsDone, taskName)(task)

  def serviceStop(taskName: String)(task: () => Future[Done]): Unit =
    CoordinatedShutdown(system).addTask(CoordinatedShutdown.PhaseServiceStop, taskName)(task)

  def beforeClusterShutdown(taskName: String)(task: () => Future[Done]): Unit =
    CoordinatedShutdown(system).addTask(CoordinatedShutdown.PhaseBeforeClusterShutdown, taskName)(task)

  def clusterShardingShutdownRegion(taskName: String)(task: () => Future[Done]): Unit =
    CoordinatedShutdown(system).addTask(CoordinatedShutdown.PhaseClusterShardingShutdownRegion, taskName)(task)

  def clusterLeave(taskName: String)(task: () => Future[Done]): Unit =
    CoordinatedShutdown(system).addTask(CoordinatedShutdown.PhaseClusterLeave, taskName)(task)

  def clusterExiting(taskName: String)(task: () => Future[Done]): Unit =
    CoordinatedShutdown(system).addTask(CoordinatedShutdown.PhaseClusterExiting, taskName)(task)

  def clusterExitingDone(taskName: String)(task: () => Future[Done]): Unit =
    CoordinatedShutdown(system).addTask(CoordinatedShutdown.PhaseClusterExitingDone, taskName)(task)

  def clusterShutdown(taskName: String)(task: () => Future[Done]): Unit =
    CoordinatedShutdown(system).addTask(CoordinatedShutdown.PhaseClusterShutdown, taskName)(task)

  def beforeActorSystemTerminate(taskName: String)(task: () => Future[Done]): Unit =
    CoordinatedShutdown(system).addTask(CoordinatedShutdown.PhaseBeforeActorSystemTerminate, taskName)(task)

  def actorSystemTerminate(taskName: String)(task: () => Future[Done]): Unit =
    CoordinatedShutdown(system).addTask(CoordinatedShutdown.PhaseActorSystemTerminate, taskName)(task)
}
