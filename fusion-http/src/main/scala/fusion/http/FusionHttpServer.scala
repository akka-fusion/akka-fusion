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

import akka.Done
import akka.actor.ExtendedActorSystem
import akka.actor.Extension
import akka.actor.ExtensionId
import akka.actor.ExtensionIdProvider
import fusion.core.component.Components
import fusion.core.extension.FusionCore
import fusion.core.extension.FusionExtension
import fusion.http.constant.HttpConstants
import helloscala.common.Configuration

import scala.concurrent.Future

private[http] class HttpServerComponents(system: ExtendedActorSystem)
    extends Components[HttpServer](HttpConstants.PATH_DEFAULT) {
  override val configuration: Configuration = Configuration(system.settings.config)
  override protected def createComponent(id: String): HttpServer = new HttpServer(id, system)
  override protected def componentClose(c: HttpServer): Future[Done] = c.closeAsync()
}

class FusionHttpServer private (protected val _system: ExtendedActorSystem) extends FusionExtension {
  val components = new HttpServerComponents(_system)
  def component: HttpServer = components.component
  FusionCore(system).shutdowns.serviceUnbind("StopFusionHttpServer") { () =>
    components.closeAsync()(system.dispatcher)
  }
}

object FusionHttpServer extends ExtensionId[FusionHttpServer] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): FusionHttpServer = new FusionHttpServer(system)
  override def lookup(): ExtensionId[_ <: Extension] = FusionHttpServer
}
