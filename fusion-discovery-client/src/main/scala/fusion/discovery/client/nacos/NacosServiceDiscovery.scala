package fusion.discovery.client.nacos

import akka.actor.ExtendedActorSystem
import akka.discovery.ServiceDiscovery.Resolved
import akka.discovery.ServiceDiscovery.ResolvedTarget
import akka.discovery.Lookup
import akka.discovery.ServiceDiscovery
import com.alibaba.nacos.api.exception.NacosException
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.Await
import scala.concurrent.Future

class NacosServiceDiscovery(system: ExtendedActorSystem) extends ServiceDiscovery with StrictLogging {
  import system.dispatcher
  private val namingService = FusionNacos(system).component.namingService

  override def lookup(lookup: Lookup, resolveTimeout: FiniteDuration): Future[ServiceDiscovery.Resolved] = {
    val f = Future {
      val instances = namingService
        .selectInstances(lookup.serviceName, true)
        .map(instance => ResolvedTarget(instance.ip, Some(instance.port), None))
        .toVector
      Resolved(lookup.serviceName, instances)
    }.recover {
      case e: NacosException =>
        logger.debug(s"Nacos服务 ${lookup.serviceName} 未能找到；${e.toString}")
        Resolved(lookup.serviceName, Nil)
    }
    Await.ready(f, resolveTimeout)
  }

}
