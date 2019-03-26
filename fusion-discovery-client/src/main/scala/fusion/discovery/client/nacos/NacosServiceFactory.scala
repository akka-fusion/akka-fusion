package fusion.discovery.client.nacos

import java.util.Properties

import com.alibaba.nacos.api.NacosFactory
import com.typesafe.scalalogging.StrictLogging
import fusion.discovery.DiscoveryUtils
import fusion.discovery.client.{FusionConfigService, FusionNamingService}

object NacosServiceFactory extends StrictLogging {

  def configService(props: Properties): FusionConfigService =
    new NacosConfigServiceImpl(props, NacosFactory.createConfigService(props))

  def configService(addr: String): FusionConfigService =
    new NacosConfigServiceImpl(
      NacosPropertiesUtils.configProps(DiscoveryUtils.methodConfPath),
      NacosFactory.createConfigService(addr))

  def namingService(props: Properties): FusionNamingService =
    new NacosNamingServiceImpl(props, NacosFactory.createNamingService(props))

  def namingService(addr: String): FusionNamingService =
    new NacosNamingServiceImpl(
      NacosPropertiesUtils.configProps(DiscoveryUtils.methodConfPath),
      NacosFactory.createNamingService(addr))
}
