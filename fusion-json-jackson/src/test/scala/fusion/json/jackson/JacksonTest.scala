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

package fusion.json.jackson

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
    val tree: JsonNode = Jackson.valueToTree(data)
    println(tree)
  }

}
