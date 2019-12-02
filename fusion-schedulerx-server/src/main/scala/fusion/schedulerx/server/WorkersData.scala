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

import akka.actor.typed.ActorRef
import fusion.schedulerx.SchedulerXSettings
import fusion.schedulerx.protocol.{ Worker, WorkerServiceStatus }

import scala.collection.mutable

private[server] class WorkersData(settings: SchedulerXSettings) {
  class RunWorkerStatus(val workerId: String, var runners: List[String])

  private val workers = mutable.Map.empty[String, WorkerServiceStatus]
  private var workerIds = Vector.empty[String]
  private var curIndex = 0

  def size: Int = workers.size

  def update(workerId: String, status: WorkerServiceStatus): Unit = {
    if (!workerIds.contains(workerId)) {
      workerIds :+= workerId
    }
    workers.update(workerId, status)
  }

  def remove(workerId: String): Option[WorkerServiceStatus] = {
    if (workers.contains(workerId)) {
      workerIds = workerIds.filterNot(_ == workerId)
      checkCurIndex()
      workers.remove(workerId)
    } else None
  }

  def findAvailableWorkers(maybeWorkerId: Option[String]): Either[String, ActorRef[Worker.Command]] = {
    if (workerIds.isEmpty) Left("Workers is empty!")
    else
      maybeWorkerId match {
        case Some(wid) =>
          workers
            .get(wid)
            .filter(_.availableWork(settings))
            .map(_.worker)
            .toRight(s"Worker is busy, note available. workerId: $wid.")
        case None =>
          checkCurIndex()
          val done = curIndex
          var maybe: Option[ActorRef[Worker.Command]] = None
          do {
            maybe = workers.get(workerIds(curIndex)).filter(_.availableWork(settings)).map(_.worker)
            curIndexIncrement()
          } while (maybe.isEmpty && curIndex != done)
          maybe.toRight(s"Too many requests, workers not available.")
      }
  }

  @inline private def curIndexIncrement(): Unit = {
    curIndex += 1
    if (curIndex >= workerIds.size) {
      curIndex = 0
    }
  }

  @inline private def checkCurIndex(): Unit = {
    if (curIndex >= workerIds.size) {
      curIndex = 0
    }
  }
}
