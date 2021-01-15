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

package fusion.cloud.consul.config

import akka.actor.typed.{ ActorSystem, ExtensionId }
import com.typesafe.config.Config
import fusion.cloud.config.FusionCloudConfig
import fusion.cloud.consul.FusionConsul
import fusion.core.extension.FusionCore

/**
 * @author Yang Jing <a href="mailto:yang.xunjing@qq.com">yangbajing</a>
 * @date 2020-12-02 11:42:25
 */
class FusionCloudConfigConsul()(implicit val system: ActorSystem[_]) extends FusionCloudConfig {
  val fusionConsul: FusionConsul = FusionConsul.fromByConfig(system.settings.config)
  FusionCore(system).shutdowns.addJvmShutdownHook(fusionConsul.close())
  override def config: Config = system.settings.config
}

object FusionCloudConfigConsul extends ExtensionId[FusionCloudConfigConsul] {
  override def createExtension(system: ActorSystem[_]): FusionCloudConfigConsul = new FusionCloudConfigConsul()(system)
}
