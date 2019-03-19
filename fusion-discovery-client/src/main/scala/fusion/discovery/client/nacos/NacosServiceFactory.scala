package fusion.discovery.client.nacos

import java.util.Properties

import com.alibaba.nacos.api.NacosFactory
import com.typesafe.scalalogging.StrictLogging
import fusion.discovery.client.{FusionConfigService, FusionNamingService}

object NacosServiceFactory extends StrictLogging {

  def configService(props: Properties): FusionConfigService = {
//    logger.info(
//      "create ConfigService props: " + props.asScala.map { case (key, value) => key + ": " + value }.mkString("; "))
    new NacosConfigServiceImpl(props, NacosFactory.createConfigService(props))
  }

  def configService(addr: String): FusionConfigService =
    new NacosConfigServiceImpl(NacosPropertiesUtils.configProps, NacosFactory.createConfigService(addr))

  def namingService(props: Properties): FusionNamingService =
    new NacosNamingServiceImpl(props, NacosFactory.createNamingService(props))

  def namingService(addr: String): FusionNamingService =
    new NacosNamingServiceImpl(NacosPropertiesUtils.configProps, NacosFactory.createNamingService(addr))
}
