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

package fusion.cloud.discovery

import helloscala.common.util.{ CollectionUtils, StringUtils }

/**
 * @author Yang Jing <a href="mailto:yang.xunjing@qq.com">yangbajing</a>
 * @since   2021-01-11 09:27:50
 */
case class ServiceInstance(
    id: String = "",
    name: String = "",
    address: Option[String] = None,
    port: Option[Int] = None,
    checks: Vector[ServiceCheck] = Vector(),
    tags: Vector[String] = Vector(),
    meta: Map[String, String] = Map()) {
  def merge(maybeInstance: Option[ServiceInstance]): ServiceInstance = maybeInstance.getOrElse(merge(this))

  def merge(other: ServiceInstance): ServiceInstance = {
    if (other eq this) this
    else {
      copy(
        id = if (StringUtils.isBlank(other.id)) this.id else other.id,
        name = if (StringUtils.isBlank(other.name)) this.name else other.name,
        address = if (other.address.isEmpty) this.address else other.address,
        port = if (other.port.isEmpty) this.port else other.port,
        checks = if (CollectionUtils.isEmpty(other.checks)) this.checks else other.checks,
        tags = if (CollectionUtils.isEmpty(other.tags)) this.tags else other.tags,
        meta = if (CollectionUtils.isEmpty(other.meta)) this.meta else other.meta)
    }
  }

}

case class ServiceCheck(
    args: Vector[String] = Vector(),
    interval: String = "",
    ttl: String = "",
    http: String = "",
    tcp: String = "",
    grpc: String = "",
    grpcUseTls: Boolean = false,
    timeout: String = "",
    notes: String = "",
    deregisterCriticalServerAfter: String = "",
    tlsSkipVerify: Boolean = false,
    status: String = "")
