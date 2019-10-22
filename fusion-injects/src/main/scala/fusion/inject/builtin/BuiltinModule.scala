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

package fusion.inject.builtin

import akka.actor.typed.ActorSystem
import akka.stream.Materializer
import akka.{actor => classic}
import com.google.inject.AbstractModule
import com.typesafe.config.Config
import fusion.core.extension.FusionCore
import fusion.core.util.FusionUtils
import helloscala.common.Configuration
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContextExecutor

@Singleton
class ConfigurationProvider @Inject()() extends Provider[Configuration] {
  private[this] val configuration = Configuration.fromDiscovery()
  override def get(): Configuration = configuration
}

@Singleton
class ConfigProvider @Inject()(configuration: Configuration) extends Provider[Config] {
  override def get(): Config = configuration.underlying
}

@Singleton
class ActorSystemProvider @Inject()(configuration: Configuration) extends Provider[ActorSystem[_]] {
  private[this] val system = FusionUtils.createActorSystem(configuration)
  FusionCore(system)
  sys.addShutdownHook { system.terminate() }
  override def get(): ActorSystem[_] = system
}

@Singleton
class ClassicActorSystemProvider @Inject()(system: ActorSystem[_]) extends Provider[classic.ActorSystem] {
  import akka.actor.typed.scaladsl.adapter._
  override def get(): classic.ActorSystem = system.toClassic
}

@Singleton
class ExecutionContextExecutorProvider @Inject()(system: ActorSystem[_]) extends Provider[ExecutionContextExecutor] {
  override def get(): ExecutionContextExecutor = system.executionContext
}

@Singleton
class MaterializerProvider @Inject()(system: classic.ActorSystem) extends Provider[Materializer] {
  private[this] val materializer = Materializer(system)

  override def get(): Materializer = materializer
}

class BuiltinModule extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[Configuration]).toProvider(classOf[ConfigurationProvider])
    bind(classOf[ActorSystem[_]]).toProvider(classOf[ActorSystemProvider])
    bind(classOf[classic.ActorSystem]).toProvider(classOf[ClassicActorSystemProvider])
    bind(classOf[ExecutionContextExecutor]).toProvider(classOf[ExecutionContextExecutorProvider])
    bind(classOf[ExecutionContext]).to(classOf[ExecutionContextExecutor])
    bind(classOf[Materializer]).toProvider(classOf[MaterializerProvider])
  }

}
