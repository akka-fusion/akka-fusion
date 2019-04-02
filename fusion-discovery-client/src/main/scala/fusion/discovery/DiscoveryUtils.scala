package fusion.discovery

import fusion.discovery.client.FusionConfigService
import fusion.discovery.client.FusionNamingService
import fusion.discovery.client.nacos.NacosConstants
import fusion.discovery.client.nacos.NacosDiscoveryProperties
import fusion.discovery.client.nacos.NacosPropertiesUtils
import fusion.discovery.client.nacos.NacosServiceFactory
import helloscala.common.Configuration

object DiscoveryUtils {
  lazy val METHOD: String = Configuration().getOrElse[String]("fusion.discovery.method", NacosConstants.NAME)

  lazy val defaultSetting: NacosDiscoveryProperties = NacosPropertiesUtils.configProps(methodConfPath)

  lazy val defaultConfigService: FusionConfigService =
    try {
      NacosServiceFactory.configService(defaultSetting)
    } catch {
      case e: Throwable => throw new Error(s"获取ConfigService失败，$METHOD", e)
    }

  lazy val defaultNamingService: FusionNamingService =
    try {
      NacosServiceFactory.namingService(NacosPropertiesUtils.namingProps(methodConfPath))
    } catch { case e: Throwable => throw new Error(s"获取NamingService失败，$METHOD", e) }

  def methodConfPath = s"${DiscoveryConstants.CONF_PATH}.$METHOD"
}
