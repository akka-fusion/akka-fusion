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

package fusion.common.util

import java.nio.file.Paths

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import helloscala.common.util.DigestUtils
import org.scalatest.FunSuiteLike

import scala.concurrent.Await
import scala.concurrent.duration._

class StreamUtilsTest extends ScalaTestWithActorTestKit with FunSuiteLike {
  private implicit val ec = system.executionContext
  private val PROJECT_BASE = sys.props("user.dir")

  test("digest") {
    fromPath(s"$PROJECT_BASE/README.md") shouldBe fromPathFuture(s"$PROJECT_BASE/README.md")
  }

  private def fromPath(path: String) = DigestUtils.sha256HexFromPath(Paths.get(path))
  private def fromPathFuture(path: String) = Await.result(StreamUtils.reactiveSha256Hex(Paths.get(path)), 10.seconds)
}
