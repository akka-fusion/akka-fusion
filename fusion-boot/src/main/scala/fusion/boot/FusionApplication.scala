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

package fusion.boot

import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import com.typesafe.config.Config
import fusion.common.FusionProtocol
import fusion.core.util.FusionUtils
import helloscala.common.Configuration

class FusionApplication private (system: ActorSystem[FusionProtocol.Command]) {
  def run(): FusionApplication = {
    this
  }
}

object FusionApplication {
  def apply(system: ActorSystem[FusionProtocol.Command]): FusionApplication = new FusionApplication(system)

  def apply(behavior: Behavior[FusionProtocol.Command], name: String, config: Config): FusionApplication =
    apply(ActorSystem(behavior, name, config))

  def apply(behavior: Behavior[FusionProtocol.Command]): FusionApplication = {
    val configuration = Configuration.fromDiscovery()
    apply(behavior, FusionUtils.getName(configuration.underlying), configuration.underlying)
  }

  def apply(): FusionApplication = new FusionApplication(FusionUtils.createFromDiscovery())
}
