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

import java.nio.charset.StandardCharsets
import java.nio.file.{ Files, Path }
import java.time.OffsetDateTime

import akka.NotUsed
import akka.actor.typed._
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors, TimerScheduler }
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ HttpRequest, StatusCodes }
import akka.stream.scaladsl.{ FileIO, Source }
import akka.util.ByteString
import com.typesafe.config.ConfigFactory
import fusion.common.FusionProtocol
import fusion.json.jackson.Jackson
import fusion.schedulerx.job.ProcessResult
import fusion.schedulerx.protocol.{ JobInstanceDetail, JobType, Worker }
import fusion.schedulerx.worker.WorkerImpl
import fusion.schedulerx.worker.job.internal.WorkerJobContextImpl
import fusion.schedulerx.{ Constants, FileUtils, SchedulerXSettings }

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success, Try }

object JobInstance {
  sealed trait JobCommand extends Worker.Command

  private case class JobResult(result: Try[ProcessResult]) extends JobCommand
  private case object JobTimeout extends JobCommand

  def apply(worker: ActorRef[Worker.Command], instanceData: JobInstanceDetail): Behavior[JobCommand] =
    Behaviors.setup(context =>
      Behaviors.withTimers(timers => new JobInstance(worker, instanceData, timers, context).runJob(context.system)))
}

import fusion.schedulerx.worker.job.JobInstance._
class JobInstance private (
    worker: ActorRef[Worker.Command],
    instanceData: JobInstanceDetail,
    timers: TimerScheduler[JobCommand],
    context: ActorContext[JobCommand]) {
  private val settings = SchedulerXSettings(context.system.settings.config)
  private val runDir = FileUtils.createWorkerRunDirectory(instanceData.instanceId)
  timers.startSingleTimer(JobTimeout, JobTimeout, instanceData.timeout)

  def receive(): Behavior[JobCommand] = Behaviors.receiveMessage {
    case JobTimeout =>
      val result = ProcessResult(StatusCodes.InternalServerError.intValue, "Timeout")
      worker ! WorkerImpl.JobInstanceResult(instanceData.instanceId, result, context.self)
      resultWriteToDir(result)
      Behaviors.stopped
    case JobResult(value) =>
      val result = value match {
        case Success(v)         => v
        case Failure(exception) => ProcessResult(500, exception.toString)
      }
      worker ! WorkerImpl.JobInstanceResult(instanceData.instanceId, result, context.self)
      resultWriteToDir(result)
      Behaviors.stopped
  }

  def runJob(system: ActorSystem[_]): Behavior[JobCommand] = {
    implicit val ec = system.dispatchers.lookup(DispatcherSelector.fromConfig(Constants.Dispatcher.WORKER_BLOCK))

    val jobContext = createJobContext()
    var future: Future[ProcessResult] = null
    jobContext.jobType match {
      case JobType.JAVA =>
        if (settings.worker.runOnce) {
          future = runClassJob(runDir, jobContext)
        } else
          instanceData.jarUrl match {
            case Some(jarUrl) =>
              runJarJob(runDir, jarUrl, jobContext).onComplete {
                case Success(result) if result.status == 200 => // do nothing
                case value                                   => context.self ! JobResult(value)
              }
            case None => future = runClassJob(runDir, jobContext)
          }
      case _ =>
        future = runShellJob(runDir)
    }
    if (null ne future) {
      context.pipeToSelf(future)(JobResult)
    }
    receive()
  }

  private def runShellJob(runDir: Path)(implicit ec: ExecutionContext): Future[ProcessResult] = {
    implicit val system = context.system
    val codeContent =
      instanceData.codeContent.getOrElse(
        throw new IllegalAccessException(s"${instanceData.`type`} 任务需要指定 'codeContent'。"))
    val path = Files.createTempFile("", s".${instanceData.`type`.ext}")
    Source.single(ByteString(codeContent)).runWith(FileIO.toPath(path)).map {
      case ioResult if ioResult.count > 0L => executeCommand(s"${instanceData.`type`.program} $path")
      case _                               => throw new IllegalStateException(s"${instanceData.`type`} 任务执行失败")
    }
  }

  private def runJarJob(runDir: Path, jarUrl: String, jobContext: WorkerJobContextImpl)(
      implicit ec: ExecutionContext): Future[ProcessResult] = {
    import akka.actor.typed.scaladsl.adapter._
    implicit val cs = context.system.toClassic
    try {
      val configFile = runDir.resolve("application.conf")
      writeJobInstanceDir(runDir, configFile)
      val path = runDir.resolve("run-worker.jar")
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
          executeCommand(s"java -Dconfig.file=$configFile -jar $path fusion.schedulerx.worker.JobInstanceMain")
        }
    } catch {
      case e: Throwable => Future.failed(e)
    }
  }

  private def runClassJob(runDir: Path, jobContext: WorkerJobContextImpl)(
      implicit ec: ExecutionContext): Future[ProcessResult] = {
    try {
      val mainClass =
        instanceData.mainClass.getOrElse(
          throw new IllegalAccessException(s"${instanceData.`type`} 作业需要指定 'mainClass'。"))
      val clz = Class.forName(mainClass)
      val system = context.system
      val tryValue =
        if (classOf[MapReduceJobProcessor].isAssignableFrom(clz))
          system.dynamicAccess.createInstanceFor[MapReduceJobProcessor](clz, Nil)
        else if (classOf[MapJobProcessor].isAssignableFrom(clz))
          system.dynamicAccess.createInstanceFor[MapJobProcessor](clz, Nil)
        else system.dynamicAccess.createInstanceFor[JobProcessor](clz, Nil)
      val processor = tryValue match {
        case Success(value) => value
        case Failure(e)     => throw e
      }
      Future {
        // TODO MapJobProcessor 和 MapReduceJobProcessor 怎么执行？
        processor.preStart(jobContext)
        val result = processor.execute(jobContext)
        processor.postStop(jobContext) match {
          case ProcessResult.Empty => result
          case value               => value
        }
      }
    } catch {
      case e: Throwable => Future.failed(e)
    }
  }

  private def createJobContext(): WorkerJobContextImpl = {
    // TODO
    WorkerJobContextImpl(
      instanceData.instanceId,
      instanceData.name,
      instanceData.`type`,
      Map(),
      Map(),
      Nil,
      instanceData.schedulerTime,
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

  def resultWriteToDir(result: ProcessResult): Unit = {
    Files.write(runDir.resolve("result.json"), Jackson.defaultObjectMapper.writeValueAsBytes(result))
  }

  private def writeJobInstanceDir(runDir: Path, configFile: Path): Unit = {
    val config = context.system.settings.config

    val workerFormat = ActorRefResolver(context.system).toSerializationFormat(worker)

    val mergeableConfig = ConfigFactory.parseString(s"""worker {
        |  runOnce = true
        |  runJobWorkerActor = "$workerFormat"
        |  runDir = runDir
        |}
        |akka.remote.artery.canonical.port = 0
        |akka.cluster.roles = [worker]
        |""".stripMargin).withFallback(config.getConfig(Constants.SCHEDULERX))

    val content = s"${Constants.SCHEDULERX} { ${mergeableConfig.root().render()} }"
    Files.write(configFile, content.getBytes(StandardCharsets.UTF_8))
    Files.write(runDir.resolve("instance.json"), Jackson.defaultObjectMapper.writeValueAsBytes(instanceData))
  }
}
