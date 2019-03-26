package fusion.actuator.model

import fusion.core.model.Health
import fusion.test.FusionTestFunSuite
import helloscala.common.jackson.Jackson

class ModelTest extends FusionTestFunSuite {
  test("health") {
    val health = Health(
      "UP",
      Map(
        "diskSpace"    -> Health.up("total" -> 316933124096L, "free" -> 124918386688L, "threshold" -> 10485760),
        "db"           -> Health.up("database" -> "MySQL", "hello" -> 1),
        "refreshScope" -> Health.up()))
    println(Jackson.prettyStringify(health))
  }

}
