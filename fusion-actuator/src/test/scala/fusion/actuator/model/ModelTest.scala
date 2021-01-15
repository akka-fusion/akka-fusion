/*
 * Copyright 2019-2021 helloscala.com
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

package fusion.actuator.model

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import fusion.core.model.Health
import fusion.json.jackson.JacksonObjectMapperExtension
import fusion.testkit.FusionWordSpecLike

class ModelTest extends ScalaTestWithActorTestKit with FusionWordSpecLike {
  "model" must {
    "health" in {
      val health = Health(
        "UP",
        Map(
          "diskSpace" -> Health.up("total" -> 316933124096L, "free" -> 124918386688L, "threshold" -> 10485760),
          "db" -> Health.up("database" -> "MySQL", "hello" -> 1),
          "refreshScope" -> Health.up()))
      val jsonStr = JacksonObjectMapperExtension(system).objectMapperJson.prettyStringify(health)
      jsonStr should include(""""total" : 316933124096""")
    }
  }
}
