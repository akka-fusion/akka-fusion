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

package fusion.schedulerx.worker.job.internal

import java.time.OffsetDateTime

import fusion.schedulerx.job.ProcessResult
import fusion.schedulerx.protocol.JobType
import fusion.schedulerx.worker.SchedulerXWorker
import fusion.schedulerx.worker.job.{ JobInstanceData, WorkerJobContext }

class WorkerJobContextImpl(override val worker: SchedulerXWorker) extends WorkerJobContext {
  override def jobUpstreamData: Seq[JobInstanceData] = ???

  override def taskName: String = ???

  override def taskId: Int = ???

  override def taskPayload: AnyRef = ???

  override def taskResults: Map[Int, ProcessResult] = ???

  override def jobId: String = ???

  override def jobName: String = ???

  override def jobType: JobType = ???

  override def jobParameters: Map[String, String] = ???

  override def schedulerTime: OffsetDateTime = ???

  override def beginScheduleTime: OffsetDateTime = ???
}
