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

package fusion.schedulerx.server

import java.util.Date

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }
import akka.actor.typed.{ ActorRef, ActorSystem, Behavior }
import akka.util.Timeout
import fusion.schedulerx.SchedulerXSettings
import fusion.schedulerx.protocol._
import fusion.schedulerx.server.quartz.QuartzJob
import fusion.schedulerx.server.repository.BrokerRepository
import helloscala.common.util.TimeUtils
import org.quartz._
import org.quartz.impl.matchers.GroupMatcher

import scala.concurrent.duration._
import scala.util.{ Failure, Success }

object BrokerScheduler {
  case class Request(in: Any, replyTo: ActorRef[Response])
  def apply(
      brokerId: String,
      brokerSettings: BrokerSettings,
      settings: SchedulerXSettings,
      brokerRepository: BrokerRepository,
      brokerRef: ActorRef[Broker.Command],
      scheduler: Scheduler): Behavior[Request] =
    Behaviors.setup(
      context =>
        new BrokerScheduler(brokerId, brokerSettings, settings, brokerRepository, brokerRef, scheduler, context)
          .receive())
}

import fusion.schedulerx.server.BrokerScheduler._
class BrokerScheduler private (
    brokerId: String,
    brokerSettings: BrokerSettings,
    settings: SchedulerXSettings,
    brokerRepository: BrokerRepository,
    brokerRef: ActorRef[Broker.Command],
    scheduler: Scheduler,
    context: ActorContext[BrokerScheduler.Request]) {
  import context.executionContext
  private implicit val system: ActorSystem[Nothing] = context.system
  implicit val timeout: Timeout = 3.seconds

  def receive(): Behavior[Request] = Behaviors.receiveMessage[Request] {
    case Request(in: GetJobInstanceListRequest, replyTo) =>
      brokerRef.ask[Broker.JobInstanceList](ref => Broker.GetJobInstanceList(in.jobId, ref)).onComplete {
        case Success(value) => replyTo ! ResponseResult.ok(value.instances)
        case Failure(e)     => replyTo ! ResponseResult.error(400, e.getMessage)
      }
      Behaviors.same

    case Request(in: GetJobInstanceRequest, replyTo) =>
      brokerRef.ask[JobInstanceDetail](ref => Broker.GetJobInstance(in.jobId, ref)).onComplete {
        case Success(instanceDetail) => replyTo ! ResponseResult.ok(instanceDetail)
        case Failure(e)              => replyTo ! ResponseResult.error(400, e.getMessage)
      }
      Behaviors.same

    case Request(value, replyTo) =>
      val response = try {
        processRequest(value)
      } catch {
        case e: IllegalArgumentException => ResponseResult(400, e.getMessage)
        case e: Throwable                => ResponseResult(500, e.getMessage)
      }
      replyTo ! response
      Behaviors.same
  }

  def processRequest(value: Any): Response = value match {
    case in: CreateJobRequest  => registerJob(in)
    case in: GetJobInfoRequest => getJobInfo(in)
    case in: DeleteJobRequest  => deleteJobInfo(in)
    case in: EnableJobRequest  => enableJobInfo(in)
    case in: DisableJobRequest => disableJobInfo(in)
    case in: ExecuteJobRequest => executeJobInfo(in)
    case in: StopJobRequest    => stopJobInfo(in)
  }

  def stopJobInfo(in: StopJobRequest): ResponseResult = {
    brokerRef ! Broker.KillJobInstance(in.instanceId)
    ResponseResult.OK
  }

  def executeJobInfo(in: ExecuteJobRequest): ResponseResult = {
    val data = new JobDataMap()
    for ((key, value) <- in.instanceParameters) {
      data.put(key, value)
    }
    scheduler.triggerJob(JobKey.jobKey(in.jobId, in.groupId), data)
    ResponseResult.OK
  }
  def disableJobInfo(in: DisableJobRequest): ResponseResult = {
    scheduler.pauseJob(JobKey.jobKey(in.jobId, in.groupId))
    brokerRepository.disableJobInfo(in) match {
      case 1 => ResponseResult.OK
      case 0 => ResponseResult.NotFound
    }
  }

  def enableJobInfo(in: EnableJobRequest): ResponseResult = {
    scheduler.resumeJob(JobKey.jobKey(in.jobId, in.groupId))
    brokerRepository.enableJobInfo(in) match {
      case 1 => ResponseResult.OK
      case 0 => ResponseResult.NotFound
    }
  }

  def deleteJobInfo(in: DeleteJobRequest): ResponseResult = {
    scheduler.deleteJob(JobKey.jobKey(in.jobId, in.groupId))
    brokerRepository.deleteJobInfo(in) match {
      case 1 => ResponseResult.OK
      case 0 => ResponseResult.NotFound
    }
  }
  def getJobInfo(in: GetJobInfoRequest): ResponseResult = {
    brokerRepository.getJobInfo(in) match {
      case Some(info) => ResponseResult.ok(info)
      case None       => ResponseResult.NotFound
    }
  }

  def registerJob(in: CreateJobRequest): ResponseResult = {
    val jb = JobBuilder.newJob(classOf[QuartzJob]).withIdentity(in.name, in.groupId)
    in.description.foreach(jb.withDescription)

    val tb = TriggerBuilder.newTrigger().withIdentity(in.name, in.groupId)
    val t = in.timeType.getOrElse(1)
    require(t < 3, s"Field 'timeType' in [1, 2], invalid value is ${in.timeType}.")
    t match {
      case 1 => // cron
        tb.withSchedule(CronScheduleBuilder.cronSchedule(in.timeExpression.get))
      case 2 => // simple
        tb.withSchedule(SimpleScheduleBuilder.repeatSecondlyForever())
    }
    in.triggerStartTime.foreach(odt => tb.startAt(Date.from(odt.toInstant)))
    in.triggerEndTime.foreach(odt => tb.endAt(Date.from(odt.toInstant)))

    val date = scheduler.scheduleJob(jb.build(), tb.build())
    date.toInstant.atOffset(TimeUtils.DEFAULT_OFFSET)
    ResponseResult.OK
  }
}
