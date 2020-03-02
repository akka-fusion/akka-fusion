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

import akka.actor.typed.ActorSystem
import akka.discovery.Lookup
import akka.discovery.ServiceDiscovery
import akka.discovery.ServiceDiscovery.Resolved
import akka.discovery.ServiceDiscovery.ResolvedTarget
import com.typesafe.scalalogging.StrictLogging
import fusion.core.extension.FusionCore
import helloscala.common.exception.HSBadGatewayException

import scala.concurrent.Future
import scala.concurrent.Promise
import scala.concurrent.duration.FiniteDuration
import scala.util.Success

class NacosServiceDiscovery(system: ActorSystem[_]) extends ServiceDiscovery with StrictLogging {
  import system.executionContext
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
    }
    resolveAndTimeout(lookup, resolveTimeout, f)
  }

  @inline private def resolveAndTimeout(
      lookup: Lookup,
      resolveTimeout: FiniteDuration,
      f: Future[Resolved]): Future[Resolved] = {
    val promise = Promise[Resolved]()
    val cancellable = system.scheduler.scheduleOnce(
      resolveTimeout,
      () => promise.failure(HSBadGatewayException(s"${lookup.serviceName} resolve timeout，$resolveTimeout")))
    Future.firstCompletedOf(List(f, promise.future)).andThen {
      case Success(_) if !cancellable.isCancelled => cancellable.cancel()
    }
  }
}
