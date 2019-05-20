package fusion.discovery.client.nacos

import java.util.Properties

import com.alibaba.nacos.api.NacosFactory
import com.typesafe.scalalogging.StrictLogging
import fusion.discovery.DiscoveryUtils
import fusion.discovery.client.FusionConfigService
import fusion.discovery.client.FusionNamingService

object NacosServiceFactory extends StrictLogging {

  def configService(props: Properties): FusionConfigService =
    new NacosConfigServiceImpl(props, NacosFactory.createConfigService(props))

  def configService(serverAddr: String, namespace: String): FusionConfigService = {
    val props = new Properties()
    props.put("serverAddr", serverAddr)
    props.put("namespace", namespace)
    configService(props)
  }

  def configService(serverAddr: String): FusionConfigService =
    new NacosConfigServiceImpl(
      NacosPropertiesUtils.configProps(DiscoveryUtils.methodConfPath),
      NacosFactory.createConfigService(serverAddr))

  def namingService(props: Properties): FusionNamingService =
    new NacosNamingServiceImpl(props, NacosFactory.createNamingService(props))

  def namingService(addr: String): FusionNamingService =
    new NacosNamingServiceImpl(
      NacosPropertiesUtils.configProps(DiscoveryUtils.methodConfPath),
      NacosFactory.createNamingService(addr))
}
