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

package fusion.cloud.discovery.client.nacos

import java.util.Properties
import com.alibaba.nacos.api.NacosFactory
import com.typesafe.scalalogging.StrictLogging
import fusion.cloud.discovery.DiscoveryUtils
import fusion.cloud.discovery.client.{FusionConfigService, FusionNamingService}
import fusion.cloud.discovery.DiscoveryUtils
import fusion.cloud.discovery.client.{FusionConfigService, FusionNamingService}

object NacosServiceFactory extends StrictLogging {

  def configService(props: Properties): FusionConfigService =
    new NacosConfigServiceImpl(props, NacosFactory.createConfigService(props))

  def configService(serverAddr: String, namespace: String): FusionConfigService = {
    val props = new Properties()
    props.put("serverAddr", serverAddr)
    props.put("namespace", namespace)
    configService(props)
  }

  def configService(serverAddr: String): FusionConfigService =
    new NacosConfigServiceImpl(
      NacosPropertiesUtils.configProps(DiscoveryUtils.methodConfPath),
      NacosFactory.createConfigService(serverAddr)
    )

  def namingService(props: Properties): FusionNamingService =
    new NacosNamingServiceImpl(props, NacosFactory.createNamingService(props))

  def namingService(addr: String): FusionNamingService =
    new NacosNamingServiceImpl(
      NacosPropertiesUtils.configProps(DiscoveryUtils.methodConfPath),
      NacosFactory.createNamingService(addr)
    )
}