package fusion.discovery.client.nacos

import java.util.Properties

import com.typesafe.config.ConfigFactory
import fusion.core.constant.PropKeys._
import helloscala.common.Configuration

object NacosPropertiesUtils {

  def namingProps: NacosDiscoveryProperties = {
    val props = Configuration(ConfigFactory.load().resolve()).get[Properties](NacosConstants.CONF_PATH)
    wrap(props)
  }

  def configProps: NacosDiscoveryProperties = {
    val props = Configuration(ConfigFactory.load().resolve()).get[Properties](NacosConstants.CONF_PATH)
    wrap(props)
  }

  private def wrap(props: Properties): Properties = {
    val config = ConfigFactory.load()
    props.put(INSTANCE_IP, System.getProperty("fusion.server.host", config.getString("fusion.server.host")))
    props.put(INSTANCE_PORT, System.getProperty("fusion.server.port", config.getInt("fusion.server.port").toString))
    props
  }

}
