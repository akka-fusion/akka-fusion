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

import akka.actor.typed.{ActorSystem, ExtensionId}
import akka.http.scaladsl.model.Uri
import akka.management.scaladsl.AkkaManagement
import com.orbitz.consul.model.agent.{ImmutableRegCheck, ImmutableRegistration}
import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging
import fusion.cloud.consul.FusionConsulFactory.DISCOVERY
import fusion.cloud.consul.config.FusionCloudConfigConsul
import fusion.cloud.discovery.{FusionCloudDiscovery, ServiceCheck, ServiceInstance}
import fusion.core.event.http.HttpBindingServerEvent
import fusion.core.extension.FusionCore
import helloscala.common.util.{NetworkUtils, StringUtils}

import java.net.InetSocketAddress
import java.util.concurrent.locks.ReentrantReadWriteLock
import scala.concurrent.Future
import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success}

/**
 * @author Yang Jing <a href="mailto:yang.xunjing@qq.com">yangbajing</a>
 * @date 2020-12-02 11:43:49
 */
class FusionCloudDiscoveryConsul()(implicit val system: ActorSystem[_])
    extends FusionCloudDiscovery
    with StrictLogging {
  private val instLock = new ReentrantReadWriteLock(true)
  private var _instance: Option[ServiceInstance] = None
  val cloudConfig: FusionCloudConfigConsul = FusionCloudConfigConsul(system)

  init()

  private def init(): Unit = {
    FusionCore(system).events.http.addListener {
      case HttpBindingServerEvent(Success(inet), isSecure) => registerCurrentService(inet, isSecure)
      case HttpBindingServerEvent(Failure(e), _)           => logger.error("Http Server绑定错误，未能自动注册到Nacos", e)
    }
  }

  private def registerCurrentService(inet: InetSocketAddress, isSecure: Boolean): Unit = {
    import system.executionContext
    val managementF: Future[Uri] = AkkaManagement(system).start()
    for {
      uri <- managementF
    } {
      val originalInst = configureServiceInstance(
        Some(ServiceInstance(address = Some(inet.getHostString), port = Some(inet.getPort)))
      )
      val address = originalInst.address.getOrElse(uri.authority.host.address())

      var http = s"http://$address:${uri.authority.port}"
      if (config.hasPath("akka.management.http.base-path")) {
        http += "/" + config.getString("akka.management.http.base-path")
      }
      http += "/health/alive"
      val check = ServiceCheck(interval = "5.s", http = http)
      val currentInst = originalInst.copy(checks = originalInst.checks :+ check)

      logger.info(s"Startup HTTP server successful, register instance was $currentInst.")
      register(currentInst)
    }
  }

  def getServiceInstance: Option[ServiceInstance] = {
    val lock = instLock.readLock()
    lock.lock()
    try _instance
    finally lock.unlock()
  }

  def config: Config = cloudConfig.config

  def register(registration: ImmutableRegistration): FusionCloudDiscoveryConsul = {
    logger.info(s"Register current service instance is $registration")
    cloudConfig.fusionConsul.register(registration)
    this
  }

  override def configureServiceInstance(maybeInstance: Option[ServiceInstance]): ServiceInstance = {
    val lock = instLock.writeLock()
    lock.lock()
    try {
      val applicationName = cloudConfig.applicationName
      val firstOnlineInet4Address = NetworkUtils.firstOnlineInet4Address()
      val serverHost = firstOnlineInet4Address.map(_.getHostAddress).getOrElse(cloudConfig.serverHost)
      val inst = getServiceInstance.getOrElse(ServiceInstance()).merge(maybeInstance)

      val serverPort = inst.port.getOrElse(cloudConfig.serverPort)
      val grpcServerPort = serverPort
      val tags =
        if (config.hasPath(DISCOVERY.TAGS)) config.getStringList(DISCOVERY.TAGS)
        else new java.util.LinkedList[String]()
      tags.add("secure=" + (config.hasPath(DISCOVERY.SECURE) && config.getBoolean(DISCOVERY.SECURE)))
      tags.add(s"gRPC.port=$grpcServerPort")

      inst.copy(
        id = s"$applicationName-$serverHost-$serverPort",
        name = applicationName,
        address = Some(serverHost),
        port = Some(serverPort),
        tags = tags.asScala.toVector
      )
    } finally {
      lock.unlock()
    }
  }

  override def register(servInst: ServiceInstance): ServiceInstance = {
    val lock = instLock.writeLock()
    lock.lock()
    try {
      val builder = ImmutableRegistration.builder().id(servInst.id).name(servInst.name)
      servInst.address.foreach(builder.address)
      servInst.port.foreach(builder.port)
      builder.addAllTags(servInst.tags.asJava)
      servInst.checks.foreach { sc =>
        val regCheckBuilder = ImmutableRegCheck.builder()
        if (StringUtils.isNoneBlank(sc.interval)) {
          regCheckBuilder.interval(sc.interval)
        }
        if (StringUtils.isNoneBlank(sc.http)) {
          regCheckBuilder.http(sc.http)
        }
        builder.addChecks(regCheckBuilder.build())
      }
      register(builder.build())

      _instance = Some(servInst)
      servInst
    } finally {
      lock.unlock()
    }
  }
}

object FusionCloudDiscoveryConsul extends ExtensionId[FusionCloudDiscoveryConsul] {

  override def createExtension(system: ActorSystem[_]): FusionCloudDiscoveryConsul =
    new FusionCloudDiscoveryConsul()(system)
}
