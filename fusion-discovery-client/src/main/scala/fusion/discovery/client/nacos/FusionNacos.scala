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

import akka.Done
import akka.actor.ExtendedActorSystem
import akka.actor.Extension
import akka.actor.ExtensionId
import akka.actor.ExtensionIdProvider
import com.typesafe.scalalogging.StrictLogging
import fusion.common.constant.FusionConstants
import fusion.core.component.Components
import fusion.core.extension.FusionCore
import fusion.core.extension.FusionExtension
import fusion.discovery.DiscoveryUtils
import helloscala.common.Configuration

import scala.concurrent.Future

final private[discovery] class NacosComponents(system: ExtendedActorSystem)
    extends Components[NacosDiscoveryComponent](DiscoveryUtils.methodConfPath) {
  import system.dispatcher

  override protected def createComponent(id: String): NacosDiscoveryComponent =
    new NacosDiscoveryComponent(id, NacosPropertiesUtils.configProps(id), configuration.getConfiguration(id), system)

  override protected def componentClose(c: NacosDiscoveryComponent): Future[Done] = Future {
    c.close()
    Done
  }

  override def configuration: Configuration = Configuration(system.settings.config)

}

final class FusionNacos private (protected val _system: ExtendedActorSystem)
    extends FusionExtension
    with StrictLogging {

  val components = new NacosComponents(_system)

  def component: NacosDiscoveryComponent = components.component
  FusionCore(system).shutdowns.serviceStop("StopFusionNacos") { () =>
    components.closeAsync()(system.dispatcher)
  }

  // XXX 将覆盖 Configration.fromDiscovery() 调用 Configuration.setServiceName() 设置的全局服务名
  component.properties.serviceName.foreach { serviceName =>
    System.setProperty(FusionConstants.SERVICE_NAME_PATH, serviceName)
//    System.setProperty(NacosConstants.DEFAULT_SERVER_NAME_PATH, serviceName)
  }

  // XXX 重要，除了打印默认Naming服务状态外，同时还会触发服务自动注册（若配置为true的话）
  logger.info(component.namingService.getServerStatus)
}

object FusionNacos extends ExtensionId[FusionNacos] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): FusionNacos = new FusionNacos(system)
  override def lookup(): ExtensionId[_ <: Extension] = FusionNacos
}
