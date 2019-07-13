package fusion.discovery.client.nacos

import java.util.Properties

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.StrictLogging
import fusion.common.constant.PropKeys._
import helloscala.common.Configuration

object NacosPropertiesUtils extends StrictLogging {

  @inline def configProps(path: String): NacosDiscoveryProperties = {
    Configuration(ConfigFactory.load().resolve()).get[Properties](path)
  }

}
