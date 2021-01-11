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

package docs.http

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.server.Directives
import akka.{actor => classic}
import fusion.http.FusionHttpServer

// #SampleHttp
object SampleHttp extends App with Directives {
  implicit val classicSystem = classic.ActorSystem()
  val system = ActorSystem.wrap(classicSystem)

  val route = path("hello") {
    get {
      complete("Hello，Akka Fusion！")
    }
  }
  FusionHttpServer(system).component.startRouteSync(route)
}
// #SampleHttp
