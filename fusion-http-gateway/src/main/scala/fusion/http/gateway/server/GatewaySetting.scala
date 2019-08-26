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

package fusion.http.gateway.server

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.ActorSystem
import akka.discovery.Discovery
import akka.discovery.ServiceDiscovery
import akka.discovery.ServiceDiscovery.ResolvedTarget
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.headers.`Timeout-Access`
import akka.pattern.CircuitBreaker
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigMemorySize
import fusion.core.extension.FusionCore
import fusion.core.setting.CircuitBreakerSetting
import helloscala.common.Configuration
import helloscala.common.util.AsInt

import scala.collection.immutable
import scala.concurrent.duration._

final class GatewayRoutingSettings(c: Configuration) {
  def verboseErrorMessages: Option[Boolean] = c.get[Option[Boolean]]("verbose-error-messages")
  def fileGetConditional: Option[Boolean] = c.get[Option[Boolean]]("file-get-conditional")
  def renderVanityFooter: Option[Boolean] = c.get[Option[Boolean]]("render-vanity-footer")
  def rangeCountLimit: Option[Int] = c.get[Option[Int]]("range-count-limit")
  def rangeCoalescingThreshold: Option[Long] = c.get[Option[Long]]("range-coalescing-threshold")
  def decodeMaxBytesPerChunk: Option[Int] = c.get[Option[Int]]("decode-max-bytes-per-chunk")
  def decodeMaxSize: Option[Long] = c.get[Option[ConfigMemorySize]]("decode-max-size").map(_.toBytes)
  def sizeLimit: Option[Long] = c.get[Option[ConfigMemorySize]]("size-limit").map(_.toBytes)
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
    upstream: GatewayUpstream,
    timeout: FiniteDuration,
    schema: String = "http",
    proxyTo: Option[String],
    circuitBreaker: Option[CircuitBreaker],
    notProxyHeaders: Set[String],
    routingSettings: GatewayRoutingSettings,
    gateway: Option[immutable.Seq[GatewayLocation]]) {

  def proxyToUri(uri: Uri): Uri = {
    proxyTo
      .map(realProxyTo => uri.copy(path = Uri.Path(realProxyTo + uri.path.toString().drop(location.length))))
      .getOrElse(uri)
      .copy(scheme = schema)
  }
}

final class GatewaySetting(system: ActorSystem, prefix: String) {

  private val configuration = FusionCore(system).configuration
  private var _upstreams: immutable.Seq[GatewayUpstream] = _
  private var _locations: immutable.Seq[GatewayLocation] = _
  private var _defaultTimeout: FiniteDuration = _
  private val c = configuration.getConfiguration(prefix)

  init()

  def notProxyHeaders: Set[String] = c.getOrElse("not-proxy-headers", Seq(`Timeout-Access`.lowercaseName)).toSet
  def upstreams: immutable.Seq[GatewayUpstream] = _upstreams
  def locations: immutable.Seq[GatewayLocation] = _locations
  def defaultTimeout: FiniteDuration = _defaultTimeout
  private def init(): Unit = {
    val upstreamsConfig = c.getConfiguration("upstreams")
    _defaultTimeout = c.get[FiniteDuration]("timeout")
    val defaultCircuitBreaker = {
      val circuitBreakerSetting = CircuitBreakerSetting(
        if (c.hasPath("default-circuit-breaker"))
          c.getConfiguration("default-circuit-breaker").withFallback(deftCircuitBreakerConf)
        else deftCircuitBreakerConf)
      if (circuitBreakerSetting.enable)
        Some(
          CircuitBreaker(
            system.scheduler,
            circuitBreakerSetting.maxFailures,
            circuitBreakerSetting.callTimeout,
            circuitBreakerSetting.resetTimeout))
      else None
    }

    _upstreams = upstreamsConfig.subKeys.map { upstreamName =>
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

    _locations = getLocations(upstreams, c.getConfiguration("locations"), _defaultTimeout, defaultCircuitBreaker)
  }

  private def getLocations(
      upstreams: Seq[GatewayUpstream],
      locationsConfig: Configuration,
      defaultTimeout: FiniteDuration,
      defaultCircuitBreaker: Option[CircuitBreaker]): Vector[GatewayLocation] = {
    locationsConfig.subKeys.map { locationName =>
      val c = locationsConfig.getConfiguration(locationName)
      val upstreamName = c.getString("upstream")
      val proxyUpstream = upstreams
        .find(pu => pu.name == upstreamName)
        .getOrElse(throw new ExceptionInInitializerError(s"upstream不存在, $upstreamName"))
      val timeout = c.getOrElse("timeout", defaultTimeout)
      val schema = c.getOrElse("schema", "http")
      val proxyTo = c.get[Option[String]]("proxy-to")

      val circuitBreaker = defaultCircuitBreaker.orElse(
        CircuitBreakerSetting.getCircuitBreaker(system, s"$prefix.locations.$locationName.circuit-breaker"))

      val notProxyHeaders = c.getOrElse("not-proxy-headers", Seq[String]()).toSet
      val routingSettings =
        new GatewayRoutingSettings(c.getOrElse("routing-settings", Configuration(ConfigFactory.parseString("{}"))))
      //      val gateway =
      //        if (c.hasPath("locations"))
      //          Some(getLocations(upstreams, c.getConfiguration("locations"), defaultTimeout))
      //        else None
      GatewayLocation(
        locationName,
        proxyUpstream,
        timeout,
        schema,
        proxyTo,
        circuitBreaker,
        notProxyHeaders,
        routingSettings,
        None /*gateway*/ )
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

  private def deftCircuitBreakerConf = configuration.getConfiguration("fusion.default.circuit-breaker")
}

object GatewaySetting {

  def fromActorSystem(system: ActorSystem, prefix: String): GatewaySetting = new GatewaySetting(system, prefix)

}
