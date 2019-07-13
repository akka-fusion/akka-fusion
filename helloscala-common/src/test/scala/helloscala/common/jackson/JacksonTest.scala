package helloscala.common.jackson

import java.time.Instant
import java.time.LocalDateTime

import com.fasterxml.jackson.databind.JsonNode
import org.scalatest.FunSuite
import org.scalatest.MustMatchers

import scala.collection.mutable

case class Item(name: String, updatedAt: Option[Instant], now: LocalDateTime)

class JacksonTest extends FunSuite with MustMatchers {

  test("Instant") {
    val now = LocalDateTime.now()
    val str = Jackson.stringify(Item("name", Some(Instant.ofEpochMilli(1554799028397L)), now))
    str must startWith("""{"name":"name","updatedAt":1554799028397""")
    println(str)

    val item = Jackson.defaultObjectMapper.readValue(str, classOf[Item])
    println(item)
  }

  test("scala map to json") {
    val data: mutable.Map[String, Object] = scala.collection.mutable.Map("name" -> "羊八井", "list" -> List("a", "b", "c"))
    val tree: JsonNode                    = Jackson.valueToTree(data)
    println(tree)
  }

}
