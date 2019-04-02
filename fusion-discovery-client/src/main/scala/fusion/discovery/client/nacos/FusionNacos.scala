package fusion.discovery.client.nacos

import akka.actor.ExtendedActorSystem
import akka.actor.Extension
import akka.actor.ExtensionId
import akka.actor.ExtensionIdProvider
import com.typesafe.scalalogging.StrictLogging
import fusion.core.extension.FusionExtension
import fusion.discovery.DiscoveryUtils
import fusion.discovery.client.FusionConfigService
import fusion.discovery.client.FusionNamingService

final class FusionNacos private (protected val _system: ExtendedActorSystem)
    extends FusionExtension
    with StrictLogging {
  if (DiscoveryUtils.defaultSetting.isAutoRegisterInstance) {
    logger.info(s"开始自动注册服务到Nacos: ${system.settings.config}")
    namingService.registerInstanceCurrent(system.settings.config)
  }

  system.registerOnTermination {
    namingService.deregisterInstanceCurrent(system.settings.config)
  }

  def configService: FusionConfigService = DiscoveryUtils.defaultConfigService
  def namingService: FusionNamingService = DiscoveryUtils.defaultNamingService
}

object FusionNacos extends ExtensionId[FusionNacos] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): FusionNacos = new FusionNacos(system)
  override def lookup(): ExtensionId[_ <: Extension]                     = FusionNacos
}
