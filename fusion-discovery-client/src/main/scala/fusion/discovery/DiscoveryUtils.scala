package fusion.discovery

import fusion.discovery.client.FusionConfigService
import fusion.discovery.client.nacos.NacosConstants
import fusion.discovery.client.nacos.NacosPropertiesUtils
import fusion.discovery.client.nacos.NacosServiceFactory
import helloscala.common.Configuration

object DiscoveryUtils {
  lazy val METHOD: String = Configuration.load().getOrElse[String](DiscoveryConstants.CONF_METHOD, NacosConstants.NAME)

  lazy val defaultConfigService: FusionConfigService =
    try {
      NacosServiceFactory.configService(NacosPropertiesUtils.configProps(methodConfPath))
    } catch {
      case e: Throwable => throw new Error(s"获取ConfigService失败，$METHOD", e)
    }

  def methodConfPath = s"${DiscoveryConstants.CONF_PATH}.$METHOD"
}
