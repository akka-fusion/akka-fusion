package fusion.discovery.client.nacos

import java.util.Properties

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.StrictLogging
import fusion.core.constant.PropKeys._
import helloscala.common.Configuration

object NacosPropertiesUtils extends StrictLogging {

  def namingProps(path: String): NacosDiscoveryProperties = {
    val props = Configuration(ConfigFactory.load().resolve()).get[Properties](path)
    wrap(props)
  }

  def configProps(path: String): NacosDiscoveryProperties = {
    val props = Configuration(ConfigFactory.load().resolve()).get[Properties](path)
    wrap(props)
  }

  private def wrap(props: Properties): Properties = {
    val configuration = Configuration()
    configuration.get[Option[String]]("fusion.server.host") match {
      case Some(host) => props.put(INSTANCE_IP, host)
      case _          => logger.warn("fusion.server.host 未设置")
    }
    configuration.get[Option[Int]]("fusion.server.port") match {
      case Some(port) => props.put(INSTANCE_PORT, port.toString)
      case _          => logger.warn("fusion.server.port 未设置")
    }
    props
  }

}
