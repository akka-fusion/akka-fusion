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
import fusion.json.jackson.http.{ JacksonHttpHelper, JacksonSupport }
import fusion.json.jackson.{ JacksonObjectMapperExtension, ScalaObjectMapper }
import helloscala.common.Configuration
import javax.inject.{ Inject, Provider, Singleton }

import scala.concurrent.ExecutionContextExecutor

final class TypedActorSystemWrapper(val system: ActorSystem[_])

@Singleton
class TypedActorSystemProvider @Inject() (system: classic.ActorSystem) extends Provider[TypedActorSystemWrapper] {
  import akka.actor.typed.scaladsl.adapter._
  override def get: TypedActorSystemWrapper = new TypedActorSystemWrapper(system.toTyped)
}

@Singleton
class ExecutionContextExecutorProvider @Inject() (system: classic.ActorSystem)
    extends Provider[ExecutionContextExecutor] {
  override def get: ExecutionContextExecutor = system.dispatcher
}

@Singleton
class MaterializerProvider @Inject() (system: classic.ActorSystem) extends Provider[Materializer] {
  override def get: Materializer = Materializer.matFromSystem(system)
}

@Singleton
class SchedulerProvider @Inject() (system: classic.ActorSystem) extends Provider[Scheduler] {
  import akka.actor.typed.scaladsl.adapter._
  override def get: Scheduler = system.scheduler.toTyped
}

@Singleton
class ScalaObjectMapperProvider @Inject() (system: classic.ActorSystem) extends Provider[ScalaObjectMapper] {
  override def get: ScalaObjectMapper = JacksonObjectMapperExtension(system).objectMapperJson
}

@Singleton
class CborObjectMapperProvider @Inject() (system: classic.ActorSystem) extends Provider[ScalaObjectMapper] {
  override def get: ScalaObjectMapper = JacksonObjectMapperExtension(system).objectMapperCbor
}

@Singleton
class JacksonSupportProvider @Inject() (system: classic.ActorSystem) extends Provider[JacksonSupport] {
  override def get: JacksonSupport = JacksonObjectMapperExtension(system).jacksonSupport
}

@Singleton
class JacksonHttpHelperProvider @Inject() (system: classic.ActorSystem) extends Provider[JacksonHttpHelper] {
  override def get: JacksonHttpHelper = JacksonHttpHelper(system)
}
