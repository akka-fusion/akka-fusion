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

package fusion.scheduler.route

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.server.Route
import fusion.scheduler.SchedulerAggregate
import fusion.http.server.AbstractRoute
import fusion.scheduler.model.JobCancelDTO
import fusion.scheduler.model.JobDTO
import fusion.scheduler.model.JobPauseDTO
import fusion.scheduler.model.JobResumeDTO
import fusion.scheduler.model.Key

class SchedulerRoute(system: ActorSystem[_]) extends AbstractRoute {
  private val schedulerService = SchedulerAggregate(system).schedulerService

  override def route: Route = pathPrefix("scheduler") {
    getJobRoute ~
    createJobRoute ~
    pauseJobRoute ~
    resumeJobRoute ~
    cancelJobRoute
  }

  import fusion.json.circe.http.FailFastProtobufCirceSupport._

  def createJobRoute: Route = pathPost("create") {
    entity(as[JobDTO]) { dto =>
      onSuccess(schedulerService.createJob(dto)) { result =>
        complete(result)
      }
    }
  }

  def pauseJobRoute: Route = pathPost("pause") {
    entity(as[JobPauseDTO]) { dto =>
      complete(schedulerService.pauseJob(dto))
    }
  }

  def resumeJobRoute: Route = pathPost("resume") {
    entity(as[JobResumeDTO]) { dto =>
      complete(schedulerService.resumeJob(dto))
    }
  }

  def cancelJobRoute: Route = pathPost("cancel") {
    entity(as[JobCancelDTO]) { dto =>
      complete(schedulerService.cancelJob(dto))
    }
  }

  def getJobRoute: Route = pathGet("item") {
    parameters(('name, 'group)) { (name, group) =>
      complete(schedulerService.getJob(Key(group, name)))
    }
  }

}
