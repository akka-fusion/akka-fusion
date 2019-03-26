package fusion.discovery

import fusion.discovery.client.nacos.{NacosConstants, NacosPropertiesUtils, NacosServiceFactory}
import fusion.discovery.client.{FusionConfigService, FusionNamingService}

object DiscoveryUtils {
  lazy val METHOD: String = DiscoveryConstants.CONF_PATH + '.' +
    sys.props.get("fusion.discovery.method").getOrElse(NacosConstants.NAME)

  lazy val defaultConfigService: FusionConfigService =
    try {
      NacosServiceFactory.configService(NacosPropertiesUtils.configProps(methodConfPath))
    } catch {
      case e: Throwable => throw new Error(s"获取ConfigService失败，$METHOD", e)
    }

  lazy val defaultNamingService: FusionNamingService =
    try {
      NacosServiceFactory.namingService(NacosPropertiesUtils.namingProps(methodConfPath))
    } catch { case e: Throwable => throw new Error(s"获取NamingService失败，$METHOD", e) }

  def methodConfPath = s"$DiscoveryConstants.CONF_PATH.$METHOD"
}
