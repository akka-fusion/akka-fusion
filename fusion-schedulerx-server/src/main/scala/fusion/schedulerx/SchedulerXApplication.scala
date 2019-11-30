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

package fusion.schedulerx

import akka.actor
import akka.actor.typed.scaladsl.adapter._
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import com.typesafe.config.ConfigFactory
import fusion.schedulerx.route.Routes

object SchedulerXApplication {
  def main(args: Array[String]): Unit = {
    val schedulerX = SchedulerX(ConfigFactory.load())
    implicit val system = schedulerX.system
    SchedulerXBroker(schedulerX)
    startHttp(new Routes(system))(system.toClassic)
  }

  private def startHttp(routes: Routes)(implicit system: actor.ActorSystem): Unit = {
    val route: Route = routes.route
    val host = "127.0.0.1"
    val port = 9999
    Http().bindAndHandle(route, host, port)
  }
}
