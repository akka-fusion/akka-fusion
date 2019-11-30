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

import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import com.typesafe.config.Config
import fusion.boot.FusionApplication
import fusion.core.FusionProtocol
import fusion.core.util.FusionUtils
import helloscala.common.Configuration

import scala.concurrent.Future

class FusionHttpApplication private (system: ActorSystem[FusionProtocol.Command]) {
  def run(route: Route): FusionHttpApplication = {
    FusionApplication(system).run()
    FusionHttpServer(system).component.startRouteSync(route)
    this
  }

  def httpServer: HttpServer = FusionHttpServer(system).component

  def isStarted: Boolean = httpServer.isStarted

  def isRunning: Boolean = httpServer.isRunning

  def whenBinding: Option[Future[Http.ServerBinding]] = httpServer.whenBinding
}

object FusionHttpApplication {
  def apply(system: ActorSystem[FusionProtocol.Command]): FusionHttpApplication = new FusionHttpApplication(system)

  def apply(behavior: Behavior[FusionProtocol.Command], name: String, config: Config): FusionHttpApplication =
    apply(ActorSystem(behavior, name, config))

  def apply(behavior: Behavior[FusionProtocol.Command]): FusionHttpApplication = {
    val configuration = Configuration.fromDiscovery()
    apply(behavior, FusionUtils.getName(configuration.underlying), configuration.underlying)
  }

  def apply(): FusionHttpApplication = new FusionHttpApplication(FusionUtils.createFromDiscovery())
}
