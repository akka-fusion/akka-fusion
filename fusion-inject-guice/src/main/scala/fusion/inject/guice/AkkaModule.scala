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

import akka.actor.ExtendedActorSystem
import com.google.inject.AbstractModule
import com.google.inject.name.Names
import com.typesafe.config.Config
import helloscala.common.Configuration
import akka.actor.typed.scaladsl.adapter._

class AkkaModule(configuration: Configuration, system: ExtendedActorSystem) extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[ExtendedActorSystem]).toInstance(system)
    bind(classOf[Configuration]).toInstance(configuration)
    bind(classOf[Config]).toInstance(configuration.underlying)
    bind(classOf[akka.actor.ActorSystem]).toInstance(system)
    bind(classOf[akka.actor.typed.ActorSystem[Nothing]]).toInstance(system.toTyped)
  }
}
