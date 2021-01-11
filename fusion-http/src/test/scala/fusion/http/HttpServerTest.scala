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

package fusion.http

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.typed.scaladsl.adapter._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, Uri}
import akka.http.scaladsl.server.Directives._
import fusion.testkit.FusionFunSuiteLike

class HttpServerTest extends ScalaTestWithActorTestKit with FusionFunSuiteLike {
  implicit private val classicSystem = system.toClassic

  private val route = path("hello") {
    complete("hello")
  } ~
    path("404") {
      complete("404")
    }

  test("bad") {
    val local = FusionHttpServer(system).component.socketAddress
    println(local.getAddress.getHostAddress)
    println(local.getHostName + " " + local.getAddress + " " + local.getHostString + " " + local.getPort)
    val request =
      HttpRequest(
        uri = Uri(s"http://${local.getHostString}:${local.getPort}/404")
          .withQuery(Uri.Query("name" -> "羊八井", "age" -> 33.toString, "username" -> "yangbajing"))
      )
    val response = Http().singleRequest(request).futureValue
    println(response)
  }

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    FusionHttpServer(system).component.startRouteSync(route)
  }
}
