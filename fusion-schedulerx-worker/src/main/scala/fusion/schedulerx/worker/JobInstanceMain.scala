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

package fusion.schedulerx.worker

import java.io.File

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ ActorRef, ActorRefResolver }
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.StrictLogging
import fusion.common.FusionProtocol
import fusion.json.jackson.Jackson
import fusion.schedulerx.SchedulerX
import fusion.schedulerx.protocol.{ JobInstanceData, Worker }
import fusion.schedulerx.worker.job.JobInstance
import fusion.schedulerx.worker.job.JobInstance.JobCommand

import scala.concurrent.duration._
import scala.concurrent.{ Await, ExecutionContext }

object JobInstanceMain extends StrictLogging {
  def main(args: Array[String]): Unit = {
    val schedulerX = SchedulerX.fromOriginalConfig(ConfigFactory.load())
    implicit val system = schedulerX.system
    implicit val timeout: Timeout = 5.seconds
    val instData = Jackson.defaultObjectMapper.treeToValue[JobInstanceData](
      Jackson.defaultObjectMapper.readTree(new File(schedulerX.schedulerXSettings.worker.runDir.get)))
    val worker = ActorRefResolver(schedulerX.system)
      .resolveActorRef[Worker.Command](schedulerX.schedulerXSettings.worker.runJobWorkerActor.get)
    val future = schedulerX.system
      .ask[ActorRef[JobCommand]](FusionProtocol.Spawn(JobInstance(worker, instData), instData.instanceId))
    future.failed.foreach { e =>
      val v = Jackson.defaultObjectMapper.writeValueAsString(instData)
      logger.error(s"Startup JobInstance failure: ${e.getMessage}, JobInstanceData: $v", e)
      try {
        system.terminate()
        Await.ready(system.whenTerminated, 60.seconds)
      } finally System.exit(-1)
    }(ExecutionContext.Implicits.global)
  }
}
