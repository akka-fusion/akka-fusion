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

package fusion.mail

import akka.actor.ExtendedActorSystem
import akka.actor.Extension
import akka.actor.ExtensionId
import akka.actor.ExtensionIdProvider
import fusion.core.extension.FusionCore
import fusion.core.extension.FusionExtension
import helloscala.common.Configuration

class FusionMail private (protected val _system: ExtendedActorSystem) extends FusionExtension {
  val components = new MailComponents(Configuration(system.settings.config))
  def component: MailHelper = components.component
  FusionCore(system).shutdowns.beforeActorSystemTerminate("StopFusionMail") { () =>
    components.closeAsync()(system.dispatcher)
  }
}

object FusionMail extends ExtensionId[FusionMail] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): FusionMail = new FusionMail(system)
  override def lookup(): ExtensionId[_ <: Extension] = FusionMail
}
