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

package fusion.http

import akka.Done
import akka.actor.ExtendedActorSystem
import fusion.common.component.Components
import fusion.common.extension.{FusionExtension, FusionExtensionId}
import fusion.core.extension.FusionCore
import fusion.http.constant.HttpConstants
import helloscala.common.Configuration

import scala.concurrent.Future

private[http] class HttpServerComponents(system: ExtendedActorSystem)
    extends Components[HttpServer](HttpConstants.PATH_DEFAULT) {
  override val configuration: Configuration = Configuration(system.settings.config)
  override protected def createComponent(id: String): HttpServer = new HttpServer(id, system)
  override protected def componentClose(c: HttpServer): Future[Done] = c.closeAsync()
}

class FusionHttpServer private (override val classicSystem: ExtendedActorSystem) extends FusionExtension {
  val components = new HttpServerComponents(classicSystem)
  def component: HttpServer = components.component
  FusionCore(classicSystem).shutdowns.serviceUnbind("StopFusionHttpServer") { () =>
    components.closeAsync()(classicSystem.dispatcher)
  }
}

object FusionHttpServer extends FusionExtensionId[FusionHttpServer] {
  override def createExtension(system: ExtendedActorSystem): FusionHttpServer = new FusionHttpServer(system)
}
