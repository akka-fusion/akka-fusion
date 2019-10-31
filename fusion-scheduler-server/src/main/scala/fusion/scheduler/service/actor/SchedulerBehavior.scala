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

package fusion.scheduler.service.actor

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import fusion.job.FusionJob
import fusion.job.FusionScheduler
import fusion.scheduler.model._
import fusion.scheduler.service.SchedulerServiceComponent

object SchedulerBehavior extends SchedulerServiceComponent {
  val name = "fusion-scheduler"

  def apply(): Behavior[JobCommand] = Behaviors.setup { context =>
    implicit val fusionScheduler: FusionScheduler = FusionJob(context.system).component
    context.log.info(s"${context.self} SchedulerBehavior started")

    Behaviors.receiveMessagePartial {
      case JobCancelDTOReplyTo(Some(dto), replyActor) =>
        replyActor ! cancelJob(dto)
        Behaviors.same
      case JobDTOReplyTO(Some(dto), replyActor) =>
        replyActor ! createJob(dto)
        Behaviors.same
      case JobGetDTO(Some(key), replyActor) =>
        replyActor ! getJob(key)
        Behaviors.same
      case JobPauseDTOReplyTo(Some(dto), replyActor) =>
        replyActor ! pauseJob(dto)
        Behaviors.same
      case JobResumeDTOReplyTo(Some(dto), replyActor) =>
        replyActor ! resumeJob(dto)
        Behaviors.same
      case End =>
        fusionScheduler.close()
        Behaviors.stopped
    }
  }

}
