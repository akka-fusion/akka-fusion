/*
 * Copyright 2019-2021 helloscala.com
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

package fusion.common.extension

import akka.Done
import akka.actor.CoordinatedShutdown.UnknownReason
import akka.actor.{ ActorSystem, Cancellable, CoordinatedShutdown }

import scala.concurrent.Future

private[fusion] class FusionCoordinatedShutdown(coordinatedShutdown: CoordinatedShutdown) {
  def this(system: ActorSystem) {
    this(CoordinatedShutdown(system))
  }

  def run(): Future[Done] = coordinatedShutdown.run(UnknownReason)

  def addJvmShutdownHook[T](hook: => T): Cancellable =
    coordinatedShutdown.addCancellableJvmShutdownHook(hook)

  def addTask(phase: String, taskName: String)(task: () => Future[Done]): Unit =
    coordinatedShutdown.addTask(phase, taskName)(task)

  def beforeServiceUnbind(taskName: String)(task: () => Future[Done]): Unit =
    coordinatedShutdown.addTask(CoordinatedShutdown.PhaseBeforeServiceUnbind, taskName)(task)

  def serviceUnbind(taskName: String)(task: () => Future[Done]): Unit =
    coordinatedShutdown.addTask(CoordinatedShutdown.PhaseServiceUnbind, taskName)(task)

  def serviceRequestsDone(taskName: String)(task: () => Future[Done]): Unit =
    coordinatedShutdown.addTask(CoordinatedShutdown.PhaseServiceRequestsDone, taskName)(task)

  def serviceStop(taskName: String)(task: () => Future[Done]): Unit =
    coordinatedShutdown.addTask(CoordinatedShutdown.PhaseServiceStop, taskName)(task)

  def beforeClusterShutdown(taskName: String)(task: () => Future[Done]): Unit =
    coordinatedShutdown.addTask(CoordinatedShutdown.PhaseBeforeClusterShutdown, taskName)(task)

  def clusterShardingShutdownRegion(taskName: String)(task: () => Future[Done]): Unit =
    coordinatedShutdown.addTask(CoordinatedShutdown.PhaseClusterShardingShutdownRegion, taskName)(task)

  def clusterLeave(taskName: String)(task: () => Future[Done]): Unit =
    coordinatedShutdown.addTask(CoordinatedShutdown.PhaseClusterLeave, taskName)(task)

  def clusterExiting(taskName: String)(task: () => Future[Done]): Unit =
    coordinatedShutdown.addTask(CoordinatedShutdown.PhaseClusterExiting, taskName)(task)

  def clusterExitingDone(taskName: String)(task: () => Future[Done]): Unit =
    coordinatedShutdown.addTask(CoordinatedShutdown.PhaseClusterExitingDone, taskName)(task)

  def clusterShutdown(taskName: String)(task: () => Future[Done]): Unit =
    coordinatedShutdown.addTask(CoordinatedShutdown.PhaseClusterShutdown, taskName)(task)

  def beforeActorSystemTerminate(taskName: String)(task: () => Future[Done]): Unit =
    coordinatedShutdown.addTask(CoordinatedShutdown.PhaseBeforeActorSystemTerminate, taskName)(task)

  def actorSystemTerminate(taskName: String)(task: () => Future[Done]): Unit =
    coordinatedShutdown.addTask(CoordinatedShutdown.PhaseActorSystemTerminate, taskName)(task)
}
