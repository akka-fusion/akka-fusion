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

package fusion.scheduler.service

import akka.actor.typed.ActorRef
import akka.actor.typed.Scheduler
import akka.actor.typed.scaladsl.AskPattern._
import akka.util.Timeout
import fusion.ResultBO
import fusion.scheduler.grpc.SchedulerService
import fusion.scheduler.model._

import scala.concurrent.Future
import scala.concurrent.duration._

class SchedulerServiceImpl(schedulerProxy: ActorRef[JobCommand])(implicit scheduler: Scheduler)
    extends SchedulerService {
  implicit private val timeout: Timeout = Timeout(5.seconds)

  override def cancelJob(in: JobCancelDTO): Future[ResultBO] = {
    schedulerProxy.ask[ResultBO](replyTo => JobCancelDTOReplyTo(Some(in), replyTo))
  }

  override def createJob(in: JobDTO): Future[JobBO] = {
    schedulerProxy.ask[JobBO](JobDTOReplyTO(Some(in), _))
  }

  override def getJob(in: Key): Future[JobBO] = {
    schedulerProxy.ask[JobBO](replyTo => JobGetDTO(Some(in), replyTo))
  }

  override def pauseJob(in: JobPauseDTO): Future[ResultBO] = {
    schedulerProxy.ask[ResultBO](JobPauseDTOReplyTo(Some(in), _))
  }

  override def resumeJob(in: JobResumeDTO): Future[ResultBO] = {
    schedulerProxy.ask[ResultBO](JobResumeDTOReplyTo(Some(in), _))
  }

}
