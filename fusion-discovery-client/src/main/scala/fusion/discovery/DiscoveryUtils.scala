package fusion.discovery

import fusion.discovery.client.nacos.{NacosConstants, NacosPropertiesUtils, NacosServiceFactory}
import fusion.discovery.client.{FusionConfigService, FusionNamingService}

object DiscoveryUtils {
  lazy val DEFAULT_PROPS_PREFIX: String = DiscoveryConstants.CONF_PATH + '.' + sys.props
    .get("fusion.discovery.method")
    .getOrElse(NacosConstants.NAME)

  lazy val defaultConfigService: FusionConfigService =
    if (DEFAULT_PROPS_PREFIX.endsWith("nacos")) {
      NacosServiceFactory.configService(NacosPropertiesUtils.configProps)
    } else {
      throw new Error(s"获取ConfigService失败，$DEFAULT_PROPS_PREFIX")
    }

  lazy val defaultNamingService: FusionNamingService = if (DEFAULT_PROPS_PREFIX.endsWith("nacos")) {
    NacosServiceFactory.namingService(NacosPropertiesUtils.namingProps)
  } else {
    throw new Error(s"获取NamingService失败，$DEFAULT_PROPS_PREFIX")
  }

}
