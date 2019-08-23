package fusion.discovery.client.nacos

import akka.actor.ExtendedActorSystem
import akka.actor.ExtensionId
import com.typesafe.scalalogging.StrictLogging
import fusion.core.event.http.HttpBindingServerEvent
import fusion.core.extension.FusionCore
import fusion.discovery.client.FusionConfigService
import fusion.discovery.client.FusionNamingService
import fusion.discovery.model.DiscoveryInstance
import helloscala.common.Configuration

import scala.util.Failure
import scala.util.Success

class NacosDiscoveryComponent(
    id: String,
    val properties: NacosDiscoveryProperties,
    c: Configuration,
    system: ExtendedActorSystem)
    extends AutoCloseable
    with StrictLogging {
  private var currentInstances: List[DiscoveryInstance] = Nil
  val configService: FusionConfigService                = NacosServiceFactory.configService(properties)
  val namingService: FusionNamingService                = NacosServiceFactory.namingService(properties)

  logger.info(s"自动注册服务到Nacos: ${properties.isAutoRegisterInstance}")
  if (properties.isAutoRegisterInstance) {
    system.dynamicAccess.getObjectFor[ExtensionId[_]]("fusion.http.FusionHttpServer") match {
      case Success(obj) =>
        logger.info(s"fusion.http.FusionHttpServer object存在：$obj，注册HttpBindingListener延时注册到Nacos。")
        FusionCore(system).events.http.addListener {
          case HttpBindingServerEvent(Success(_), _) => registerCurrentService()
          case HttpBindingServerEvent(Failure(e), _) => logger.error("Http Server绑定错误，未能自动注册到Nacos", e)
        }
      case Failure(_) => registerCurrentService()
    }
  }

  def registerCurrentService(): Unit = {
    val inst = namingService.registerInstanceCurrent()
    currentInstances ::= inst
  }

  override def close(): Unit = {
    currentInstances.foreach(inst => namingService.deregisterInstance(inst))
  }

}
