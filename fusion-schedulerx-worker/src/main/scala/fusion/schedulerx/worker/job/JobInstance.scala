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

import java.nio.file.Files
import java.time.OffsetDateTime

import akka.NotUsed
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors, TimerScheduler }
import akka.actor.typed.{ ActorRef, ActorSystem, Behavior, DispatcherSelector }
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.stream.scaladsl.{ FileIO, Source }
import akka.util.ByteString
import fusion.common.FusionProtocol
import fusion.schedulerx.Constants
import fusion.schedulerx.job.ProcessResult
import fusion.schedulerx.protocol.worker.{ WorkerCommand, WorkerJobResult }
import fusion.schedulerx.protocol.{ JobInfoData, JobType }
import fusion.schedulerx.worker.job.internal.WorkerJobContextImpl

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success, Try }

object JobInstance {
  sealed trait JobCommand extends WorkerCommand

  private case class JobResult(result: Try[ProcessResult]) extends JobCommand
  private case object JobTimeout extends JobCommand

  def apply(worker: ActorRef[WorkerCommand], jobInfo: JobInfoData): Behavior[JobCommand] =
    Behaviors.setup(context =>
      Behaviors.withTimers(timers => new JobInstance(worker, jobInfo, timers, context).runJob(context.system)))
}

import fusion.schedulerx.worker.job.JobInstance._
class JobInstance private (
    worker: ActorRef[WorkerCommand],
    jobInfo: JobInfoData,
    timers: TimerScheduler[JobCommand],
    context: ActorContext[JobCommand]) {
  timers.startSingleTimer(JobTimeout, JobTimeout, jobInfo.timeout)

  def receive(): Behavior[JobCommand] = Behaviors.receiveMessage {
    case JobTimeout =>
      worker ! WorkerJobResult(ProcessResult(503, "Timeout"))
      Behaviors.stopped
    case JobResult(value) =>
      val result = value match {
        case Success(v)         => v
        case Failure(exception) => ProcessResult(500, exception.toString)
      }
      worker ! WorkerJobResult(result)
      Behaviors.stopped
  }

  def runJob(system: ActorSystem[_]): Behavior[JobCommand] = {
    implicit val ec = system.dispatchers.lookup(DispatcherSelector.fromConfig(Constants.Dispatcher.WORKER_BLOCK))

    val jobContext = createJobContext()
    val future: Future[ProcessResult] = jobContext.jobType match {
      case JobType.JAVA => jobInfo.jarUrl.map(runJarJob(_, jobContext)).getOrElse(runClassJob(jobContext))
      case _            => runShellJob()
    }
    context.pipeToSelf(future)(JobResult)
    receive()
  }

  private def runShellJob()(implicit ec: ExecutionContext): Future[ProcessResult] = {
    implicit val system = context.system
    val codeContent =
      jobInfo.codeContent.getOrElse(throw new IllegalAccessException(s"${jobInfo.`type`} 任务需要指定 'codeContent'。"))
    val path = Files.createTempFile("", s".${jobInfo.`type`.ext}")
    Source.single(ByteString(codeContent)).runWith(FileIO.toPath(path)).map {
      case ioResult if ioResult.count > 0L => executeCommand(s"${jobInfo.`type`.program} $path")
      case _                               => throw new IllegalStateException(s"${jobInfo.`type`} 任务执行失败")
    }
  }

  private def runJarJob(jarUrl: String, jobContext: WorkerJobContextImpl)(
      implicit ec: ExecutionContext): Future[ProcessResult] = {
    import akka.actor.typed.scaladsl.adapter._
    implicit val cs = context.system.toClassic
    try {
      val mainClass =
        jobInfo.mainClass.getOrElse(throw new IllegalAccessException("${jobInfo.`type`} 作业需要指定 'mainClass'。"))
      val path = Files.createTempFile("", ".jar")
      Http()
        .singleRequest(HttpRequest(uri = jarUrl))
        .flatMap { resp =>
          val cl = resp.entity.contentLengthOption
          resp.entity.dataBytes.runWith(FileIO.toPath(path)).map {
            case ioResult if cl.contains(ioResult.count) => NotUsed
            case _                                       => throw new IllegalStateException(s"Jar包未下载完成，jar url: $jarUrl")
          }
        }
        .map { _ =>
          executeCommand(s"java -jar $path $mainClass")
        }
    } catch {
      case e: Throwable => Future.failed(e)
    }
  }

  private def runClassJob(jobContext: WorkerJobContextImpl)(implicit ec: ExecutionContext): Future[ProcessResult] = {
    try {
      val mainClass = jobInfo.mainClass.getOrElse(throw new IllegalAccessException("Java作业需要指定 'mainClass'。"))
      val clz = Class.forName(mainClass)
      val system = context.system
      val tryValue = if (classOf[MapReduceJobProcessor].isAssignableFrom(clz)) {
        system.dynamicAccess.createInstanceFor[MapReduceJobProcessor](clz, Nil)
      } else if (classOf[MapJobProcessor].isAssignableFrom(clz)) {
        system.dynamicAccess.createInstanceFor[MapJobProcessor](clz, Nil)
      } else {
        system.dynamicAccess.createInstanceFor[JobProcessor](clz, Nil)
      }
      val processor = tryValue match {
        case Success(value) => value
        case Failure(e)     => throw e
      }
      Future {
        processor.preProcess(jobContext)
        val result = processor.processor(jobContext)
        processor.postProcess(jobContext) match {
          case ProcessResult.Empty => result
          case value               => value
        }
      }
    } catch {
      case e: Throwable => Future.failed(e)
    }
  }

  private def createJobContext(): WorkerJobContextImpl = {
    WorkerJobContextImpl(
      jobInfo.id,
      jobInfo.name,
      jobInfo.`type`,
      Map(),
      Nil,
      jobInfo.schedulerTime,
      OffsetDateTime.now(),
      "",
      0,
      null,
      Map())(context.system.asInstanceOf[ActorSystem[FusionProtocol.Command]])
  }

  @throws[Exception]
  private def executeCommand(cmd: String): ProcessResult = {
    import scala.sys.process._
    val ret = cmd.!
    ProcessResult(if (ret == 0) 200 else ret, "")
  }
}
