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

package fusion.core.util

import java.util.Objects
import java.util.concurrent.atomic.{ AtomicBoolean, AtomicLong }

import akka.actor.typed.{ ActorSystem, Behavior }
import com.typesafe.config.Config
import fusion.common.FusionProtocol
import fusion.common.constant.FusionConstants
import helloscala.common.Configuration

object FusionUtils {
  private var _system: ActorSystem[_] = _
  private val _isSetupSystem = new AtomicBoolean(false)
  private val _traceIdGenerator = new AtomicLong(0)

  def generateTraceId(): Long = _traceIdGenerator.incrementAndGet()

  def createFromDiscovery(): ActorSystem[FusionProtocol.Command] = createActorSystem(Configuration.fromDiscovery())

  def createActorSystem(configuration: Configuration): ActorSystem[FusionProtocol.Command] =
    createActorSystem(configuration.underlying)

  def createActorSystem(config: Config): ActorSystem[FusionProtocol.Command] =
    createActorSystem(getName(config), config)

  def createActorSystem(name: String, config: Configuration): ActorSystem[FusionProtocol.Command] =
    createActorSystem(name, config.underlying)

  def createActorSystem(name: String, config: Config): ActorSystem[FusionProtocol.Command] = {
    createActorSystem(FusionProtocol.behavior, getName(config), config)
  }

  def createActorSystem(
      behavior: Behavior[FusionProtocol.Command],
      config: Config): ActorSystem[FusionProtocol.Command] = {
    createActorSystem(behavior, getName(config), config)
  }

  def createActorSystem(
      behavior: Behavior[FusionProtocol.Command],
      name: String,
      config: Config): ActorSystem[FusionProtocol.Command] = {
    ActorSystem(behavior, getName(config), config)
  }

  def actorSystem(): ActorSystem[_] = {
    if (_isSetupSystem.get()) Objects.requireNonNull(_system)
    else throw new NullPointerException("请调用 FusionCore(system) 设置全局 ActorSystem")
  }

  private[fusion] def setupActorSystem(system: ActorSystem[_]): Unit = {
    if (_isSetupSystem.compareAndSet(false, true)) {
      _system = system
    } else {
      throw new IllegalStateException("setupActorSystem(system: ActorSystem) 函数只允许调用一次")
    }
  }

  @inline def getName(config: Config): String =
    if (config.hasPath(FusionConstants.NAME_PATH)) config.getString(FusionConstants.NAME_PATH) else FusionConstants.NAME
}
