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

package fusion.discovery.client.nacos

import akka.actor.ExtendedActorSystem
import akka.discovery.ServiceDiscovery.Resolved
import akka.discovery.ServiceDiscovery.ResolvedTarget
import akka.discovery.Lookup
import akka.discovery.ServiceDiscovery
import com.alibaba.nacos.api.exception.NacosException
import com.typesafe.scalalogging.StrictLogging
import fusion.core.extension.FusionCore

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.Await
import scala.concurrent.Future

class NacosServiceDiscovery(system: ExtendedActorSystem) extends ServiceDiscovery with StrictLogging {
  import system.dispatcher
  private val namingService = FusionNacos(system).component.namingService
  private val c = FusionCore(system).configuration.getConfiguration("akka.discovery.nacos")
  private def oneHealth = c.getBoolean("one-health")

  override def lookup(lookup: Lookup, resolveTimeout: FiniteDuration): Future[ServiceDiscovery.Resolved] = {
    val f = Future {
      val instances = if (oneHealth) {
        val instance = namingService.selectOneHealthyInstance(lookup.serviceName)
        Vector(ResolvedTarget(instance.ip, Some(instance.port), None))
      } else {
        namingService
          .selectInstances(lookup.serviceName, true)
          .map(instance => ResolvedTarget(instance.ip, Some(instance.port), None))
          .toVector
      }
      Resolved(lookup.serviceName, instances)
    }.recover {
      case e: NacosException =>
        logger.debug(s"Nacos服务 ${lookup.serviceName} 未能找到；${e.toString}")
        Resolved(lookup.serviceName, Nil)
    }
    Await.ready(f, resolveTimeout)
  }

}
