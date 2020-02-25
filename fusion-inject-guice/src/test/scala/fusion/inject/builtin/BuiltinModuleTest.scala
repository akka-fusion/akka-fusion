/*
 * Copyright 2019 akka-fusion.com
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

package fusion.inject.builtin

import com.fasterxml.jackson.databind.ObjectMapper
import fusion.inject.FusionInjector
import fusion.json.jackson.http.JacksonSupport
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class BuiltinModuleTest extends AnyFunSuite with Matchers {
  val injector = new FusionInjector()

  test("ObjectMapper") {
    val objectMapper = injector.instance[ObjectMapper]
    objectMapper.writeValueAsString(Map("name" -> "杨景")) shouldBe """{"name":"杨景"}"""
  }

  test("JacksonSupport") {
    val jacksonSupport = injector.instance[JacksonSupport]
    println(jacksonSupport)
  }
}
