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

import akka.actor.typed.ActorRef
import fusion.json.jackson.CborSerializable
import fusion.schedulerx.job.ProcessResult

object Broker {
  trait Command extends CborSerializable

  case class RegistrationWorker(namespace: String, workerId: String, worker: ActorRef[Worker.Command]) extends Command

  case class WorkerStatus(counter: Long, status: WorkerServiceStatus) extends Command

  case class TriggerJobReply(
      status: Int,
      instanceId: String,
      startTime: Option[OffsetDateTime],
      serviceStatus: WorkerServiceStatus)
      extends Command

  case class JobInstanceResult(instanceId: String, result: ProcessResult, serverStatus: WorkerServiceStatus)
      extends Command

  case class KillJobInstance(instanceId: String) extends Command

  case class GetJobInstanceList(jobId: String, replyTo: ActorRef[JobInstanceList]) extends Command

  case class JobInstanceList(instances: Seq[JobInstanceDetail])

  case class GetJobInstance(jobId: String, replyTo: ActorRef[JobInstanceDetail]) extends Command
}
