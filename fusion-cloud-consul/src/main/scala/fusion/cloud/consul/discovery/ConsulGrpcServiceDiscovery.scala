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

package fusion.cloud.consul.discovery

import java.net.InetAddress
import java.util
import java.util.concurrent.TimeoutException
import akka.actor.ActorSystem

import scala.collection.immutable.Seq
import scala.concurrent.{ ExecutionContext, Future, Promise }
import akka.pattern.after
import com.google.common.net.HostAndPort
import com.orbitz.consul.Consul
import com.orbitz.consul.async.ConsulResponseCallback
import com.orbitz.consul.model.ConsulResponse
import ConsulGrpcServiceDiscovery._
import akka.discovery.ServiceDiscovery.{ Resolved, ResolvedTarget }
import akka.discovery.consul.ConsulSettings
import akka.discovery.{ Lookup, ServiceDiscovery }
import com.orbitz.consul.model.catalog.CatalogService
import com.orbitz.consul.option.QueryOptions

import scala.concurrent.duration.FiniteDuration
import scala.util.Try
import scala.jdk.CollectionConverters._

/**
 * @author Yang Jing <a href="mailto:yang.xunjing@qq.com">yangbajing</a>
 * @date   2021-01-14 17:45:25
 */
class ConsulGrpcServiceDiscovery(system: ActorSystem) extends ServiceDiscovery {

  private val settings = ConsulSettings.get(system)

  private val consul =
    Consul.builder().withHostAndPort(HostAndPort.fromParts(settings.consulHost, settings.consulPort)).build()

  override def lookup(lookup: Lookup, resolveTimeout: FiniteDuration): Future[Resolved] = {
    implicit val ec = system.dispatcher
    Future.firstCompletedOf(
      Seq(
        after(resolveTimeout, using = system.scheduler)(
          Future.failed(new TimeoutException(s"Lookup for [${lookup}] timed-out, within [${resolveTimeout}]!"))),
        lookupInConsul(lookup.serviceName)))
  }

  private def lookupInConsul(name: String)(implicit executionContext: ExecutionContext): Future[Resolved] = {
    val consulResult = for {
      servicesWithTags <- getServicesWithTags
      serviceIds = servicesWithTags.getResponse
        .entrySet()
        .asScala
        .filter(e => e.getValue.contains(settings.applicationNameTagPrefix + name))
        .map(_.getKey)
      catalogServices <- Future.sequence(serviceIds.map(id => getService(id).map(_.getResponse.asScala.toList)))
      resolvedTargets = catalogServices.flatten.toSeq.map(catalogService =>
        extractResolvedTargetFromCatalogService(catalogService))
    } yield resolvedTargets
    consulResult.map(targets => Resolved(name, scala.collection.immutable.Seq(targets: _*)))
  }

  private def extractResolvedTargetFromCatalogService(catalogService: CatalogService) = {
    val port = catalogService.getServiceTags.asScala
      .find(_.startsWith(settings.applicationAkkaManagementPortTagPrefix))
      .map(_.replace(settings.applicationAkkaManagementPortTagPrefix, ""))
      .flatMap { maybePort =>
        Try(maybePort.toInt).toOption
      }
    val address = catalogService.getServiceAddress
    ResolvedTarget(
      host = address,
      port = Some(port.getOrElse(catalogService.getServicePort)),
      address = Try(InetAddress.getByName(address)).toOption)
  }

  private def getServicesWithTags: Future[ConsulResponse[util.Map[String, util.List[String]]]] = {
    ((callback: ConsulResponseCallback[util.Map[String, util.List[String]]]) =>
      consul.catalogClient().getServices(callback)).asFuture
  }

  private def getService(name: String) =
    ((callback: ConsulResponseCallback[util.List[CatalogService]]) =>
      consul.catalogClient().getService(name, QueryOptions.BLANK, callback)).asFuture
}

object ConsulGrpcServiceDiscovery {

  implicit class ConsulResponseFutureDecorator[T](f: ConsulResponseCallback[T] => Unit) {

    def asFuture: Future[ConsulResponse[T]] = {
      val callback = new ConsulResponseFutureCallback[T]
      Try(f(callback)).recover[Unit] {
        case ex: Throwable => callback.fail(ex)
      }
      callback.future
    }
  }

  final case class ConsulResponseFutureCallback[T]() extends ConsulResponseCallback[T] {

    private val promise = Promise[ConsulResponse[T]]

    def fail(exception: Throwable) = promise.failure(exception)

    def future: Future[ConsulResponse[T]] = promise.future

    override def onComplete(consulResponse: ConsulResponse[T]): Unit = {
      promise.success(consulResponse)
    }

    override def onFailure(throwable: Throwable): Unit = {
      promise.failure(throwable)
    }
  }
}
