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

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import fusion.discoveryx.model.{ InstanceRegister, ServerStatusQuery }
import helloscala.common.IntStatus
import org.scalatest.WordSpecLike

class DiscoveryXNamingClientTest extends ScalaTestWithActorTestKit with WordSpecLike {
  private val namingService = DiscoveryXNamingClient(system)
  private val namespace = "scala-meetup"
  private val serviceName = "akka"
  private val groupName = "default"

  "DiscoveryXNamingService" must {
    "serverStatus" in {
      val result = namingService.serverStatus(ServerStatusQuery()).futureValue
      result.status should be(IntStatus.OK)
    }
    "registerInstance" in {
      val in =
        InstanceRegister(namespace, serviceName, groupName, s"127.0.0.200", 50000, healthy = true)
      val reply = namingService.registerInstance(in).futureValue
      println(reply)
      reply.status should be(IntStatus.OK)
    }
  }
}
