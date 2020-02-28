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

package fusion.actuator

import akka.actor.typed.scaladsl.adapter._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import fusion.json.jackson.JacksonObjectMapperExtension
import fusion.testkit.FusionFunSuiteLike

class FusionActuatorTest extends FusionFunSuiteLike with ScalatestRouteTest {
  test("route") {
    val fusion = FusionActuator(system.toTyped)
    val route = fusion.route
    val contextPath = fusion.actuatorSetting.contextPath
    Get(s"/$contextPath/health") ~> route ~> check {
      status shouldBe StatusCodes.OK
      val text = responseAs[String]
      val json = JacksonObjectMapperExtension(system).objectMapperJson.readTree(text)
      text should not be empty
      json.hasNonNull("details") shouldBe true
    }
  }

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    FusionActuator(system.toTyped)
  }
}
