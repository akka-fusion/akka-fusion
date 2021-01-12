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

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.StrictLogging
import helloscala.common.Configuration

object NacosPropertiesUtils extends StrictLogging {

  @inline def configProps(path: String): NacosDiscoveryProperties = {
    Configuration(ConfigFactory.load().resolve()).get[Properties](path)
  }
}