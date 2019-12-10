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

package fusion.discoveryx.server.naming

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.serialization.Serialization
import akka.serialization.jackson.JacksonObjectMapperProvider
import fusion.core.extension.FusionCore
import fusion.discoveryx.model.{ InstanceRegister, InstanceReply }
import org.scalatest.WordSpecLike

class NamingsTest extends ScalaTestWithActorTestKit with WordSpecLike {
  val core = FusionCore(system)
  "Namings" must {
    "jackson" in {
      Serialization.withTransportInformation(core.classicSystem) { () =>
        val mapper = JacksonObjectMapperProvider(system).getOrCreate("json-jackson", None)
        val replyTo = system.deadLetters[InstanceReply]
        val str =
          mapper
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(Namings.RegisterInstance(InstanceRegister("namespace", "serviceName"), replyTo))
        println(str)
        println(replyTo.path)
        println(replyTo.path.name)
        println(replyTo.path.elements)
      }
    }
  }
}
