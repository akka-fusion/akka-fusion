package helloscala.common.jackson

import java.time.Instant

import org.scalatest.FunSuite
import org.scalatest.MustMatchers

case class Item(name: String, updatedAt: Option[Instant])

class JacksonTest extends FunSuite with MustMatchers {

  test("Instant") {
    val str = Jackson.stringify(Item("name", Some(Instant.ofEpochMilli(1554799028397L))))
    str must be("""{"name":"name","updatedAt":1554799028397}""")
  }

}
