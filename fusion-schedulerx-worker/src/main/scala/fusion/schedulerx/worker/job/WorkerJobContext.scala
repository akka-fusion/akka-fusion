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

import fusion.schedulerx.job.{ JobContext, ProcessResult }
import fusion.schedulerx.worker.SchedulerXWorker

import scala.collection.immutable

trait WorkerJobContext extends JobContext {
  def worker: SchedulerXWorker

  // 上游传过来的数据，因为可能会有多个上游，所有这里返回一个列表
  def jobUpstreamData: immutable.Seq[JobInstanceData]

  // 任务名
  def taskName: String

  // 分布式子任务，根任务ID始终为 0
  def taskId: Int

  // 是否根任务
  @inline def isRootTask: Boolean = taskId == 0

  // Task Payload数据，需要可序列化
  def taskPayload: AnyRef

  def taskPayloadUnsafeCast[T]: T = taskPayload.asInstanceOf[T]

  def taskResults: Map[Int, ProcessResult]
}
