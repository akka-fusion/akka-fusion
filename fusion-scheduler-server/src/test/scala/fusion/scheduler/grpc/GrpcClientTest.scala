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

package fusion.scheduler.grpc

import java.util.concurrent.TimeUnit

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.typed.scaladsl.adapter._
import akka.grpc.GrpcClientSettings
import akka.stream.Materializer
import fusion.core.extension.FusionCore
import fusion.scheduler.model._
import fusion.test.FusionTestFunSuite

class GrpcClientTest extends ScalaTestWithActorTestKit with FusionTestFunSuite {
  FusionCore(system)
  implicit private val mat = Materializer(system)
  implicit private val classicSystem = system.toClassic
  implicit private def ec = mat.executionContext
  private val clientSettings = GrpcClientSettings.fromConfig(SchedulerService.name)
  private val client = SchedulerServiceClient(clientSettings)
  private var triggerName = ""

  test("createJob") {
    val createDTO = JobDTO(
      "fusion-log",
      data = Map("$$callback$$" -> "http://127.0.0.1:8095/echo"),
      schedule = Some(TriggerSchedule(ScheduleType.SIMPLE, interval = Some("2.seconds"))))
    val createBO = client.createJob(createDTO).futureValue
    triggerName = createBO.triggers.head.name
    println(createBO)
    createDTO.group shouldBe createBO.group
    TimeUnit.SECONDS.sleep(10)
  }

  test("cancelJob") {
    val cancelDTO = JobCancelDTO(Some(Key("fusion-log", triggerName)))
    val cancelBO = client.cancelJob(cancelDTO).futureValue
    println(cancelBO)
    cancelBO.status shouldBe 200
  }

  override protected def afterAll(): Unit = {
    client.close()
    super.afterAll()
  }

}
