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

package fusion.http.util

import java.util.concurrent.ConcurrentHashMap

import akka.actor.ActorSystem
import akka.http.scaladsl.model.Uri.Authority
import akka.stream.ActorMaterializer
import akka.testkit.TestKit
import fusion.http.HttpSourceQueue
import fusion.test.FusionTestFunSuite

class HttpUtilsTest extends TestKit(ActorSystem()) with FusionTestFunSuite {
  test("authority") {
    val a = Authority.parse("hongka-server-account")
    println(a)
    println(a.host)
    println(a.host.address())
  }

  test("testForExtension") {
    HttpUtils.customMediaTypes must not be empty
    HttpUtils.customMediaTypes.map(_._2.binary) must contain(true)
  }

  test("copyUri") {
    implicit val mat = ActorMaterializer()
    val httpSourceQueueMap = new ConcurrentHashMap[Authority, HttpSourceQueue]()
    httpSourceQueueMap.computeIfAbsent(Authority.parse("10.0.0.9:8888"), _ => {
      val q = HttpUtils.cachedHostConnectionPool("10.0.0.9", 8888, 512)
      println(s"new queue: $q")
      q
    })
    httpSourceQueueMap.computeIfAbsent(Authority.parse("10.0.0.8:8888"), _ => {
      val q = HttpUtils.cachedHostConnectionPool("10.0.0.8", 8888, 512)
      println(s"new queue: $q")
      q
    })
    httpSourceQueueMap.computeIfAbsent(Authority.parse("10.0.0.7:8097"), _ => {
      val q = HttpUtils.cachedHostConnectionPool("10.0.0.7", 8097, 512)
      println(s"new queue: $q")
      q
    })
    httpSourceQueueMap.computeIfAbsent(Authority.parse("10.0.0.9:8888"), _ => {
      val q = HttpUtils.cachedHostConnectionPool("10.0.0.9", 8888, 512)
      println(s"new queue: $q")
      q
    })
    httpSourceQueueMap.computeIfAbsent(Authority.parse("10.0.0.8:8888"), _ => {
      val q = HttpUtils.cachedHostConnectionPool("10.0.0.8", 8888, 512)
      println(s"new queue: $q")
      q
    })

    httpSourceQueueMap.forEach((a, q) => println(s"$a  <->  $q"))
  }

}
