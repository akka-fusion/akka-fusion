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

import java.util.UUID

import fusion.scheduler.service.job.HongkaDefaultJob
import fusion.scheduler.util.JobUtils
import fusion.job.FusionScheduler
import helloscala.common.IntStatus
import fusion.ResultBO
import fusion.scheduler.model._
import org.quartz.JobKey
import org.quartz.TriggerKey

import scala.jdk.CollectionConverters._

trait SchedulerServiceComponent {

  def cancelJob(dto: JobCancelDTO)(implicit scheduler: FusionScheduler): ResultBO = {
    var ret = false
    dto.triggerKey.foreach { key =>
      ret = scheduler.unscheduleJob(TriggerKey.triggerKey(key.name, key.group))
    }
    dto.jobKey.foreach { key =>
      val triggerKeys = scheduler.getTriggersOfJob(JobKey.jobKey(key.name, key.group)).map(_.getKey)
      ret = scheduler.unscheduleJobs(triggerKeys)
    }
    ResultBO(if (ret) IntStatus.OK else IntStatus.BAD_REQUEST)
  }

  def createJob(dto: JobDTO)(implicit scheduler: FusionScheduler): JobBO = {
    val jobDetail = JobUtils.toJobBuilder(dto).ofType(classOf[HongkaDefaultJob]).build()
    val triggerKey = TriggerKey.triggerKey(jobDetail.getKey.getName, dto.group)
    val trigger = JobUtils.toTriggerBuilder(dto, Some(triggerKey)).build()
    val createdAt = scheduler.scheduleJob(jobDetail, trigger)
    val data = jobDetail.getJobDataMap.asScala.map { case (key, value) => key.toString -> value.toString }.toMap
    val triggerBO = JobTriggerBO(
      trigger.getKey.getGroup,
      trigger.getKey.getName,
      Some(Key(jobDetail.getKey.getGroup, jobDetail.getKey.getName)),
      dto.schedule,
      createdAt.toEpochMilli)
    JobBO(dto.group, jobDetail.getKey.getName, jobDetail.getDescription, data, List(triggerBO), createdAt.toEpochMilli)
  }

  def getJob(dto: JobGetDTO)(implicit scheduler: FusionScheduler): JobBO = {
    val in = dto.in.get
    val jobKey = JobKey.jobKey(in.name, in.group)
    Option(scheduler.getJobDetail(jobKey)) match {
      case Some(jobDetail) =>
        val triggers = scheduler.getTriggersOfJob(jobKey).map { trigger =>
          JobTriggerBO(
            trigger.getKey.getGroup,
            trigger.getKey.getName,
            Some(in),
            Some(JobUtils.toTriggerSchedule(trigger)),
            UUID.fromString(trigger.getKey.getName).timestamp(),
            JobUtils.getTimesTriggered(trigger),
            Option(trigger.getNextFireTime).map(_.getTime).getOrElse(0L),
            Option(trigger.getPreviousFireTime).map(_.getTime).getOrElse(0L),
            Option(trigger.getEndTime).map(_.getTime).getOrElse(0L))
        }
        JobBO(
          jobKey.getGroup,
          jobKey.getName,
          Option(jobDetail.getDescription).getOrElse(""),
          jobDetail.getJobDataMap.asScala.map { case (k, v) => k -> v.toString }.toMap,
          triggers,
          UUID.fromString(jobKey.getName).timestamp())
      case _ => JobBO(jobKey.getGroup, jobKey.getName)
    }
  }

  def pauseJob(in: JobPauseDTO)(implicit scheduler: FusionScheduler): ResultBO = {
    in.triggerKey.foreach(key => scheduler.pauseTrigger(TriggerKey.triggerKey(key.name, key.group)))
    in.jobKey.foreach(key => scheduler.pauseJob(JobKey.jobKey(key.name, key.group)))
    ResultBO(IntStatus.OK)
  }

  def resumeJob(in: JobResumeDTO)(implicit scheduler: FusionScheduler): ResultBO = {
    in.triggerKey.foreach(key => scheduler.resumeTrigger(TriggerKey.triggerKey(key.name, key.group)))
    in.jobKey.foreach(key => scheduler.resumeJob(JobKey.jobKey(key.name, key.group)))
    ResultBO(IntStatus.OK)
  }
}
