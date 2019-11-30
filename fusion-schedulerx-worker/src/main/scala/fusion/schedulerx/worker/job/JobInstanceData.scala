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

package fusion.schedulerx.worker.job

import java.time.{ LocalDateTime, OffsetDateTime }

import fusion.schedulerx.protocol.JobType

/**
 * 只有Job执行成功（ProcessResult.status == 200）才会生成此作业实例数据，并可以传递到下游
 */
trait JobInstanceData {
  def jobId: String

  def jobName: String

  def jobType: JobType

  // 调度执行计划开始时间
  def schedulerTime: LocalDateTime

  // 作业实际开始时间
  def beginScheduleTime: OffsetDateTime

  // 作业实际结束时间
  def doneScheduleTime: OffsetDateTime

  // 是否手动完成，手动完成的任务也作为成功看待
  def isManualDone: Boolean

  /**
   * 上游处理结果： [[fusion.schedulerx.job.ProcessResult.result]]
   */
  def result: String
}
