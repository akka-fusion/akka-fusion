package fusion.discovery.client.nacos

import akka.actor.ActorSystem
import akka.actor.ExtendedActorSystem
import akka.actor.Extension
import akka.actor.ExtensionId
import akka.actor.ExtensionIdProvider
import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging
import fusion.common.constant.FusionConstants
import fusion.core.extension.FusionExtension
import fusion.core.util.Components
import fusion.discovery.DiscoveryUtils

final private[discovery] class NacosComponents(system: ActorSystem)
    extends Components[NacosDiscovery](DiscoveryUtils.methodConfPath) {

  override protected def createComponent(id: String): NacosDiscovery =
    new NacosDiscovery(NacosPropertiesUtils.configProps(id), system)

  override protected def componentClose(c: NacosDiscovery): Unit = c.close()

  override def config: Config = system.settings.config
}

final class FusionNacos private (protected val _system: ExtendedActorSystem)
    extends FusionExtension
    with StrictLogging {

  def component: NacosDiscovery = components.component

  val components = new NacosComponents(system)
  system.registerOnTermination { components.close() }

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
  override def lookup(): ExtensionId[_ <: Extension]                     = FusionNacos
}
