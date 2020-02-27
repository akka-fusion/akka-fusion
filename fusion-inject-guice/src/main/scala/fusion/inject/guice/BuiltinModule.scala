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

package fusion.inject.guice

import java.util.concurrent.Executor

import akka.actor.typed.Scheduler
import akka.stream.Materializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.AbstractModule
import com.google.inject.name.Names
import fusion.inject.builtin._
import fusion.json.jackson.http.{ JacksonHttpHelper, JacksonSupport }
import fusion.json.jackson.{ JacksonConstants, ScalaObjectMapper }

import scala.concurrent.{ ExecutionContext, ExecutionContextExecutor }

class BuiltinModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[TypedActorSystemWrapper]).toProvider(classOf[TypedActorSystemProvider])
    bind(classOf[Materializer]).toProvider(classOf[MaterializerProvider])
    bind(classOf[ExecutionContextExecutor]).toProvider(classOf[ExecutionContextExecutorProvider])
    bind(classOf[ExecutionContext]).to(classOf[ExecutionContextExecutor])
    bind(classOf[Executor]).to(classOf[ExecutionContextExecutor])
    bind(classOf[Scheduler]).toProvider(classOf[SchedulerProvider])
    bind(classOf[ScalaObjectMapper]).toProvider(classOf[ScalaObjectMapperProvider])
    bind(classOf[ScalaObjectMapper])
      .annotatedWith(Names.named(JacksonConstants.JACKSON_JSON))
      .to(classOf[ScalaObjectMapper])
    bind(classOf[ObjectMapper]).to(classOf[ScalaObjectMapper])
    bind(classOf[ScalaObjectMapper])
      .annotatedWith(Names.named(JacksonConstants.JACKSON_CBOR))
      .toProvider(classOf[CborObjectMapperProvider])
    bind(classOf[JacksonSupport]).toProvider(classOf[JacksonSupportProvider])
    bind(classOf[JacksonHttpHelper]).toProvider(classOf[JacksonHttpHelperProvider])
  }
}
