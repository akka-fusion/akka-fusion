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

package fusion.discovery.client

import akka.actor.ActorSystem
import akka.discovery.Discovery
import akka.http.scaladsl.model.Uri
import com.typesafe.scalalogging.StrictLogging
import helloscala.common.exception.HSBadGatewayException

import scala.concurrent.Future
import scala.util.Random

private class AkkaDiscoveryHttpClient(val clientSetting: DiscoveryHttpClientSetting)(implicit val system: ActorSystem)
    extends DiscoveryHttpClient
    with StrictLogging {
  private val discovery =
    clientSetting.discoveryMethod.map(Discovery(system).loadServiceDiscovery).getOrElse(Discovery(system).discovery)
  override def buildUri(uri: Uri): Future[Uri] = {
    if (uri.authority.host.isNamedHost()) {
      discovery.lookup(uri.authority.host.address(), clientSetting.discoveryTimeout).map { resolved =>
        val target = resolved.addresses match {
          case list if list.isEmpty   => throw HSBadGatewayException(s"服务没有有效的访问地址，${resolved.serviceName}")
          case list if list.size == 1 => list.head
          case list                   => list.apply(Random.nextInt(list.size))
        }
        uri.withAuthority(
          target.host,
          target.port.getOrElse(throw HSBadGatewayException(s"服务地址未指定port，${resolved.serviceName} $target")))
      }
    } else {
      Future.successful(uri)
    }
  }

}
