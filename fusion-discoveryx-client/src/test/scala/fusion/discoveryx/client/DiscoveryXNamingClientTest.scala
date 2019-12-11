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

package fusion.discoveryx.client

import java.util.concurrent.TimeUnit

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import fusion.discoveryx.model.{ InstanceQuery, InstanceRegister, InstanceRemove, ServerStatusQuery }
import helloscala.common.IntStatus
import org.scalatest.{ OptionValues, WordSpecLike }

class DiscoveryXNamingClientTest extends ScalaTestWithActorTestKit with WordSpecLike with OptionValues {
  private val namingClient = DiscoveryXNamingClient(system)
  private val namespace = "scala-meetup"
  private val serviceName = "akka"
  private val groupName = "default"

  "DiscoveryXNamingService" must {
    "serverStatus" in {
      val result = namingClient.serverStatus(ServerStatusQuery()).futureValue
      result.status should be(IntStatus.OK)
    }
    "registerInstance" in {
      val in =
        InstanceRegister(namespace, serviceName, groupName, "127.0.0.1", 8000, healthy = true)
      val reply = namingClient.registerInstance(in).futureValue
      println(reply)
      reply.status should be(IntStatus.OK)
    }
    "registerInstance2" in {
      val in =
        InstanceRegister(namespace, serviceName, groupName, "127.0.0.1", 8002, healthy = true)
      val reply = namingClient.registerInstance(in).futureValue
      println(reply)
      reply.status should be(IntStatus.OK)
    }
    "queryInstance all healthy" in {
      val in = InstanceQuery(namespace, serviceName, groupName, allHealthy = true)
      val reply = namingClient.queryInstance(in).futureValue
      reply.status should be(IntStatus.OK)
      val queried = reply.data.queried.value
      queried.instances should have size 2
    }
    "queryInstance one healthy" in {
      val in = InstanceQuery(namespace, serviceName, groupName, oneHealthy = true)
      val reply = namingClient.queryInstance(in).futureValue
      reply.status should be(IntStatus.OK)
      val queried = reply.data.queried.value
      queried.instances should have size 1
    }
    "removeInstance" in {
      val reply =
        namingClient.removeInstance(InstanceRemove(namespace, serviceName, groupName, "127.0.0.1", 8002)).futureValue
      reply.status should be(IntStatus.OK)
    }
    "queryInstance all healthy again" in {
      val in = InstanceQuery(namespace, serviceName, groupName, allHealthy = true)
      val reply = namingClient.queryInstance(in).futureValue
      reply.status should be(IntStatus.OK)
      val queried = reply.data.queried.value
      queried.instances should have size 1
      queried.instances.exists(_.namespace == namespace) shouldBe true
      queried.instances.exists(_.serviceName == serviceName) shouldBe true
      queried.instances.exists(_.groupName == groupName) shouldBe true
    }
    "hearbeat" in {
      TimeUnit.SECONDS.sleep(10)
    }
  }
}
