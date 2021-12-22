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

import akka.actor.ClassicActorSystemProvider
import akka.discovery.{ Discovery, ServiceDiscovery }
import akka.http.scaladsl.model.HttpRequest

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

/**
 * @author Yang Jing <a href="mailto:yang.xunjing@qq.com">yangbajing</a>
 * @since v0.0.1 2021-08-13 16:26:11
 */
object DiscoveryHttpUtils {
  implicit def mapToServiceDiscoveryFromSystemProvider(provider: ClassicActorSystemProvider): ServiceDiscovery =
    Discovery(provider).discovery

  def wrapperService(serviceName: String, resolveTimeout: FiniteDuration = 2.seconds)(
      implicit discovery: ServiceDiscovery,
      ec: ExecutionContext): HttpRequest = {
    discovery.lookup(serviceName, resolveTimeout).map { resolved =>
      resolved.addresses
    }
    ???
  }
}
