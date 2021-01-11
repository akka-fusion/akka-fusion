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

package fusion.cloud

import akka.actor.typed.{ActorSystem, SpawnProtocol}
import com.typesafe.config.{Config, ConfigException, ConfigFactory}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
 * @author Yang Jing <a href="mailto:yang.xunjing@qq.com">yangbajing</a>
 * @date   2021-01-11 14:33:30
 */
object FusionConfigFactory {

  def fromByConfig(localConfig: Config = ConfigFactory.load()): FusionFactory = {
    val localSystem =
      ActorSystem(SpawnProtocol(), "localTemp", ConfigFactory.parseString("akka.actor.provider = local"))
    try {
      val fqcn = localConfig.getString("fusion.config-factory")
      localSystem.dynamicAccess
        .getObjectFor[FusionConfigFactory](fqcn)
        .getOrElse(throw new ExceptionInInitializerError("Configuration 'fusion.config-factory' not exists."))
        .createConfig(localConfig)
    } catch {
      case e: ConfigException =>
        throw new ExceptionInInitializerError(e)
    } finally {
      localSystem.terminate()
      Await.result(localSystem.whenTerminated, Duration.Inf)
    }
  }
}

trait FusionConfigFactory {
  def createConfig(localConfig: Config = ConfigFactory.load()): FusionFactory with FusionActorSystemFactory
}
