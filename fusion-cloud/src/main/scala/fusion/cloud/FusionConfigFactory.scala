/*
 * Copyright 2019-2021 helloscala.com
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

import akka.actor.ReflectiveDynamicAccess
import com.typesafe.config.{ Config, ConfigFactory }

/**
 * @author Yang Jing <a href="mailto:yang.xunjing@qq.com">yangbajing</a>
 * @date   2021-01-11 14:33:30
 */
object FusionConfigFactory {
  val dynamicAccess = new ReflectiveDynamicAccess(Thread.currentThread().getContextClassLoader)

  def fromByConfig(localConfig: Config = ConfigFactory.load()): FusionFactory = {
    val fqcn = localConfig.getString("fusion.config-factory")
    dynamicAccess
      .getObjectFor[FusionConfigFactory](fqcn)
      .orElse(dynamicAccess.createInstanceFor[FusionConfigFactory](fqcn, Nil))
      .getOrElse(throw new ExceptionInInitializerError("Configuration 'fusion.config-factory' not exists."))
      .createConfig(localConfig)
  }
}

trait FusionConfigFactory {
  def createConfig(localConfig: Config = ConfigFactory.load()): FusionFactory with FusionActorSystemFactory
}
