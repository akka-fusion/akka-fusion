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

package fusion.test

import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.Millis
import org.scalatest.time.Span

import scala.concurrent.duration._

trait FusionTestSuite extends MustMatchers with OptionValues with EitherValues with ScalaFutures {
  implicit override def patienceConfig: PatienceConfig =
    PatienceConfig(scaled(Span(patienceTimeout.toMillis, Millis)), scaled(Span(15, Millis)))

  protected def patienceTimeout: FiniteDuration = 2.second
}

trait FusionTestFunSuite extends FunSuiteLike with FusionTestSuite

trait FusionTestWordSpec extends WordSpecLike with FusionTestSuite
