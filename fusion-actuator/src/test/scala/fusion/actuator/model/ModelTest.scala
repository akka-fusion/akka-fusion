package fusion.actuator.model

import fusion.core.model.Health
import fusion.test.FusionTestFunSuite
import helloscala.common.jackson.Jackson
import scala.concurrent.duration._
import scala.compat.java8.DurationConverters._

class ModelTest extends FusionTestFunSuite {
  test("health") {
    val health = Health(
      "UP",
      Map(
        "diskSpace"    -> Health.up("total" -> 316933124096L, "free" -> 124918386688L, "threshold" -> 10485760),
        "db"           -> Health.up("database" -> "MySQL", "hello" -> 1),
        "refreshScope" -> Health.up()))
    val jsonStr = Jackson.prettyStringify(health)
    jsonStr must include(""""total" : 316933124096""")
  }

  test("") {
    println(10.seconds)
    println(10.minutes)
    println(10.hours)
    println(10.millis)
    println(10.days)
    println(10.seconds.toJava)
    println(10.minutes.toJava)
    println(10.hours.toJava)
    println(10.millis.toJava)
    println(10.days.toJava)
  }

}
