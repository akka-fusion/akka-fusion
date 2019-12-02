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

package fusion.schedulerx.protocol

import java.time.OffsetDateTime

import akka.actor.Address
import akka.actor.typed.ActorRef
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import fusion.schedulerx.SchedulerXSettings

case class RunningJob(jobId: String, startTime: OffsetDateTime)

case class ServerStatus(
    cpuSize: Int,
    totalMemory: Long,
    availableMemory: Long,
    totalSpace: Long,
    freeSpace: Long,
    // an array of the system load averages for 1, 5, and 15 minutes
    loadAverage: Array[Double])

@JsonIgnoreProperties(Array("availableWork", "address"))
case class WorkerServiceStatus(
    worker: ActorRef[Worker.Command],
    workerId: String,
    datetime: OffsetDateTime,
    runningJobs: List[RunningJob],
    jobMaxConcurrent: Int,
    serverStatus: ServerStatus) {
  def availableWork(settings: SchedulerXSettings): Boolean = {
    runningJobs.size < jobMaxConcurrent //&& serverStatus.loadAverage(2) < 2.0
  }

  def address: Address = worker.path.address
}
