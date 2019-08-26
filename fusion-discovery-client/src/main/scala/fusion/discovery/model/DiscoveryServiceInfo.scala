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

case class DiscoveryServiceInfo(
    name: String,
    groupName: String,
    clusters: String,
    cacheMillis: Long = 1000L,
    hosts: Seq[DiscoveryInstance] = Nil,
    lastRefTime: Long = 0L,
    checksum: String = "",
    allIPs: Boolean = false) {
  def ipCount: Int = hosts.size
  def expired: Boolean = System.currentTimeMillis - lastRefTime > cacheMillis
  def isValid: Boolean = hosts != null
}
