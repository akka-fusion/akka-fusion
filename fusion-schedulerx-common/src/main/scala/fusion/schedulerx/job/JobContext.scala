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

package fusion.schedulerx.job

import java.time.OffsetDateTime

import fusion.schedulerx.protocol.JobType

trait JobContext {
  // 作业ID
  def jobId: String

  // 作业名
  def jobName: String

  def jobType: JobType

  // Job参数，此Job的所有任务实例都会收到一样的作业参数
  def jobParameters: Map[String, String]

  // 作业调度计划开始时间
  def schedulerTime: OffsetDateTime

  // 作业实际开始时间
  def beginScheduleTime: OffsetDateTime
}
