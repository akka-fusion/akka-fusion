package fusion.mail

import akka.actor.ExtendedActorSystem
import akka.actor.Extension
import akka.actor.ExtensionId
import akka.actor.ExtensionIdProvider
import fusion.core.extension.FusionCore
import fusion.core.extension.FusionExtension
import helloscala.common.Configuration

class FusionMail private (protected val _system: ExtendedActorSystem) extends FusionExtension {
  val components            = new MailComponents(Configuration(system.settings.config))
  def component: MailHelper = components.component
  FusionCore(system).shutdowns.beforeActorSystemTerminate("StopFusionMail") { () =>
    components.closeAsync()(system.dispatcher)
  }
}

object FusionMail extends ExtensionId[FusionMail] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): FusionMail = new FusionMail(system)
  override def lookup(): ExtensionId[_ <: Extension]                    = FusionMail
}
