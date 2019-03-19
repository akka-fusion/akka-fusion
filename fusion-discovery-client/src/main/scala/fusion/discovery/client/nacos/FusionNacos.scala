package fusion.discovery.client.nacos

import akka.actor.{ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider}
import fusion.core.extension.FusionExtension
import fusion.discovery.DiscoveryUtils
import fusion.discovery.client.{FusionConfigService, FusionNamingService}
import scala.concurrent.duration._

final class FusionNacos private (protected val _system: ExtendedActorSystem) extends FusionExtension {
  namingService.registerInstanceCurrent(system.settings.config)

  val heatbeat = system.scheduler.schedule(10.seconds, 5.seconds)(
    )(system.dispatcher)
  system.registerOnTermination {
    heatbeat.cancel()
    namingService.deregisterInstanceCurrent(system.settings.config)
  }

  def configService: FusionConfigService = DiscoveryUtils.defaultConfigService
  def namingService: FusionNamingService = DiscoveryUtils.defaultNamingService
}

object FusionNacos extends ExtensionId[FusionNacos] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): FusionNacos = new FusionNacos(system)
  override def lookup(): ExtensionId[_ <: Extension] = FusionNacos
}
