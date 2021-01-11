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

package fusion.inject.guice

import java.util.concurrent.atomic.AtomicBoolean

import akka.actor.typed._
import akka.actor.typed.scaladsl.adapter._
import akka.actor.{ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider}
import akka.util.Timeout
import com.google.inject.Key
import com.typesafe.config.Config
import fusion.common.FusionProtocol
import fusion.common.constant.FusionKeys
import fusion.core.FusionApplication
import fusion.inject.InjectSupport
import helloscala.common.Configuration
import javax.inject.Named

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.reflect.ClassTag

trait GuiceApplication extends FusionApplication with InjectSupport with Extension { self =>

  val injector: FusionInjector

  override lazy val configuration: Configuration = instance[Configuration]

  if (configuration.getOrElse(FusionKeys.GLOBAL_APPLICATION_ENABLE, false)) {
    FusionApplication.setApplication(self)
  }
  GuiceApplication.setApplication(self)

  override def config: Config = configuration.underlying

  override def extendedActorSystem: ExtendedActorSystem = instance[ExtendedActorSystem]

  override def typedSystem: ActorSystem[Nothing] = instance[ActorSystem[_]]

  override def instance[T](implicit ev: ClassTag[T]): T = injector.instance[T]

  override def instance[T](a: Named)(implicit ev: ClassTag[T]): T = injector.instance[T](a)

  override def getInstance[T](c: Class[T]): T = injector.getInstance(c)

  override def getInstance[T](c: Class[T], a: Named): T = injector.getInstance(Key.get(c, a))
}

object GuiceApplication extends ExtensionId[GuiceApplication] with ExtensionIdProvider {
  private var _application: GuiceApplication = _
  private val notSet = new AtomicBoolean(true)

  private[guice] def setApplication(app: GuiceApplication): Unit = {
    if (notSet.compareAndSet(true, false)) {
      _application = app
    } else {
      throw new ExceptionInInitializerError("The GuiceApplication can only be set once.")
    }
  }

  override def createExtension(system: ExtendedActorSystem): GuiceApplication = {
    val application = _application
    require(
      application.extendedActorSystem eq system,
      "The [[ActorSystem]] passed in is not the same instance as the [[FusionApplication#ActorSystem]]."
    )
    application
  }

  override def lookup(): ExtensionId[_ <: Extension] = GuiceApplication
}

class ClassicGuiceApplication(val injector: FusionInjector) extends GuiceApplication {

  override def spawn[T](behavior: Behavior[T], props: Props): ActorRef[T] =
    classicSystem.spawnAnonymous(behavior, props)

  override def spawn[T](behavior: Behavior[T], name: String, props: Props): ActorRef[T] =
    classicSystem.spawn(behavior, name, props)
}

class TypedGuiceApplication(val injector: FusionInjector) extends GuiceApplication {
  import akka.actor.typed.scaladsl.AskPattern._
  implicit private val timeout: Timeout = 5.seconds
  implicit private val scheduler: Scheduler = typedSystem.scheduler

  override def spawn[T](behavior: Behavior[T], props: Props): ActorRef[T] = _spawn(behavior, null, props)

  override def spawn[T](behavior: Behavior[T], name: String, props: Props): ActorRef[T] = _spawn(behavior, name, props)

  private def _spawn[T](behavior: Behavior[T], name: String, props: Props): ActorRef[T] = {
    val f = typedSystem
      .unsafeUpcast[FusionProtocol.Command]
      .ask[ActorRef[T]](replyTo => FusionProtocol.Spawn(behavior, name, props, replyTo))
    Await.result(f, timeout.duration)
  }
}
