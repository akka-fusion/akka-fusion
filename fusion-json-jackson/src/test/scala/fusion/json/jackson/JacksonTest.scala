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

package fusion.json.jackson

import java.time.{ Instant, LocalDateTime }

import com.fasterxml.jackson.databind.JsonNode
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import scala.collection.mutable

case class Item(name: String, updatedAt: Option[Instant], now: LocalDateTime)

class JacksonTest extends AnyFunSuite with Matchers {
  test("Instant") {
    val now = LocalDateTime.now()
    val str = Jackson.stringify(Item("name", Some(Instant.parse("2019-04-09T08:37:08.397Z")), now))
    str should startWith("""{"name":"name","updatedAt":"2019-04-09T08:37:08.397Z",""")
    println(str)

    val item = Jackson.defaultObjectMapper.readValue(str, classOf[Item])
    println(item)
  }

  test("scala map to json") {
    val data: mutable.Map[String, Object] = scala.collection.mutable.Map("name" -> "羊八井", "list" -> List("a", "b", "c"))
    val tree: JsonNode = Jackson.valueToTree(data)
    println(tree)
  }
}
