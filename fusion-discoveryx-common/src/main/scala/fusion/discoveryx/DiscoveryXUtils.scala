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

package fusion.discoveryx

import fusion.discoveryx.model.{ Instance, InstanceRegister }
import helloscala.common.util.{ DigestUtils, StringUtils }

object DiscoveryXUtils {
  def makeInstanceId(namespace: String, serviceName: String, ip: String, port: Int): String = {
    require(StringUtils.isNoneBlank(namespace), s"namespace invalid, is: $namespace")
    require(StringUtils.isNoneBlank(serviceName), s"serviceName invalid, is: $serviceName")
    require(StringUtils.isNoneBlank(ip), s"ip invalid, is: $ip")
    require(port > 0, s"port invalid: is: $port")
    DigestUtils.sha1Hex(namespace + serviceName + ip + port)
  }

  def toInstance(in: InstanceRegister): Instance = {
    Instance(
      makeInstanceId(in.namespace, in.serviceName, in.ip, in.port),
      in.namespace,
      in.serviceName,
      in.groupName,
      in.ip,
      in.port,
      in.weight,
      in.healthy,
      in.enabled,
      in.metadata)
  }
}
