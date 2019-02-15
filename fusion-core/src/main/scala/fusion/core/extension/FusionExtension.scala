package fusion.core.extension

import akka.actor.{ActorSystem, ExtendedActorSystem, Extension}

trait FusionExtension extends Extension {
  protected val _system: ExtendedActorSystem
  implicit def system: ActorSystem = _system
}
