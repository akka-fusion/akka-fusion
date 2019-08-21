package fusion.http.gateway.server

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.ActorSystem
import akka.discovery.Discovery
import akka.discovery.ServiceDiscovery
import akka.discovery.ServiceDiscovery.ResolvedTarget
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.Uri
import helloscala.common.Configuration
import helloscala.common.util.AsInt

import scala.collection.immutable
import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

final class ProxySetting private (
    val serviceName: String,
    val requestFunc: HttpRequest => HttpRequest,
    val fallback: Option[HttpRequest => Future[HttpResponse]],
    val scheme: String)

object ProxySetting {

  def apply(serviceName: String, fallback: HttpRequest => Future[HttpResponse]): ProxySetting =
    apply(serviceName, identity, Some(fallback), "http")

  def apply(serviceName: String, uri: Uri, fallback: Option[HttpRequest => Future[HttpResponse]]): ProxySetting =
    apply(serviceName, request => request.copy(uri = uri), fallback, "http")

  def apply(
      serviceName: String,
      requestFunc: HttpRequest => HttpRequest,
      fallback: HttpRequest => Future[HttpResponse]): ProxySetting =
    apply(serviceName, requestFunc, Some(fallback), "http")

  def apply(
      serviceName: String,
      requestFunc: HttpRequest => HttpRequest,
      fallback: Option[HttpRequest => Future[HttpResponse]],
      scheme: String): ProxySetting = new ProxySetting(serviceName, requestFunc, fallback, scheme)
}

final case class GatewayUpstream(
    name: String,
    serviceName: Option[String],
    discovery: Option[ServiceDiscovery],
    targets: Vector[ResolvedTarget]) {
  @transient val targetsCounter = new AtomicInteger(0)
}

final case class GatewayLocation(
    location: String,
    upstream: String,
    proxyUpstream: GatewayUpstream,
    timeout: FiniteDuration,
    schema: String = "http",
    proxyTo: Option[String] = None,
    gateway: Option[immutable.Seq[GatewayLocation]] = None)

final case class GatewaySetting(
    upstreams: immutable.Seq[GatewayUpstream],
    locations: immutable.Seq[GatewayLocation],
    defaultTimeout: FiniteDuration)

object GatewaySetting {

  def fromActorSystem(system: ActorSystem, prefix: String = "fusion.gateway.http.default"): GatewaySetting = {
    val c               = Configuration(system.settings.config.getConfig(prefix))
    val upstreamsConfig = c.getConfiguration("upstreams")
    val defaultTimeout  = c.get[FiniteDuration]("timeout")

    val upstreams = upstreamsConfig.subKeys.map { upstreamName =>
      val serviceName = upstreamsConfig
        .get[Option[String]](s"$upstreamName.service-name")
        .orElse(upstreamsConfig.get[Option[String]](s"$upstreamName.serviceName"))
      val discovery = upstreamsConfig
        .get[Option[String]](s"$upstreamName.discovery-method")
        .orElse(upstreamsConfig.get[Option[String]](s"$upstreamName.discoveryMethod"))
        .map(method => Discovery(system).loadServiceDiscovery(method))
        .orElse(serviceName.map(_ => Discovery(system).discovery))
      val targets = upstreamsConfig.getOrElse[Seq[String]](s"$upstreamName.targets", Nil).map(toResolvedTarget).toVector
      GatewayUpstream(upstreamName, serviceName, discovery, targets)
    }.toVector

    val locations = getLocations(upstreams, c.getConfiguration("locations"), defaultTimeout)

    GatewaySetting(upstreams, locations, defaultTimeout)
  }

  private def getLocations(
      upstreams: Seq[GatewayUpstream],
      locationsConfig: Configuration,
      defaultTimeout: FiniteDuration): Vector[GatewayLocation] = {
    locationsConfig.subKeys.map { locationName =>
      val c            = locationsConfig.getConfiguration(locationName)
      val upstreamName = c.getString("upstream")
      val proxyUpstream = upstreams
        .find(pu => pu.name == upstreamName)
        .getOrElse(throw new ExceptionInInitializerError(s"Upstream not exists, $upstreamName"))
      val timeout = c.getOrElse("timeout", defaultTimeout)
      val schema  = c.getOrElse("schema", "http")
      val proxyTo = c.get[Option[String]]("proxy-to")
//      val gateway =
//        if (c.hasPath("locations"))
//          Some(getLocations(upstreams, c.getConfiguration("locations"), defaultTimeout))
//        else None
      GatewayLocation(locationName, upstreamName, proxyUpstream, timeout, schema, proxyTo, None /*gateway*/ )
    }.toVector
  }

  private def toResolvedTarget(target: String): ResolvedTarget = {
    target.trim.split(':') match {
      case Array(host)              => ResolvedTarget(host, None, None)
      case Array(host, AsInt(port)) => ResolvedTarget(host, Some(port), None)
      case other =>
        throw new ExceptionInInitializerError(s"toResolvedTarget($target) 错误，需要 host:<port>，实际：${other.mkString(":")}")
    }
  }

}
