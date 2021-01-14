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

package fusion.common.extension

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter._
import akka.actor.{ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider}
import helloscala.common.Configuration

// #FusionExtension
trait FusionExtension extends Extension {
  val classicSystem: ExtendedActorSystem

  val configuration: Configuration = Configuration(classicSystem.settings.config)
  def typedSystem: ActorSystem[Nothing] = classicSystem.toTyped
}
// #FusionExtension

trait FusionExtensionId[T <: FusionExtension] extends ExtensionId[T] with ExtensionIdProvider {
  def get(system: ActorSystem[_]): T = apply(system)

  override def lookup(): ExtensionId[_ <: Extension] = this
}
