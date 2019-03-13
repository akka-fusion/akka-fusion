package fusion.discovery.client.nacos

import java.util.Properties

import com.alibaba.nacos.api.NacosFactory

object NacosService {
  def configService(props: Properties): ConfigService = new ConfigService(NacosFactory.createConfigService(props))
  def configService(addr: String): ConfigService = new ConfigService(NacosFactory.createConfigService(addr))
  def namingService(props: Properties): NamingService = new NamingService(NacosFactory.createNamingService(props))
  def namingService(addr: String): NamingService = new NamingService(NacosFactory.createNamingService(addr))
}
