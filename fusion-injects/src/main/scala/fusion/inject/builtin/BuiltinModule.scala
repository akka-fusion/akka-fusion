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

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.Materializer
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
class ActorSystemProvider @Inject()(configuration: Configuration) extends Provider[ActorSystem] {
  private[this] val system = FusionUtils.createActorSystem(configuration)
  FusionCore(system)
  sys.addShutdownHook { system.terminate() }
  override def get(): ActorSystem = system
}

@Singleton
class ExecutionContextExecutorProvider @Inject()(system: ActorSystem) extends Provider[ExecutionContextExecutor] {
  override def get(): ExecutionContextExecutor = system.dispatcher
}

@Singleton
class ActorMaterializerProvider @Inject()(system: ActorSystem) extends Provider[ActorMaterializer] {
  private[this] val materializer = ActorMaterializer()(system)

  override def get(): ActorMaterializer = materializer
}

class BuiltinModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[Configuration]).toProvider(classOf[ConfigurationProvider])
    bind(classOf[ActorSystem]).toProvider(classOf[ActorSystemProvider])
    bind(classOf[ExecutionContextExecutor]).toProvider(classOf[ExecutionContextExecutorProvider])
    bind(classOf[ExecutionContext]).to(classOf[ExecutionContextExecutor])
    bind(classOf[ActorMaterializer]).toProvider(classOf[ActorMaterializerProvider])
    bind(classOf[Materializer]).to(classOf[ActorMaterializer])
  }
}
