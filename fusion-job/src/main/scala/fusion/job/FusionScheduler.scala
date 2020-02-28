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

package fusion.job

import java.time.Instant
import java.util.Date

import org.quartz._
import org.quartz.impl.matchers.GroupMatcher

import scala.jdk.CollectionConverters._

object FusionScheduler {
  def apply(scheduler: Scheduler): FusionScheduler = new FusionScheduler(scheduler)
}

final class FusionScheduler private (val scheduler: Scheduler) extends AutoCloseable {
  scheduler.start()

  def getSchedulerName: String = scheduler.getSchedulerName

  def getSchedulerInstanceId: String = scheduler.getSchedulerInstanceId

  def getContext: SchedulerContext = scheduler.getContext

  def isStarted: Boolean = scheduler.isStarted

  def getMetaData: SchedulerMetaData = scheduler.getMetaData

  def currentlyExecutingJobs: Vector[JobExecutionContext] = scheduler.getCurrentlyExecutingJobs.asScala.toVector

  def getListenerManager: ListenerManager = scheduler.getListenerManager

  def scheduleJob(jobDetail: JobDetail, trigger: Trigger): Instant = scheduler.scheduleJob(jobDetail, trigger).toInstant

  def scheduleJob(trigger: Trigger): Instant = scheduler.scheduleJob(trigger).toInstant

  def scheduleJobs(triggersAndJobs: Map[JobDetail, Set[_ <: Trigger]], replace: Boolean): Unit = {
    val payload = new java.util.HashMap[JobDetail, java.util.Set[_ <: Trigger]](triggersAndJobs.size)
    for ((key, sets) <- triggersAndJobs) {
      payload.put(key, sets.asJava)
    }
    scheduler.scheduleJobs(payload, replace)
  }

  def scheduleJob(jobDetail: JobDetail, triggersForJob: Set[_ <: Trigger], replace: Boolean): Unit = {
    scheduler.scheduleJob(jobDetail, new java.util.HashSet(triggersForJob.asJava), replace)
  }

  def unscheduleJob(triggerKey: TriggerKey): Boolean = scheduler.unscheduleJob(triggerKey)

  def unscheduleJobs(triggerKeys: Iterable[TriggerKey]): Boolean =
    scheduler.unscheduleJobs(new java.util.ArrayList(triggerKeys.asJavaCollection))

  def rescheduleJob(triggerKey: TriggerKey, newTrigger: Trigger): Date = scheduler.rescheduleJob(triggerKey, newTrigger)

  def pauseTrigger(triggerKey: TriggerKey): Unit = scheduler.pauseTrigger(triggerKey)

  def resumeTrigger(triggerKey: TriggerKey): Unit = scheduler.resumeTrigger(triggerKey)

  def pauseJob(jobKey: JobKey): Unit = scheduler.pauseJob(jobKey)

  def resumeJob(jobKey: JobKey): Unit = scheduler.resumeJob(jobKey)

  def addJob(jobDetail: JobDetail, replace: Boolean): Unit = scheduler.addJob(jobDetail, replace)

  def addJob(jobDetail: JobDetail, replace: Boolean, storeNonDurableWhileAwaitingScheduling: Boolean): Unit =
    scheduler.addJob(jobDetail, replace, storeNonDurableWhileAwaitingScheduling)

  def deleteJob(jobKey: JobKey): Boolean = scheduler.deleteJob(jobKey)

  def deleteJobs(jobKeys: Iterable[JobKey]): Boolean =
    scheduler.deleteJobs(new java.util.ArrayList(jobKeys.asJavaCollection))

  def triggerJob(jobKey: JobKey): Unit = scheduler.triggerJob(jobKey)

  def triggerJob(jobKey: JobKey, data: JobDataMap): Unit = scheduler.triggerJob(jobKey, data)

  def jobGroupNames: Vector[String] = scheduler.getJobGroupNames.asScala.toVector

  def jobKeys(matcher: GroupMatcher[JobKey]): Set[JobKey] = scheduler.getJobKeys(matcher).asScala.toSet

  def getTriggersOfJob(jobKey: JobKey): Vector[Trigger] = scheduler.getTriggersOfJob(jobKey).asScala.toVector

  def getTriggerGroupNames: Vector[String] = scheduler.getTriggerGroupNames.asScala.toVector

  def getTriggerKeys(matcher: GroupMatcher[TriggerKey]): Set[TriggerKey] =
    scheduler.getTriggerKeys(matcher).asScala.toSet

  def getPausedTriggerGroups: Set[String] = scheduler.getPausedTriggerGroups.asScala.toSet

  def getJobDetail(jobKey: JobKey): JobDetail = scheduler.getJobDetail(jobKey)

  def getTrigger(triggerKey: TriggerKey): Trigger = scheduler.getTrigger(triggerKey)

  def getTriggerState(triggerKey: TriggerKey): Trigger.TriggerState = scheduler.getTriggerState(triggerKey)

  def resetTriggerFromErrorState(triggerKey: TriggerKey): Unit = scheduler.resetTriggerFromErrorState(triggerKey)

  def addCalendar(calName: String, calendar: Calendar, replace: Boolean, updateTriggers: Boolean): Unit =
    scheduler.addCalendar(calName, calendar, replace, updateTriggers)

  def deleteCalendar(calName: String): Boolean = scheduler.deleteCalendar(calName)

  def getCalendar(calName: String): Calendar = scheduler.getCalendar(calName)

  def calendarNames: Vector[String] = scheduler.getCalendarNames.asScala.toVector

  def interrupt(jobKey: JobKey): Boolean = scheduler.interrupt(jobKey)

  def interrupt(fireInstanceId: String): Boolean = scheduler.interrupt(fireInstanceId)

  def checkExists(jobKey: JobKey): Boolean = scheduler.checkExists(jobKey)

  def checkExists(triggerKey: TriggerKey): Boolean = scheduler.checkExists(triggerKey)

  def isClosed: Boolean = scheduler.isShutdown

  def close(): Unit = scheduler.shutdown(true)
}
