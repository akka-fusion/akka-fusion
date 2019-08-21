package fusion.core.extension

import akka.actor.ActorSystem
import akka.actor.ExtendedActorSystem
import akka.actor.Extension
import helloscala.common.Configuration

// #FusionExtension
trait FusionExtension extends Extension {
  protected val _system: ExtendedActorSystem
  implicit val system: ActorSystem = _system
  def configuration: Configuration = FusionCore(system).configuration
}
// #FusionExtension
