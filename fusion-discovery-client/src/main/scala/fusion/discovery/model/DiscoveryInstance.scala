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

package fusion.discovery.model

import com.alibaba.nacos.api.common.Constants

case class DiscoveryInstance(
    ip: String,
    port: Int,
    serviceName: String,
    clusterName: String = Constants.DEFAULT_CLUSTER_NAME,
    weight: Double = 1.0d,
    healthy: Boolean = true,
    enable: Boolean = true,
    ephemeral: Boolean = true,
    metadata: Map[String, String] = Map(),
    group: String = Constants.DEFAULT_GROUP,
    // ip#port#clasterName#group@@serviceName
    instanceId: String = null) {

  def isEnabled: Boolean = enable

  def toInetAddr: String = ip + ":" + port
}
