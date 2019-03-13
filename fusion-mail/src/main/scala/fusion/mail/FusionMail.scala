package fusion.mail

import akka.actor.{ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider}
import fusion.core.extension.FusionExtension

class FusionMail private (protected val _system: _root_.akka.actor.ExtendedActorSystem) extends FusionExtension {
  val components = new MailComponents(system.settings.config)
  def component: MailHelper = components.component
}

object FusionMail extends ExtensionId[FusionMail] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): FusionMail = new FusionMail(system)
  override def lookup(): ExtensionId[_ <: Extension] = FusionMail
}
