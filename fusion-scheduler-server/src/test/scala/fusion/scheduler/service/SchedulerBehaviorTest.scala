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

import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import fusion.core.extension.FusionCore
import fusion.json.circe.CirceUtils
import fusion.protobuf.internal.ActorSystemUtils
import fusion.test.FusionTestFunSuite
import org.scalatest.BeforeAndAfterAll
import org.scalatest.Matchers
import fusion.scheduler.model._

object TestJob {
  trait JobCommand
  case class JobCreate(key: String) extends JobCommand
  trait JobResponse
  case class JobResult(key: String) extends JobResponse

  def apply(): Behavior[JobBO] = Behaviors.setup { context =>
    Behaviors.receiveMessage {
      case _ =>
        Behaviors.same
    }
  }
}

object TestGuardian {
  trait Command
  case class PBMessage(name: String, actorRefString: String) extends Command

  case class ResultBO(result: String)

  trait WCommand
  case class WrapperCommand(command: PBMessage, replyTo: ActorRef[ResultBO]) extends WCommand

  def apply(): Behavior[WCommand] = Behaviors.setup { context =>
    val jobActor = context.spawn(TestJob(), "job")
    println("jobActor: " + jobActor.path.toStringWithoutAddress)
    println("deadLetters: " + ActorSystemUtils.system.deadLetters.path.toStringWithoutAddress)
    val command = JobDTO(schedule = Some(TriggerSchedule(ScheduleType.SIMPLE)))
    val job = JobDTOReplyTO(Some(command), replyActor = jobActor)
//    Serialization.withTransportInformation(context.system.toClassic.asInstanceOf[ExtendedActorSystem]) { () =>
//      val mapper = JacksonObjectMapperProvider(context.system.toClassic).getOrCreate("jackson-json", None)
//      println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(job))
//      val str = JsonUtils.protobuf.toJsonString(job)
//      println(str)
//      println(JsonUtils.protobuf.fromJsonString[JobDTO](str))
//    }
    Behaviors.receiveMessage {
      case WrapperCommand(pbMessage, replyTo) =>
        replyTo ! ResultBO(pbMessage.name)
        Behaviors.same
    }
  }
}

class SchedulerBehaviorTest extends FusionTestFunSuite with Matchers with BeforeAndAfterAll {
  val system = ActorSystem(TestGuardian(), "test")
  FusionCore(system)

  test("ref2string") {
    val job = JobDTO(description = Some("description"), schedule = Some(TriggerSchedule(ScheduleType.CRON)))
//    Serialization.withTransportInformation(system.toClassic.asInstanceOf[ExtendedActorSystem]) { () =>
//      val mapper = JacksonObjectMapperProvider(system.toClassic).getOrCreate("jackson-json", None)
//
//      println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(job))
//      val str = JsonUtils.protobuf.toJsonString(job)
//      println(str)
//      println(JsonUtils.protobuf.fromJsonString[JobDTO]("""{}"""))
//    }

    println("circe to string: " + CirceUtils.toJsonString(job))
    println(
      "string to circe: " + CirceUtils.fromJsonString[JobDTO](
        """{"description":"description","data":{},"schedule":{"type":1},"replyActor":""}"""))
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    system.terminate()
  }
}
