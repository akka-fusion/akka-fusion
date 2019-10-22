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

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import fusion.job.FusionJob
import fusion.job.FusionScheduler
import fusion.scheduler.model._

object SchedulerBehavior extends SchedulerServiceComponent {
  val name = "fusion-scheduler"

  def apply(): Behavior[JobCommand] = Behaviors.setup { context =>
    implicit val fusionScheduler: FusionScheduler = FusionJob(context.system).component

    Behaviors.receiveMessagePartial {
      case dto: JobCancelDTO =>
        dto.replyActor ! cancelJob(dto)
        Behaviors.same
      case dto: JobDTO =>
        dto.replyActor ! createJob(dto)
        Behaviors.same
      case dto: JobGetDTO =>
        dto.replyActor ! getJob(dto)
        Behaviors.same
      case dto: JobPauseDTO =>
        dto.replyActor ! pauseJob(dto)
        Behaviors.same
      case dto: JobResumeDTO =>
        dto.replyActor ! resumeJob(dto)
        Behaviors.same
      case End =>
        fusionScheduler.close()
        Behaviors.stopped
    }
  }

}
