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

package fusion.core.util

import java.util.concurrent.atomic.AtomicLong

import akka.actor.{typed, ActorSystem}
import com.typesafe.config.Config
import fusion.common.FusionProtocol
import fusion.common.constant.{FusionConstants, FusionKeys}
import helloscala.common.Configuration

object FusionUtils {
  private val _traceIdGenerator = new AtomicLong(0)

  def generateTraceId(): Long = _traceIdGenerator.incrementAndGet()

  def createFromDiscovery(): ActorSystem = createActorSystem(Configuration.fromDiscovery())

  def createTypedActorSystem(configuration: Configuration): typed.ActorSystem[FusionProtocol.Command] =
    akka.actor.typed.ActorSystem(FusionProtocol.behavior, getName(configuration.underlying))

  def createActorSystem(configuration: Configuration): ActorSystem =
    createActorSystem(configuration.underlying)

  def createActorSystem(config: Config): ActorSystem =
    createActorSystem(getName(config), config)

  def createActorSystem(name: String, config: Configuration): ActorSystem =
    createActorSystem(name, config.underlying)

  def createActorSystem(name: String, config: Config): ActorSystem = ActorSystem(name, config)

  @inline def getName(config: Config): String =
    if (config.hasPath(FusionKeys.AKKA_NAME)) config.getString(FusionKeys.AKKA_NAME)
    else FusionConstants.FUSION
}
