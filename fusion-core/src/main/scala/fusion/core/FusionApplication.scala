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

package fusion.core

import java.util.Objects

import akka.actor.ExtendedActorSystem
import com.typesafe.config.Config
import fusion.common.{ReceptionistFactory, SpawnFactory}
import fusion.core
import helloscala.common.Configuration

trait ClassicApplication extends SpawnFactory {
  def classicSystem: akka.actor.ActorSystem = extendedActorSystem
  def extendedActorSystem: ExtendedActorSystem
  def configuration: Configuration
  def config: Config
}

trait TypedApplication extends ClassicApplication with ReceptionistFactory

trait FusionApplication extends TypedApplication

object FusionApplication {
  private var _application: FusionApplication = _

  def application: FusionApplication = {
    Objects.requireNonNull(
      _application,
      "The configuration parameter `fusion.global-application-enable = on` needs to be set to enable this feature."
    )
  }

  private[fusion] def setApplication(application: FusionApplication): Unit = {
    _application = application
  }

  def start(): FusionApplication = {
    val config = Configuration.generateConfig()
    val constructor = Thread
      .currentThread()
      .getContextClassLoader
      .loadClass(config.getString("fusion.application-loader"))
      .getConstructor()

    constructor
      .newInstance()
      .asInstanceOf[FusionApplicationLoader]
      .load(new core.FusionApplicationLoader.Context(Configuration(config)))
  }
}
