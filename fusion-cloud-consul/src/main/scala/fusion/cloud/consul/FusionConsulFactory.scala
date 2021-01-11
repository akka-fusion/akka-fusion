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

package fusion.cloud.consul

import akka.actor.BootstrapSetup
import akka.actor.setup.ActorSystemSetup
import akka.actor.typed.{ActorSystem, Behavior, Props}
import com.typesafe.config.{Config, ConfigFactory}
import fusion.cloud.{FusionConfigFactory, FusionFactory}

import java.util.Objects

/**
 * @author Yang Jing <a href="mailto:yang.xunjing@qq.com">yangbajing</a>
 * @date 2020-12-01 22:48:40
 */
class FusionConsulFactory(val fusionConsul: FusionConsul, val config: Config) extends FusionFactory {
  private var _actorSystem: ActorSystem[Nothing] = _
  def actorSystem: ActorSystem[Nothing] = Objects.requireNonNull(_actorSystem, "The _actorSystem is not initialized.")

  override def initActorSystem[T](guardianBehavior: Behavior[T]): ActorSystem[T] =
    synchronized {
      if (_actorSystem == null) {
        _actorSystem = createActorSystem(guardianBehavior)
        init()
      }
      _actorSystem.asInstanceOf[ActorSystem[T]]
    }

  private def init(): Unit = {
    actorSystem.classicSystem.registerOnTermination(() => fusionConsul.close())
  }

  override private[fusion] def createActorSystem[T](guardianBehavior: Behavior[T]): ActorSystem[T] = {
    ActorSystem(
      guardianBehavior,
      config.getString("fusion.akka.name"),
      ActorSystemSetup.create(BootstrapSetup(config)),
      Props.empty
    )
  }

}

object FusionConsulFactory extends FusionConfigFactory {

  object CONFIG {
    private val BASE = "fusion.cloud.consul.config"
    val KEY = s"$BASE.key"
    val PREFIX = s"$BASE.prefix"
    val DEFAULT_CONTEXT = s"$BASE.default-context"
    val DATA_KEY = s"$BASE.data-key"
  }

  object DISCOVERY {
    private val BASE = "fusion.cloud.consul.discovery"
    val TAGS = s"$BASE.tags"
    val SECURE = s"$BASE.secure"
  }

  override def createConfig(localConfig: Config = ConfigFactory.load()): FusionConsulFactory = {
    val fusionConsul = FusionConsul.fromByConfig(localConfig)
    val key = if (localConfig.hasPath(CONFIG.KEY)) {
      localConfig.getString(CONFIG.KEY)
    } else {
      val prefix = localConfig.getString(CONFIG.PREFIX)
      val defaultContext = localConfig.getString(CONFIG.DEFAULT_CONTEXT)
      val dataKey = localConfig.getString(CONFIG.DATA_KEY)
      val profile = Option(System.getProperty("fusion.profiles.active")).orElse(
        if (localConfig.hasPath("fusion.profiles.active")) Some(localConfig.getString("fusion.profiles.active"))
        else None
      )
      prefix + "/" + defaultContext + profile.map("-" + _).getOrElse("") + "/" + dataKey
    }
    val config = fusionConsul.getConfig(key, localConfig)
    new FusionConsulFactory(fusionConsul, config)
  }

  def apply(consul: FusionConsul): FusionConsulFactory = new FusionConsulFactory(consul, consul.getConfig(CONFIG.KEY))
}
