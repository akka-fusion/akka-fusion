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

package fusion.cloud.config

import akka.actor.typed.Extension
import com.typesafe.config.Config

/**
 * @author Yang Jing <a href="mailto:yang.xunjing@qq.com">yangbajing</a>
 * @since 2020-12-02 11:41:25
 */
trait FusionCloudConfig extends Extension with BaseConfigProperties

trait BaseConfigProperties {
  def config: Config

  def applicationName: String = config.getString("fusion.application.name")
  def serverPort: Int = config.getInt("fusion.http.default.server.port")
  def serverHost: String = config.getString("fusion.http.default.server.host")
}
