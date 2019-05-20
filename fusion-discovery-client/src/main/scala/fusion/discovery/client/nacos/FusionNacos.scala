package fusion.discovery.client.nacos

import akka.actor.ActorSystem
import akka.actor.ExtendedActorSystem
import akka.actor.Extension
import akka.actor.ExtensionId
import akka.actor.ExtensionIdProvider
import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging
import fusion.core.extension.FusionExtension
import fusion.core.util.Components
import fusion.discovery.DiscoveryUtils

final private[discovery] class NacosComponents(system: ActorSystem)
    extends Components[NacosDiscovery](DiscoveryUtils.methodConfPath) {

  override protected def createComponent(id: String): NacosDiscovery =
    new NacosDiscovery(NacosPropertiesUtils.configProps(id), system)

  override protected def componentClose(c: NacosDiscovery): Unit = c.close()

  override def config: Config = system.settings.config
}

final class FusionNacos private (protected val _system: ExtendedActorSystem)
    extends FusionExtension
    with StrictLogging {

  def component: NacosDiscovery = components.component

  val components = new NacosComponents(system)
  system.registerOnTermination { components.close() }
}

object FusionNacos extends ExtensionId[FusionNacos] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): FusionNacos = new FusionNacos(system)
  override def lookup(): ExtensionId[_ <: Extension]                     = FusionNacos
}
