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

package fusion.discoveryx

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter._
import akka.{ actor => classic }
import com.typesafe.config.Config
import fusion.common.{ FusionProtocol, FusionActorRefFactory }
import fusion.discoveryx.common.Constants
import helloscala.common.config.FusionConfigFactory

class DiscoveryX(val settings: DiscoveryXSettings, val config: Config, val system: ActorSystem[FusionProtocol.Command])
    extends FusionActorRefFactory {
  def classicSystem: classic.ActorSystem = system.toClassic
}

object DiscoveryX {
  def fromMergedConfig(config: Config): DiscoveryX =
    fromActorSystem(ActorSystem(FusionProtocol.behavior, Constants.DISCOVERYX, config))

  def fromActorSystem(system: ActorSystem[FusionProtocol.Command]): DiscoveryX = {
    new DiscoveryX(DiscoveryXSettings(system.settings.config), system.settings.config, system)
  }

  def fromOriginalConfig(originalConfig: Config): DiscoveryX = {
    val config = FusionConfigFactory.arrangeConfig(originalConfig, Constants.DISCOVERYX, Seq("akka"))
    fromActorSystem(ActorSystem(FusionProtocol.behavior, Constants.DISCOVERYX, config))
  }
}
