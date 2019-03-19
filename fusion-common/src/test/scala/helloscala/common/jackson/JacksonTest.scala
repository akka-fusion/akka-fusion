package helloscala.common.jackson

import java.time.Instant

import org.scalatest.FunSuite

case class Item(name: String, updatedAt: Option[Instant])

class JacksonTest extends FunSuite {

  test("Instant") {
    val str = Jackson.stringify(Item("name", Some(Instant.now())))
    println(str)
  }

}
