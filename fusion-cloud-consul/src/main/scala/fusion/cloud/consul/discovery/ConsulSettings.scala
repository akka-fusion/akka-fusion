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

package fusion.cloud.consul.discovery

import akka.actor._

/**
 * @author Yang Jing <a href="mailto:yang.xunjing@qq.com">yangbajing</a>
 * @since v0.0.1 2021-08-18 09:32:32
 */
final class ConsulSettings(system: ExtendedActorSystem) extends Extension {
  private val consulConfig = system.settings.config.getConfig("akka.discovery.fusion-akka-consul")

  val applicationNameTagPrefix: String = consulConfig.getString("application-name-tag-prefix")
  val applicationAkkaManagementPortTagPrefix: String =
    consulConfig.getString("application-akka-management-port-tag-prefix")
  val applicationGrpcPortTagPrefix: String =
    consulConfig.getString("application-grpc-port-tag-prefix")
}

object ConsulSettings extends ExtensionId[ConsulSettings] with ExtensionIdProvider {
  override def get(system: ActorSystem): ConsulSettings = super.get(system)

  override def get(system: ClassicActorSystemProvider): ConsulSettings = super.get(system)

  override def lookup: ConsulSettings.type = ConsulSettings

  override def createExtension(system: ExtendedActorSystem): ConsulSettings = new ConsulSettings(system)
}
