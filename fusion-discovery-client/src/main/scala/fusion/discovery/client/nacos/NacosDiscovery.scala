package fusion.discovery.client.nacos

import akka.actor.ActorRefFactory
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.StrictLogging
import fusion.discovery.DiscoveryUtils
import fusion.discovery.client.FusionConfigService
import fusion.discovery.client.FusionNamingService
import fusion.discovery.http.HttpClient
import fusion.discovery.model.DiscoveryInstance

class NacosDiscovery(setting: NacosDiscoveryProperties, context: ActorRefFactory)
    extends AutoCloseable
    with StrictLogging {
  private var currentInstance: Option[DiscoveryInstance] = None
  val configService: FusionConfigService                 = NacosServiceFactory.configService(setting)

  val namingService: FusionNamingService =
    NacosServiceFactory.namingService(NacosPropertiesUtils.namingProps(DiscoveryUtils.methodConfPath))
  val httpClient = HttpClient(namingService, ActorMaterializer()(context))
  logger.info(s"自动注册服务到Nacos: ${setting.isAutoRegisterInstance}")
  if (setting.isAutoRegisterInstance) {
    val inst = namingService.registerInstanceCurrent()
    currentInstance = Option(inst)
  }

  override def close(): Unit = {
    currentInstance.foreach(inst => namingService.deregisterInstance(inst))
  }

}
