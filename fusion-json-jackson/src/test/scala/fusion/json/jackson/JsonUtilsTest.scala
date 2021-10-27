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

package fusion.json.jackson

import org.scalatest.OptionValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.net.URL
import java.nio.file.{ Path, Paths }
import java.time._

/**
 * @author Yang Jing <a href="mailto:yang.xunjing@qq.com">yangbajing</a>
 * @since v1.1 2021-09-09 11:45:24
 */
class JsonUtilsTest extends AnyWordSpec with Matchers with OptionValues {
  private val now = OffsetDateTime.now()
  private val testObject = TestObject(
    1,
    1.0f,
    1.1,
    1L,
    "text",
    now.toInstant,
    now.toLocalDateTime,
    now,
    now.toZonedDateTime,
    now.toLocalDate,
    now.toLocalTime,
    Paths.get("/etc/hosts"),
    SubTestObject("sub", 7),
    Seq(SubTestObject("sub", 1), SubTestObject("sub", 2)),
    new URL("http://www.hjgpscm.com/index.html?a=name&b=32"))

  "JsonUtilsTest" should {
    "getInt" in {}

    "readTree" in {}

    "readValue" in {}

    "createArrayNode" in {}

    "stringify" in {
      JsonUtils.stringify(testObject) shouldBe JsonUtils.writeValueAsString(testObject)
    }

    "pretty" in {
      val text = JsonUtils.pretty(testObject)
      println(text)
      text should include("file:///etc/hosts")
    }

    "writeValueAsString" in {
      val jsonText = JsonUtils.writeValueAsString(testObject)

      val value = JsonUtils.readValue(jsonText, classOf[TestObject])
      value.bigint shouldBe testObject.bigint
      value.url.getProtocol shouldBe "http"
      value.url.getPath shouldBe "index.html"
      value.url.getQuery shouldBe "a=name&b=32"
      value.url.getAuthority shouldBe "www.hjgpscm.com"
    }

    "readObjectNode" in {}

    "valueToTree" in {}

    "createObjectNode" in {}

    "readArrayNode" in {}

    "getCopy" in {}

    "writeValueAsBytes" in {}

    "getString" in {}

    "getLong" in {}

    "treeToValue" in {}
  }
}

case class TestObject(
    integer: Int,
    singleFloat: Float,
    doubleFloat: Double,
    bigint: Long,
    text: String,
    instant: Instant,
    ldt: LocalDateTime,
    odt: OffsetDateTime,
    zdt: ZonedDateTime,
    ld: LocalDate,
    lt: LocalTime,
    path: Path,
    inner: SubTestObject,
    list: Seq[SubTestObject],
    url: URL)

case class SubTestObject(text: String, value: Int)
