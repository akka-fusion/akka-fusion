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

package fusion.discovery

import fusion.discovery.client.FusionConfigService
import fusion.discovery.client.nacos.NacosConstants
import fusion.discovery.client.nacos.NacosPropertiesUtils
import fusion.discovery.client.nacos.NacosServiceFactory
import helloscala.common.Configuration

object DiscoveryUtils {
  lazy val METHOD: String = Configuration.load().getOrElse[String](DiscoveryConstants.CONF_METHOD, NacosConstants.NAME)

  lazy val defaultConfigService: FusionConfigService =
    try {
      NacosServiceFactory.configService(NacosPropertiesUtils.configProps(methodConfPath))
    } catch {
      case e: Throwable => throw new Error(s"获取ConfigService失败，$METHOD", e)
    }

  def methodConfPath = s"${DiscoveryConstants.CONF_PATH}.$METHOD"
}
