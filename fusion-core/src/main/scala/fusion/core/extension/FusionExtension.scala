package fusion.core.extension

import akka.actor.ActorSystem
import akka.actor.ExtendedActorSystem
import akka.actor.Extension

trait FusionExtension extends Extension {
  protected val _system: ExtendedActorSystem
  implicit def system: ActorSystem = _system
}
