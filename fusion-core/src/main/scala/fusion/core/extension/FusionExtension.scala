package fusion.core.extension

import akka.actor.ActorSystem
import akka.actor.ExtendedActorSystem
import akka.actor.Extension
import helloscala.common.Configuration

trait FusionExtension extends Extension {
  protected val _system: ExtendedActorSystem
  implicit def system: ActorSystem = _system
  def configuration: Configuration = FusionCore(system).configuration
}
