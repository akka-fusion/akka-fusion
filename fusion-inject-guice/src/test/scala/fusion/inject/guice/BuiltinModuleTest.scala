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

package fusion.inject.guice

import akka.actor.ExtendedActorSystem
import com.fasterxml.jackson.databind.ObjectMapper
import fusion.core.util.FusionUtils
import fusion.json.jackson.http.JacksonSupport
import helloscala.common.Configuration
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class BuiltinModuleTest extends AnyFunSuite with Matchers {
  private val configuration = Configuration.load()

  private val injector = {
    import akka.actor.typed.scaladsl.adapter._
    val system = FusionUtils.createTypedActorSystem(configuration)
    new FusionInjector(configuration, new AkkaModule(configuration, system.toClassic, system))
  }

  test("ExtendedActorSystem") {
    val system = injector.instance[ExtendedActorSystem]
    println(system)
  }

  test("ActorSystem[Nothing]") {
    val system = injector.instance[akka.actor.typed.ActorSystem[_]] //(Names.named("typed"))
    println(system)
  }

  test("ObjectMapper") {
    val objectMapper = injector.instance[ObjectMapper]
    objectMapper.writeValueAsString(Map("name" -> "杨景")) shouldBe """{"name":"杨景"}"""
  }

  test("JacksonSupport") {
    val jacksonSupport = injector.instance[JacksonSupport]
    println(jacksonSupport)
  }
}
