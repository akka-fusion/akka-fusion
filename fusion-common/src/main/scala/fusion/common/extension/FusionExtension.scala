/*
 * Copyright 2019 akka-fusion.com
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

package fusion.common.extension

import akka.actor.ExtendedActorSystem
import akka.actor.typed.scaladsl.adapter._
import akka.actor.typed.{ ActorSystem, Extension, ExtensionId }
import fusion.common.FusionProtocol
import helloscala.common.Configuration
// #FusionExtension
trait FusionExtension extends Extension {
  val system: ActorSystem[_]

  def fusionSystem: ActorSystem[FusionProtocol.Command] = system.asInstanceOf[ActorSystem[FusionProtocol.Command]]

  def classicSystem: ExtendedActorSystem = system.toClassic.asInstanceOf[ExtendedActorSystem]

  def configuration: Configuration = Configuration(system.settings.config)
}
// #FusionExtension

trait FusionExtensionId[T <: FusionExtension] extends ExtensionId[T] {
  def get(system: ActorSystem[_]): T = apply(system)
}
