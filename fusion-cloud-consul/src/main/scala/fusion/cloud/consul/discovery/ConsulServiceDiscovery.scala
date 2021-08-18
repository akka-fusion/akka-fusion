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

import akka.actor.ActorSystem
import akka.actor.typed.scaladsl.adapter._
import akka.discovery.ServiceDiscovery.{ Resolved, ResolvedTarget }
import akka.discovery.{ Lookup, ServiceDiscovery }
import akka.pattern.after
import com.orbitz.consul.async.ConsulResponseCallback
import com.orbitz.consul.model.ConsulResponse
import com.orbitz.consul.model.catalog.CatalogService
import com.orbitz.consul.option.QueryOptions
import fusion.cloud.consul.config.FusionCloudConsul

import java.net.InetAddress
import java.util
import java.util.concurrent.TimeoutException
import scala.collection.immutable.Seq
import scala.collection.mutable
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ ExecutionContext, Future, Promise }
import scala.jdk.CollectionConverters._
import scala.util.Try

/**
 * @author Yang Jing <a href="mailto:yang.xunjing@qq.com">yangbajing</a>
 * @since   2021-01-14 17:45:25
 */
class ConsulServiceDiscovery(system: ActorSystem) extends ServiceDiscovery {
  import ConsulServiceDiscovery._

  private val settings = ConsulSettings.get(system)

  private val consul = FusionCloudConsul(system.toTyped).fusionConsul.consul

  override def lookup(lookup: Lookup, resolveTimeout: FiniteDuration): Future[Resolved] = {
    implicit val ec: ExecutionContext = system.dispatcher
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
    val serviceTags = catalogService.getServiceTags.asScala
    val port = findGrpcPort(serviceTags)
      .orElse(findAkkaManagementPort(serviceTags))
      .flatMap(maybePort => Try(maybePort.toInt).toOption)
    val address = catalogService.getServiceAddress
    ResolvedTarget(
      host = address,
      port = Some(port.getOrElse(catalogService.getServicePort)),
      address = Try(InetAddress.getByName(address)).toOption)
  }

  private def findAkkaManagementPort(serviceTags: mutable.Buffer[String]) = {
    serviceTags
      .find(_.startsWith(settings.applicationAkkaManagementPortTagPrefix))
      .map(_.replace(settings.applicationAkkaManagementPortTagPrefix, ""))
  }

  private def findGrpcPort(serviceTags: mutable.Buffer[String]) = {
    serviceTags
      .find(_.startsWith(settings.applicationGrpcPortTagPrefix))
      .map(_.replace(settings.applicationGrpcPortTagPrefix, ""))
  }

  private def getServicesWithTags: Future[ConsulResponse[util.Map[String, util.List[String]]]] = {
    ((callback: ConsulResponseCallback[util.Map[String, util.List[String]]]) =>
      consul.catalogClient().getServices(callback)).asFuture
  }

  private def getService(name: String) =
    ((callback: ConsulResponseCallback[util.List[CatalogService]]) =>
      consul.catalogClient().getService(name, QueryOptions.BLANK, callback)).asFuture

}

object ConsulServiceDiscovery {

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

    private val promise = Promise[ConsulResponse[T]]()

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
