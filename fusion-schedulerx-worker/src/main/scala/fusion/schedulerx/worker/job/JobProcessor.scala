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

import fusion.schedulerx.job.ProcessResult

trait JobProcessor {
  def preStart(context: WorkerJobContext): Unit = {}

  @throws[Exception]
  def execute(context: WorkerJobContext): ProcessResult

  def postStop(context: WorkerJobContext): ProcessResult = ProcessResult.Empty

  /**
   * 被前端强制杀死时执行，之后仍正常调用 postStop
   */
  def onKill(context: WorkerJobContext): Unit = {}
}

trait MapJobProcessor extends JobProcessor {
  override def execute(context: WorkerJobContext): ProcessResult = {
    if (context.isRootTask) {
      mainTaskExecute(context)
    } else {
      subTaskExecute(context)
    }
  }

  @throws[Exception]
  def mainTaskExecute(context: WorkerJobContext): ProcessResult

  @throws[Exception]
  def subTaskExecute(context: WorkerJobContext): ProcessResult

  def preStartAll(context: WorkerJobContext): Unit = {}

  def postStopAll(context: WorkerJobContext): ProcessResult = ProcessResult.Empty

  /**
   *
   * @param payloads 任务数据，AnyRef需要可被序列化
   * @param taskName 任务名字
   * @return
   */
  def map(payloads: Iterable[AnyRef], taskName: String): ProcessResult
}

trait MapReduceJobProcessor extends MapJobProcessor {
  @throws[Exception]
  def reduce(context: WorkerJobContext): ProcessResult
}
