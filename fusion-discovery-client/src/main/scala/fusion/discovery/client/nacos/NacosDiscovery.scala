package fusion.discovery.client.nacos

import akka.actor.ActorRefFactory
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.StrictLogging
import fusion.discovery.client.FusionConfigService
import fusion.discovery.client.FusionNamingService
import fusion.discovery.client.NacosHttpClient
import fusion.discovery.model.DiscoveryInstance

class NacosDiscovery(val properties: NacosDiscoveryProperties, context: ActorRefFactory)
    extends AutoCloseable
    with StrictLogging {
  private var currentInstances: List[DiscoveryInstance] = Nil
  val configService: FusionConfigService                = NacosServiceFactory.configService(properties)
  val namingService: FusionNamingService                = NacosServiceFactory.namingService(properties)
  val httpClient: NacosHttpClient                       = NacosHttpClient(namingService, ActorMaterializer()(context))

  logger.info(s"自动注册服务到Nacos: ${properties.isAutoRegisterInstance}")
  if (properties.isAutoRegisterInstance) {
    val inst = namingService.registerInstanceCurrent()
    currentInstances ::= inst
  }

  override def close(): Unit = {
    httpClient.close()
    currentInstances.foreach(inst => namingService.deregisterInstance(inst))
  }

}
