package fusion.core.extension

import akka.actor.{ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider}

final class FusionCore private (system: ExtendedActorSystem) extends Extension {
  // System、服务发现
}

object FusionCore extends ExtensionId[FusionCore] with ExtensionIdProvider {
  override def lookup(): ExtensionId[_ <: Extension] = FusionCore
  override def createExtension(system: ExtendedActorSystem): FusionCore = new FusionCore(system)
}
