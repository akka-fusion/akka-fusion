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

package fusion.inject.builtin

import akka.actor.typed.{ ActorSystem, Scheduler }
import akka.stream.Materializer
import akka.{ actor => classic }
import com.typesafe.config.Config
import fusion.common.config.FusionConfigFactory
import fusion.common.constant.FusionConstants
import fusion.json.jackson.{ ScalaObjectMapper, ScalaObjectMapperExtension }
import helloscala.common.Configuration
import javax.inject.{ Inject, Provider, Singleton }

import scala.concurrent.ExecutionContextExecutor

@Singleton
class ConfigurationProvider @Inject() () extends Provider[Configuration] {
  override lazy val get: Configuration = Configuration(
    FusionConfigFactory.arrangeConfig(Configuration.fromDiscovery().underlying, FusionConstants.FUSION))
}

@Singleton
class ConfigProvider @Inject() (configuration: Configuration) extends Provider[Config] {
  override lazy val get: Config = configuration.underlying
}

@Singleton
class ActorSystemProvider @Inject() (config: Config) extends Provider[classic.ActorSystem] {
  override lazy val get: classic.ActorSystem = {
    classic.ActorSystem(config.getString("akka-name"), config)
  }
}

@Singleton
class TypedActorSystemProvider @Inject() (system: classic.ActorSystem) extends Provider[ActorSystem[_]] {
  import akka.actor.typed.scaladsl.adapter._
  override lazy val get: ActorSystem[_] = system.toTyped
}

@Singleton
class ExecutionContextExecutorProvider @Inject() (system: classic.ActorSystem)
    extends Provider[ExecutionContextExecutor] {
  override lazy val get: ExecutionContextExecutor = system.dispatcher
}

@Singleton
class MaterializerProvider @Inject() (system: classic.ActorSystem) extends Provider[Materializer] {
  override lazy val get: Materializer = Materializer.matFromSystem(system)
}

@Singleton
class SchedulerProvider @Inject() (system: classic.ActorSystem) extends Provider[Scheduler] {
  import akka.actor.typed.scaladsl.adapter._
  override lazy val get: Scheduler = system.scheduler.toTyped
}

@Singleton
class ScalaObjectMapperProvider @Inject() (system: ActorSystem[_]) extends Provider[ScalaObjectMapper] {
  override lazy val get: ScalaObjectMapper = ScalaObjectMapperExtension(system).jsonObjectMapper
}
