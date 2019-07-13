package fusion.discovery.client.nacos

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.StrictLogging
import fusion.discovery.client.FusionConfigService
import fusion.discovery.client.FusionNamingService
import fusion.discovery.client.nacos
import fusion.discovery.model.DiscoveryInstance

class NacosDiscovery(val properties: NacosDiscoveryProperties, system: ActorSystem)
    extends AutoCloseable
    with StrictLogging {
  private var currentInstances: List[DiscoveryInstance] = Nil
  val configService: FusionConfigService                = NacosServiceFactory.configService(properties)
  val namingService: FusionNamingService                = NacosServiceFactory.namingService(properties)
  val httpClient: NacosHttpClient                       = nacos.NacosHttpClient(namingService, ActorMaterializer()(system))

  logger.info(s"自动注册服务到Nacos: ${properties.isAutoRegisterInstance}")
  if (properties.isAutoRegisterInstance) {
    registerCurrentService()
  }

  def registerCurrentService(): Unit = {
    val inst = namingService.registerInstanceCurrent()
    currentInstances ::= inst
  }

  override def close(): Unit = {
    httpClient.close()
    currentInstances.foreach(inst => namingService.deregisterInstance(inst))
  }

}
