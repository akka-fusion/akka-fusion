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

package fusion.scheduler.util

import java.util.TimeZone
import java.util.concurrent.TimeUnit

import fusion.scheduler.model.JobDTO
import fusion.scheduler.model.ScheduleType
import fusion.scheduler.model.TriggerSchedule
import helloscala.common.exception.HSBadRequestException
import helloscala.common.util.TimeUtils
import helloscala.common.util.Utils
import org.quartz._

import scala.concurrent.duration.Duration
import scala.jdk.CollectionConverters._

object JobUtils {

  def getTimesTriggered(trigger: Trigger): Long = {
    trigger match {
      case simple: SimpleTrigger => simple.getTimesTriggered
      case _                     => 0
    }
  }

  def toTriggerSchedule(trigger: Trigger): TriggerSchedule = {
    val schedule = trigger match {
      case cron: CronTrigger =>
        TriggerSchedule(
          ScheduleType.CRON,
          cronExpression = Option(cron.getCronExpression),
          timezone = Option(cron.getTimeZone.toZoneId.toString))
      case simple: SimpleTrigger =>
        TriggerSchedule(
          ScheduleType.CRON,
          Option(Duration(simple.getRepeatInterval, TimeUnit.MILLISECONDS).toString()),
          Option(simple.getRepeatCount))
    }
    schedule.copy(
      misfireInstruction = Option(trigger.getMisfireInstruction),
      startAt = Some(TimeUtils.toLocalDateTime(trigger.getStartTime).format(TimeUtils.formatterDateTime)),
      triggerPriority = Some(trigger.getPriority))
  }

  def toJobBuilder(dto: JobDTO): JobBuilder = {
    JobBuilder
      .newJob()
      .storeDurably(dto.durable.getOrElse(true))
      .withIdentity(JobKey.jobKey(Utils.timeBasedUuid().toString, dto.group))
      .withDescription(dto.description.getOrElse(""))
      .setJobData(Option(dto.data).map(data => new JobDataMap(data.asJava)).getOrElse(new JobDataMap()))
  }

  def toTriggerBuilder(dto: JobDTO, triggerKey: Option[TriggerKey] = None): TriggerBuilder[Trigger] = {
    val b = TriggerBuilder
      .newTrigger()
      .withIdentity(triggerKey.getOrElse(TriggerKey.triggerKey(Utils.timeBasedUuid().toString, dto.group)))
      .withDescription(dto.description.getOrElse(""))

    val schedule = dto.schedule.getOrElse(throw HSBadRequestException("调度配置未设置：schedule"))
    val builder = schedule.`type` match {
      case ScheduleType.SIMPLE => generateSimpleSchedule(schedule)
      case ScheduleType.CRON   => generateCronSchedule(schedule)
      case other               => throw HSBadRequestException(s"无效的Schedule调度配置: $other")
    }
    b.withSchedule(builder)

    schedule.startAt match {
      case None =>
        b.startNow()
      case Some(startAt) =>
        val startTime = java.util.Date.from(TimeUtils.toLocalDateTime(startAt).toInstant(TimeUtils.ZONE_CHINA_OFFSET))
        b.startAt(startTime)
    }

    schedule.triggerPriority.foreach(b.withPriority)

    b
  }

  private def generateCronSchedule(cron: TriggerSchedule): CronScheduleBuilder = {
    val ss = CronScheduleBuilder.cronSchedule(
      cron.cronExpression.getOrElse(throw HSBadRequestException("未设置日历调度表达示，schedule.cronExpression")))
    cron.timezone.foreach { timezone =>
      val tz = TimeZone.getTimeZone(timezone)
      ss.inTimeZone(tz)
    }
    cron.misfireInstruction.foreach {
      case Trigger.MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY => ss.withMisfireHandlingInstructionIgnoreMisfires()
      case CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING        => ss.withMisfireHandlingInstructionDoNothing()
      case CronTrigger.MISFIRE_INSTRUCTION_FIRE_ONCE_NOW     => ss.withMisfireHandlingInstructionFireAndProceed()
      case other                                             => throw HSBadRequestException(s"CronSchedule 无效的MISFIRE值：$other")
    }
    ss
  }

  private def generateSimpleSchedule(simple: TriggerSchedule): SimpleScheduleBuilder = {
    val ss = SimpleScheduleBuilder.simpleSchedule()
    simple.interval match {
      case Some(interval)                      => ss.withIntervalInMilliseconds(Duration(interval).toMillis)
      case _ if simple.repeatCount.contains(0) => ss.withIntervalInMilliseconds(1L)
      case _                                   => throw HSBadRequestException("interval 未指定时必需设置 repeatCount 为 0")
    }
    if (simple.repeatCount.exists(_ > -1)) {
      ss.withRepeatCount(simple.repeatCount.get)
    } else {
      ss.repeatForever()
    }
    simple.misfireInstruction.foreach {
      case SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW        => ss.withMisfireHandlingInstructionFireNow()
      case Trigger.MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY => ss.withMisfireHandlingInstructionIgnoreMisfires()
      case SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_EXISTING_COUNT =>
        ss.withMisfireHandlingInstructionNextWithExistingCount()
      case SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT =>
        ss.withMisfireHandlingInstructionNextWithRemainingCount()
      case SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NOW_WITH_EXISTING_REPEAT_COUNT =>
        ss.withMisfireHandlingInstructionNowWithExistingCount()
      case SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NOW_WITH_REMAINING_REPEAT_COUNT =>
        ss.withMisfireHandlingInstructionNowWithRemainingCount()
      case other => throw HSBadRequestException(s"SimpleSchedule 无效的MISFIRE值：$other")
    }
    ss
  }

}
