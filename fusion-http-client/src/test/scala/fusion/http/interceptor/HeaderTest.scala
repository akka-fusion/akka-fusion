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

package fusion.http.interceptor

import java.util.concurrent.atomic.AtomicLong

import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import fusion.test.FusionTestFunSuite
import org.scalatest.MustMatchers

import scala.collection.immutable

class HeaderTest extends FusionTestFunSuite with ScalatestRouteTest with MustMatchers {
  private val traceIdGenerator = new AtomicLong(0L)

  def generateXTraceId(headers: immutable.Seq[HttpHeader]): String =
    headers.find(_.name() == "X-Trace-Id").map(_.value()).getOrElse(traceIdGenerator.incrementAndGet().toString)

  test("X-Trace-Id") {
    val route = path("test") {
      mapRequest(request =>
        request.copy(headers = RawHeader("X-Trace-Id", generateXTraceId(request.headers)) +: request.headers)) {
        complete("test")
      }
    }
    Get("test") ~> route ~> check {
      headerValueByName("X-Trace-Id") mustBe "1"
    }
    Get("test") ~> route ~> check {
      headerValueByName("X-Trace-Id") mustBe "2"
    }
  }
}
