package fusion.http.gateway.server

import akka.actor.ExtendedActorSystem
import akka.actor.Extension
import akka.actor.ExtensionId
import akka.actor.ExtensionIdProvider
import akka.http.scaladsl.server.Route
import fusion.core.extension.FusionExtension

class FusionHttpGateway private (protected val _system: ExtendedActorSystem) extends FusionExtension {
  val components       = new HttpGatewayComponents(_system)
  def component: Route = components.component
}

object FusionHttpGateway extends ExtensionId[FusionHttpGateway] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): FusionHttpGateway = new FusionHttpGateway(system)
  override def lookup(): ExtensionId[_ <: Extension]                           = FusionHttpGateway
}
